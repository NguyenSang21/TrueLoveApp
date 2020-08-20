package com.example.truelove.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.burhanrashid52.photoeditor.EditImageActivity;
import com.example.truelove.R;
import com.example.truelove.adapter.UserAdapter;
import com.example.truelove.custom_class.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.compat.GeoDataClient;
import com.google.android.libraries.places.compat.PlaceDetectionClient;
import com.google.android.libraries.places.compat.Places;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private User user[];
    private UserAdapter userAdapter;
    private FirebaseAuth mAuth;

    private DatabaseReference userDb;
    private String currentUId;

    ListView listView;
    List<User> rowItems;

    // LOCATION UPDATE WHEN OPEN APP
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean locationPermissionGranted;
    static public final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Double latitude;
    private Double longitude;
    private DatabaseReference databaseReference;
    private User userCurrent;
    protected LocationListener locationListener;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userDb = FirebaseDatabase.getInstance().getReference().child("users");
        mAuth = FirebaseAuth.getInstance();
        currentUId = mAuth.getCurrentUser().getUid();

        personalUI();

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
                userDb.child(userId).child("connections").child("nope").child(currentUId).setValue(true);

                Toast.makeText(MainActivity.this, "Left!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                User userObj = (User) dataObject;
                String userId = userObj.getUid();
                userDb.child(userId).child("connections").child("yeps").child(currentUId).setValue(true);
                isConnectionMatch(userId);

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

        // that run URL online for screen ChatUI
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // ================ location ==================================

        // get infor of user
        userCurrent= new User();
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        userCurrent.setUid(userId);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // set permission
        getLocationPermission();

        //get location
        getDeviceLocation();

    }

    private void isConnectionMatch(String userId) {
        DatabaseReference currentUserConnectReference = userDb.child(currentUId).child("connections").child("yeps").child(userId);
        currentUserConnectReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Toast.makeText(MainActivity.this, "new connection", Toast.LENGTH_SHORT).show();
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
                if(dataSnapshot.exists() && !dataSnapshot.child("connections").child("nope").hasChild(currentUId)
                        && !dataSnapshot.child("connections").child("yeps").hasChild(currentUId)
                        && dataSnapshot.child("sex").getValue().equals(oppositeUserSex)) {
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

    public void gotoFinder(View view) {
        Intent intent = new Intent(MainActivity.this, Finder.class);
        startActivity(intent);
        return;
    }

    private void personalUI() {
        getSupportActionBar().hide();
    }


    // upadate location of user current
    private void saveUserInfomation()  {
        final Map userInfo = new HashMap();
        userInfo.put("name", userCurrent.getName());
        userInfo.put("email", userCurrent.getEmail());
        userInfo.put("phone", userCurrent.getPhone());
        userInfo.put("address", userCurrent.getAddress());
        userInfo.put("age", userCurrent.getAge());
        userInfo.put("userId", userCurrent.getUid());
        userInfo.put("latitude", latitude);
        userInfo.put("longitude", longitude);
        userInfo.put("sex", userCurrent.getSex());
        userInfo.put("img", userCurrent.getImg());
        databaseReference.updateChildren(userInfo);
//        finish();
    }

    private void getUserInfo() {

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("name") != null) {
                        userCurrent.setName(map.get("name").toString());
                    }
                    if (map.get("age") != null) {
                        userCurrent.setAge(Integer.valueOf(map.get("age").toString()));
                    }
                    if (map.get("address") != null) {
                        userCurrent.setAddress(map.get("address").toString());
                    }
                    if (map.get("phone") != null) {
                        userCurrent.setPhone(map.get("phone").toString());
                    }
                    if (map.get("email") != null) {
                        userCurrent.setEmail(map.get("email").toString());
                    }
                    if (map.get("img") != null) {
                        userCurrent.setImg(map.get("img").toString());
                    }
                    if (map.get("sex") != null) {
                        String sexs = map.get("sex").toString();
                        userCurrent.setSex(sexs);
                    }
                    saveUserInfomation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            Location lastKnownLocation = task.getResult();

                            if (lastKnownLocation != null) {

                                double lat = lastKnownLocation.getLatitude();
                                double longi = lastKnownLocation.getLongitude();
                                Toast.makeText(MainActivity.this, "LATITUDE =" + lat + "LONGI =" + longi , Toast.LENGTH_SHORT).show();
                                latitude=lat;
                                longitude=longi;
                                Toast.makeText(MainActivity.this, "LATITUDE =" + lat + "LONGI =" + longi , Toast.LENGTH_SHORT).show();
                                getUserInfo();

                            }
                        } else {

                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
}