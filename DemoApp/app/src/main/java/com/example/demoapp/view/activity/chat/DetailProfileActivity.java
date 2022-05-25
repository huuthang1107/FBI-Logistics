package com.example.demoapp.view.activity.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.demoapp.R;
import com.example.demoapp.adapter.chat.PostsAdapter;
import com.example.demoapp.databinding.ActivityDetailProfileBinding;
import com.example.demoapp.model.Post;
import com.example.demoapp.view.activity.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DetailProfileActivity extends AppCompatActivity {

    private List<Post> postList;
    PostsAdapter postsAdapter;
    private String uid;
    private ActivityDetailProfileBinding binding;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setTitle("Profile");
//        actionBar.setDisplayShowHomeEnabled(true);
//        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        // get uid of clicked user to retrieve his posts
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");


        getAllInformationUser();
        loadHistPosts();
        postList = new ArrayList<>();
        postsAdapter = new PostsAdapter(this, postList);

        checkUserStatus();





        checkUserStatus();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void getAllInformationUser() {
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // check until required data get
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // get data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    // set data in profile
                    binding.tvName.setText(name);
                    binding.tvEmail.setText(email);
                    binding.tvPhone.setText(phone);
                    try {
                        Picasso.get().load(image).into(binding.ivAvatar);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_users).into(binding.ivAvatar);
                    }

                    try {
                        Picasso.get().load(cover).into(binding.ivCover);
                    } catch (Exception e) {

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadHistPosts() {
        // linealayout for rcv
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // show newest post first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        // set this layout to rcv
        binding.recyclerPosts.setLayoutManager(layoutManager);

        // init post list
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("uid").equalTo(uid);
        // get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);
                    // add to list
                    postList.add(post);


                    // set this adapter to rcv
                    binding.recyclerPosts.setAdapter(postsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailProfileActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchHistPosts(String searchQuery){

        // init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        // query to load posts
        Query query = ref.orderByChild("uid").equalTo(uid);
        // get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);

                    if (post.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            post.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        // add to list
                        postList.add(post);
                    }


                    // adapter
                    postsAdapter = new PostsAdapter(DetailProfileActivity.this, postList);
                    // set this adapter to rcv
                    binding.recyclerPosts.setAdapter(postsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailProfileActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // call when user press search button
                if(!TextUtils.isEmpty(query)){
                    // search
                    searchHistPosts(query);
                }else{
                    loadHistPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // called whenever user type any letter
                if(!TextUtils.isEmpty(newText)){
                    // search
                    searchHistPosts(newText);
                }else{
                    loadHistPosts();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }

    private void  checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            uid =  user.getUid();
        }else{
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}