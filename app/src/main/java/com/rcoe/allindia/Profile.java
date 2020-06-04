package com.rcoe.allindia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import de.hdodenhof.circleimageview.CircleImageView;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Profile extends BaseActivity {

    private FirebaseAuth firebaseAuth;
    private String userId="";
    private EditText fname,lname,city,pincode,email,dob,mobile;
    private Spinner spinner;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private ProgressBar progressBar;
    private CircularProgressBar cpb;
    private DatabaseReference reference;
    private List<String> states;
    private ProgressDialog progressDialog;
    private Button editButton;
    private ConstraintLayout layout;
    private Uri imageUri = null;
    final Calendar myCalendar = Calendar.getInstance();

    private CircleImageView iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setTitle("");


        spinner     = findViewById(R.id.spinner);
        fname       = findViewById(R.id.fname);
        lname       = findViewById(R.id.lname);
        city        = findViewById(R.id.city);
        email       = findViewById(R.id.emailId);
        mobile      = findViewById(R.id.mobile_no);
        pincode     = findViewById(R.id.pinCode);
        dob         = findViewById(R.id.dob);
        iv          = findViewById(R.id.profile_photo);
        progressBar = findViewById(R.id.progressBar);
        cpb         = findViewById(R.id.cpb);
        editButton  = findViewById(R.id.editButton);
        layout      = findViewById(R.id.lay);

        progressDialog = new ProgressDialog(Profile.this);
        progressDialog.setMessage("Updating Profile Photo");
        progressDialog.setCancelable(false);

        String arr[] = {"Andhra Pradesh","Arunachal Pradesh ","Assam","Bihar",
                "Chhattisgarh","Goa","Gujarat","Haryana","Himachal Pradesh","Jammu and Kashmir",
                "Jharkhand","Karnataka","Kerala","Madhya Pradesh","Maharashtra","Manipur","Meghalaya",
                "Mizoram","Nagaland","Odisha","Punjab","Rajasthan","Sikkim","Tamil Nadu",
                "Telangana","Tripura","Uttar Pradesh","Uttarakhand","West Bengal",
                "Andaman and Nicobar Islands","Chandigarh","Dadra and Nagar Haveli","Daman and Diu",
                "Lakshadweep","National Capital Territory of Delhi","Puducherry","Ladakh"};

        states = new ArrayList<String>(Arrays.asList(arr));
        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, states);
        dataAdapter.setDropDownViewResource(R.layout.custom_spinner);
        spinner.setAdapter(dataAdapter);

        firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        userId = user.getUid();

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth)
            {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                new DatePickerDialog(Profile.this, date , myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        email.setEnabled(false);
        mobile.setEnabled(false);
        email.setTextColor(Color.rgb(108,111,112));
        mobile.setTextColor(Color.rgb(108,111,112));
        loadProfile();
        getDataFromDatabase();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               spinner.setSelection(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnected(Profile.this)!=true)
                {
                    Toast.makeText(Profile.this,"Please check your Internet Connection",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    alertDialougeBox();
                }

            }
        });

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crop();
            }
        });
    }

    void getDataFromDatabase()
    {
        reference = FirebaseDatabase.getInstance().getReference().child("user").child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String f_name    = dataSnapshot.child("fname").getValue().toString();
                String l_name    = dataSnapshot.child("lname").getValue().toString();
                String s_pinnner = dataSnapshot.child("state").getValue().toString();
                String m_obile   = dataSnapshot.child("mobile").getValue().toString();
                String e_mail    = dataSnapshot.child("email").getValue().toString();
                String c_ity     = dataSnapshot.child("city").getValue().toString();
                String d_ob      = dataSnapshot.child("dob").getValue().toString();
                String p_incode  = dataSnapshot.child("pincode").getValue().toString();

                fname.setText(f_name);
                lname.setText(l_name);
                mobile.setText(m_obile);
                email.setText(e_mail);
                city.setText(c_ity);
                dob.setText(d_ob);
                pincode.setText(p_incode);
                for(int i=0;i<states.size();i++)
                {
                    if(states.get(i).equals(s_pinnner))
                    {
                        spinner.setSelection(i);
                        break;
                    }

                }
                getSupportActionBar().setTitle("Welcome "+f_name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void loadProfile()
    {
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        storageReference.child(userId).child("Images/profile")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Glide.with(Profile.this).load(uri)
                        .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop()
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        }).into(iv);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK)
            {
                try
                {
                    imageUri = result.getUri();
                    updateProfile(imageUri);

                }
                catch (Exception e)
                {
                    Toast.makeText(Profile.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
            else if(resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Toast.makeText(Profile.this,result.getError().getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void crop()
    {
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(Profile.this);
    }

    private void updateLabel()
    {
        String myFormat = "dd-MM-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        dob.setText(sdf.format(myCalendar.getTime()));
    }

    void profile_alert()
    {
        LayoutInflater inflater = LayoutInflater.from(Profile.this);
        View view = inflater.inflate(R.layout.profile_photo_updated_alert,null);
        TextView tv = view.findViewById(R.id.message);
        tv.setText("Profile Photo Successfully Updated !");
        final AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        AlertDialog ad = builder.create();
        ad.show();
        loadProfile();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch(id) {

            case R.id.refresh :
                startActivity(new Intent(Profile.this,Profile.class));
                finish();
                break;

            case R.id.new_complaint_registration:
                startActivity(new Intent(Profile.this, Welcome.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.complaint_status :
                startActivity(new Intent(Profile.this,ComplaintStatus.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.search_complaint :
                startActivity(new Intent(Profile.this,ComplaintSearch.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.profile :
                startActivity(new Intent(Profile.this,Profile.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.logout:
                super.alert(R.layout.logout_alert,Profile.this,firebaseAuth);
                break;

            case R.id.share :
                share();
                break;

            case R.id.change_password :
                startActivity(new Intent(Profile.this,ChangePassword.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.chats :
                startActivity(new Intent(Profile.this,ChatActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.find_friends :
                startActivity(new Intent(Profile.this, FindFriends.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.received_requests :
                startActivity(new Intent(Profile.this, Requests.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    public void onBackPressed() {

        startActivity(new Intent(Profile.this,Welcome.class));
        finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);

    }

    private void alertDialougeBox()
    {

        LayoutInflater inflater = LayoutInflater.from(Profile.this);
        View view = inflater.inflate(R.layout.edit_details_alert,null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
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
                InputMethodManager imm= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(layout.getWindowToken(),0);

                reference = FirebaseDatabase.getInstance().getReference().child("user").child(userId);
                reference.child("fname").setValue(fname.getText().toString());
                reference.child("lname").setValue(lname.getText().toString());
                reference.child("city").setValue(city.getText().toString());
                reference.child("state").setValue(spinner.getSelectedItem().toString());
                reference.child("pincode").setValue(pincode.getText().toString());
                reference.child("dob").setValue(dob.getText().toString());
                dialog.dismiss();
                getDataFromDatabase();
                Toast.makeText(Profile.this,"Details Successfully Updated",Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog ad = builder.create();
        ad.show();
    }

    private void updateProfile(final Uri imageUri)
    {
        progressDialog.show();
        final StorageReference childImageRef = storageReference.child(userId).child("Images").child("profile");
        UploadTask uploadUserProfile = childImageRef.putFile(imageUri);
        uploadUserProfile.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                cpb.setProgress((100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                Task<Uri> downloadUrl = childImageRef.getDownloadUrl();
                downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        FirebaseDatabase.getInstance().getReference().child("user")
                                .child(userId).child("url").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                profile_alert();
                            }
                        });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(Profile.this, "Image Upload Failed !", Toast.LENGTH_SHORT).show();
            }
        });
    }
}