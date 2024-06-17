package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private EditText home_TXT_id;
    String betteryLevel;
    private MaterialButton home_BTN_sign;
    String pass;

    private static final int CAMERA_PERMISSION_CODE = 100;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private String direction;
    private String ssid;
    private float[] gravity = new float[3];
    private float[] geomagnetic = new float[3];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        requestCameraPermission();

        findView();
        initView();
    }




    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, magnetometer);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.97f;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * event.values[0];
            geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * event.values[1];
            geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * event.values[2];
        }

        if (gravity != null && geomagnetic != null) {
            float[] R = new float[9];
            float[] I = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
            if (success) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimuth = orientation[0]; // Azimuth in radians
                float degree = (float) Math.toDegrees(azimuth); // Convert to degrees
                if (degree < 0) {
                    degree += 360;
                }

                // Determine the direction
                if (degree >= 345 || degree <= 15) {
                    direction = "North";
                } else if (degree > 15 && degree <= 75) {
                    direction = "North";
                } else if (degree > 75 && degree <= 105) {
                    direction = "East";
                } else if (degree > 105 && degree <= 165) {
                    direction = "South-East";
                } else if (degree > 165 && degree <= 195) {
                    direction = "South";
                } else if (degree > 195 && degree <= 255) {
                    direction = "South-West";
                } else if (degree > 255 && degree <= 285) {
                    direction = "West";
                } else {
                    direction = "North";
                }

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle changes in sensor accuracy if needed
    }

    private  String getBatteryLevel(Context context) {
        Intent batteryStatus = context.registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int batteryLevel = -1;
        int batteryScale = 1;
        if (batteryStatus != null) {
            batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, batteryLevel);
            batteryScale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, batteryScale);
        }
        String b= String.valueOf(batteryLevel / (int) batteryScale * 100);

        return b;
    }

    private void initView() {
        home_BTN_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pass=home_TXT_id.getText().toString();
                if(cheakEnter()){
                    Toast.makeText(MainActivity.this,"login succsesfull",Toast.LENGTH_SHORT).show();
                }else{
                    updateUI();
                }
            }
        });
    }

    private boolean cheackBetterylevel(){
        betteryLevel=(getBatteryLevel(MainActivity.this));

        if(pass.equals(betteryLevel)){
            return true;
        }
        return false;
    }

    private boolean cheakEnter(){

        if (cheackBetterylevel()){
            if(direction=="North"){
                if( checkCameraPermission()){

                }else{
                    Toast.makeText(MainActivity.this,"you must give me access to camera",Toast.LENGTH_SHORT).show();
                    return false;
                }
            }else{
                Toast.makeText(MainActivity.this,"face to north",Toast.LENGTH_SHORT).show();
                return false;
            }
        }else {
            Toast.makeText(MainActivity.this,"your pass must be your bettery charge",Toast.LENGTH_SHORT).show();

            return false;
        }
        return true;
    }

    private boolean checkCameraPermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                // You can now use the camera
            } else {
                // Permission denied
                // Handle permission denied, e.g., display a message or disable functionality
            }
        }
    }

    private void findView() {
        home_TXT_id=findViewById(R.id.home_TXT_id);
        home_BTN_sign=findViewById(R.id.home_BTN_sign);
    }


    private void updateUI(){
        home_TXT_id.setText("");
        requestCameraPermission();

    }
}
