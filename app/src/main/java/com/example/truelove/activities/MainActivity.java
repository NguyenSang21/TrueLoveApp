package com.example.truelove.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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
import com.example.truelove.custom_class.FinderDistance;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

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


    //-------------------- setting
    private Double latUserCurrentSetting;
    private Double longiUserCurrentSetting;
    // get following setting
    private List<FinderDistance> listFinders= new ArrayList<FinderDistance>();
    private int km=2;
    private int minDistanceMatch=20;
    private int maxDistanceMatch=60;

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

        userAdapter = new UserAdapter(this, R.layout.item, listFinders );

        SwipeFlingAdapterView flingContainer = findViewById(R.id.frame);
        flingContainer.setAdapter(userAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                rowItems.remove(0);
                listFinders.remove(0);
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                FinderDistance userObj = (FinderDistance) dataObject;
                String userId = userObj.getUser().getUid();
                userDb.child(userId).child("connections").child("nope").child(currentUId).setValue(true);

                Toast.makeText(MainActivity.this, "Left!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                FinderDistance userObj = (FinderDistance) dataObject;
                String userId = userObj.getUser().getUid();
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

        //update locaiton currnte of user current
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
                    String caption = (String) dataSnapshot.child("caption").getValue();

                    if(caption == null || caption.equals("")) {
                        caption = "Thích động vật, nuôi chó";
                    }
                    userInfo.setCaption(caption);
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
//        mAuth.signOut();
//        Intent intent = new Intent(MainActivity.this, ChooseLoginRegistrationActivity.class);
//        startActivity(intent);
//        finish();
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
                    if (map.get("caption") != null) {
                        userCurrent.setCaption(map.get("caption").toString());
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

    public void goToSetting(View view) {
        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(intent);
        return;
    }

    //-------------------------------- setting ------------------------------
    private void getDeviceLocationSetting() {
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
                                latUserCurrentSetting = lastKnownLocation.getLatitude();
                                longiUserCurrentSetting = lastKnownLocation.getLongitude();
                                // run tinh khoang cach all user
                                getAllUserSetting();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Turn on GPS on your phone !!! " , Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    //===================get all user ==========================
    private void getAllUserSetting() {
        DatabaseReference matchDb = FirebaseDatabase.getInstance().getReference().child("users");
        matchDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(DataSnapshot match : dataSnapshot.getChildren()) {
                        fetchMatchInformationSetting(match.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchMatchInformationSetting(String key) {
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("users").child(key);
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    User obj = new User();
                    obj.setUid(dataSnapshot.getKey());

                    if(dataSnapshot.child("name").equals("Titus")){
                        System.out.print("");
                    }

                    if (dataSnapshot.child("age") != null) {
                        obj.setAge(Integer.valueOf(dataSnapshot.child("age").getValue().toString()));
                    }
                    if(dataSnapshot.child("name").getValue() != null) {
                        obj.setName(dataSnapshot.child("name").getValue().toString());
                    }

                    if(dataSnapshot.child("img").getValue() != null) {
                        obj.setImg(dataSnapshot.child("img").getValue().toString());
                    }

                    if(dataSnapshot.child("address").getValue() != null) {
                        obj.setAddress(dataSnapshot.child("address").getValue().toString());
                    }

                    if (dataSnapshot.child("sex").getValue()  != null) {
                        String sex = dataSnapshot.child("sex").getValue().toString();
                        if (sex == null) {
                            return;
                        } else if (sex.equals("male")) {
//                            sex = "male";
                            obj.setSex("Nam");
                        } else if (sex.equals("female")) {
//                            sex = "female";
                            obj.setSex("Nữ");
                        }
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
                    // KHONG SERACH USER HIEN TAI
                    if(!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(obj.getUid())){
                        // user dang search chua get hoac chua match ai het
                        if(dataSnapshot.child("connections")==null){
                            getFindersWithUserCurrentSetting(obj);
                        }else{
                            // user đang search không thich và không match vs user current
                        /*    if(!dataSnapshot.child("connections").child("nope").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid()) &&
                                    !dataSnapshot.child("connections").child("yeps").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            && !dataSnapshot.child("sex").getValue().equals(userCurrent.getSex()))*/

                                if(dataSnapshot.exists() && !dataSnapshot.child("connections").child("nope").hasChild(currentUId)
                                        && !dataSnapshot.child("connections").child("yeps").hasChild(currentUId)&&
                            !dataSnapshot.child("sex").getValue().equals(userCurrent.getSex()))
                            {
                                getFindersWithUserCurrentSetting(obj);
                            }
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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


    // distance user current with all user
    private void getFindersWithUserCurrentSetting(User in){

        if(latUserCurrentSetting == null || longiUserCurrentSetting == null){
            Toast.makeText(MainActivity.this, "turn on GPS on your phone !!! " , Toast.LENGTH_SHORT).show();
        }

        // khoang cach user
        if(in!=null){
            FinderDistance userFinderDistance= new FinderDistance();
            userFinderDistance.setUser(in);
            Location userCurrentLocation= new Location(" User Current");
            userCurrentLocation.setLatitude( latUserCurrentSetting);
            userCurrentLocation.setLongitude(longiUserCurrentSetting);

            Location yourLocation= new Location("You");
            yourLocation.setLatitude( in.getLatitude());
            yourLocation.setLongitude(in.getLongitude());
            float kq=  userCurrentLocation.distanceTo(yourLocation);

            // get address of you
            String addressOfYou = locationToAddressSetting(in.getLatitude(),in.getLongitude());
            userFinderDistance.setAddressCurrentOfYou(addressOfYou);

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
            // SET MIN =2, MAX = GET KM ON SCREEN
            float kmOfUser=0;
            if("meter".equals(userFinderDistance.getUnit())){
                kmOfUser=userFinderDistance.getDistance()/1000;
            }else{
                kmOfUser=userFinderDistance.getDistance();
            }
            // km va tuoi setting
            if( (kmOfUser >= 2 && kmOfUser <= km)  && (userFinderDistance.getUser().getAge() >= minDistanceMatch && userFinderDistance.getUser().getAge() <= maxDistanceMatch)){
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
            userAdapter.notifyDataSetChanged();
        }
    }


    private String locationToAddressSetting(Double latitude, Double longitude){
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName(); //

        StringBuilder addressLocation2=new StringBuilder();
        if( addresses.get(0).getSubAdminArea()!=null){
            addressLocation2.append(addresses.get(0).getSubAdminArea()+",");
        }
        if(addresses.get(0).getAdminArea()!=null){
            addressLocation2.append(addresses.get(0).getAdminArea()+",");
        }
        if(addresses.get(0).getCountryName()!=null){
            addressLocation2.append(addresses.get(0).getCountryName());
        }

        return addressLocation2.toString();
    }

    void getSettingOfUser(){
        databaseReference.child("setting").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("ageMaxMatch") != null) {
                        maxDistanceMatch= Integer.valueOf(map.get("ageMaxMatch").toString());
                    }

                    if (map.get("ageMinMatch") != null) {
                        minDistanceMatch= Integer.valueOf(map.get("ageMinMatch").toString());
                    }

                    if (map.get("distanceMatch") != null) {
                        km=Integer.valueOf(map.get("distanceMatch").toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSettingOfUser();
        listFinders.clear();
        getDeviceLocationSetting();
        userAdapter.notifyDataSetChanged();
    }
}