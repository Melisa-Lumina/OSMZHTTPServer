package com.example.osmzhttpserver;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Environment;

public class CameraActivity extends Activity {
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final String TAG = "CameraActivity";
    private Camera mCamera;
    private CameraPreview mPreview;

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private void initializeCamera() {
        // Check if the device has a camera
        if (!checkCameraHardware(this)) {
            Log.e(TAG, "No camera found.");
            return;
        }

        try {
            // Create an instance of Camera
            mCamera = getCameraInstance();
            if (mCamera != null) {
                // Create our Preview view and set it as the content of our activity.
                mPreview = new CameraPreview(this, mCamera);
                FrameLayout preview = findViewById(R.id.camera_preview);
                preview.addView(mPreview);

                // Schedule the timer to take a picture every second
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        // Trigger the camera to take a picture
                        mCamera.takePicture(null, null, mPicture);
                    }
                }, 0, 1000); // Delay of 0 milliseconds, repeat every 1000 milliseconds (1 second)
            } else {
                Log.e(TAG, "Failed to initialize camera.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing camera: " + e.getMessage());
        }
    }

    private boolean checkCameraHardware(CameraActivity cameraActivity) {
        if (cameraActivity.getPackageManager().hasSystemFeature(android.content.pm.PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ System.currentTimeMillis() + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "Picture taken.");

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                Log.d(TAG, "Image saved successfully.");
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        // Schedule the timer to take a picture every second
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Trigger the camera to take a picture
                mCamera.takePicture(null, null, mPicture);
            }
        }, 0, 1000); // Delay of 0 milliseconds, repeat every 1000 milliseconds (1 second)
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the timer when the activity is destroyed to prevent memory leaks
        Timer timer = new Timer();
        timer.cancel();
    }
}
