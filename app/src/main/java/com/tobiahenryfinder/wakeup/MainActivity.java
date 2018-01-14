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
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private boolean startWakeUp = false;
    public Vibrator vibrator;
    public CountDownTimer timer;
    public TextView timeLeft;
    public Button goButton;
    public Random rand = new Random(System.currentTimeMillis());
    public CheckBox vibrateCB;
    public CheckBox actionsCB;
    public CheckBox popupCB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        final EditText hoursField = (EditText) findViewById(R.id.hoursField);
        final EditText minutesField = (EditText) findViewById(R.id.minutesField);
        final EditText intervalField = (EditText) findViewById(R.id.intervalField);
        vibrateCB = (CheckBox) findViewById(R.id.vibrateCB);
        vibrateCB.setChecked(true);
        timeLeft = (TextView) findViewById(R.id.timeLeft);
        goButton = (Button) findViewById(R.id.goButton);
        popupCB = (CheckBox) findViewById(R.id.popupCB);
        SensorManager sensorManager =(SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor acceleromterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        actionsCB = (CheckBox) findViewById(R.id.physicalActionsCB);


        if (acceleromterSensor == null) {
            actionsCB.setEnabled(false);
            Log.i("No accelerometer", "This Phone has no accelerometer");
        } else
            Log.i("Accelerometer", "This phone has an accelerometer");

        final CheckBox popupCB = (CheckBox) findViewById(R.id.popupCB);

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Button clicked", "Go button clicked");
                if (!startWakeUp && !checkForValidEntries()) {
                    Toast.makeText(getApplicationContext(), "Hours and Minutes must be greater than or equal to 0, but only one of them can be at a time." +
                            " Interval must be greater than .25 but less than the total time. At least 1 box must be checked.", Toast.LENGTH_LONG).show();
                } else {
                    if (!startWakeUp) {
                        startWakeUp = true;
                        goButton.setText("Stop");
                        popupCB.setEnabled(false);
                        actionsCB.setEnabled(false);
                        vibrateCB.setEnabled(false);
                        float hours = Float.parseFloat(hoursField.getText().toString());
                        float minutes = Float.parseFloat(minutesField.getText().toString());
                        float interval = Float.parseFloat(intervalField.getText().toString());
                        startKeepThemAwake(hours, minutes, interval);
                    } else {
                        stopKeepThemAwake(false);

                    }
                }
            }

            private boolean checkForValidEntries() {

                if (hoursField.getText().toString().equals("") || minutesField.getText().toString().equals("") || intervalField.getText().toString().equals("")) {
                    Log.i("Hours empty", "Hours Empty");
                    return false;
                }

                if(!vibrateCB.isChecked() && !popupCB.isChecked() && !actionsCB.isChecked()){
                    return false;
                }
                float hours = Float.parseFloat(hoursField.getText().toString());
                float minutes = Float.parseFloat(minutesField.getText().toString());
                float interval = Float.parseFloat(intervalField.getText().toString());
                if (hours < 0 || minutes < 0 || interval < .25) {
                    return false;
                } else if (hours == 0 && minutes == 0)
                    return false;
                else if (interval >= (hours *60) + minutes){
                    return false;
                }
                return true;
            }
        });

    }

    private void popupAlertWithVibrations() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("STAY AWAKE");
        alertDialog.setMessage("VIBRATION WON'T STOP UNTIL YOU CLICK OK");

        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                vibrator.cancel();
            }
        });


        vibrator.vibrate(3600000);
        alertDialog.show();

    }

    private void popupAlertWithSound(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("STAY AWAKE");
        alertDialog.setMessage("SOUND WON'T STOP UNTIL YOU CLICK OK");

        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                //stop sound
            }
        });
        //make sound happen
        alertDialog.show();

    }

    private void stopKeepThemAwake(boolean didIt) {
        startWakeUp = false;
        goButton.setText("Start");
        timer.cancel();
        if (didIt)
            timeLeft.setText("YOU DID IT");
        else
            timeLeft.setText("");


        popupCB.setEnabled(true);
        actionsCB.setEnabled(true);
        vibrateCB.setEnabled(true);
    }

    private void startKeepThemAwake(float hours, float minutes, float interval) {
        float hoursInMilliseconds = (hours * 3600) * 1000;
        float minutesInMilliseconds = minutes * 60000;
        final long intervalInMilliseconds = ((long) (interval * 60000));
        Log.i("intervalInMilli", String.format("%d", intervalInMilliseconds));
        final long totalTimeInMilliseconds = ((long) (hoursInMilliseconds + minutesInMilliseconds));
        timer = new CountDownTimer(totalTimeInMilliseconds, 1000) {
            private long timeStart = System.currentTimeMillis();

            @Override
            public void onTick(long millisUntilFinished) {
                String text = String.format(Locale.getDefault(), "Time Remaining: Hours: %02d  min: %02d sec: %02d",
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished), TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60);
                timeLeft.setText(text);

                Log.i("time passed", String.format("%d", System.currentTimeMillis() - timeStart));
                if ((System.currentTimeMillis() - timeStart) % intervalInMilliseconds <= 100 && (System.currentTimeMillis()-timeStart) > 1000) {
                    doSomethingAnnoying();
                }

            }

            @Override
            public void onFinish() {
                stopKeepThemAwake(true);

            }
        }.start();
    }

    private void doSomethingAnnoying() {
        Log.i("annoying", "Doing something annoying");
        boolean actionHappened = false;
        while(!actionHappened){
        int option = rand.nextInt(3);

        switch (option) {
            case 0:
                Log.i("vibration", "Vibrating");
                if(vibrateCB.isChecked()) {
                    popupAlertWithVibrations();
                    actionHappened = true;
                }
                break;
            case 1:
                //make sound with alert happen
                if(popupCB.isChecked()) {
                    Log.i("Alert", "Annoying alert");
                    //popupAlertWithSound()
                    actionHappened = true;
                }
                break;
            case 2:
                //physical activity
                Log.i("Physical Challenge", "Physical Challenge");
                if(actionsCB.isChecked()){
                    //accelerometer stuff
                    //popupAlertUntilMovement()
                    actionHappened = true;
                }
                break;
            }
        }
    }

}
