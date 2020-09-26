package com.example.truelove.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.truelove.R;
import com.example.truelove.adapter.AlbumAdapter;
import com.example.truelove.custom_class.Album;
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

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFindersActivity extends AppCompatActivity {

    private TextView profileName, cationUser, profileAddress, profileAge, txtNameUserCurrent;
    private TextView profileSex;
    private CircleImageView profileImage;
    private ImageButton matchNope;
    private ImageView relativeLayoutBackgrouduser;

    private String matchID;
    private String isChatScreen;
    private DatabaseReference databaseReference;

    private String userId;

    private Uri resultUri;
    private String uriImage = "default";
    private RecyclerView recyclerViewAlbums;
    private ArrayList albumArray = new ArrayList<Album>();

    private Button btnBack;
    private ImageButton btnGoChatScreen;

    // set defaul my school if data null
    private Double latitudeCurrent = 10.762918;
    private Double longitudeCurrent = 106.682284;
    GridLayoutManager gridLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_finders);
        personalUI();
        mapping();

        matchID = getIntent().getExtras().getString("matchId");
        isChatScreen = getIntent().getExtras().getString("chatScreen");

        if(!StringUtils.isEmpty(isChatScreen)){
            btnGoChatScreen.setVisibility(View.GONE);
        }
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(matchID);

        getUserInfo();
        getAlbums();


        matchNope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.child("connections").child("nope").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                databaseReference.child("connections").child("yeps").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                // delete match tren tren thang dang chat
                databaseReference.child("connections").child("matches").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                DatabaseReference databaseReferenceMy = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                databaseReferenceMy.child("connections").child("matches").child(matchID).removeValue();
                databaseReferenceMy.child("connections").child("yeps").child(matchID).removeValue();
                // detechat tren thang
                Toast.makeText(ProfileFindersActivity.this, " You has nope " + profileName.getText().toString(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("matchIdReturn", matchID);
                intent.putExtra("matchNope", "matchNope");
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    void getAlbums() {
        albumArray.clear();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerViewAlbums.setLayoutManager(gridLayoutManager);
        final AlbumAdapter albumAdapter = new AlbumAdapter(getApplicationContext(), albumArray);
        albumAdapter.setActitityCurrent(this);
        albumAdapter.setUserIdMatch(this.matchID);
        recyclerViewAlbums.setAdapter(albumAdapter);

        DatabaseReference currentUserConnectReference = databaseReference.child("albums");
        currentUserConnectReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                albumArray.clear();
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    if (childDataSnapshot != null) {
                        Album album = new Album();
                        album.setIdStore((String) childDataSnapshot.getKey());
                        album.setImageUrl((String) childDataSnapshot.getValue());
                        albumArray.add(album);
                        albumAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isConnectionMatch(String userId) {
        final String currentUId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("users");
        DatabaseReference currentUserConnectReference = userDb.child(currentUId).child("connections").child("yeps").child(userId);
        currentUserConnectReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(ProfileFindersActivity.this, "new connection", Toast.LENGTH_SHORT).show();
                    String keyChat = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();

                    userDb.child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUId).child("chatId").setValue(keyChat);

                    userDb.child(currentUId).child("connections").child("matches").child(dataSnapshot.getKey()).child("chatId").setValue(keyChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserInfo() {

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("name") != null) {
                        profileName.setText(map.get("name").toString());
                        txtNameUserCurrent.setText(map.get("name").toString());
                    }
                    if (map.get("age") != null) {
                        profileAge.setText(map.get("age").toString());
                    }
                    if (map.get("address") != null) {
                        profileAddress.setText(map.get("address").toString());
                    }
                   /* if (map.get("phone") != null) {
                        profilePhone.setText(map.get("phone").toString());
                    }*/
                    if (map.get("caption") != null) {
                        cationUser.setText(map.get("caption").toString());
                    }
                    if (map.get("img") != null && !"default".equals(map.get("img"))) {
                        uriImage = map.get("img").toString();
                        Glide.with(getApplication()).load(uriImage).into(profileImage);
                    }
                    if (map.get("userbackgroud") != null && !"default".equals(map.get("userbackgroud"))) {
                        String anhbiaUriImage = map.get("userbackgroud").toString();

                        Bitmap bitmapUserOther = null;
                        try {
                            bitmapUserOther = bitmapUserOther = BitmapFactory.decodeStream((InputStream) new URL(anhbiaUriImage).getContent());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Drawable d = new BitmapDrawable(getResources(), bitmapUserOther);
                        relativeLayoutBackgrouduser.setImageDrawable(d);
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
                    if (map.get("latitude") != null) {
                        latitudeCurrent = Double.valueOf(map.get("latitude").toString());
                    }
                    if (map.get("longitude") != null) {
                        longitudeCurrent = Double.valueOf(map.get("longitude").toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void mapping() {

        profileName = findViewById(R.id.profileName);
        cationUser = findViewById(R.id.cationUser);
//        profilePhone = findViewById(R.id.profilePhone);
        profileImage = findViewById(R.id.profileImage);
        profileAddress = findViewById(R.id.profileAddress);
        profileAge = findViewById(R.id.profileAge);
        profileSex = findViewById(R.id.profileSex);
        matchNope = findViewById(R.id.macthNope);
        recyclerViewAlbums = findViewById(R.id.recyclerViewAlbums);
        relativeLayoutBackgrouduser = findViewById(R.id.backgroudImageProcess);
        txtNameUserCurrent = findViewById(R.id.txtNameUserCurrent);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });
        btnGoChatScreen=findViewById(R.id.btnGoChatScreen);
        btnGoChatScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Toasttt", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                Bundle b = new Bundle();
                b.putString("matchId", matchID);
                intent.putExtras(b);
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData();
            resultUri = imageUri;
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
            w.setStatusBarColor(Color.parseColor("#FB6667"));
        }
    }
}