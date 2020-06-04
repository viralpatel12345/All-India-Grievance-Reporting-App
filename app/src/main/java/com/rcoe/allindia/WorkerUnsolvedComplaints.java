package com.rcoe.allindia;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WorkerUnsolvedComplaints extends BaseActivity
{
    private FirebaseAuth firebaseAuth;
    private String postal_code,status,worker_id;
    private RecyclerView recyclerView;
    private TextView count;
    private DatabaseReference reference;
    private ArrayList<UserAndComplaint> list;
    private WorkerMyAdapter adapter;
    private ImageView updown;
    private View divider;
    private int flag=0;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_unsolved_complaints);

        postal_code       = getIntent().getStringExtra("postal_code");
        status            = getIntent().getStringExtra("status");
        divider           = findViewById(R.id.divider4);
        updown            = findViewById(R.id.updown);
        count             = findViewById(R.id.total_unsolved_count);
        firebaseAuth      = FirebaseAuth.getInstance();
        worker_id         = firebaseAuth.getCurrentUser().getUid();
        recyclerView      = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(mDividerItemDecoration);

        list = new ArrayList<UserAndComplaint>();

        if(status.equals("solved"))
        {
            getDataFromDatabase("Complaint Solved");
        }
        else if(status.equals("unsolved"))
        {
            getDataFromDatabase("Complaint Not Solved");
        }

        updown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flag == 0)
                {
                    flag=1;
                    updown.setImageResource(R.drawable.up);
                    Collections.sort(list, new Comparator<UserAndComplaint>() {
                        @Override
                        public int compare(UserAndComplaint c1, UserAndComplaint c2) {
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

                    adapter = new WorkerMyAdapter(WorkerUnsolvedComplaints.this,list);
                    recyclerView.setAdapter(adapter);
                }
                else if(flag == 1)
                {
                    flag=0;
                    updown.setImageResource(R.drawable.down);
                    Collections.sort(list, new Comparator<UserAndComplaint>() {
                        @Override
                        public int compare(UserAndComplaint c1, UserAndComplaint c2)
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

                    adapter = new WorkerMyAdapter(WorkerUnsolvedComplaints.this,list);
                    recyclerView.setAdapter(adapter);
                }
            }
        });

    }

    private void getDataFromDatabase(final String option)
    {
        if(option.equals("Complaint Solved"))
        {
            getSupportActionBar().setTitle("SOLVED");
        }
        else if(option.equals("Complaint Not Solved"))
        {
            getSupportActionBar().setTitle("NOT SOLVED");
        }
        updown.setImageResource(R.drawable.down);
        list.clear();
        reference = FirebaseDatabase.getInstance().getReference().child("user");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    if(dataSnapshot1.child("complaints").exists()) {
                        for (DataSnapshot dataSnapshot2 : dataSnapshot1.child("complaints").getChildren())
                        {
                            if (dataSnapshot2.child("postalcode").getValue().toString().equals(postal_code) &&
                                    dataSnapshot2.child("status").getValue().toString().equals(option))
                            {
                                if(status.equals("solved") &&
                                        dataSnapshot2.child("workerId").getValue().toString().equals(worker_id))
                                {
                                    UserAndComplaint userAndComplaint = new
                                            UserAndComplaint(dataSnapshot1.getKey(),
                                            dataSnapshot1.child("email").getValue().toString(),
                                            dataSnapshot2.getKey(),dataSnapshot2.child("c_type").getValue().toString(),
                                            dataSnapshot2.child("c_subtype").getValue().toString(),
                                            dataSnapshot2.child("dateofcomplaint").getValue().toString(),
                                            dataSnapshot2.child("solved_url").getValue().toString(),
                                            postal_code,status);

                                    list.add(userAndComplaint);

                                }
                                else if(status.equals("unsolved"))
                                {
                                    UserAndComplaint userAndComplaint = new
                                            UserAndComplaint(dataSnapshot1.getKey(),
                                            dataSnapshot1.child("email").getValue().toString(),
                                            dataSnapshot2.getKey(),dataSnapshot2.child("c_type").getValue().toString(),
                                            dataSnapshot2.child("c_subtype").getValue().toString(),
                                            dataSnapshot2.child("dateofcomplaint").getValue().toString(),
                                            dataSnapshot2.child("url").getValue().toString(),
                                            postal_code,status);

                                    list.add(userAndComplaint);

                                }
                            }
                        }
                    }
                }

                if(list.size()==0)
                {
                    Toast.makeText(WorkerUnsolvedComplaints.this,"Complaints Not Found",Toast.LENGTH_LONG).show();
                }
                else
                {
                    Collections.sort(list, new Comparator<UserAndComplaint>() {
                        @Override
                        public int compare(UserAndComplaint c1, UserAndComplaint c2) {
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
                    count.setText("Total complaints solved by me : "+list.size());
                }
                else if(option.equals("Complaint Not Solved"))
                {
                    count.setText("Total Unsolved Complaints : "+list.size());
                }

                divider.setVisibility(View.VISIBLE);
                adapter = new WorkerMyAdapter(WorkerUnsolvedComplaints.this,list);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(WorkerUnsolvedComplaints.this,"Ohhh .... Something is wrong",Toast.LENGTH_SHORT).show();
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

        int id      = item.getItemId();

        switch (id) {

            case R.id.refresh:
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerUnsolvedComplaints.this,WorkerUnsolvedComplaints.class);
                    intent.putExtra("postal_code",postal_code);
                    intent.putExtra("status",status);
                    startActivity(intent);
                    finish();
                    break;
                }

            case R.id.share:
                share();
                break;

            case R.id.home:
                startActivity(new Intent(WorkerUnsolvedComplaints.this,WorkerHome.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.logout:
                super.alert(R.layout.logout_alert,WorkerUnsolvedComplaints.this,firebaseAuth);
                break;

            case R.id.solved_complaints :
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerUnsolvedComplaints.this,WorkerUnsolvedComplaints.class);
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
                    Intent intent = new Intent(WorkerUnsolvedComplaints.this,WorkerUnsolvedComplaints.class);
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
                    Intent intent = new Intent(WorkerUnsolvedComplaints.this,WorkerComplaintSearch.class);
                    intent.putExtra("postal_code",postal_code);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    break;
                }

            case R.id.profile :
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerUnsolvedComplaints.this,WorkerProfile.class);
                    intent.putExtra("postal_code",postal_code);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    break;
                }

            case R.id.change_password :
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerUnsolvedComplaints.this,WorkerChangePassword.class);
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
        startActivity(new Intent(WorkerUnsolvedComplaints.this,WorkerHome.class));
        finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
