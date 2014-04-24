package edu.gwu.rpg.nativesensorinterface;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.graphics.SurfaceTexture;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.util.Log;

public class NativeCameraInterface
    implements TextureView.SurfaceTextureListener {
    private Camera mCamera;
    private NativeSensorInterface mNativeInterface;
    private TextureView mTextureView;
    private long mTimestamp;

    public NativeCameraInterface(NativeSensorInterface nativeInterface,
                                 TextureView textureView) {
        mTimestamp = 0;
        mNativeInterface = nativeInterface;
        mTextureView = textureView;
        mTextureView.setSurfaceTextureListener(this);

        mCamera = Camera.open();
        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    mNativeInterface.PostImage(mTimestamp,
                                               data);
                }
            });
    }

    public void stop() {
        mCamera.stopPreview();
    }

    public void close() {
        mCamera.release();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface,
                                          int width, int height) {
        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException ioe) {
            // Something bad happened
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
                                            int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        mTimestamp = surface.getTimestamp();
    }
}
