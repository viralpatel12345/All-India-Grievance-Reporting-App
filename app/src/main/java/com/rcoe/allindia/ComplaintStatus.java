package com.rcoe.allindia;

import androidx.annotation.NonNull;
import androidx.core.view.MenuCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ComplaintStatus extends BaseActivity {

    private FirebaseAuth firebaseAuth;
    private String userId;
    private RecyclerView recyclerView;
    private TextView count;
    private DatabaseReference reference;
    private Spinner spinner;
    private ArrayList<Complaints> list;
    private MyAdapter adapter;
    private ImageView updown;
    private View divider;
    private int flag=0,pos=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_status);
        getSupportActionBar().setTitle("MY COMPLAINTS");

        divider           = findViewById(R.id.divider3);
        spinner           = findViewById(R.id.cmp_spinner);
        updown            = findViewById(R.id.updown);
        count             = findViewById(R.id.count);
        firebaseAuth      = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        userId            = user.getUid();
        recyclerView      = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(mDividerItemDecoration);

        list = new ArrayList<Complaints>();

        String arr[] = {"Choose the complaint status","Solved Complaints","Unsolved Complaints"};

        List<String> complaint = new ArrayList<String>();
        for(String s : arr)
        {
            complaint.add(s);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, complaint){
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

        updown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flag == 0)
                {
                    flag=1;
                    updown.setImageResource(R.drawable.up);
                    Collections.sort(list, new Comparator<Complaints>() {
                        @Override
                        public int compare(Complaints c1, Complaints c2)
                        {
                            try
                            {
                                return (new SimpleDateFormat("E dd-MM-yyyy hh:mm:ss", Locale.ENGLISH)
                                        .parse(c1.getDateofcomplaint())).compareTo(new SimpleDateFormat("E dd-MM-yyyy hh:mm:ss", Locale.ENGLISH)
                                        .parse(c2.getDateofcomplaint()));
                            }
                            catch(Exception e)
                            {

                            }
                            return 0;
                        }
                    });

                    adapter = new MyAdapter(ComplaintStatus.this,list);
                    recyclerView.setAdapter(adapter);
                }
                else if(flag == 1)
                {
                    flag=0;
                    updown.setImageResource(R.drawable.down);
                    Collections.sort(list, new Comparator<Complaints>() {
                        @Override
                        public int compare(Complaints c1, Complaints c2)
                        {
                            try
                            {
                                return (new SimpleDateFormat("E dd-MM-yyyy hh:mm:ss", Locale.ENGLISH)
                                        .parse(c2.getDateofcomplaint())).compareTo(new SimpleDateFormat("E dd-MM-yyyy hh:mm:ss", Locale.ENGLISH)
                                        .parse(c1.getDateofcomplaint()));
                            }
                            catch(Exception e)
                            {

                            }
                            return 0;
                        }
                    });

                    adapter = new MyAdapter(ComplaintStatus.this,list);
                    recyclerView.setAdapter(adapter);
                }
            }
        });

        dataAdapter.setDropDownViewResource(R.layout.custom_spinner);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==1)
                {
                    pos = 1;
                    getDataFromDatabase("Complaint Solved");
                }
                else if(position==2)
                {
                    pos = 2;
                    getDataFromDatabase("Complaint Not Solved");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch(id) {

            case R.id.home:
                startActivity(new Intent(ComplaintStatus.this, Home.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.refresh :
                if(pos==0)
                {
                    startActivity(new Intent(ComplaintStatus.this, ComplaintStatus.class));
                    finish();
                }
                else if(pos==1)
                {
                    list.clear();
                    getDataFromDatabase("Complaint Solved");
                }
                else if(pos==2)
                {
                    list.clear();
                    getDataFromDatabase("Complaint Not Solved");
                }
                break;

            case R.id.new_complaint_registration:
                startActivity(new Intent(ComplaintStatus.this, Welcome.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.complaint_status :
                startActivity(new Intent(ComplaintStatus.this,ComplaintStatus.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.search_complaint :
                startActivity(new Intent(ComplaintStatus.this,ComplaintSearch.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.profile :
                startActivity(new Intent(ComplaintStatus.this,Profile.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.logout:
                super.alert(R.layout.logout_alert,ComplaintStatus.this,firebaseAuth);
                break;

            case R.id.share :
                share();
                break;

            case R.id.change_password :
                startActivity(new Intent(ComplaintStatus.this,ChangePassword.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.chats :
                startActivity(new Intent(ComplaintStatus.this,ChatActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.find_friends :
                startActivity(new Intent(ComplaintStatus.this, FindFriends.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.received_requests :
                startActivity(new Intent(ComplaintStatus.this, Requests.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;


            default:
                return super.onOptionsItemSelected(item);

        }

        return true;
    }

    private void getDataFromDatabase(final String option)
    {
        list.clear();
        updown.setImageResource(R.drawable.down);
        reference = FirebaseDatabase.getInstance().getReference().child("user").child(userId).child("complaints");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    if(option.equals("Complaint Solved"))
                    {
                        if(dataSnapshot1.child("status").getValue().toString().equals("Complaint Solved"))
                        {
                            Complaints c = new Complaints(dataSnapshot1.child("c_type").getValue().toString(),
                                    dataSnapshot1.child("c_subtype").getValue().toString(),
                                    dataSnapshot1.child("solved_url").getValue().toString(),
                                    dataSnapshot1.child("dateofcomplaint").getValue().toString(),
                                    dataSnapshot1.getKey());

                            list.add(c);
                        }
                    }
                    else if(option.equals("Complaint Not Solved"))
                    {
                        if(dataSnapshot1.child("status").getValue().toString().equals("Complaint Not Solved") ||
                           dataSnapshot1.child("status").getValue().toString().equals("Complaint Partially Solved"))
                        {
                            Complaints c = new Complaints(dataSnapshot1.child("c_type").getValue().toString(),
                                    dataSnapshot1.child("c_subtype").getValue().toString(),
                                    dataSnapshot1.child("url").getValue().toString(),
                                    dataSnapshot1.child("dateofcomplaint").getValue().toString(),
                                    dataSnapshot1.getKey());

                            list.add(c);
                        }
                    }
                }

                if(list.size()==0)
                {
                    Toast.makeText(ComplaintStatus.this,"Complaints Not Found",Toast.LENGTH_LONG).show();
                }
                else
                {
                    Collections.sort(list, new Comparator<Complaints>() {
                        @Override
                        public int compare(Complaints c1, Complaints c2) {
                            try {
                                return (new SimpleDateFormat("E dd-MM-yyyy hh:mm:ss", Locale.ENGLISH)
                                        .parse(c2.getDateofcomplaint()))
                                        .compareTo(new SimpleDateFormat("E dd-MM-yyyy hh:mm:ss", Locale.ENGLISH)
                                        .parse(c1.getDateofcomplaint()));
                            } catch (Exception e) {

                            }
                            return 0;
                        }
                    });

                }

                if(option.equals("Complaint Solved"))
                {
                    count.setText("Total Solved Complaints : "+list.size());
                }
                else
                {
                    count.setText("Total Unsolved Complaints : "+list.size());
                }

                divider.setVisibility(View.VISIBLE);
                adapter = new MyAdapter(ComplaintStatus.this,list);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ComplaintStatus.this,"Ohhh .... Something is wrong",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onBackPressed()
    {
        startActivity(new Intent(ComplaintStatus.this,Home.class));
        finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
