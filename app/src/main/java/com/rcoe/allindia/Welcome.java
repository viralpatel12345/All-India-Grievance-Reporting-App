package com.rcoe.allindia;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class Welcome extends BaseActivity {

    public static final int REQUEST_LOCATION = 1;
    private ProgressDialog progressDialog = null;
    private Spinner s1, s2;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private String latitude=null, longitude=null, imageRef=null, area=null, postal_code=null, userId, complaintId;
    private Uri imageUri = null;
    private Geocoder geocoder;
    private ImageView imageView;
    private TextView complaintDescription, latitude_textView, longitude_textView, area_TextView;
    private Button getlocation, submit, copyButton;
    private LocationManager locationManager;

    public List<String> getList(String... str) {
        List<String> list = new ArrayList<String>();
        for (String s : str) {
            list.add(s);
        }
        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        getSupportActionBar().setTitle("NEW COMPLAINT");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("user");
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        }

        progressDialog = new ProgressDialog(Welcome.this);
        progressDialog.setMessage("Submitting your complaint");
        progressDialog.setCancelable(false);

        s1 = findViewById(R.id.new_complaint_spinner);
        s2 = findViewById(R.id.sub_complaint_spinner);
        complaintDescription = findViewById(R.id.description);
        getlocation = findViewById(R.id.getLocationButton);
        submit = findViewById(R.id.submit_btn);
        imageView = findViewById(R.id.imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crop();
            }
        });
        getlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    OnGPS();
                } else {
                    getLocation();
                }
            }
        });

        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this,
                R.array.complaint_type,
                R.layout.custom_spinner);

        s1.setAdapter(adapter);

        s1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String sp1 = String.valueOf(s1.getSelectedItem());
                if (sp1.contentEquals("-")) {
                    createSpinner2(getList(""), s2);
                    s2.setEnabled(false);
                }
                if (sp1.contentEquals("Roads and traffic")) {
                    s2.setEnabled(true);
                    List<String> list = getList("-", "Potholes", "Street Light Problem", "Rough Road Surface",
                            "Trench for pipeline / drainage line", "Crosscut for drainage line / electric cable",
                            "Raised drainage chember", "Lowered drainage chember",
                            "Bad Patches", "Bad Patches around drainage chember");

                    createSpinner2(list, s2);

                }
                if (sp1.contentEquals("Solid Waste Management")) {
                    s2.setEnabled(true);
                    List<String> list = getList("-", "Garbage not lifted from co-authorised collection point",
                            "Garbage not lifted from house gully", "Garbage not lifted from municipal market",
                            "Garbage lorry not covered", "Garbage lorry not reported for service",
                            "Collection point not attended properly", "Providing/removing/replacing dustbins",
                            "Removal of dead animals", "Sweeping of road", "Cleaning of P.S.C. block/channels",
                            "No attendance at public toilets", "Removal of Debris", "Silt to be lifted on road",
                            "Non attendant of Nuisance Detector");

                    createSpinner2(list, s2);

                }
                if (sp1.contentEquals("Buildings")) {
                    s2.setEnabled(true);
                    List<String> list = getList("-", "Unauthorised construction/development",
                            "Unauthorised alteration/renovation of Building, Flat, Tenement,etc.",
                            "Permission for temporary monsoon shed", "Regularisation of Balcony enclosures",
                            "Repairs permission for tolerated/censured unauthorised structures",
                            "Generating authority u/s 499 MMC Act for carrying out essential repairs",
                            "Heavy leakage from ceiling", "Change of user - Res to Commercial");

                    createSpinner2(list, s2);

                }
                if (sp1.contentEquals("Factories")) {
                    s2.setEnabled(true);
                    List<String> list = getList("-", "Nuisance due to masala mills", "Air pollution",
                            "Pollution due to chemical effluents", "Unauthorised factory, workshop or garage");

                    createSpinner2(list, s2);
                }
                if (sp1.contentEquals("Drainage")) {
                    s2.setEnabled(true);
                    List<String> list = getList("-", "Drainage chokes and blockages",
                            "Overflowing drains or manholes", "Odour (foul smell) from drains",
                            "Replacement of missing/damaged manholes/inspection",
                            "Raising of manhole (except in monsoon)", "Cleaning of septic tank",
                            "Repairs to pipe sewers");

                    createSpinner2(list, s2);
                }
                if (sp1.contentEquals("Health")) {
                    s2.setEnabled(true);
                    List<String> list = getList("-", "Issue of Birth / Death Certificate",
                            "Unauthorised Food Selling/Preparation", "Unauthorised Flour Mill");

                    createSpinner2(list, s2);
                }
                if (sp1.contentEquals("Storm water drain")) {
                    s2.setEnabled(true);
                    List<String> list = getList("-", "Flooding during monsoon",
                            "Pre-monsoon work halted", "Providing damaged/missing grating manhole covers over SWD's",
                            "Removal of silt from nalla/across culverts", "Repair of damaged open SWD", "Cleaning of water entrance", "Cleaning of open SWD",
                            "Unauthorised Stalls and Huts on nallas");

                    createSpinner2(list, s2);
                }
                if (sp1.contentEquals("Licence")) {
                    s2.setEnabled(true);
                    List<String> list = getList("-", "Unauthorised stalls on roads, footpath or SWD drain",
                            "Unauthorised banners/advertisement/boards on roads/footpath", "Unauthorised storage of explosives", "Trade without Licence", "Unauthorised workshop or Garage", "Storage and sale of plastic bags");

                    createSpinner2(list, s2);
                }
                if (sp1.contentEquals("Water Supply")) {
                    s2.setEnabled(true);
                    List<String> list = getList("-", "Leaks in water lines",
                            "Burst water main", "Contaminated water supply", "Shortage of water supply", "Unauthorised tapping of water connections",
                            "Unauthorised use of water-Change of User", "Use of booster pump", "Removal of water meters", "Providing water by tankers",
                            "Water supply during non-supply hours", "Leakage near meter", "Overflow of overhead tank/ suction tank", "Non receipt of water bill");

                    createSpinner2(list, s2);
                }
                if (sp1.contentEquals("Colony officer")) {
                    s2.setEnabled(true);
                    List<String> list = getList("-", "Unauthorised construction in slum",
                            "Unauthorised repairs/renovation in slum", "Delay in transfer case", "Unauthorised extension/construction", "Unauthorised commercial activity");

                    createSpinner2(list, s2);
                }
                if (sp1.contentEquals("Pest Control")) {
                    s2.setEnabled(true);
                    List<String> list = getList("-", "Mosquito nuisance",
                            "Rat nuisance", "Fogging", "Nuisance due to white ants", "Nuisance due to cockroaches", "Unauthorised/uncovered water storage tanks");

                    createSpinner2(list, s2);
                }
                if (sp1.contentEquals("Repairs to Municipal Property")) {
                    s2.setEnabled(true);
                    List<String> list = getList("-", "Maintenance of Municipal property, School, Dispensaries, Maternity home, Gardens",
                            "Maintenance of electric pumps in municipal colonies", "Protection of municipal play grounds/gardens",
                            "Providing for monsoon to municipal property to avoid leakage", "Providing/repairing doors,windows of P.S. Blocks",
                            "Proper electric supply to municipal property", "Major repairs to municipal property", "Minor repairs to municipal property");

                    createSpinner2(list, s2);
                }
                if (sp1.contentEquals("Garden and Tree")) {
                    s2.setEnabled(true);
                    List<String> list = getList("-", "Permission for Tree Trimming",
                            "Roadside Tree Trimming", "Tree Trimming in Public Premises", "Fallen Tree in Public Premises", "Fallen Tree in PVT Premises",
                            "Garden Cleaning", "Garden Lights", "Lifting Tree Cutting/Cut Wood", "Garden Repair/Maintenance", "Dead/Dangerous Tree");

                    createSpinner2(list, s2);
                }
                if (sp1.contentEquals("Encroachment")) {
                    s2.setEnabled(true);
                    List<String> list = getList("-", "Municipal Land - Road/Footpath/Storm Water Drain",
                            "Municipal Plot", "Municipal Colony/Slum", "Hawkers", "Private Land/Building/Society/Factories");

                    createSpinner2(list, s2);
                }
                if (sp1.contentEquals("Shops and Establishment")) {
                    s2.setEnabled(true);
                    List<String> list = getList("-", "Employing Children",
                            "Online Renewal Application", "Open beyond permissible hours", "Running without licence", "Non observance of Holidays",
                            "Not providing minimum wages", "Shop open on weekly holiday", "Found staff working more than on muster");

                    createSpinner2(list, s2);
                }
                if (sp1.contentEquals("Estate")) {
                    s2.setEnabled(true);
                    List<String> list = getList("-", "Pertaining to rent",
                            "Transfer of tenancy", "Unauthorised construction on the Plot/Room", "Unauthorised use of Room", "Non-maintenance of Premises",
                            "Pending Transfer cases", "Unauthorised addition/alteration in the premises", "Unauthorised materials/furniture found",
                            "Extension in the premises without permission", "Slab fallen down", "Unauthorised shed on building in premises");

                    createSpinner2(list, s2);
                }
                if (sp1.contentEquals("School or College")) {
                    s2.setEnabled(true);
                    List<String> list = getList("-", "Short supply of water",
                            "Choke in main drain", "Drinking water not available", "Toilet not cleaned", "Door/Window/Staircase etc found broken",
                            "No electricity supply", "Lamp, Tubelight to be replaced", "Furniture found broken in the classes", "Found encroachment in the school premise",
                            "No Teacher", "No warning/alarm system", "Overflow of waste material", "Pan/Gutka hawker near school premise", "Bad Quality of material given to students");

                    createSpinner2(list, s2);
                }
                if (sp1.contentEquals("Sewerage Operation Control")) {
                    s2.setEnabled(true);
                    List<String> list = getList("-", "Drainage chokes and blockages",
                            "Overflow drains or manholes", "Replacement of missing/damaged manholes covers", "Sunken manholes", "Cleaning of septic tank",
                            "Repair to pipe sewers/main sewers", "Odour from dr");

                    createSpinner2(list, s2);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (s1.getSelectedItem().toString().equals("-") || s2.getSelectedItem().toString().equals("-") ||
                        latitude == null || longitude == null || area_TextView.getText().toString().isEmpty() ||
                        complaintDescription.getText().toString().isEmpty() ||
                        imageUri == null) {
                    Toast.makeText(Welcome.this, "Please fill all the Details Correctly", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (!isConnected(Welcome.this))
                    {
                        Toast.makeText(Welcome.this, "Please check your Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        submit_complaint();
                    }

                }
            }
        });
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(Welcome.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Welcome.this,
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
        LayoutInflater inflater = LayoutInflater.from(Welcome.this);
        View view = inflater.inflate(R.layout.gps_enable_alert, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(Welcome.this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            case R.id.home:
                startActivity(new Intent(Welcome.this, Home.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.refresh :
                startActivity(new Intent(Welcome.this,Welcome.class));
                finish();
                break;

            case R.id.new_complaint_registration:
                startActivity(new Intent(Welcome.this, Welcome.class));
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                finish();
                break;

            case R.id.search_complaint :
                startActivity(new Intent(Welcome.this, ComplaintSearch.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.profile:
                startActivity(new Intent(Welcome.this, Profile.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.complaint_status:
                startActivity(new Intent(Welcome.this, ComplaintStatus.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.logout:
                super.alert(R.layout.logout_alert,Welcome.this,firebaseAuth);
                break;

            case R.id.share:
                share();
                break;

            case R.id.change_password:
                startActivity(new Intent(Welcome.this, ChangePassword.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.chats :
                startActivity(new Intent(Welcome.this, ChatActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.find_friends :
                startActivity(new Intent(Welcome.this, FindFriends.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.received_requests :
                startActivity(new Intent(Welcome.this, Requests.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            default:
                return super.onOptionsItemSelected(item);

        }

        return true;
    }

    public void createSpinner2(List<String> list, Spinner s2) {
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(Welcome.this,
                R.layout.custom_spinner, list);
        dataAdapter.setDropDownViewResource(R.layout.custom_spinner);
        dataAdapter.notifyDataSetChanged();
        s2.setAdapter(dataAdapter);

    }

    private void showAlertMessage() {
        LayoutInflater inflater = LayoutInflater.from(Welcome.this);
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
            postal_code  = addressList.get(0).getPostalCode();
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

        final AlertDialog.Builder builder = new AlertDialog.Builder(Welcome.this);
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
                getlocation.setText("LOCATION CAPTURED SUCCESSFULLY");
                getlocation.setFocusable(true);
                getlocation.setEnabled(false);
                dialog.dismiss();
            }
        });

        final AlertDialog ad = builder.create();
        ad.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                try {
                    imageUri = result.getUri();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    imageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    Toast.makeText(Welcome.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(Welcome.this, result.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void crop() {
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(Welcome.this);
    }

    public String get_complaintId() {
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 32; i++) {
            String rand_char = String.valueOf(rand.nextInt(10));
            sb.append(rand_char);
        }
        return sb.toString();
    }

    public String getCurrentDateAndTime() {
        DateFormat dateFormat = new SimpleDateFormat("E dd-MM-yyyy hh:mm:ss aa");
        Date date = new Date();
        return dateFormat.format(date).toUpperCase();
    }

    public void storeUserDataInDatabase() {
        String area1 = area_TextView.getText().toString();
        String[] splitted;
        splitted = area1.split(",");
        String im1 = splitted[splitted.length - 2];
        String im2 = im1.trim();
        String state = im2.substring(0, im2.length() - 6).trim();

        Details details = new Details(s1.getSelectedItem().toString(), s2.getSelectedItem().toString(),
                area,postal_code, latitude, longitude, complaintDescription.getText().toString(), "Complaint Not Solved", state, getCurrentDateAndTime());

        databaseReference.child(userId).child("complaints").child(complaintId).setValue(details)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            databaseReference.child(userId).child("complaints").child(complaintId).child("url").setValue(imageRef)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                progressDialog.dismiss();
                                                success_alert();
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    public void submit_complaint() {
        if (area != null && postal_code!=null)
        {
            progressDialog.show();
            firebaseStorage = FirebaseStorage.getInstance();
            storageReference = firebaseStorage.getReference();
            complaintId = get_complaintId();
            final StorageReference childImageRef = storageReference.child(userId).child("complaints").child(complaintId).child("complaint");
            final UploadTask uploadUserProfile = childImageRef.putFile(imageUri);

            uploadUserProfile.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> downloadUrl = childImageRef.getDownloadUrl();
                    downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageRef = uri.toString();
                            if(imageRef!=null)
                            {
                                storeUserDataInDatabase();
                            }
                            else
                            {
                                Toast.makeText(Welcome.this, "Failed to submit your complaint", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Welcome.this, "Image Upload Failed !", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });


        }
        else
        {
            Toast.makeText(Welcome.this, "Area not found ! Please try again", Toast.LENGTH_LONG).show();
        }

    }

    void success_alert()
    {
        progressDialog.dismiss();
        LayoutInflater inflater = LayoutInflater.from(Welcome.this);
        View view = inflater.inflate(R.layout.profile_photo_updated_alert, null);
        TextView tv   = view.findViewById(R.id.message);
        final Button copyId = view.findViewById(R.id.copy_btn);
        copyId.setVisibility(View.VISIBLE);
        copyId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(Welcome.this,complaintId);
            }
        });

        tv.setText("Complaint Successfully Submitted with complaintId : " + complaintId);
        final AlertDialog.Builder builder = new AlertDialog.Builder(Welcome.this);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                copyId.setVisibility(View.GONE);
                startActivity(new Intent(Welcome.this, Home.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

        AlertDialog ad = builder.create();
        ad.show();
    }

    @Override
    public void onBackPressed()
    {
        startActivity(new Intent(Welcome.this, Home.class));
        finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}