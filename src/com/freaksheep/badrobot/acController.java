package com.freaksheep.badrobot;

import android.hardware.Sensor;
import android.hardware.SensorManager;

public class acController {
    private static final int INITIAL_POSITION = 6;

    private final static float MAX_SPEED = 100;
    private final int mDifficulty;

    Sensor mSensor;
    
    /*
     * difficulty 1-3 Normal es 2
     */
    public acController(SensorManager mSensorManager, int dificulty) {
        this.mSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.mDifficulty = dificulty;
    }

    public acController(SensorManager mSensorManager) {
        this(mSensorManager, 2);
    }

    public Sensor getmSensor() {
		return mSensor;
	}

	public void setmSensor(Sensor mSensor) {
		this.mSensor = mSensor;
	}

	public int getmDifficulty() {
		return mDifficulty;
	}

	/*
     * calcula top, right, bottom and right
     */
    private int[] setVelocity(float x, float y) {
        float v1, v2;
        float Factor = (MAX_SPEED / 10) * ((7 - mDifficulty) * MAX_SPEED / 100);
        int back = 0, front = 0, left = 0, right = 0;

        // X Axis
        if (x >= -1 && x <= 1) {
            back = front = 0;
        } else if (x > 1) {
            v1 = (x - 1) * Factor;
            if (v1 > MAX_SPEED) {
                v1 = MAX_SPEED;
            } else {
                v1 = Math.round(v1 / 10) * 10; // de 10 en 10
            }
            back = (int) v1;
        } else if (x < -1) {
            v1 = (-1) * ((x + 1) * Factor);
            if (v1 > MAX_SPEED) {
                v1 = MAX_SPEED;
            } else {
                v1 = Math.round(v1 / 10) * 10;
            }
            front = (int) v1;
        } else {
            back = front = 0;
        }

        // Y Axis
        if (y >= -1 && y <= 1) {
            left = right = 0;
        } else if (y > 1) {
            v2 = (y - 1) * Factor;
            if (v2 > MAX_SPEED) {
                v2 = MAX_SPEED;
            } else {
                v2 = Math.round(v2 / 10) * 10;
            }
            right = (int) v2;
        } else if (y < -1) {
            v2 = (-1) * ((y + 1) * Factor);
            if (v2 > MAX_SPEED) {
                v2 = MAX_SPEED;
            } else {
                v2 = Math.round(v2 / 10) * 10;
            }
            left = (int) v2;
        } else {
            left = right = 0;
        }

        return new int[] { front, back, left, right };
    }

    public int[] calcVelocity(float x, float y) {
        x = x - INITIAL_POSITION;
        return setVelocity(x, y);
    }

    
    
}
