package com.example.truelove.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.truelove.R;
import com.example.truelove.adapter.FindersAdapter;
import com.example.truelove.custom_class.FinderDistance;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.compat.GeoDataClient;
import com.google.android.libraries.places.compat.PlaceDetectionClient;
import com.google.android.libraries.places.compat.Places;

import com.example.truelove.custom_class.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Finder extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean locationPermissionGranted;
    static public final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    // list user find appear here
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<User> listAllUser = new ArrayList<User>();
    private List<FinderDistance> listFinders= new ArrayList<FinderDistance>();
    private Double latUserCurrent;
    private Double longiUserCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finder);

        personalUI();

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // set permission
        getLocationPermission();
        // get device location
        getDeviceLocation();
        // get all user
        getAllUser();

        // list user find appear here
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewFinder);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(Finder.this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new FindersAdapter(getDatasetFinders(), Finder.this);
        recyclerView.setAdapter(mAdapter);

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
                                latUserCurrent = lastKnownLocation.getLatitude();
                                longiUserCurrent = lastKnownLocation.getLongitude();
                                Toast.makeText(Finder.this, "LATITUDE =" + latUserCurrent + "LONGI =" + longiUserCurrent , Toast.LENGTH_SHORT).show();
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

    private List<FinderDistance> getDatasetFinders() {
        return listFinders;
    }

    // distance user current with all user
    private void getFindersWithUserCurrent(User in){

        if(latUserCurrent == null || longiUserCurrent == null){
            Toast.makeText(Finder.this, "turn on GPS on your phone !!! " , Toast.LENGTH_SHORT).show();
        }

        // khoang cach user
        if(in!=null){
                FinderDistance userFinderDistance= new FinderDistance();
                userFinderDistance.setUser(in);
                Location userCurrentLocation= new Location(" User Current");
                userCurrentLocation.setLatitude( latUserCurrent);
                userCurrentLocation.setLongitude(longiUserCurrent);

                Location yourLocation= new Location("You");
                yourLocation.setLatitude( in.getLatitude());
                yourLocation.setLongitude(in.getLongitude());
                float kq=  userCurrentLocation.distanceTo(yourLocation);
                if(kq>1000){
                    // meter to km
                    float kqReality=(float) Math.round((kq/1000) * 10)/10;
                    userFinderDistance.setDistance(kqReality);
                    userFinderDistance.setUnit("km");
                }else{
                    // meter to km
                    float kqReality=(float) Math.round(kq * 10)/10;
                    userFinderDistance.setDistance(kqReality);
                    userFinderDistance.setUnit("meter");
                }
                listFinders.add(userFinderDistance);
            }
        // sort tang dan
        Collections.sort(listFinders, new Comparator<FinderDistance>() {
            @Override
            public int compare(FinderDistance truoc, FinderDistance sau) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                float truocDis=truoc.getDistance();
                float sauDis=sau.getDistance();
                if("km".equals(truoc.getUnit())){
                    truocDis=truocDis*1000;
                }
                if("km".equals(sau.getUnit())){
                    sauDis=sauDis*1000;
                }
                return truocDis > sauDis ? 1 : -1;
            }
        });
        mAdapter.notifyDataSetChanged();
    }

    //===================get all user ==========================
    private void getAllUser() {
        DatabaseReference matchDb = FirebaseDatabase.getInstance().getReference().child("users");
        matchDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(DataSnapshot match : dataSnapshot.getChildren()) {
                        fetchMatchInformation(match.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    DatabaseReference databaseReference;
    private void fetchMatchInformation(String key) {
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("users").child(key);
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    User obj = new User();
                    String userId = dataSnapshot.getKey();
                    String name = "";
                    String profileImageUrl = "";

                    if(dataSnapshot.child("name").getValue() != null) {
                        name = dataSnapshot.child("name").getValue().toString();
                    }

                    if(dataSnapshot.child("img").getValue() != null) {
                        profileImageUrl = dataSnapshot.child("img").getValue().toString();
                    }

                    if(dataSnapshot.child("latitude").getValue()==null || dataSnapshot.child("longitude").getValue() == null){
                        Double lat=radomLocationlatitude();
                        Double longi=radomLocationlongitude();
                        obj.setLatitude(lat);
                        obj.setLongitude(longi);
                    }else{
                        obj.setLatitude(Double.valueOf(dataSnapshot.child("latitude").getValue().toString().trim()));
                        obj.setLongitude(Double.valueOf(dataSnapshot.child("longitude").getValue().toString().trim()));
                    }

                    obj.setUid(userId);
                    obj.setName(name);
                    obj.setImg(profileImageUrl);

                    getFindersWithUserCurrent(obj);

                    // for update location for all user to testing
                    /*try {
                        saveUserInfomation(dataSnapshot);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    // for testing update location for all user
    private void saveUserInfomation(DataSnapshot dataSnapshot) throws IOException {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(dataSnapshot.getKey());
        String name = dataSnapshot.child("name").getValue().toString().trim();
        String email = dataSnapshot.child("email").getValue().toString().trim();
        String phone = dataSnapshot.child("phone").getValue().toString().trim();
        String address = dataSnapshot.child("address").getValue().toString().trim();
        String image = dataSnapshot.child("img").getValue().toString().trim();
        int age = Integer.parseInt(dataSnapshot.child("age").getValue().toString().trim());

        final Map userInfo = new HashMap();

        userInfo.put("name", name);
        userInfo.put("email", email);
        userInfo.put("phone", phone);
        userInfo.put("address", address);
        userInfo.put("age", age);
        userInfo.put("userId", dataSnapshot.getKey());

        String uriImage =  dataSnapshot.child("sex").getValue().toString().trim();
        if (uriImage == null) {
            return;
        } else if (uriImage.equals("")) {
//                            sex = "male";
            userInfo.put("sex", "male");
        } else if (uriImage.equals("Nữ")) {
//                            sex = "female";
            userInfo.put("sex", "female");
        }

        userInfo.put("img",image);

        if(dataSnapshot.child("latitude").getValue()==null || dataSnapshot.child("longitude").getValue() == null){
            Double lat=radomLocationlatitude();
            Double longi=radomLocationlongitude();
            userInfo.put("latitude", lat);
            userInfo.put("longitude", longi);
        }else{
            userInfo.put("latitude", dataSnapshot.child("latitude").getValue().toString().trim());
            userInfo.put("longitude", dataSnapshot.child("longitude").getValue().toString().trim());
        }
        System.out.println(userInfo);
        databaseReference.updateChildren(userInfo);

    }

    Double radomLocationlongitude(){
        // bac cu chi 11.076944, 106.457601
        // nam can gio 10.449601, 106.892934
        // dong 10.869324, 106.833883
        // tay 10.756016, 106.494680
        Double minlatitude=new Double("106.457601");
        Double maxlatitude=new Double("106.892934");
        Random r = new Random();
        Double random = minlatitude + r.nextFloat() * (maxlatitude - minlatitude);
        return random;

    }

    Double radomLocationlatitude(){
        // bac cu chi 11.076944, 106.457601
        // nam can gio 10.449601, 106.892934
        // dong 10.869324, 106.833883
        // tay 10.756016, 106.494680
        Double minlatitude=new Double("10.449601");
        Double maxlatitude=new Double("10.869324");
        Random r = new Random();
        Double random = minlatitude + r.nextFloat() * (maxlatitude - minlatitude);
        return random;
    }
}