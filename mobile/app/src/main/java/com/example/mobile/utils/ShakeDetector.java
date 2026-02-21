package com.example.mobile.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeDetector implements SensorEventListener {

    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7f;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 3000;

    public interface OnShakeListener {
        void onShake(int count);
    }

    private OnShakeListener listener;
    private long mShakeTimestamp;
    private int mShakeCount;

    public void setOnShakeListener(OnShakeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (listener == null) return;

        float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
        float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
        float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;

        float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            long now = System.currentTimeMillis();

            if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) return;

            if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                mShakeCount = 0;
            }

            mShakeTimestamp = now;
            mShakeCount++;

            listener.onShake(mShakeCount);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}