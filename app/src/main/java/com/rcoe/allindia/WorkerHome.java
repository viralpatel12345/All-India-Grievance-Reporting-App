package com.rcoe.allindia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.List;
import java.util.Locale;

public class WorkerHome extends BaseActivity
{
    public static final int REQUEST_LOCATION = 1;
    private ProgressDialog progressDialog = null;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private DatabaseReference workersReference;
    private DatabaseReference userRef;
    private String currentUserID,latitude = null, longitude = null, area = null, postalCode = null;
    private Geocoder geocoder;
    private int total=0,solved=0,unsolved=0;
    private TextView latitude_textView, longitude_textView, area_TextView, postalcode_home, location_textview,
                     tc,tsc,tusc,tc_text,tsc_text,tusc_text,final_tc,final_tsc,final_tusc,refresh_btn;
    private Button getlocation, copyButton;
    private LocationManager locationManager;
    private String postal_code=null;
    private CircularProgressBar cpb1,cpb2,cpb3;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_home);
        getSupportActionBar().setTitle("HOME");

        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        firebaseAuth      = FirebaseAuth.getInstance();
        currentUser       = firebaseAuth.getCurrentUser();
        currentUserID     = currentUser.getUid();
        workersReference  = FirebaseDatabase.getInstance().getReference();
        userRef           = FirebaseDatabase.getInstance().getReference().child("user");
        postalcode_home   = findViewById(R.id.current_location);
        location_textview = findViewById(R.id.location_tv);
        getlocation       = findViewById(R.id.getLocationButton);
        tc                = findViewById(R.id.total_complaints);
        tsc               = findViewById(R.id.total_solved_complaints);
        tusc              = findViewById(R.id.total_unsolved_complaints);
        tc_text           = findViewById(R.id.tc_text);
        tsc_text          = findViewById(R.id.tsc_text);
        tusc_text         = findViewById(R.id.tusc_text);
        final_tc          = findViewById(R.id.final_tc_tv);
        final_tsc         = findViewById(R.id.final_tsc_tv);
        final_tusc        = findViewById(R.id.final_tusc_tv);
        cpb1              = findViewById(R.id.circularProgressBar2);
        cpb2              = findViewById(R.id.circularProgressBar3);
        cpb3              = findViewById(R.id.circularProgressBar4);
        refresh_btn       = findViewById(R.id.refresh_button);
        progressDialog    = new ProgressDialog(WorkerHome.this);
        progressDialog.setCancelable(false);

        if(getIntent().hasExtra("device_token"))
        {
            saveToken();
        }

        fetchLocationFromDatabase();

        getlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    OnGPS();
                } else {
                    progressDialog.setMessage("Fetching current location");
                    progressDialog.show();
                    getLocation();
                }
            }
        });

        refresh_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchData();
            }
        });
    }

    private void saveToken()
    {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if(task.isSuccessful())
                        {
                            workersReference.child("Workers").child(currentUserID).child("token")
                                    .setValue(task.getResult().getToken())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    });
                        }
                    }
                });
    }


    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(WorkerHome.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(WorkerHome.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        }
        else
        {

            Location locationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location locationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location locationPassive = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (locationGps != null) {
                double lat = locationGps.getLatitude();
                double log = locationGps.getLongitude();

                latitude = String.valueOf(lat);
                longitude = String.valueOf(log);

                showAlertMessage();

            } else if (locationNetwork != null) {
                double lat = locationNetwork.getLatitude();
                double log = locationNetwork.getLongitude();

                latitude = String.valueOf(lat);
                longitude = String.valueOf(log);

                showAlertMessage();

            } else if (locationPassive != null) {
                double lat = locationPassive.getLatitude();
                double log = locationPassive.getLongitude();

                latitude = String.valueOf(lat);
                longitude = String.valueOf(log);

                showAlertMessage();

            } else {
                Toast.makeText(this, "Can't get your location", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void GPSAlertDialog() {
        LayoutInflater inflater = LayoutInflater.from(WorkerHome.this);
        View view = inflater.inflate(R.layout.gps_enable_alert, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(WorkerHome.this);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

            }
        });

        final AlertDialog ad = builder.create();
        ad.show();
    }

    private void OnGPS() {
        GPSAlertDialog();
    }

    private void showAlertMessage()
    {
        progressDialog.dismiss();
        LayoutInflater inflater = LayoutInflater.from(WorkerHome.this);
        View view = inflater.inflate(R.layout.location_captured_success, null);

        latitude_textView = view.findViewById(R.id.latitude_textView);
        longitude_textView = view.findViewById(R.id.longitude_textView);
        area_TextView = view.findViewById(R.id.area_textview);
        copyButton = view.findViewById(R.id.copy_btn);
        latitude_textView.setText("Latitude - " + latitude);
        longitude_textView.setText("Longitude - " + longitude);

        try {
            geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addressList;
            addressList = geocoder.getFromLocation(Double.parseDouble(latitude), Double.parseDouble(longitude), 1);
            area        = addressList.get(0).getAddressLine(0);
            postalCode  = addressList.get(0).getPostalCode();
            area_TextView.setText("Area - " + area);
        } catch (Exception e) {

        }

        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/place/" + latitude + "," + longitude));
                startActivity(intent);
            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(WorkerHome.this);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.setMessage("Saving");
                progressDialog.show();
                getlocation.setText("Update current location");
                getlocation.setFocusable(true);
                workersReference.child("Workers").child(currentUserID).child("postalcode").setValue(postalCode)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                              if(task.isSuccessful())
                              {
                                  progressDialog.dismiss();
                                  fetchLocationFromDatabase();
                              }
                            }
                        });

            }
        });

        final AlertDialog ad = builder.create();
        ad.show();
    }

    private void fetchLocationFromDatabase()
    {
        workersReference.child("Workers").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child("postalcode").exists())
                {
                    postal_code = dataSnapshot.child("postalcode").getValue().toString();
                    location_textview.setVisibility(View.VISIBLE);
                    getlocation.setVisibility(View.VISIBLE);
                    location_textview.setText("CURRENT LOCATION");
                    getlocation.setText("Update current location");
                    postalcode_home.setVisibility(View.VISIBLE);
                    postalcode_home.setText(postal_code);

                    fetchData();

                }
                else
                {
                    getlocation.setVisibility(View.VISIBLE);
                    location_textview.setVisibility(View.VISIBLE);
                    location_textview.setText("LOCATION NOT FOUND");
                    getlocation.setText("Fetch current location");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void fetchData()
    {
        total=0;
        solved=0;
        unsolved=0;
        refresh_btn.setVisibility(View.VISIBLE);
        final_tc.setVisibility(View.VISIBLE);
        final_tsc.setVisibility(View.VISIBLE);
        final_tusc.setVisibility(View.VISIBLE);
        tc_text.setVisibility(View.VISIBLE);
        tsc_text.setVisibility(View.VISIBLE);
        tusc_text.setVisibility(View.VISIBLE);
        tc.setVisibility(View.VISIBLE);
        tsc.setVisibility(View.VISIBLE);
        tusc.setVisibility(View.VISIBLE);
        cpb1.setVisibility(View.VISIBLE);
        cpb2.setVisibility(View.VISIBLE);
        cpb3.setVisibility(View.VISIBLE);

        postal_code = postalcode_home.getText().toString();

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    if(dataSnapshot1.child("complaints").exists())
                    {
                        for (DataSnapshot dataSnapshot2 : dataSnapshot1.child("complaints").getChildren()) {
                            if (dataSnapshot2.child("postalcode").getValue().toString().equals(postal_code))
                            {
                                if (dataSnapshot2.child("status").getValue().toString().equals("Complaint Solved"))
                                {
                                    solved += 1;
                                    tsc.setText(String.valueOf(solved));
                                }
                                else if (dataSnapshot2.child("status").getValue().toString().equals("Complaint Not Solved") ||
                                        dataSnapshot2.child("status").getValue().toString().equals("Complaint Partially Solved"))
                                {
                                    unsolved += 1;
                                    tusc.setText(String.valueOf(unsolved));
                                }

                                total += 1;
                                tc.setText(String.valueOf(total));
                            }

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(total==0)
        {
            tc.setText("0");
        }
        if(solved==0)
        {
            tsc.setText("0");
        }
        if(unsolved==0)
        {
            tusc.setText("0");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id      = item.getItemId();
        postal_code = postalcode_home.getText().toString();

        switch (id) {

            case R.id.refresh:
                startActivity(new Intent(WorkerHome.this,WorkerHome.class));
                finish();
                break;

            case R.id.share:
                share();
                break;

            case R.id.home:
                startActivity(new Intent(WorkerHome.this,WorkerHome.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.logout:
                super.alert(R.layout.logout_alert,WorkerHome.this,firebaseAuth);
                break;

            case R.id.solved_complaints :
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerHome.this,WorkerUnsolvedComplaints.class);
                    intent.putExtra("postal_code",postal_code);
                    intent.putExtra("status","solved");
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    break;
                }

            case R.id.unsolved_complaints :
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerHome.this,WorkerUnsolvedComplaints.class);
                    intent.putExtra("postal_code",postal_code);
                    intent.putExtra("status","unsolved");
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    break;
                }

            case R.id.search_complaint :
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerHome.this,WorkerComplaintSearch.class);
                    intent.putExtra("postal_code",postal_code);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    break;
                }

            case R.id.profile :
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerHome.this,WorkerProfile.class);
                    intent.putExtra("postal_code",postal_code);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    break;
                }

            case R.id.change_password :
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerHome.this,WorkerChangePassword.class);
                    intent.putExtra("postal_code",postal_code);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    break;
                }


            default:
                return super.onOptionsItemSelected(item);

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.alert(R.layout.logout_alert,WorkerHome.this,firebaseAuth);
    }
}