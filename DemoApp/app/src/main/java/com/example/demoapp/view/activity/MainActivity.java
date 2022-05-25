package com.example.demoapp.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demoapp.R;
import com.example.demoapp.view.activity.fcl.FclActivity;
import com.example.demoapp.view.activity.loginAndRegister.RegisterActivity;
import com.example.demoapp.view.activity.sale.SaleActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                nextActivity();
            }
        }, 2000);
    }

    private void nextActivity() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("Users");
        if(user == null){
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        }else{
            Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // check until required data get
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        // get data
                        String position = "" + ds.child("position").getValue();
                        if(position.equals("Sale")){
                            startActivity(new Intent(MainActivity.this, SaleActivity.class));
                        }else if(position.equals("FCL")){
                            startActivity(new Intent(MainActivity.this, FclActivity.class));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        finish();
    }

}