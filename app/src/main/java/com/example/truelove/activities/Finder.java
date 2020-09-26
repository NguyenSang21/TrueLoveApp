package com.example.truelove.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.truelove.R;
import com.example.truelove.adapter.FindersAdapter;
import com.example.truelove.custom_class.FinderDistance;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.example.truelove.custom_class.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
    private AVLoadingIndicatorView avi;

    private Button btnFinder;

    private String currentUserID;
    private NestedScrollView idNestedScrollView;

    private boolean isFinderMatch=false;
//    private Toolbar toolbar;

    private SeekBar seekBarKM;
    private TextView txtKM;
    private int km;
    private boolean isRunningSearch=false;
    private TextView statusSearch;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finder);

        personalUI();

        avi = findViewById(R.id.avi);
        idNestedScrollView = findViewById(R.id.idNestedScrollView);
        statusSearch=findViewById(R.id.statusSearch);
        statusSearch.setVisibility(View.GONE);
        btnBack=findViewById(R.id.btnBack);
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // set permission
        getLocationPermission();

//        toolbar = findViewById(R.id.toolbarFinder);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // back button pressed
//                finish();
//            }
//        });

        // list user find appear here
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewFinder);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(Finder.this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new FindersAdapter(getDatasetFinders(), Finder.this);

        //set button find user
        btnFinder= findViewById(R.id.btnFinder);
        btnFinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isRunningSearch){
                    statusSearch.setVisibility(View.VISIBLE);
                    listFinders.clear();
                    mAdapter.notifyDataSetChanged();
                    handleSearch();
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

        // get information user current
        /*currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserCurrent();*/

        // match KM
        seekBarKM = (SeekBar) findViewById(R.id.seekBarKM);
        txtKM = (TextView) findViewById(R.id.txtKM);
        seekBarKM.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (progress <= 2) {
                    progressValue = 2;
                } else {
                    progressValue = progress;
                }
                txtKM.setText(String.valueOf(progressValue) + " km");
                km = progressValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    /*private void getUserCurrent(){
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
    }*/


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(Activity.RESULT_OK==resultCode){
            try{
                String matchIdReturn= data.getExtras().getString("matchIdReturn");
                Iterator in= listFinders.iterator();
                while (in.hasNext()){
                    FinderDistance obj =(FinderDistance) in.next();
                    if(obj.getUser().getUid().equals(matchIdReturn)){
                        listFinders.remove(obj);
                        mAdapter.notifyDataSetChanged();
                        return;
                    }
                }
            }catch(Exception e){
                System.out.print(e);
            }
        }
    }

    private void personalUI() {
        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            // w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            // w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // w.setStatusBarColor(Color.parseColor("#FB6667"));
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
                                    // run tinh khoang cach all user
                                    getAllUser();
                            }
                        } else {
                            Toast.makeText(Finder.this, "Turn on GPS on your phone !!! " , Toast.LENGTH_SHORT).show();
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

                // get address of you
                String addressOfYou = locationToAddress(in.getLatitude(),in.getLongitude());
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
                if(kmOfUser >= 2 && kmOfUser <= km){
                    statusSearch.setVisibility(View.GONE);
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
            recyclerView.setAdapter(mAdapter);
        }
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
                    stopAnim();
                    idNestedScrollView.setBackground(null);
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
                    obj.setUid(dataSnapshot.getKey());

                    if(dataSnapshot.child("name").equals("Titus")){
                        System.out.print("");
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
                        /*// user dang search chua get hoac chua match ai het
                        if(dataSnapshot.child("connections")==null){
                            getFindersWithUserCurrent(obj);
                        }else{
                            // user đang search không thich và không match vs user current
                            if(!dataSnapshot.child("connections").child("nope").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid()) &&
                                    !dataSnapshot.child("connections").child("yeps").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                getFindersWithUserCurrent(obj);
                            }
                        }*/
                        // user dang search da match
                        if(dataSnapshot.child("connections")==null){
                            //getFindersWithUserCurrent(obj);
                            statusSearch.setText("Chưa match user nào !");
                        }else{
                            // user đang search đã match vs user current
                            if(dataSnapshot.child("connections").child("matches").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                getFindersWithUserCurrent(obj);
                            }
                        }
                    }


                    mAdapter.notifyDataSetChanged();
                    // for update location for all user to testing
                   /* try {
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
//        String email = dataSnapshot.child("email").getValue().toString().trim();
//        String phone = dataSnapshot.child("phone").getValue().toString().trim();
        String caption = dataSnapshot.child("caption").getValue().toString().trim();
        String image = dataSnapshot.child("img").getValue().toString().trim();
        int age = Integer.parseInt(dataSnapshot.child("age").getValue().toString().trim());

        final Map userInfo = new HashMap();

        userInfo.put("name", name);
//        userInfo.put("email", email);
//        userInfo.put("phone", phone);

        userInfo.put("caption", caption);
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

            Geocoder geocoder;
            List<Address> addresses = null;
            geocoder = new Geocoder(Finder.this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(lat, longi, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            } catch (IOException e) {
                e.printStackTrace();
            }

            String addresss = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
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

            userInfo.put("latitude", lat);
            userInfo.put("longitude", longi);

            userInfo.put("address", addressLocation2.toString());
        }else{
            userInfo.put("latitude", dataSnapshot.child("latitude").getValue().toString().trim());
            userInfo.put("longitude", dataSnapshot.child("longitude").getValue().toString().trim());

            // update address
            Geocoder geocoder;
            List<Address> addresses = null;
            geocoder = new Geocoder(Finder.this, Locale.getDefault());

            try {
                Double a= Double.valueOf(dataSnapshot.child("latitude").getValue().toString());
                Double b= Double.valueOf(dataSnapshot.child("longitude").getValue().toString());
                addresses = geocoder.getFromLocation(a, b, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            } catch (IOException e) {
                e.printStackTrace();
            }

           String addresss = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
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

            userInfo.put("address", addressLocation2.toString());
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

    private String locationToAddress(Double latitude, Double longitude){
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(Finder.this, Locale.getDefault());

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

    void startAnim(){
        isRunningSearch=true;
        avi.show();
        avi.smoothToShow();
        // or avi.smoothToShow();
    }

    void stopAnim(){
        isRunningSearch=false;
        avi.hide();
        btnFinder.setEnabled(true);
        // or avi.smoothToHide();
    }


    void handleSearch() {
        startAnim();
        btnFinder.setEnabled(false);
       /* try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        idNestedScrollView.setBackgroundResource(R.drawable.bg_finder);
        // get device location
        getDeviceLocation();
    }
}