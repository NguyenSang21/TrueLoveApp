package com.example.truelove.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.truelove.R;
import com.example.truelove.custom_class.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private EditText profileName, profileEMail, profilePhone, profileAddress, profileAge;
    private TextView profileSex;
    private CircleImageView profileImage;
    private Button profileConfirm, btnBack;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private String userId;

    private Uri resultUri;
    private String uriImage ="default";

    // set defaul my school if data null
    private Double latitudeCurrent=10.762918;
    private Double longitudeCurrent=106.682284;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mapping();

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        getUserInfo();

        profileConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    saveUserInfomation();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });
    }

    private void getUserInfo() {

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("name") != null) {
                        profileName.setText(map.get("name").toString());
                    }
                    if (map.get("age") != null) {
                        profileAge.setText(map.get("age").toString());
                    }
                    if (map.get("address") != null) {
                        profileAddress.setText(map.get("address").toString());
                    }
                    if (map.get("phone") != null) {
                        profilePhone.setText(map.get("phone").toString());
                    }
                    if (map.get("email") != null) {
                        profileEMail.setText(map.get("email").toString());
                    }
                    if (map.get("img") != null) {
                        uriImage = map.get("img").toString();
                        Glide.with(getApplication()).load(uriImage).into(profileImage);
                    }
                    if (map.get("sex") != null) {
                        String sexs = map.get("sex").toString();
                        if (sexs == null) {
                            return;
                        } else if (sexs.equals("male")) {
//                            sex = "male";
                            profileSex.setText("Nam");
                        } else if (sexs.equals("female")) {
//                            sex = "female";
                            profileSex.setText("Nữ");
                        }
                    }
                    if(map.get("latitude")!=null){
                        latitudeCurrent= Double.valueOf(map.get("latitude").toString());
                    }
                    if(map.get("longitude")!=null){
                        longitudeCurrent= Double.valueOf(map.get("longitude").toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveUserInfomation() throws IOException {
        String name = profileName.getText().toString().trim();
        String email = profileEMail.getText().toString().trim();
        String phone = profilePhone.getText().toString().trim();
        String address = profileAddress.getText().toString().trim();
        int age = Integer.parseInt(profileAge.getText().toString().trim());

        final Map userInfo = new HashMap();

        userInfo.put("name", name);
        userInfo.put("email", email);
        userInfo.put("phone", phone);
        userInfo.put("address", address);
        userInfo.put("age", age);
        userInfo.put("userId", this.userId);
        userInfo.put("latitude", latitudeCurrent);
        userInfo.put("longitude", longitudeCurrent);

        String sexss =  profileSex.getText().toString();
        if (sexss == null) {
            return;
        } else if (sexss.equals("")) {
//                            sex = "male";
            userInfo.put("sex", "male");
        } else if (sexss.equals("Nữ")) {
//                            sex = "female";
            userInfo.put("sex", "female");
        }

        if (resultUri != null) {
            StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileImages").child(userId);
            Bitmap bitmap =  null;
            bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = filepath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                }
            });

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> dowloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    dowloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageUrl = uri.toString();
                            userInfo.put("img", imageUrl);
                            databaseReference.updateChildren(userInfo);
                            Toast.makeText(ProfileActivity.this, "Lưu thành công!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    finish();
                }
            });
        }else{
            userInfo.put("img", uriImage);
            databaseReference.updateChildren(userInfo);
            Toast.makeText(ProfileActivity.this, "Lưu thành công!", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void mapping() {

        profileName = findViewById(R.id.profileName);
        profileEMail = findViewById(R.id.profileEMail);
        profilePhone = findViewById(R.id.profilePhone);
        profileImage = findViewById(R.id.profileImage);
        profileConfirm = findViewById(R.id.profileConfirm);
        profileAddress = findViewById(R.id.profileAddress);
        profileAge = findViewById(R.id.profileAge);
        profileSex = findViewById(R.id.profileSex);
        btnBack = findViewById(R.id.btnBack);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            profileImage.setImageURI(resultUri);
        }
    }

    public void goToback(View view) {
        onBackPressed();
    }
}