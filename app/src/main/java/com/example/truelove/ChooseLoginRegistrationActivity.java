package com.example.truelove;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ChooseLoginRegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_login_registration);
        mapping();

    }

    private void mapping() {
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        switch (viewId) {
            case R.id.btnLogin:
                Intent intent = new Intent(ChooseLoginRegistrationActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            case R.id.btnRegister:
                break;
        }

    }
}