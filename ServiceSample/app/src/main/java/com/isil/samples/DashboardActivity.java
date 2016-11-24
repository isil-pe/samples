package com.isil.samples;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener{

    private View btnStart;
    private View btnStop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        btnStart= findViewById(R.id.btnStart);
        btnStop= findViewById(R.id.btnStop);

        btnStart.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnStart:
                startLocationService();
                break;
            case R.id.btnStop:
                stopLocationService();
                break;
        }
    }

    private void startLocationService() {
        startService(new Intent(this, LocationService.class));
    }

    private void stopLocationService() {
        stopService(new Intent(this, LocationService.class));
    }
}
