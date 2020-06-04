package com.rcoe.allindia;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.regex.Pattern;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WorkerComplaintSearch extends BaseActivity {

    private int flag=0,flag1=0;
    private String postal_code,workerId;
    private ConstraintLayout constraintLayout;
    private AutoCompleteTextView editText;
    private ImageView iv,sortButton;
    private TextView result;
    private View divider;
    private WorkerSearchAdapter myAdapter;
    private RecyclerView recyclerView;
    private DatabaseReference reference;
    private FirebaseAuth firebaseAuth;
    private ArrayList<UserAndComplaint> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_complaint_search);
        getSupportActionBar().setTitle("SEARCH COMPLAINT");

        postal_code  = getIntent().getStringExtra("postal_code");

        editText          = findViewById(R.id.complaints);
        iv                = findViewById(R.id.getDataButton);
        constraintLayout  = findViewById(R.id.constraintLayout);
        result            = findViewById(R.id.result);
        sortButton        = findViewById(R.id.updown);
        divider           = findViewById(R.id.divider8);

        list = new ArrayList<UserAndComplaint>();

        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this,
                R.array.complaint_type,
                R.layout.custom_spinner);

        editText.setAdapter(adapter);

        firebaseAuth        = FirebaseAuth.getInstance();
        FirebaseUser worker = firebaseAuth.getCurrentUser();
        workerId            = worker.getUid();
        recyclerView        = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(mDividerItemDecoration);


        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flag1 == 0)
                {
                    flag1=1;
                    sortButton.setImageResource(R.drawable.up);
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

                    myAdapter = new WorkerSearchAdapter(WorkerComplaintSearch.this,list);
                    recyclerView.setAdapter(myAdapter);
                }
                else if(flag1 == 1)
                {
                    flag1=0;
                    sortButton.setImageResource(R.drawable.down);
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

                    myAdapter = new WorkerSearchAdapter(WorkerComplaintSearch.this,list);
                    recyclerView.setAdapter(myAdapter);
                }
            }
        });


        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(constraintLayout.getWindowToken(), 0);
                list.clear();
                final String input = editText.getText().toString().trim();
                reference = FirebaseDatabase.getInstance().getReference().child("user");

                if (Pattern.compile("\\d{32}").matcher(input).matches())
                {
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                if (dataSnapshot1.child("complaints").exists()) {
                                    for (DataSnapshot dataSnapshot2 : dataSnapshot1.child("complaints").getChildren())
                                    {
                                        if(dataSnapshot2.getKey().equals(input))
                                        {
                                          flag=1;

                                          if(dataSnapshot2.child("postalcode").getValue().toString().equals(postal_code))
                                          {
                                             if(dataSnapshot2.child("status").getValue().toString().equals("Complaint Solved"))
                                             {
                                                 if(dataSnapshot2.child("workerId").getValue().toString().equals(workerId))
                                                 {
                                                     UserAndComplaint userAndComplaint = new
                                                             UserAndComplaint(dataSnapshot1.getKey(),
                                                             dataSnapshot1.child("email").getValue().toString(),
                                                             dataSnapshot2.getKey(),dataSnapshot2.child("c_type").getValue().toString(),
                                                             dataSnapshot2.child("c_subtype").getValue().toString(),
                                                             dataSnapshot2.child("dateofcomplaint").getValue().toString(),
                                                             dataSnapshot2.child("solved_url").getValue().toString(),
                                                             postal_code,"solved");

                                                     list.add(userAndComplaint);
                                                     break;
                                                 }
                                             }
                                             else if(dataSnapshot2.child("status").getValue().toString().equals("Complaint Not Solved"))
                                             {
                                                 UserAndComplaint userAndComplaint = new
                                                         UserAndComplaint(dataSnapshot1.getKey(),
                                                         dataSnapshot1.child("email").getValue().toString(),
                                                         dataSnapshot2.getKey(),dataSnapshot2.child("c_type").getValue().toString(),
                                                         dataSnapshot2.child("c_subtype").getValue().toString(),
                                                         dataSnapshot2.child("dateofcomplaint").getValue().toString(),
                                                         dataSnapshot2.child("url").getValue().toString(),
                                                         postal_code,"unsolved");

                                                 list.add(userAndComplaint);
                                                 break;
                                             }
                                          }
                                        }

                                    }
                                }
                                if(flag==1)
                                {
                                    flag=0;
                                    break;
                                }
                            }

                            if(list.size()==0)
                            {
                                divider.setVisibility(View.VISIBLE);
                                sortButton.setVisibility(View.GONE);
                                result.setVisibility(View.VISIBLE);
                                result.setText("complaint not found");
                            }
                            else if(list.size()==1)
                            {
                                divider.setVisibility(View.VISIBLE);
                                result.setVisibility(View.VISIBLE);
                                sortButton.setVisibility(View.GONE);
                                result.setText("1 complaint found");
                            }

                            myAdapter = new WorkerSearchAdapter(WorkerComplaintSearch.this,list);
                            recyclerView.setAdapter(myAdapter);
                            myAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else
                {
                    reference.addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren())
                            {
                                if(dataSnapshot1.child("complaints").exists())
                                {
                                    for(DataSnapshot dataSnapshot2:dataSnapshot1.child("complaints").getChildren())
                                    {
                                       if(dataSnapshot2.child("postalcode").getValue().toString().equals(postal_code) &&
                                          dataSnapshot2.child("c_type").getValue().toString().equals(input))
                                       {
                                           if(dataSnapshot2.child("status").getValue().toString().equals("Complaint Solved"))
                                           {
                                               if(dataSnapshot2.child("workerId").getValue().toString().equals(workerId))
                                               {
                                                   UserAndComplaint userAndComplaint = new
                                                           UserAndComplaint(dataSnapshot1.getKey(),
                                                           dataSnapshot1.child("email").getValue().toString(),
                                                           dataSnapshot2.getKey(),dataSnapshot2.child("c_type").getValue().toString(),
                                                           dataSnapshot2.child("c_subtype").getValue().toString(),
                                                           dataSnapshot2.child("dateofcomplaint").getValue().toString(),
                                                           dataSnapshot2.child("solved_url").getValue().toString(),
                                                           postal_code,"solved");

                                                   list.add(userAndComplaint);
                                               }
                                           }
                                           else if(dataSnapshot2.child("status").getValue().toString().equals("Complaint Not Solved"))
                                           {
                                               UserAndComplaint userAndComplaint = new
                                                       UserAndComplaint(dataSnapshot1.getKey(),
                                                       dataSnapshot1.child("email").getValue().toString(),
                                                       dataSnapshot2.getKey(),dataSnapshot2.child("c_type").getValue().toString(),
                                                       dataSnapshot2.child("c_subtype").getValue().toString(),
                                                       dataSnapshot2.child("dateofcomplaint").getValue().toString(),
                                                       dataSnapshot2.child("url").getValue().toString(),
                                                       postal_code,"unsolved");

                                               list.add(userAndComplaint);
                                           }
                                       }
                                    }
                                }
                            }

                            if(list.size()==0)
                            {
                                sortButton.setVisibility(View.GONE);
                                divider.setVisibility(View.VISIBLE);
                                result.setVisibility(View.VISIBLE);
                                result.setText("complaint not found");
                            }
                            else if(list.size()==1)
                            {
                                sortButton.setVisibility(View.GONE);
                                divider.setVisibility(View.VISIBLE);
                                result.setVisibility(View.VISIBLE);
                                result.setText("1 complaint found");
                            }
                            else if(list.size()>1)
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

                                sortButton.setVisibility(View.VISIBLE);
                                divider.setVisibility(View.VISIBLE);
                                result.setVisibility(View.VISIBLE);
                                result.setText(list.size()+" complaints found");
                            }

                            myAdapter = new WorkerSearchAdapter(WorkerComplaintSearch.this,list);
                            recyclerView.setAdapter(myAdapter);
                            myAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
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
                    Intent intent = new Intent(WorkerComplaintSearch.this,WorkerComplaintSearch.class);
                    intent.putExtra("postal_code",postal_code);
                    startActivity(intent);
                    finish();
                    break;
                }

            case R.id.share:
                share();
                break;

            case R.id.home:
                startActivity(new Intent(WorkerComplaintSearch.this,WorkerHome.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.logout:
                super.alert(R.layout.logout_alert,WorkerComplaintSearch.this,firebaseAuth);
                break;

            case R.id.solved_complaints :
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerComplaintSearch.this,WorkerUnsolvedComplaints.class);
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
                    Intent intent = new Intent(WorkerComplaintSearch.this,WorkerUnsolvedComplaints.class);
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
                    Intent intent = new Intent(WorkerComplaintSearch.this,WorkerComplaintSearch.class);
                    intent.putExtra("postal_code",postal_code);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    break;
                }

            case R.id.profile :
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerComplaintSearch.this,WorkerProfile.class);
                    intent.putExtra("postal_code",postal_code);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    break;
                }

            case R.id.change_password :
                if(postal_code!=null)
                {
                    Intent intent = new Intent(WorkerComplaintSearch.this,WorkerChangePassword.class);
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
        startActivity(new Intent(WorkerComplaintSearch.this,WorkerHome.class));
        finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}