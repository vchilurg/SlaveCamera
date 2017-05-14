package com.example.cgv.slavecamera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private static final String VIDEO_PATH_NAME = "/Pictures/test.3gp";
    private static final int MY_REQUEST_CODE = 1;
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private View mToggleButton;
    private boolean mInitSuccesful;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            //use checkSelfPermission()
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        MY_REQUEST_CODE);
            }
        } else {
            //simply use the required feature
            //as the user has already granted permission to them during installation
        }

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mToggleButton = (ToggleButton) findViewById(R.id.toggleRecordingButton);
        mToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            // toggle video recording
            public void onClick(View v) {
                if (((ToggleButton)v).isChecked())
                   // mMediaRecorder.start();
                try {
                    mMediaRecorder.start();
                } catch (IllegalStateException e) {
                    // This is thrown if the previous calls are not called with the
                    // proper order
                    e.printStackTrace();
                }
                else {
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    try {
                        initRecorder(mHolder.getSurface());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void initRecorder(Surface surface) throws  IOException {

        if (mCamera == null) {
            mCamera = Camera.open();
            mCamera.unlock();
        }

        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setPreviewDisplay(surface);
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        File file = new File(Environment.getExternalStorageDirectory(), VIDEO_PATH_NAME);
        // "touch" the file
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null)
                if (!parent.exists())
                    if (!parent.mkdirs())
                        throw new IOException("Cannot create " +
                                "parent directories for file: " + file);

            file.createNewFile();
            mMediaRecorder.setOutputFile(file.getAbsolutePath());

            // No limit. Check the space on disk!
            mMediaRecorder.setMaxDuration(-1);
            mMediaRecorder.setVideoFrameRate(15);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);

            try {
                mMediaRecorder.prepare();
            } catch (IllegalStateException e) {
                // This is thrown if the previous calls are not called with the
                // proper order
                e.printStackTrace();
            }

            mInitSuccesful = true;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            if(!mInitSuccesful)
                initRecorder(mHolder.getSurface());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        // Release MediaRecorder and especially the Camera as it's a shared
        // object that can be used by other applications
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mCamera.release();

        // once the objects have been released they can't be reused
        mMediaRecorder = null;
        mCamera = null;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Now user should be able to use camera
            }
            else {
                // Your app will not have this permission. Turn off all functions
                // that require this permission or it will force close like your
                // original question
            }
        }
    }
}
