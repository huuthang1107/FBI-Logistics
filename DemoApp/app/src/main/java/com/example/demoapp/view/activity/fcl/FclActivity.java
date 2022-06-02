package com.example.demoapp.view.activity.fcl;


import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.demoapp.R;
import com.example.demoapp.databinding.ActivityFclBinding;
import com.example.demoapp.view.activity.LoginActivity;
import com.example.demoapp.view.activity.chat.DashboardActivity;
import com.example.demoapp.view.fragment.fcl.FCLFragment;
import com.example.demoapp.view.fragment.home.HomeFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FclActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityFclBinding binding;

    private static final int FRAGMENT_HOME = 0;
    private static final int FRAGMENT_FCL = 1;
    private static final int ACTIVITY_MESSAGE = 2;
    private static final int LOG_OUT = 3;

    private FirebaseAuth mAuth;

    private int mCurrentFragment = FRAGMENT_HOME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFclBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        binding.navigationView.setNavigationItemSelectedListener(this);

        mAuth = FirebaseAuth.getInstance();

        replaceFragment(new HomeFragment());
        binding.navigationView.getMenu().findItem(R.id.tab_home_fcl).setChecked(true);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tab_home_fcl:
                if (mCurrentFragment != FRAGMENT_HOME) {
                    replaceFragment(new HomeFragment());
                    mCurrentFragment = FRAGMENT_HOME;
                    binding.toolbar.setTitle("Home");
                }
                break;
            case R.id.tab_fcl:
                if (mCurrentFragment != FRAGMENT_FCL) {
                    replaceFragment(new FCLFragment());
                    mCurrentFragment = FRAGMENT_FCL;
                    binding.toolbar.setTitle("FCL");
                }
                break;
            case R.id.tab_chat_fcl:
                if (mCurrentFragment != ACTIVITY_MESSAGE) {
                    Intent intent = new Intent(this, DashboardActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.tab_logout_fcl:
                if (mCurrentFragment != LOG_OUT) {
                    mAuth.signOut();
                    checkUserStatus();
                }
                finish();
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {

        } else {
            startActivity(new Intent(FclActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }


}