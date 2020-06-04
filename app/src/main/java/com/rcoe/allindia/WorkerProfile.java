package com.rcoe.allindia;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import de.hdodenhof.circleimageview.CircleImageView;

public class WorkerProfile extends BaseActivity {

    private FirebaseAuth firebaseAuth;
    private String userId="";
    private EditText fname,lname,email,mobile;
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
    private String postal_code=null;

    private CircleImageView iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_profile);
        getSupportActionBar().setTitle("");

        postal_code = getIntent().getStringExtra("postal_code");
        spinner     = findViewById(R.id.spinner);
        fname       = findViewById(R.id.fname);
        lname       = findViewById(R.id.lname);
        email       = findViewById(R.id.emailId);
        mobile      = findViewById(R.id.mobile_no);
        iv          = findViewById(R.id.profile_photo);
        progressBar = findViewById(R.id.progressBar);
        cpb         = findViewById(R.id.cpb);
        editButton  = findViewById(R.id.editButton);
        layout      = findViewById(R.id.lay);

        progressDialog = new ProgressDialog(WorkerProfile.this);
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
                if(isConnected(WorkerProfile.this)!=true)
                {
                    Toast.makeText(WorkerProfile.this,"Please check your Internet Connection",Toast.LENGTH_SHORT).show();
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
        reference = FirebaseDatabase.getInstance().getReference().child("Workers").child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String f_name    = dataSnapshot.child("fname").getValue().toString();
                String l_name    = dataSnapshot.child("lname").getValue().toString();
                String s_pinnner = dataSnapshot.child("state").getValue().toString();
                String m_obile   = dataSnapshot.child("mobile").getValue().toString();
                String e_mail    = dataSnapshot.child("email").getValue().toString();
                fname.setText(f_name);
                lname.setText(l_name);
                mobile.setText(m_obile);
                email.setText(e_mail);
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
        storageReference.child("Workers").child(userId).child("profile")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Glide.with(WorkerProfile.this).load(uri)
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
                    Toast.makeText(WorkerProfile.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
            else if(resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Toast.makeText(WorkerProfile.this,result.getError().getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void crop()
    {
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(WorkerProfile.this);
    }

    void profile_alert()
    {
        LayoutInflater inflater = LayoutInflater.from(WorkerProfile.this);
        View view = inflater.inflate(R.layout.profile_photo_updated_alert,null);
        TextView tv = view.findViewById(R.id.message);
        tv.setText("Profile Photo Successfully Updated !");
        final AlertDialog.Builder builder = new AlertDialog.Builder(WorkerProfile.this);
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

    private void alertDialougeBox()
    {
        LayoutInflater inflater = LayoutInflater.from(WorkerProfile.this);
        View view = inflater.inflate(R.layout.edit_details_alert,null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(WorkerProfile.this);
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

                reference = FirebaseDatabase.getInstance().getReference().child("Workers").child(userId);
                reference.child("fname").setValue(fname.getText().toString());
                reference.child("lname").setValue(lname.getText().toString());
                reference.child("state").setValue(spinner.getSelectedItem().toString());
                dialog.dismiss();
                getDataFromDatabase();
                Toast.makeText(WorkerProfile.this,"Details Successfully Updated",Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog ad = builder.create();
        ad.show();
    }

    private void updateProfile(final Uri imageUri)
    {
        progressDialog.show();
        final StorageReference childImageRef = storageReference.child("Workers").child(userId).child("profile");
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
                        FirebaseDatabase.getInstance().getReference().child("Workers")
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
                Toast.makeText(WorkerProfile.this, "Image Upload Failed !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            case R.id.refresh:
                startActivity(new Intent(WorkerProfile.this,WorkerProfile.class));
                finish();
                break;

            case R.id.share:
                share();
                break;

            case R.id.home:
                startActivity(new Intent(WorkerProfile.this,WorkerHome.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.logout:
                super.alert(R.layout.logout_alert,WorkerProfile.this,firebaseAuth);
                break;

            case R.id.solved_complaints:
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerProfile.this,WorkerUnsolvedComplaints.class);
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
                    Intent intent = new Intent(WorkerProfile.this,WorkerUnsolvedComplaints.class);
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
                    Intent intent = new Intent(WorkerProfile.this,WorkerComplaintSearch.class);
                    intent.putExtra("postal_code",postal_code);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    break;
                }

            case R.id.profile :
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerProfile.this,WorkerProfile.class);
                    intent.putExtra("postal_code",postal_code);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    break;
                }

            case R.id.change_password :
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerProfile.this,WorkerChangePassword.class);
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
    public void onBackPressed()
    {
        startActivity(new Intent(WorkerProfile.this,WorkerHome.class));
        finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
