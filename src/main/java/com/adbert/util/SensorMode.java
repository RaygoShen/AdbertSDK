package com.adbert.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.adbert.util.enums.SensorType;

public class SensorMode implements SensorEventListener {

	private SensorManager mSensorManager;
	private Sensor mSensor;
	private ConclusionListener listener;
	private Context context;

	public SensorMode(Context context, SensorType type, ConclusionListener listener) {
		this.listener = listener;
		this.context = context;
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(type.getCode());
		if (mSensor == null) {
			listener.onFail("Sensor is not supported");
		} else {
			resume();
		}
	}

	// for check
	public SensorMode(Context context) {
		this.context = context;
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
	}

	// for check
	public boolean isSupported(SensorType type) {
		mSensor = mSensorManager.getDefaultSensor(type.getCode());
		if (mSensor == null) {
			return false;
		} else
			return true;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
			// if ((int) event.values[0] < 200)
			// listener.onConclusion();
			listener.onConclusion((int) event.values[0]);
		} else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			Conclusion_ACCELEROMETER(event.values[0], event.values[1], event.values[2]);
		} else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
			if ((int) event.values[0] == 0)
				listener.onConclusion();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	private float mLastX;
	private float mLastY;
	private float mLastZ;
	private double mSpeed;
	private long mLastUpdateTime;
	private static final int SPEED_SHRESHOLD = 2000;
	private static final int UPTATE_INTERVAL_TIME = 70;

	private void Conclusion_ACCELEROMETER(float x, float y, float z) {
		long mCurrentUpdateTime = System.currentTimeMillis();
		long mTimeInterval = mCurrentUpdateTime - mLastUpdateTime;
		if (mTimeInterval < UPTATE_INTERVAL_TIME)
			return;
		mLastUpdateTime = mCurrentUpdateTime;
		float mDeltaX = x - mLastX;
		float mDeltaY = y - mLastY;
		float mDeltaZ = z - mLastZ;
		mLastX = x;
		mLastY = y;
		mLastZ = z;
		mSpeed = Math.sqrt(mDeltaX * mDeltaX + mDeltaY * mDeltaY + mDeltaZ * mDeltaZ)
				/ mTimeInterval * 10000;
		if (mSpeed >= SPEED_SHRESHOLD) {
			listener.onConclusion();
		}
	}

	public void resume() {
		if (mSensor != null)
			mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
	}

	public void pause() {
		if (mSensor != null)
			mSensorManager.unregisterListener(this);
	}

	public interface ConclusionListener {

		public void onConclusion(int value);

		public void onConclusion();

		public void onFail(String msg);
	}
}
