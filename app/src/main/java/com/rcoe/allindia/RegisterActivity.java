package com.rcoe.allindia;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import de.hdodenhof.circleimageview.CircleImageView;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class RegisterActivity extends BaseActivity {

    private ConstraintLayout layout;
    private CircleImageView profile;
    private Uri imageUri = null;
    private TextView removeImage,spinnerError;
    private EditText fname,lname,email,mobile,dob,password,city,pincode;
    private Spinner spinner;
    private RadioGroup rg;
    private DatabaseReference databaseReference;
    private Button register;
    private RadioButton rb;
    private TextView show_hide_password;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private final static String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";
    private final static String MOBILE_PATTERN   = "^[6-9]\\d{9}$";
    private boolean password_visibility = false;
    private String user_fname,user_lname,user_email,user_mobile,user_state,
            user_city,user_pincode,user_gender,user_dob,user_password;
    private String key,imageRef;
    final Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setTitle("NEW USER REGISTRATION");

        layout             = findViewById(R.id.register_layout);
        fname              = findViewById(R.id.fname);
        lname              = findViewById(R.id.lname);
        email              = findViewById(R.id.email);
        mobile             = findViewById(R.id.mobile);
        dob                = findViewById(R.id.dob);
        city               = findViewById(R.id.city);
        pincode            = findViewById(R.id.pincode);
        password           = findViewById(R.id.password);
        spinner            = findViewById(R.id.spinner);
        spinnerError       = findViewById(R.id.textView);
        profile            = findViewById(R.id.profile_image);
        removeImage        = findViewById(R.id.removeProfile);
        register           = findViewById(R.id.button);
        rg                 = findViewById(R.id.radioGroup2);
        show_hide_password = findViewById(R.id.show_hide_password);
        databaseReference  = FirebaseDatabase.getInstance().getReference().child("user");

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog   = new ProgressDialog(RegisterActivity.this);
        progressDialog.setMessage("Registration in Progress ");
        progressDialog.setCancelable(false);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        show_hide_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(password_visibility == false)
                {
                    password_visibility = true;
                    show_hide_password.setText("HIDE PASSWORD");
                    password.setTransformationMethod(null);
                }
                else
                {
                    password_visibility = false;
                    show_hide_password.setText("SHOW PASSWORD");
                    password.setTransformationMethod(new PasswordTransformationMethod());

                }
            }
        });

        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(RegisterActivity.this, date , myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        String arr[] = {" ","Andhra Pradesh","Arunachal Pradesh ","Assam","Bihar",
                        "Chhattisgarh","Goa","Gujarat","Haryana","Himachal Pradesh","Jammu and Kashmir",
                        "Jharkhand","Karnataka","Kerala","Madhya Pradesh","Maharashtra","Manipur","Meghalaya",
                        "Mizoram","Nagaland","Odisha","Punjab","Rajasthan","Sikkim","Tamil Nadu",
                        "Telangana","Tripura","Uttar Pradesh","Uttarakhand","West Bengal",
                        "Andaman and Nicobar Islands","Chandigarh","Dadra and Nagar Haveli","Daman and Diu",
                        "Lakshadweep","National Capital Territory of Delhi","Puducherry","Ladakh"};

        List<String> states = new ArrayList<String>();
        for(String s : arr)
        {
            states.add(s);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, states){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position>0)
                {
                    spinnerError.setText("I am from "+spinner.getSelectedItem().toString());
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               crop();
            }
        });

        removeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(removeImage.getText().toString()=="REMOVE PROFILE PHOTO")
                {
                    profile.setImageBitmap(null);
                    imageUri = null;
                    removeImage.setText("ADD PROFILE PHOTO");
                }
                else
                {
                    crop();
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate())
                {
                   InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                   imm.hideSoftInputFromWindow(layout.getWindowToken(),0);
                   if(isConnected(RegisterActivity.this) != true)
                   {
                       Toast.makeText(RegisterActivity.this,"Please check your Internet Connection",Toast.LENGTH_SHORT).show();
                   }
                   else
                   {
                        progressDialog.show();
                        progressDialog.setCancelable(false);
                        firebaseStorage = FirebaseStorage.getInstance();
                        storageReference = firebaseStorage.getReference();
                        firebaseAuth.createUserWithEmailAndPassword(user_email, user_password)
                                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>()
                        {
                              @Override
                              public void onComplete(@NonNull Task<AuthResult> task)
                              {
                                     if (task.isSuccessful())
                                     {
                                        key = firebaseAuth.getUid();
                                        final StorageReference childImageRef = storageReference.child(key).child("Images").child("profile");
                                        UploadTask uploadUserProfile = childImageRef.putFile(imageUri);
                                        uploadUserProfile.addOnFailureListener(new OnFailureListener()
                                        {
                                            @Override
                                            public void onFailure(@NonNull Exception e)
                                            {
                                                   Toast.makeText(RegisterActivity.this, "Image Upload Failed !", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                                        {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                                            {
                                                    Task<Uri> downloadUrl = childImageRef.getDownloadUrl();
                                                    downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>()
                                                    {
                                                        @Override
                                                        public void onSuccess(Uri uri)
                                                        {
                                                            imageRef = uri.toString();
                                                            storeUserDataInDatabase();
                                                        }
                                                    });

                                            }
                                        });

                                     }
                                     else if (task.getException() instanceof FirebaseAuthUserCollisionException)
                                     {
                                            progressDialog.dismiss();
                                            Toast.makeText(RegisterActivity.this, "The email address is already in use by another account", Toast.LENGTH_LONG).show();
                                     }
                                     else
                                     {
                                            progressDialog.dismiss();
                                            Toast.makeText(RegisterActivity.this, "Registration Failed", Toast.LENGTH_LONG).show();
                                     }

                              }
                        });
                   }
                }
            }
        });

    }   // End of protected void onCreate(Bundle savedInstanceState)

    public boolean validate()
    {
        boolean valid = true;

        user_fname    = fname.getText().toString().trim();
        user_lname    = lname.getText().toString().trim();
        user_email    = email.getText().toString().trim();
        user_state    = spinner.getSelectedItem().toString();
        user_city     = city.getText().toString().trim();
        user_pincode  = pincode.getText().toString().trim();
        user_mobile   = mobile.getText().toString().trim();
        user_dob      = dob.getText().toString().trim();
        user_password = password.getText().toString().trim();
        rb            = findViewById(rg.getCheckedRadioButtonId());
        user_gender   = rb.getText().toString();

        if(user_fname.isEmpty())
        {
            fname.setError("FIRST NAME REQUIRED *");
            fname.setFocusable(true);
            valid = false;
        }
        else
        {
            fname.setError(null);
        }

        if(imageUri==null)
        {
           Toast.makeText(RegisterActivity.this,"PROFILE PHOTO REQUIRED",Toast.LENGTH_SHORT).show();
           valid=false;
        }
        else{

        }

        if(user_lname.isEmpty())
        {
            lname.setError("LAST NAME REQUIRED *");
            lname.setFocusable(true);
            valid = false;
        }
        else
        {
            lname.setError(null);
        }

        if(user_dob.isEmpty())
        {
            dob.setError("DATE OF BIRTH REQUIRED *");
            dob.setFocusable(true);
            valid = false;
        }
        else
        {
            dob.setError(null);
        }

        if(user_city.isEmpty())
        {
            city.setError("CITY REQUIRED *");
            city.setFocusable(true);
            valid = false;
        }
        else
        {
            city.setError(null);
        }

        if(user_state=="Select Your State *")
        {
            valid = false;

        }
        else
        {

        }

        if(user_pincode.isEmpty())
        {
            pincode.setError("PINCODE REQUIRED *");
            pincode.setFocusable(true);
            valid = false;
        }
        else
        {
            pincode.setError(null);
        }

        if(Pattern.compile(PASSWORD_PATTERN).matcher(user_password).matches() != true)
        {
            if (user_password.isEmpty()) {
                password.setError("PASSWORD REQUIRED *");
                password.setFocusable(true);
            }else {
                alertdialogbox();
            }
            valid = false;
        }
        else
        {
            password.setError(null);
        }

        if(Patterns.EMAIL_ADDRESS.matcher(user_email).matches() != true)
        {
            if(user_email.isEmpty())
            {
                email.setError("EMAIL REQUIRED *");
                email.setFocusable(true);
            }
            else
            {
                email.setError("Invalid Email Format");
                email.setFocusable(true);
            }
            valid = false;
        }
        else
        {
            email.setError(null);
        }

        if(Pattern.compile(MOBILE_PATTERN).matcher(user_mobile).matches() != true)
        {
            if(user_mobile.isEmpty())
            {
                mobile.setError("MOBILE NUMBER REQUIRED *");
                mobile.setFocusable(true);
            }
            else
            {
                mobile.setError("Please Enter valid 10 digit mobile number");
                mobile.setFocusable(true);
            }
            valid = false;
        }
        else
        {
            mobile.setError(null);
        }

        return valid;
    }

    private void alertdialogbox()
    {
        LayoutInflater inflater = LayoutInflater.from(RegisterActivity.this);
        View view = inflater.inflate(R.layout.alert_dialouge,null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setNegativeButton("DISMISS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog ad= builder.create();
        ad.show();
    }

    private void storeUserDataInDatabase()
    {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task)
                    {
                        if(task.isSuccessful()) {
                            String token = task.getResult().getToken();
                            User user = new User(user_fname, user_lname, user_email, user_mobile,
                                    user_state, user_city, user_pincode, user_gender, user_dob, token);

                            databaseReference.child(key).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        databaseReference.child(key).child("url").setValue(imageRef).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    firebaseAuth.signOut();
                                                    progressDialog.dismiss();
                                                    success_alert(RegisterActivity.this, "Congratulations !\n\nHi " + fname.getText().toString() + " ,\n\nYour account has been successfully created !");
                                                }
                                            }
                                        });
                                    } else {
                                        firebaseAuth.getCurrentUser().delete();
                                        Toast.makeText(RegisterActivity.this, "Registration Failed !", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }

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
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
                    profile.setImageBitmap(bitmap);
                    removeImage.setText("REMOVE PROFILE PHOTO");
                }
                catch (Exception e)
                {
                    Toast.makeText(RegisterActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
            else if(resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Toast.makeText(RegisterActivity.this,result.getError().getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateLabel()
    {
        String myFormat = "dd-MM-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        dob.setText(sdf.format(myCalendar.getTime()));
    }

    public void crop()
    {
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1,1)
                            .start(RegisterActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.front_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            case R.id.refresh:
                startActivity(new Intent(RegisterActivity.this, RegisterActivity.class));
                finish();
                break;

            case R.id.share:
                share();
                break;

            default:
                return super.onOptionsItemSelected(item);

        }
        return true;
    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
        finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
