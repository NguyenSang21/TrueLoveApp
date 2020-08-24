package com.example.truelove.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.burhanrashid52.photoeditor.EditImageActivity;
import com.example.truelove.R;
import com.example.truelove.adapter.AlbumAdapter;
import com.example.truelove.adapter.AlbumAdapter_finders;
import com.example.truelove.custom_class.Album;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity_view extends AppCompatActivity {

    private List<Album> albumArray = new ArrayList<Album>();
    private RecyclerView recyclerViewAlbums;
    private ImageView imageFull;
    private Toolbar toolbar;

    private String userIdMatch;
    private String storeIDExist;
    private DatabaseReference databaseReference;
     AlbumAdapter_finders albumAdapter;
     private int  indexCurrentInit=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view_finder);
        personalUI();
        mapping();
        // dialog
        LayoutInflater inflater = getLayoutInflater();
        storeIDExist=getIntent().getExtras().getString("storeIDExist");
        userIdMatch=getIntent().getExtras().getString("userIdMatch");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userIdMatch);
        getAlbums();
    }

    void getAlbums() {
        albumArray.clear();

        albumAdapter= new AlbumAdapter_finders( getApplicationContext(), albumArray);
        albumAdapter.setImgFull(imageFull);
        albumAdapter.setRecyclerViews(this.recyclerViewAlbums);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(ProfileActivity_view.this, LinearLayoutManager.HORIZONTAL, false){
            @Override
            public void onLayoutCompleted(RecyclerView.State state) {
                super.onLayoutCompleted(state);
                if(indexCurrentInit!=-1){
                    setInitalImage(storeIDExist);
                }
            }
        };

        recyclerViewAlbums.setLayoutManager(layoutManager);
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

    private void mapping() {
        recyclerViewAlbums = findViewById(R.id.recyclerViewAlbums);
        recyclerViewAlbums.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d("-----", "end" + newState);
                Log.d("-----", "end");
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    albumAdapter.resetBoderImageMin(albumAdapter.viewHolderCurrent);
                    if(indexCurrentInit !=-1){
                        albumAdapter.setInitListImage(indexCurrentInit,albumArray.get(indexCurrentInit).getIdStore());
                        indexCurrentInit=-1;
                    }
                }

            }
        });
        imageFull=findViewById(R.id.imageViewFull);
        toolbar = findViewById(R.id.toolbarFinder);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                finish();
            }
        });
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

    void setInitalImage(String idStoreImage) {
        if (albumArray != null && albumArray.size() != 0) {
            for (int i = 0; i < albumArray.size(); i++) {
                final int indexs = i;
                if (!StringUtils.isEmpty(idStoreImage) && idStoreImage.equals(albumArray.get(i).getIdStore())) {
                    if(i<=2){
                        albumAdapter.setInitListImage(i,albumArray.get(i).getIdStore());
                        indexCurrentInit=-1;
                    }else{
                        recyclerViewAlbums.post(new Runnable() {
                            @Override
                            public void run() {
                                recyclerViewAlbums.smoothScrollToPosition(indexs);
                                indexCurrentInit=indexs;
                            }
                        });
                    }
                    break;
                }
            }
        }
    }
}