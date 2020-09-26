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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
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

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView textViewNameUserCurrent, txtEmailUserCurrent, txtCaption;
    private TextView profileSex, profileEMail, profileName, profilePhone, profileAddress, profileAge;
    private CircleImageView profileImage;
    private ImageView relativeLayoutBackgrouduser;
    private Button profileConfirm, btnBack, btnSetbackgourduser, btnUpImage, btnCamera, btnGallery;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String userId;
    private String avatarUriImage ="default";
    private String anhbiaUriImage ="default";
    private RecyclerView recyclerViewAlbums;
    private ArrayList albumArray = new ArrayList<Album>();

    // set defaul my school if data null
    private Double latitudeCurrent = 10.762918;
    private Double longitudeCurrent = 106.682284;
    private AlertDialog alertDialog;
    GridLayoutManager gridLayoutManager;

    // upload avatar
    private Uri avatatempEditor=null;
    private Uri avatatempMediaPick=null;
    private Uri avatarResultUri=null;

    // upload images anh bia
    private Uri anhBiaResultUri=null;
    private Uri anhBiaTempEditor=null;
    private Uri anhBiaTempMediaPick=null;

    // album
    private Uri albumResultUri=null;
    private String storeIDExist="";

    public int modeImage=0;
    //0 la avatar, 1 anh bia, 2 la albumn




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
                modeImage=0;
                if(!((Activity) ProfileActivity.this).isFinishing())
                {
                    try{
                        alertDialog.show();
                    }catch (Exception e){

                    }
                }

            }
        });

        btnSetbackgourduser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modeImage=1;
                if(!((Activity) ProfileActivity.this).isFinishing())
                {
                    try{
                        alertDialog.show();
                    }catch (Exception e){

                    }

                }
            }
        });

        btnUpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modeImage=2;
                if(!((Activity) ProfileActivity.this).isFinishing())
                {
                    try{
                        alertDialog.show();
                    }catch (Exception e){

                    }
                }
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
                if(!((Activity) ProfileActivity.this).isFinishing())
                {
                    alertDialog.hide();
                }
                cameraProcess();
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!((Activity) ProfileActivity.this).isFinishing())
                {
                    alertDialog.hide();
                }
                galleryProcess();
            }
        });
    }



    void getAlbums() {
        albumArray.clear();
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerViewAlbums.setLayoutManager(gridLayoutManager);
        final AlbumAdapter albumAdapter = new AlbumAdapter( getApplicationContext(), albumArray);
        albumAdapter.setActitityProfire(this);
        recyclerViewAlbums.setAdapter(albumAdapter);


        DatabaseReference currentUserConnectReference = databaseReference.child("albums");
        currentUserConnectReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                albumArray.clear();
                    if(dataSnapshot.getValue()!=null) {
                        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                                if(childDataSnapshot!=null){
                                    Album album = new Album();
                                    album.setIdStore(String.valueOf(childDataSnapshot.getKey()));
                                    album.setImageUrl(String.valueOf(childDataSnapshot.getValue()));
                                    albumArray.add(album);
                                    albumAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void cameraProcess(){
        // avatar
        if(modeImage==0){
            Toast.makeText(ProfileActivity.this, "CamaraProcess  avatar", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, EditImageActivity.class);
            if(avatatempEditor!=null){
                intent.putExtra("FileimageUri", avatatempEditor.getPath());
            }
            else if(avatatempMediaPick!=null){
                intent.setType("image/*");
                intent.putExtra("MediaimageUri", avatatempMediaPick);
            }else if(avatarUriImage!=null){
                if(avatarUriImage.equals("default")){
                    intent.putExtra("opencamera", "true");
                }else{
                    intent.putExtra("imageUri", avatarUriImage);
                }
            }
            startActivityForResult(intent, 1);
        } else if(modeImage==1){ // anh bia
            Toast.makeText(ProfileActivity.this, "CamaraProcess anh bia", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, EditImageActivity.class);
            if(anhBiaTempEditor!=null){
                intent.putExtra("FileimageUri", anhBiaTempEditor.getPath());
            }
            else if(anhBiaTempMediaPick!=null){
                intent.setType("image/*");
                intent.putExtra("MediaimageUri", anhBiaTempMediaPick);
            }else if(anhbiaUriImage!=null){
                if(anhbiaUriImage.equals("default")){
                    intent.putExtra("opencamera", "true");
                }else{
                    intent.putExtra("imageUri", anhbiaUriImage);
                }
            }
            startActivityForResult(intent, 1);
        } else if(modeImage==2){ // Album
            Toast.makeText(ProfileActivity.this, "CamaraProcess Album", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, EditImageActivity.class);
            intent.putExtra("opencamera", "true");
            startActivityForResult(intent, 1);
        }
    }


    private void galleryProcess(){
        Toast.makeText(ProfileActivity.this, "galleryProcess ", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    void uploadImageToServer() throws IOException {
        if (albumResultUri != null) {
            String idTeam="";
            if(!StringUtils.isEmpty(storeIDExist)){
                 idTeam=storeIDExist;
            }else{
                idTeam=userId+System.currentTimeMillis();
            }
            final String id= idTeam;
            StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileImages").child(id);
            Bitmap bitmap2 =  null;
            bitmap2 = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), albumResultUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            bitmap2.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data2 = baos.toByteArray();

            UploadTask uploadTask = filepath.putBytes(data2);

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
                            String imageUrl2 = uri.toString();
/*                            Map<String,String> mapUri= new HashMap<>();
                            mapUri.put(id,imageUrl2);*/
                            databaseReference.child("albums").child(id).setValue(imageUrl2);
                            Toast.makeText(ProfileActivity.this, "Tải lên thành công!", Toast.LENGTH_SHORT).show();
                            albumResultUri=null;
                            storeIDExist="";
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
                        textViewNameUserCurrent.setText(map.get("name").toString());
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
                        txtEmailUserCurrent.setText(map.get("email").toString());
                    }
                    if (map.get("img") != null && !"default".equals(map.get("img"))) {
                        avatarUriImage = map.get("img").toString();
                        Glide.with(getApplication()).load(avatarUriImage).into(profileImage);
                    }
                    if (map.get("userbackgroud") != null && !"default".equals(map.get("userbackgroud"))) {
                        anhbiaUriImage = map.get("userbackgroud").toString();

                        Bitmap bitmapUserOther=null;
                        try {
                            bitmapUserOther=  bitmapUserOther = BitmapFactory.decodeStream((InputStream)new URL(anhbiaUriImage).getContent());
                        } catch (OutOfMemoryError e){

                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(bitmapUserOther!=null){
                            Drawable d = new BitmapDrawable(getResources(), bitmapUserOther);
                            relativeLayoutBackgrouduser.setImageDrawable(d);
                        }else{
                            relativeLayoutBackgrouduser.setImageResource(R.drawable.backgroup_default);
                        }

                        /*Glide.with(getApplication()).load(anhbiaUriImage).into(relativeLayoutBackgrouduser);*/
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
                    if(map.get("caption")!=null) {
                        txtCaption.setText(map.get("caption").toString());
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
        String caption = txtCaption.getText().toString().trim();
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
        userInfo.put("caption", caption);

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

        if (avatarResultUri != null) {
            StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileImages").child(userId);
            Bitmap bitmap =  null;
            bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), avatarResultUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = filepath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
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
                            alertDialog.hide();

                            // luu anh bia
                            if (anhBiaResultUri != null) {
                                // anh bia thi them 001
                                String id=userId+"001";
                                StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileImages").child(id);
                                Bitmap bitmap =  null;
                                try {
                                    bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), anhBiaResultUri);
                                }catch (OutOfMemoryError e){

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
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
                                                userInfo.put("userbackgroud", imageUrl);
                                                databaseReference.updateChildren(userInfo);
                                                Toast.makeText(ProfileActivity.this, "Lưu thành công!", Toast.LENGTH_SHORT).show();
                                                alertDialog.hide();
                                            }
                                        });
                                        finish();
                                    }
                                });
                            }else{
                                userInfo.put("userbackgroud", anhbiaUriImage);
                                databaseReference.updateChildren(userInfo);
                                Toast.makeText(ProfileActivity.this, "Lưu thành công!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });
                }
            });
        }else{
            userInfo.put("img", avatarUriImage);
            databaseReference.updateChildren(userInfo);

            // luu anh bia
            if (anhBiaResultUri != null) {
                // anh bia thi them 001
                String id=userId+"001";
                StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileImages").child(id);
                Bitmap bitmap =  null;
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), anhBiaResultUri);
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
                                userInfo.put("userbackgroud", imageUrl);
                                databaseReference.updateChildren(userInfo);
                                Toast.makeText(ProfileActivity.this, "Lưu thành công!", Toast.LENGTH_SHORT).show();
                                alertDialog.hide();
                            }
                        });
                        finish();
                    }
                });
            }else{
                userInfo.put("userbackgroud", anhbiaUriImage);
                databaseReference.updateChildren(userInfo);
                Toast.makeText(ProfileActivity.this, "Lưu thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }
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
        btnSetbackgourduser=findViewById(R.id.btnBackgroundProfile);
        relativeLayoutBackgrouduser=findViewById(R.id.backgroudImageProcess);
        textViewNameUserCurrent=findViewById(R.id.nameUserCurrent);
        txtEmailUserCurrent=findViewById(R.id.emailUserCurrent);
        txtCaption = findViewById(R.id.caption);

    }

    private void mapping2(View view) {
        btnCamera = view.findViewById(R.id.btnCamera);
        btnGallery = view.findViewById(R.id.btnGallery);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // onActivityResult imagear
        listerResultOfImage(requestCode,resultCode,data);
    }

    private void listerResultOfImage(int requestCode, int resultCode, @Nullable Intent data){
        // pick image
        if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if(modeImage==0){ // avatar
                final Uri imageUri = data.getData();
                avatarResultUri = imageUri;
                profileImage.setImageURI(avatarResultUri);
                avatatempMediaPick=avatarResultUri;
                avatatempEditor=null;
            }
            else if(modeImage==1){ // anh bia
                final Uri imageUri = data.getData();
                anhBiaResultUri = imageUri;
                File f = new File(getRealPathFromURI(anhBiaResultUri));
                Drawable d = Drawable.createFromPath(f.getAbsolutePath());
//                relativeLayoutBackgrouduser.setImageDrawable(d);
                Glide.with(ProfileActivity.this).load(d).into(relativeLayoutBackgrouduser);
                anhBiaTempMediaPick=anhBiaResultUri;
                anhBiaTempEditor=null;
            }else if(modeImage==2){ // album
                final Uri imageUri = data.getData();
                albumResultUri= imageUri;
                try {
                    uploadImageToServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            /*final Uri imageUri = data.getData();
            avatarResultUri = imageUri;
            // upload Albums of user
            if(isUploadAlbums) {
                try {
                    uploadImageToServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else { // pich up Avata User
                profileImage.setImageURI(avatarResultUri);
                avatatempMediaPick=avatarResultUri;
                avatatempEditor=null;
            }*/
        }


        // camera editor
        if(requestCode == 1 && resultCode == 9999) {
            if(modeImage==0){
                String imagePath = data.getExtras().getString("filepath");
                avatarResultUri = Uri.fromFile(new File(imagePath));
                profileImage.setImageURI(avatarResultUri);
                avatatempEditor=avatarResultUri;
                avatatempMediaPick=null;
            }
            else if(modeImage==1){ // anh bia
                String imagePath = data.getExtras().getString("filepath");
                anhBiaResultUri = Uri.fromFile(new File(imagePath));
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), anhBiaResultUri);
                }catch (OutOfMemoryError e){

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(bitmap!=null){
                    Drawable d = new BitmapDrawable(getResources(), bitmap);
//                relativeLayoutBackgrouduser.setImageDrawable(d);
                    Glide.with(ProfileActivity.this).load(d).into(relativeLayoutBackgrouduser);
                }else{
                    relativeLayoutBackgrouduser.setImageResource(R.drawable.backgroup_default);
                }

                anhBiaTempEditor=anhBiaResultUri;
                anhBiaTempMediaPick=null;
            } else if(modeImage==2){ // album
                String imagePath = data.getExtras().getString("filepath");
                albumResultUri = Uri.fromFile(new File(imagePath));
                storeIDExist=data.getExtras().getString("storeIDExist");
                try {
                    uploadImageToServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

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
            //w.setStatusBarColor(Color.parseColor("#007ac1"));
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        alertDialog.hide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        alertDialog.cancel();
    }
}