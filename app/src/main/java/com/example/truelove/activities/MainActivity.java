package com.example.truelove.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.truelove.R;
import com.example.truelove.adapter.UserAdapter;
import com.example.truelove.custom_class.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private User user[];
    private UserAdapter userAdapter;
    private FirebaseAuth mAuth;

    private DatabaseReference userDb;
    private String currentUId;

    ListView listView;
    List<User> rowItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userDb = FirebaseDatabase.getInstance().getReference().child("users");
        mAuth = FirebaseAuth.getInstance();
        currentUId = mAuth.getCurrentUser().getUid();

        checkUserSex();

        rowItems = new ArrayList<User>();

        userAdapter = new UserAdapter(this, R.layout.item, rowItems );

        SwipeFlingAdapterView flingContainer = findViewById(R.id.frame);
        flingContainer.setAdapter(userAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                rowItems.remove(0);
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                User userObj = (User) dataObject;
                String userId = userObj.getUid();
                userDb.child(oppositeUserSex).child(userId).child("connections").child("nope").child(currentUId).setValue(true);

                isConnectionMatch(userId);
                Toast.makeText(MainActivity.this, "Left!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                User userObj = (User) dataObject;
                String userId = userObj.getUid();
                userDb.child(userId).child("connections").child("yeps").child(currentUId).setValue(true);
                Toast.makeText(MainActivity.this, "Right!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Toast.makeText(MainActivity.this, "Clicked!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void isConnectionMatch(String userId) {
        DatabaseReference currentUserConnectReference = userDb.child(userSex).child(currentUId).child("connections").child("yeps").child(userId);
        currentUserConnectReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()) {
                    Toast.makeText(MainActivity.this, "new connection", Toast.LENGTH_SHORT).show();
                    userDb.child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUId).setValue(true);
                    userDb.child(currentUId).child("connections").child("matches").child(dataSnapshot.getKey()).setValue(true);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String userSex;
    private String oppositeUserSex;

    public void checkUserSex() {
        final FirebaseUser userInfo = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference maleDb = userDb.child(userInfo.getUid());
        maleDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getKey().equals(userInfo.getUid())) {
                    if(dataSnapshot.exists()) {
                        if(dataSnapshot.child("sex") != null) {
                            userSex = dataSnapshot.child("sex").getValue().toString();
                            Log.d("SEXXX = " , userSex);
                            switch (userSex) {
                                case "male":
                                    oppositeUserSex = "female";
                                    break;
                                case "female":
                                    oppositeUserSex = "male";
                                    break;
                            }
                            getOppositeSexUsers();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getOppositeSexUsers() {
        userDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists() && !dataSnapshot.child("connections").child("nope").hasChild(currentUId) && !dataSnapshot.child("connections").child("yeps").hasChild(currentUId) && dataSnapshot.child("sex").getValue().equals(oppositeUserSex)) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    int age = Integer.parseInt(dataSnapshot.child("age").getValue().toString());
                    String address = dataSnapshot.child("address").getValue().toString();
                    String img = dataSnapshot.child("img").getValue().toString();
                    User userInfo = new User(dataSnapshot.getKey(), name, age, address, img);

                    rowItems.add(userInfo);
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void logoutUser (View view) {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, ChooseLoginRegistrationActivity.class);
        startActivity(intent);
        finish();
        return;
    }

    public void goToProfile(View view) {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
        return;
    }


    public void gotoMatches(View view) {
        Intent intent = new Intent(MainActivity.this, MatchesActivity.class);
        startActivity(intent);
        return;
    }
}