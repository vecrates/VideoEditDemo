package cn.vecrates.videoeditdemo.media.camera;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import cn.vecrates.videoeditdemo.MyApplication;

public class SensorControler implements SensorEventListener {

    private static final String TAG = SensorControler.class.getSimpleName();

    private static final float THREHOLD_MOVE = 0.8f;

    private SensorManager sensorManager;
    private Sensor sensor;

    private OnMovedListener listener;

    private float lastX, lastY, lastZ;

    public SensorControler() {
        sensorManager = (SensorManager) MyApplication.appContext.getSystemService(Activity.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void startListen() {
        if (sensorManager == null || sensor == null) return;
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void stopListen() {
        if (sensorManager == null || sensor == null) return;
        sensorManager.unregisterListener(this, sensor);
    }

    //当传感器感应的值发生变化时回调
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        if (Math.abs(lastX - x) >= THREHOLD_MOVE ||
                Math.abs(lastY - y) >= THREHOLD_MOVE ||
                Math.abs(lastZ - z) >= THREHOLD_MOVE) {
            if (listener != null) {
                listener.onMoved();
            }
        }
        lastX = x;
        lastY = y;
        lastZ = z;
    }


    //当传感器精度发生变化时回调
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setMovedListener(OnMovedListener listener) {
        this.listener = listener;
    }

    public interface OnMovedListener {
        void onMoved();
    }

}
