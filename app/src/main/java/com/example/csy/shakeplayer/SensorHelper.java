package com.example.csy.shakeplayer;

/**
 * Created by Owner on 2017/2/8.
 */
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorHelper implements SensorEventListener {


    private static final int SPEED_SHRESHOLD = 5000;

    private static final int UPTATE_INTERVAL_TIME = 50;

    private SensorManager sensorManager;

    private Sensor sensor;

    private OnShakeListener onShakeListener;

    private Context context;

    private float lastX;
    private float lastY;
    private float lastZ;

    private long lastUpdateTime;

    public SensorHelper(Context context) {

        this.context = context;
        start();
    }


    public void start() {

        sensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {

            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (sensor != null) {
            sensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }


    public void stop() {
        sensorManager.unregisterListener(this);
    }


    public interface OnShakeListener {
        public void onShake();
    }

    public void setOnShakeListener(OnShakeListener listener) {
        onShakeListener = listener;
    }

    /*
     * (non-Javadoc)
     * android.hardware.SensorEventListener#onAccuracyChanged(android.hardware
     * .Sensor, int)
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub

        long currentUpdateTime = System.currentTimeMillis();

        long timeInterval = currentUpdateTime - lastUpdateTime;
        // 判断是否达到了检测时间间隔
        if (timeInterval < UPTATE_INTERVAL_TIME) return;
        // 现在的时间变成last时间
        lastUpdateTime = currentUpdateTime;
        // 获得x,y,z坐标
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        // 获得x,y,z的变化值
        float deltaX = x - lastX;
        float deltaY = y - lastY;
        float deltaZ = z - lastZ;
        // 将现在的坐标变成last坐标
        lastX = x;
        lastY = y;
        lastZ = z;
        double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ
                * deltaZ)
                / timeInterval * 10000;
        // 达到速度阀值，发出提示
        if (speed >= SPEED_SHRESHOLD)
            onShakeListener.onShake();
    }
}

