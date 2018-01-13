package com.tobiahenryfinder.wakeup;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;


import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private boolean startWakeUp = false;
    public Vibrator vibrator;
    public CountDownTimer timer;
    public TextView timeLeft;
    public Button goButton;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // final Button goButton = (Button)findViewById(R.id.goButton);
        final EditText hoursField = (EditText) findViewById(R.id.hoursField);
        final EditText minutesField = (EditText) findViewById(R.id.minutesField);
        final EditText intervalField = (EditText) findViewById(R.id.intervalField);
        final CheckBox vibrateRB = (CheckBox) findViewById(R.id.vibrateRB);
        SensorManager sensorManager =(SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor acceleromterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        final CheckBox actionsRB = (CheckBox) findViewById(R.id.physicalActionRB);


        if (sensorManager != null) {
            acceleromterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        if (acceleromterSensor == null) {
            actionsRB.setEnabled(false);
            Log.i("No accelerometer", "This Phone has no accelerometer");
        } else
            Log.i("Accelerometer", "This phone has an accelerometer");

        final CheckBox popupRB = (CheckBox) findViewById(R.id.popupsRB);

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Button clicked", "Go button clicked");
                if (!startWakeUp && !checkForValidEntries()) {
                    Toast.makeText(getApplicationContext(), "Hours and Minutes must be greater than or equal to 0, but only one of them can be at a time. Interval must be >= .25", Toast.LENGTH_LONG).show();
                } else {
                    if (!startWakeUp) {
                        startWakeUp = true;
                        goButton.setText("Stop");

                        float hours = Float.parseFloat(hoursField.getText().toString());
                        float minutes = Float.parseFloat(minutesField.getText().toString());
                        float interval = Float.parseFloat(intervalField.getText().toString());


                        vibrator.vibrate(1000);

                        //testing alert dialogue
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle("Alert");
                        alertDialog.setMessage("BE ALARMED");

                        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                            }
                        });
                        alertDialog.show();
                        startKeepThemAwake(hours, minutes, interval);
                        //end alert


                    } else {

                        stopKeepThemAwake();

                    }
                }
            }

            private boolean checkForValidEntries() {

                if (hoursField.getText().toString().equals("") || minutesField.getText().toString().equals("") || intervalField.getText().toString().equals("")) {
                    Log.i("Hours empty", "Hours Empty");
                    return false;
                }

                float hours = Float.parseFloat(hoursField.getText().toString());
                float minutes = Float.parseFloat(minutesField.getText().toString());
                float interval = Float.parseFloat(intervalField.getText().toString());
                if (hours < 0 || minutes < 0 || interval < .25) {
                    return false;
                } else if (hours == 0 && minutes == 0)
                    return false;

                return true;
            }
        });

    }

    private void stopKeepThemAwake() {
        startWakeUp = false;
        goButton.setText("Start");
        timer.cancel();
        timeLeft.setText("");
    }

    private void startKeepThemAwake(float hours, float minutes, float interval) {
        float hoursInMilliseconds = (hours * 3600) * 1000;
        float minutesInMilliseconds = minutes * 60000;
        final long intervalInMilliseconds = ((long) (interval * 60000));
        Log.i("intervalInMilli", String.format("%d", intervalInMilliseconds));
        final long totalTimeInMilliseconds = ((long) (hoursInMilliseconds + minutesInMilliseconds));
        timer = new CountDownTimer(totalTimeInMilliseconds, 1000) {
            private long timeStart = System.currentTimeMillis();

            @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
            @Override
            public void onTick(long millisUntilFinished) {
                String text = String.format(Locale.getDefault(), "Time Remaining: Hours: %02d  min: %02d sec: %02d",
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished), TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60);
                timeLeft.setText(text);

                Log.i("time passed", String.format("%d", System.currentTimeMillis() - timeStart));
//                if((System.currentTimeMillis() - timeStart) % intervalInMilliseconds == 0){
//                    doSomethingAnnoying();
//                }

            }

            @Override
            public void onFinish() {
                timeLeft.setText("YOU DID IT");
                stopKeepThemAwake();
            }
        }.start();
    }

    private void doSomethingAnnoying() {
        Log.i("annoying", "Doing something annoying");
    }

}
