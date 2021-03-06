package arpg.nativesensorinterface;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

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
    private native void post_mag(long timestamp, float x, float y, float z);
    private native void post_gps(long timestamp, double lat, double lon,
                                 double alt, float std);

    private static final long MILLIS_BETWEEN_GPS_UPDATES = 1000 * 5; // 5 sec
    private static final long METERS_BETWEEN_GPS_UPDATES = 5; // 5 meters

    private SensorManager mSensorManager;
    private LocationManager mLocationManager;
    private Sensor mAccelSensor, mGyroSensor, mMagSensor;

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
    private SensorEventListener mGyroListener, mAccelListener, mMagListener;
    private LocationListener mLocationListener;
    private TextView mGpsText, mGyroText, mAccelText, mImageText, mMagText;

    public NativeSensorInterface() {
        mGpsText = mGyroText = mAccelText = mImageText = mMagText = null;
    }

    /** Initialize all the listeners */
    public void initialize(Context ctx) {
        initialize();
        mHasInitialSensorEvent = false;

        mSensorManager =
            (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        mLocationManager =
            (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);

        mAccelSensor =
            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroSensor =
            mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMagSensor =
            mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mAccelListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (!mHasInitialSensorEvent) {
                        mHasInitialSensorEvent = true;
                        mInitialSensorTimestamp = event.timestamp;
                        mRealSensorTime = SystemClock.elapsedRealtimeNanos();
                    }

                    long ts = event.timestamp - mInitialSensorTimestamp +
                        mRealSensorTime;

                    if (mAccelText != null) {
                        mAccelText.setText(String.format("Accel at %d (%.2f, %.2f, %.2f)",
                                                         ts,
                                                         event.values[0],
                                                         event.values[1],
                                                         event.values[2]));
                    }

                    post_accel(ts, event.values[0],
                               event.values[1], event.values[2]);
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

                    long ts = event.timestamp - mInitialSensorTimestamp +
                        mRealSensorTime;
                    if (mGyroText != null) {
                        mGyroText.setText(String.format("Gyro at %d (%.2f, %.2f, %.2f)",
                                                        ts,
                                                        event.values[0],
                                                        event.values[1],
                                                        event.values[2]));
                    }

                    post_gyro(ts, event.values[0], event.values[1],
                              event.values[2]);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {}
            };

        mMagListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (!mHasInitialSensorEvent) {
                        mHasInitialSensorEvent = true;
                        mInitialSensorTimestamp = event.timestamp;
                        mRealSensorTime = SystemClock.elapsedRealtimeNanos();
                    }

                    long ts = event.timestamp - mInitialSensorTimestamp +
                        mRealSensorTime;
                    if (mMagText != null) {
                        mMagText.setText(String.format("Mag at %d (%.2f, %.2f, %.2f)",
                                                       ts,
                                                       event.values[0],
                                                       event.values[1],
                                                       event.values[2]));
                    }

                    post_mag(ts, event.values[0], event.values[1],
                             event.values[2]);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {}
            };

        mLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location loc) {
                    if (mGpsText != null) {
                        String gpsStr =
                            "GPS at %d (%.2f, %.2f, %.2f) +/- %.2f";
                        mGpsText.setText(String.format(gpsStr,
                                                       loc.getElapsedRealtimeNanos(),
                                                       loc.getLatitude(),
                                                       loc.getLongitude(),
                                                       loc.getAltitude(),
                                                       loc.getAccuracy()));
                    }

                    post_gps(loc.getElapsedRealtimeNanos(),
                             loc.getLatitude(),
                             loc.getLongitude(),
                             loc.getAltitude(),
                             loc.getAccuracy());
                }

                @Override
                public void onProviderDisabled(String provider) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onStatusChanged(String provider, int status,
                                            Bundle extras) {}

            };

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                                MILLIS_BETWEEN_GPS_UPDATES,
                                                METERS_BETWEEN_GPS_UPDATES,
                                                mLocationListener);

        mSensorManager.registerListener(mAccelListener, mAccelSensor,
                                        SensorManager.SENSOR_DELAY_FASTEST);

        mSensorManager.registerListener(mGyroListener, mGyroSensor,
                                        SensorManager.SENSOR_DELAY_FASTEST);

        mSensorManager.registerListener(mMagListener, mMagSensor,
                                        SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void setTextViews(TextView gpsText, TextView gyroText,
                             TextView accelText, TextView imageText,
                             TextView magText) {
        mGpsText = gpsText;
        mGyroText = gyroText;
        mAccelText = accelText;
        mImageText = imageText;
        mMagText = magText;
    }

    public void stop() {
        mSensorManager.unregisterListener(mAccelListener);
        mSensorManager.unregisterListener(mGyroListener);
        mSensorManager.unregisterListener(mMagListener);
        mLocationManager.removeUpdates(mLocationListener);
    }

    public void postImage(long timestamp, byte[] bytes) {
        post_image(timestamp, bytes);

        if (mImageText != null) {
            mImageText.setText("Image at " + Long.toString(timestamp));
        }
    }
}
