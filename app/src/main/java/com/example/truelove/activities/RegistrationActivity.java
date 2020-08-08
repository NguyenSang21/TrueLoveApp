package com.example.truelove.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.truelove.R;
import com.example.truelove.custom_class.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.compat.GeoDataClient;
import com.google.android.libraries.places.compat.PlaceDetectionClient;
import com.google.android.libraries.places.compat.Places;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtName, edtEmail, edtPassword, edtAge, edtAddress;
    private RadioGroup mRadioGroupSex;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;
    private boolean locationPermissionGranted;
    static public final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private Double latitude;
    private Double longitude;
    private String addressLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        personalUI();
        mapping();
        btnRegister.setOnClickListener(this);
        registration();


        //==========================================================================================

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // set permission
        getLocationPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }

    private void registration() {
        mAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };
    }

    private void personalUI() {
        getSupportActionBar().hide();
        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.setStatusBarColor(Color.parseColor("#FB6667"));
        }
    }

    private void mapping() {
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        mRadioGroupSex = findViewById(R.id.radioGroupSex);
        edtAge = findViewById(R.id.edtAge);
        //edtAddress = findViewById(R.id.edtAddress);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        Toast.makeText(this, "ON CLICK !!!", Toast.LENGTH_SHORT).show();
        switch (viewId) {
            case R.id.btnRegister:
                // get device location
                getDeviceLocation();

                final String email = edtEmail.getText().toString().trim();
                final String password = edtPassword.getText().toString().trim();
                final String name = edtName.getText().toString().trim();
                final int selectedId = mRadioGroupSex.getCheckedRadioButtonId();
                final int age = Integer.parseInt(edtAge.getText().toString());
              /*  final String address = edtAddress.getText().toString().trim();*/

                final RadioButton radioButton = findViewById(selectedId);
                String sex = "";

                if (radioButton.getText() == null) {
                    return;
                } else if (radioButton.getText().equals("Nam")) {
                    sex = "male";
                } else if (radioButton.getText().equals("Nữ")) {
                    sex = "female";
                }
                final String finalSex = sex;
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    String userId = mAuth.getCurrentUser().getUid();
                                    DatabaseReference currentReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
                                    User user = new User();
                                    user.setUid(userId);
                                    user.setName(name);
                                    user.setAge(age);
                                    user.setImg("default");
                                    user.setPhone("default");
                                    user.setSex(finalSex);
                                    user.setEmail(email);
                                    if(latitude!=null&&longitude!=null){
                                        user.setLatitude(latitude);
                                        user.setLongitude(longitude);
                                        user.setAddress(addressLocation);
//                                        user.setAddress("Quận 5, HCMC");
                                    }else{
                                        // set default in my school
                                        user.setLatitude( 10.762918);
                                        user.setLongitude(106.682284);
                                        user.setAddress("Quận 5, HCMC");
                                    }

                                    System.out.println(user.toString());
                                    currentReference.setValue(user);
                                    Toast.makeText(RegistrationActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(RegistrationActivity.this, "Đăng ký thất bại!", Toast.LENGTH_SHORT).show();
                                }
                                // ...
                            }
                        });
                break;
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

                                double lat = lastKnownLocation.getLatitude();
                                double longi = lastKnownLocation.getLongitude();
                                Toast.makeText(RegistrationActivity.this, "LATITUDE =" + lat + "LONGI =" + longi , Toast.LENGTH_SHORT).show();
                                latitude=lat;
                                longitude=longi;
                                Toast.makeText(RegistrationActivity.this, "LATITUDE =" + lat + "LONGI =" + longi , Toast.LENGTH_SHORT).show();

                                Geocoder geocoder;
                                List<Address> addresses = null;
                                geocoder = new Geocoder(RegistrationActivity.this, Locale.getDefault());

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

                                addressLocation= addressLocation2.toString();

                                /*
/*                                Location truongHoc= new Location("khtn");
                                truongHoc.setLatitude( 10.762918);
                                truongHoc.setLongitude(106.682284);

                                Location etown= new Location("etown");
                                etown.setLatitude( 10.802031);
                                etown.setLongitude(106.641379);
                                float kq=  etown.distanceTo(truongHoc);

                                Toast.makeText(RegistrationActivity.this, "etown to school: "+kq , Toast.LENGTH_SHORT).show();*/
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

}