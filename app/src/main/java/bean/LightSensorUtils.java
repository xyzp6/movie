package bean;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class LightSensorUtils implements SensorEventListener{
    private  SensorManager sensorManager;//传感器管理器
    private  Sensor light;//光照传感器
    private LightListener lightListener;

    public void setLightListener(LightListener lightListener) {
        this.lightListener = lightListener;
    }

    public LightSensorUtils(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (light.equals(event.sensor)) {
            lightListener.getLight(event.values[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * 在activity的onresume中调用此方法
     */
    public void registerLight(){
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * 在activity的onpause中调用此方法
     */
    public void unregisterLight(){
        sensorManager.unregisterListener(this);
    }

    public interface LightListener{
        void getLight(float value);
    }
}

