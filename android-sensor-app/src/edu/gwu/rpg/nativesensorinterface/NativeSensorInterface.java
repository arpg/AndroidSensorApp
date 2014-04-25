package edu.gwu.rpg.nativesensorinterface;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

/** The class for handling all JNI data interchange
 *
 * Naming convention:
 *  - Java calls are CamelCase
 *  - Native functions are lower_case_with_underlines
 *  - Data methods are Post* (Image, Imu, etc.)
 */
public class NativeSensorInterface {
    private native void initialize();
    private native void post_image(long timestamp, byte[] bytes);
    private native void post_accel(long timestamp, float x, float y, float z);
    private native void post_gyro(long timestamp, float x, float y, float z);

    private SensorManager mSensorManager;
    private Sensor mAccelSensor, mGyroSensor;

    /** We need to scale our timestamps to be in the same world
     * because the image timestamps are disconnected from the
     * SensorEvent timestamps.
     *
     * We attempt to scale them by taking using the given
     * SensorEvent/SurfaceTexture timestamps as purely relative
     * increases over the elapsedRealtimeNanos().
     */
    private boolean mHasInitialSensorEvent;
    private long mInitialSensorTimestamp;
    private long mRealSensorTime, mRealTimeDiff;
    private SensorEventListener mGyroListener, mAccelListener;

    /** Initialize all the listeners */
    public void initialize(Context ctx) {
        initialize();
        mHasInitialSensorEvent = false;

        mSensorManager =
                (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        mAccelSensor =
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroSensor =
                mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mAccelListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (!mHasInitialSensorEvent) {
                        mHasInitialSensorEvent = true;
                        mInitialSensorTimestamp = event.timestamp;
                        mRealSensorTime = SystemClock.elapsedRealtimeNanos();
                    }

                    post_accel(event.timestamp - mInitialSensorTimestamp +
                               mRealSensorTime,
                               event.values[0], event.values[1], event.values[2]);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {}
            };

        mGyroListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (!mHasInitialSensorEvent) {
                        mHasInitialSensorEvent = true;
                        mInitialSensorTimestamp = event.timestamp;
                        mRealSensorTime = SystemClock.elapsedRealtimeNanos();
                    }

                    post_gyro(event.timestamp - mInitialSensorTimestamp +
                              mRealSensorTime,
                              event.values[0], event.values[1], event.values[2]);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {}
            };

        mSensorManager.registerListener(mAccelListener, mAccelSensor,
                                        SensorManager.SENSOR_DELAY_FASTEST);

        mSensorManager.registerListener(mGyroListener, mGyroSensor,
                                        SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void stop() {
        mSensorManager.unregisterListener(mAccelListener);
        mSensorManager.unregisterListener(mGyroListener);
    }

    public void postImage(long timestamp, byte[] bytes) {
        post_image(timestamp, bytes);
    }

    static {
        System.loadLibrary("gnustl_shared");
        System.loadLibrary("NativeSensorInterface");
    }
}
