package com.example.truelove.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.truelove.R;
import com.example.truelove.custom_class.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtName, edtEmail, edtPassword, edtAge, edtAddress;
    private RadioGroup mRadioGroupSex;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        personalUI();
        mapping();
        btnRegister.setOnClickListener(this);
        registration();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }

    private void registration() {
        mAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };
    }

    private void personalUI() {
        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    private void mapping() {
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        mRadioGroupSex = findViewById(R.id.radioGroupSex);
        edtAge = findViewById(R.id.edtAge);
        edtAddress = findViewById(R.id.edtAddress);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        Toast.makeText(this, "ON CLICK !!!", Toast.LENGTH_SHORT).show();
        switch (viewId) {
            case R.id.btnRegister:
                final String email = edtEmail.getText().toString().trim();
                final String password = edtPassword.getText().toString().trim();
                final String name = edtName.getText().toString().trim();
                final int selectedId = mRadioGroupSex.getCheckedRadioButtonId();
                final int age = Integer.parseInt(edtAge.getText().toString());
                final String address = edtAddress.getText().toString().trim();

                final RadioButton radioButton = findViewById(selectedId);
                String sex = "";

                if (radioButton.getText() == null) {
                    return;
                } else if (radioButton.getText().equals("Nam")) {
                    sex = "male";
                } else if (radioButton.getText().equals("Nữ")) {
                    sex = "female";
                }


                final String finalSex = sex;
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    String userId = mAuth.getCurrentUser().getUid();
                                    DatabaseReference currentReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
                                    User user = new User(userId, name, age, address, finalSex);
                                    currentReference.setValue(user);

                                    Toast.makeText(RegistrationActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(RegistrationActivity.this, "Đăng ký thất bại!", Toast.LENGTH_SHORT).show();
                                }
                                // ...
                            }
                        });
                break;
        }
    }
}