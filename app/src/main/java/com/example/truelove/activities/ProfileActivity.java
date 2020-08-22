package com.example.truelove.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AlertDialogLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.burhanrashid52.photoeditor.EditImageActivity;
import com.example.truelove.R;
import com.example.truelove.adapter.AlbumAdapter;
import com.example.truelove.custom_class.Album;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private EditText profileName, profileEMail, profilePhone, profileAddress, profileAge;
    private Button btnCamera, btnGallery, btnUpImage;
    private TextView profileSex;
    private CircleImageView profileImage;
    private Button profileConfirm, btnBack;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String userId;
    private Uri resultUri;
    private String uriImage ="default";
    private RecyclerView recyclerViewAlbums;
    private ArrayList albumArray = new ArrayList<Album>();

    // set defaul my school if data null
    private Double latitudeCurrent = 10.762918;
    private Double longitudeCurrent = 106.682284;
    private AlertDialog alertDialog;
    private Boolean isUploadAlbums = false;
    GridLayoutManager gridLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        personalUI();
        mapping();
        // dialog
        LayoutInflater inflater = getLayoutInflater();
        final View dialoglayout = inflater.inflate(R.layout.choose_camera_gallery, null);
        mapping2(dialoglayout);

        alertDialog = new AlertDialog.Builder(ProfileActivity.this)
                .setView(dialoglayout)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //Do something
                            }
                        }
                ).create();

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        getUserInfo();
        getAlbums();

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
                alertDialog.show();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.hide();
                Toast.makeText(ProfileActivity.this, "CAMERA", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ProfileActivity.this, EditImageActivity.class);
                if(uriImage!=null){
                    intent.putExtra("imageUri", uriImage);
                }
                startActivityForResult(intent, 1);
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.hide();
                Toast.makeText(ProfileActivity.this, "Gallery", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        btnUpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isUploadAlbums = true;
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);

            }
        });
    }

    void getAlbums() {
        albumArray.clear();
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerViewAlbums.setLayoutManager(gridLayoutManager);
        final AlbumAdapter albumAdapter = new AlbumAdapter(getApplicationContext(), albumArray);
        recyclerViewAlbums.setAdapter(albumAdapter);

        DatabaseReference currentUserConnectReference = databaseReference.child("albums");
        currentUserConnectReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                albumArray.clear();
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    Album album = new Album();
                    album.setImageUrl((String) childDataSnapshot.getValue());
                    albumArray.add(album);
                    albumAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void uploadImageToServer() throws IOException {
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
                            databaseReference.child("albums").push().setValue(imageUrl);
                            Toast.makeText(ProfileActivity.this, "Tải lên thành công!", Toast.LENGTH_SHORT).show();
                            resultUri=null;
                            isUploadAlbums=false;
                        }
                    });
                }
            });
        }
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
                            alertDialog.cancel();
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
        btnUpImage = findViewById(R.id.btnUpImage);
        recyclerViewAlbums = findViewById(R.id.recyclerViewAlbums);
    }

    private void mapping2(View view) {
        btnCamera = view.findViewById(R.id.btnCamera);
        btnGallery = view.findViewById(R.id.btnGallery);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            if(isUploadAlbums) {
                try {
                    uploadImageToServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else { // pich up
                profileImage.setImageURI(resultUri);
            }
        }

        // camera editor
        if(requestCode == 1 && resultCode == 9999) {
            String imagePath = data.getExtras().getString("filepath");
            resultUri = Uri.fromFile(new File(imagePath));
            profileImage.setImageURI(resultUri);
        }
    }

    public void goToback(View view) {
        onBackPressed();
    }
    private void personalUI() {
        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // w.setStatusBarColor(Color.parseColor("#FB6667"));
        }
    }
}