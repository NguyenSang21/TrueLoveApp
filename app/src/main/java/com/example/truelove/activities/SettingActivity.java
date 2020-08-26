package com.example.truelove.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.truelove.R;
import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar;

public class SettingActivity extends AppCompatActivity {
    private TextView txtKM;
    private SeekBar seekBarKM;
    private int km;
    private int minOld;
    private int maxOld;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //setup km setting
        seekBarKM = (SeekBar) findViewById(R.id.seekBarKM);
        txtKM = (TextView) findViewById(R.id.txtKM);
        seekBarKM.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (progress <= 2) {
                    progressValue = 2;
                } else {
                    progressValue = progress;
                }
                txtKM.setText(String.valueOf(progressValue) + " km");
                km = progressValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //setup range old
        RangeSeekBar rangeSeekBar = (RangeSeekBar) findViewById(R.id.rangeSeekBar);
        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                minOld = Integer.valueOf(minValue.toString());
                maxOld = Integer.valueOf(maxValue.toString());
            }
        });


    }
}