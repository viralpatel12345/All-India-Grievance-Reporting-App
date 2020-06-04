package com.rcoe.allindia;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ComplaintSearch extends BaseActivity {

    private ConstraintLayout constraintLayout;
    private AutoCompleteTextView editText;
    private ImageView iv;
    private MyAdapter myAdapter;
    private RecyclerView recyclerView;
    private DatabaseReference reference;
    private FirebaseAuth firebaseAuth;
    private String userId,url;
    private ArrayList<Complaints> list;
    private int flag=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_search);
        getSupportActionBar().setTitle("SEARCH COMPLAINT");

        editText          = findViewById(R.id.complaints);
        iv                = findViewById(R.id.getDataButton);
        constraintLayout  = findViewById(R.id.constraintLayout);

        list = new ArrayList<Complaints>();

        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this,
                R.array.complaint_type,
                R.layout.custom_spinner);

        editText.setAdapter(adapter);

        firebaseAuth      = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        userId            = user.getUid();
        recyclerView      = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(mDividerItemDecoration);

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                InputMethodManager imm= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(constraintLayout.getWindowToken(),0);
                list.clear();
                final String input = editText.getText().toString().trim();
                reference = FirebaseDatabase.getInstance().getReference().child("user").child(userId).child("complaints");
                reference.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                        {
                           if(Pattern.compile("\\d{32}").matcher(input).matches())
                           {
                               String key=dataSnapshot1.getKey();
                               if(key.equals(input))
                               {
                                   if(dataSnapshot1.child("status").getValue().toString().equals("Complaint Solved") &&
                                           dataSnapshot1.child("solved_url").exists())
                                   {
                                       url = dataSnapshot1.child("solved_url").getValue().toString();
                                   }
                                   else
                                   {
                                       url = dataSnapshot1.child("url").getValue().toString();
                                   }

                                   Complaints c = new Complaints(dataSnapshot1.child("c_type").getValue().toString(),
                                           dataSnapshot1.child("c_subtype").getValue().toString(),url,
                                           dataSnapshot1.child("dateofcomplaint").getValue().toString(),
                                           dataSnapshot1.getKey());
                                   list.add(c);
                                   break;
                               }
                               else
                               {
                                   flag=1;
                               }
                           }
                           else
                           {
                               if(dataSnapshot1.child("c_type").getValue().toString().equals(input))
                               {
                                   if(dataSnapshot1.child("status").getValue().toString().equals("Complaint Solved") &&
                                           dataSnapshot1.child("solved_url").exists())
                                   {
                                       url = dataSnapshot1.child("solved_url").getValue().toString();
                                   }
                                   else
                                   {
                                       url = dataSnapshot1.child("url").getValue().toString();
                                   }

                                   Complaints c = new Complaints(dataSnapshot1.child("c_type").getValue().toString(),
                                           dataSnapshot1.child("c_subtype").getValue().toString(),url,
                                           dataSnapshot1.child("dateofcomplaint").getValue().toString(),
                                           dataSnapshot1.getKey());

                                   list.add(c);
                               }
                           }
                        }

                        if(list.size()==0)
                        {
                            if(flag==1)
                            {
                                flag=0;
                                Toast.makeText(ComplaintSearch.this,"Invalid Complaint Id",Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                Toast.makeText(ComplaintSearch.this,"Complaint Not Found",Toast.LENGTH_LONG).show();
                            }

                        }
                        myAdapter = new MyAdapter(ComplaintSearch.this,list);
                        recyclerView.setAdapter(myAdapter);
                        myAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
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

            case R.id.home:
                startActivity(new Intent(ComplaintSearch.this, Home.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.refresh :
                startActivity(new Intent(ComplaintSearch.this,ComplaintSearch.class));
                finish();
                break;

            case R.id.new_complaint_registration:
                startActivity(new Intent(ComplaintSearch.this, Welcome.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.complaint_status :
                startActivity(new Intent(ComplaintSearch.this,ComplaintStatus.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.search_complaint :
                startActivity(new Intent(ComplaintSearch.this,ComplaintSearch.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.profile :
                startActivity(new Intent(ComplaintSearch.this,Profile.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.logout:
                super.alert(R.layout.logout_alert,ComplaintSearch.this,firebaseAuth);
                break;

            case R.id.share :
                share();
                break;

            case R.id.change_password :
                startActivity(new Intent(ComplaintSearch.this,ChangePassword.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.chats :
                startActivity(new Intent(ComplaintSearch.this,ChatActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.find_friends :
                startActivity(new Intent(ComplaintSearch.this, FindFriends.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            case R.id.received_requests :
                startActivity(new Intent(ComplaintSearch.this, Requests.class));
                finish();
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            default:
                return super.onOptionsItemSelected(item);

        }

        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ComplaintSearch.this,Home.class));
        finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
