package com.example.truelove.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.truelove.R;

public class ChooseLoginRegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_login_registration);
        mapping();
        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
    }

    private void mapping() {
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        Toast.makeText(this, "" + viewId, Toast.LENGTH_SHORT).show();
        switch (viewId) {
            case R.id.btnLogin:
                Intent intent1 = new Intent(ChooseLoginRegistrationActivity.this, LoginActivity.class);
                startActivity(intent1);
                finish();
                return;
            case R.id.btnRegister:
                Intent intent2 = new Intent(ChooseLoginRegistrationActivity.this, RegistrationActivity.class);
                startActivity(intent2);
                finish();
                break;
        }

    }
}