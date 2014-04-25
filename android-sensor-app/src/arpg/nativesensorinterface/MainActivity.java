package arpg.nativesensorinterface;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.util.Log;

public class MainActivity extends Activity {
    private NativeCameraInterface mCamera;
    private NativeSensorInterface mNativeInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNativeInterface = new NativeSensorInterface();
        mNativeInterface.initialize(this);
        TextureView texture = (TextureView)findViewById(R.id.preview);
        mCamera = new NativeCameraInterface(mNativeInterface, texture);
    }

    @Override
    public void onPause() {
        super.onPause();
        mCamera.stop();
        mNativeInterface.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCamera.close();
    }
}
