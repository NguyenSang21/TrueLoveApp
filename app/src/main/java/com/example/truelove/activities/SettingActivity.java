package com.example.truelove.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.truelove.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar;

import java.util.Map;

public class SettingActivity extends AppCompatActivity {
    private TextView txtKM;
    private SeekBar seekBarKM;
    private Button btnSave;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String userId;
    private int km=2;
    private int minOld=18;
    private int maxOld=60;
    RangeSeekBar rangeSeekBar;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        //setup km setting
        seekBarKM = (SeekBar) findViewById(R.id.seekBarKM);
        txtKM = (TextView) findViewById(R.id.txtKM);
        btnSave=findViewById(R.id.btnSave);


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
        rangeSeekBar = (RangeSeekBar) findViewById(R.id.rangeSeekBar);
        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                minOld = Integer.valueOf(minValue.toString());
                maxOld = Integer.valueOf(maxValue.toString());
            }
        });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.child("setting").child("distanceMatch").setValue(String.valueOf(km));
                databaseReference.child("setting").child("ageMinMatch").setValue(String.valueOf(minOld));
                databaseReference.child("setting").child("ageMaxMatch").setValue(String.valueOf(maxOld));
                Toast.makeText(SettingActivity.this, "Đã Save!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        getSettingOfUser();

    }



    void getSettingOfUser(){
        databaseReference.child("setting").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("ageMaxMatch") != null) {
                        rangeSeekBar.setSelectedMinValue(Integer.valueOf(map.get("ageMinMatch").toString()));
                    }

                    if (map.get("ageMinMatch") != null) {
                        rangeSeekBar.setSelectedMaxValue(Integer.valueOf(map.get("ageMaxMatch").toString()));
                    }

                    if (map.get("distanceMatch") != null) {
                        seekBarKM.setProgress(Integer.valueOf(map.get("distanceMatch").toString()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
