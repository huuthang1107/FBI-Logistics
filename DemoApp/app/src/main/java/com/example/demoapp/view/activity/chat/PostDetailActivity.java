package com.example.demoapp.view.activity.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.R;
import com.example.demoapp.adapter.chat.CommentsAdapter;
import com.example.demoapp.model.Comment;
import com.example.demoapp.view.activity.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    // To get detail of user and post
    String  hisUid, myUid, myEmail, myName, myDp,
            postId, pLikes, hisDp, hisName, pImage;

    //progress bar
    ProgressDialog progressDialog;
    boolean mProcessComment = false;
    boolean mProcessLike = false;


    // view
    ImageView uPictureIv, pImageIv;
    TextView uNameTv, pTimeTiv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
    ImageButton moreBtn;
    Button likeBtn, shareBtn;
    LinearLayout profileLayout;
    RecyclerView recyclerView;

    List<Comment> commentList;
    CommentsAdapter commentsAdapter;

    // add comment views
    EditText commentEt;
    ImageButton senBtn;
    ImageView cAvatarIv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        //Actionbar and its properties
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Detail");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // get id of post using intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        //intview
        initViews();

        loadPostInfo();
        checkUserStatus();
        loadUserInfo();

        setLikes();
        // set subtitle of actionbar
        actionBar.setSubtitle("SignedIn as: " + myEmail);

        loadComments();

        // send comment button click
        senBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        // like button click handle
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        // more button click handle
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });
    }

    private void loadComments() {
        //layout for rcv
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        // set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);

        // init comments list
        commentList  = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId)
                .child("Comments");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Comment comment = ds.getValue(Comment.class);

                    commentList.add(comment);

                    // pass myUid and postId as parameter of constructor of Comment Adapter

                    //set up adapter
                    commentsAdapter = new CommentsAdapter(getApplicationContext(), commentList, myUid, postId);

                    // set adapter
                    recyclerView.setAdapter(commentsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions() {
        // creating popup menu currently having option Delete, we will add more options later
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);

        // show delete option in only post(s) of currently signed in user

        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");



        // item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    // delete is clicked
                    beginDelete();
                } else if (id == 1) {
                    // edit is click
                    // start AddPostActivity with key "editpost" and the id of the post clicked
                    Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", postId);
                    startActivity(intent);

                }
                return false;
            }
        });
        // show menu
        popupMenu.show();
    }

    private void beginDelete() {
        // post can be with or without image
        if (pImage.equals("noImage")) {
            // post is without image
            deleteWithoutImage();

        } else {
            // post is with image
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        // progress bar
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");

        /*
            1. Delete Image using url
            2. Delete from database using post id
         */

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // image deleted, now delete database
                        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    ds.getRef().removeValue(); // remove values from firebase where pid matches

                                }
                                // deleted
                                Toast.makeText(PostDetailActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // failed, can't go further
                        progressDialog.dismiss();
                        Toast.makeText(PostDetailActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteWithoutImage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting...");

        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ds.getRef().removeValue(); // remove values from firebase where pid matches

                }
                // deleted
                Toast.makeText(PostDetailActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setLikes() {
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postId).hasChild(myUid)) {
                    // user has liked this post
                    likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0);
                    likeBtn.setText("Liked");
                } else {
                    // user has not liked this post
                    likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                    likeBtn.setText("Liked");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void likePost() {
        mProcessLike = true;
        // get id of the post clicked
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessLike) {
                    if (snapshot.child(postId).hasChild(myUid)) {
                        // already liked, so remove like
                        postRef.child(postId).child("pLikes").setValue("" + (Integer.parseInt(pLikes) - 1));
                        likesRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false;
                    } else {
                        // not liked, like it
                        postRef.child(postId).child("pLikes").setValue("" + (Integer.parseInt(pLikes) + 1));
                        likesRef.child(postId).child(myUid).setValue("Liked");
                        mProcessLike = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void postComment() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding comment...");

        //get data from commnet edit text
        String comment = commentEt.getText().toString().trim();
        // validate
        if (TextUtils.isEmpty(comment)) {
            // no value is entered
            Toast.makeText(this, "Comment is empty...", Toast.LENGTH_SHORT).show();
            return;
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());
        // each post will have a child "Comments" that will contain comments of that post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap<String, Object> hashMap = new HashMap<>();
        // put info in hashmap
        hashMap.put("cId", timeStamp);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uDp", myDp);
        hashMap.put("uName", myName);

        // put this data in db
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // added
                        progressDialog.dismiss();
                        Toast.makeText(PostDetailActivity.this, "Comment Added... ", Toast.LENGTH_SHORT).show();
                        commentEt.setText("");
                        updateCommentCount();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // failed, not added
                        progressDialog.dismiss();
                        Toast.makeText(PostDetailActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }


    private void updateCommentCount() {
        //whenever user adds comment increase the comment count as we did for like count
        mProcessComment = true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessComment) {
                    String comments = "" + snapshot.child("pComments").getValue();
                    int newCommentVal = Integer.parseInt(comments) + 1;
                    ref.child("pComments").setValue("" + newCommentVal);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadUserInfo() {
        // get current user info
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    myName = "" + ds.child("name").getValue();
                    myDp = "" + ds.child("image").getValue();

                    // set data
                    try {
                        // if image is received then set
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_face).into(cAvatarIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_face).into(cAvatarIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPostInfo() {
        // get post using the id of the post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // keep checking the post until get the required post
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // get data
                    String pTitle = "" + ds.child("pTitle").getValue();
                    String pDescr = "" + ds.child("pDescrip").getValue();
                    pLikes = "" + ds.child("pLikes").getValue();
                    String pTimeStamp = "" + ds.child("pTime").getValue();
                    pImage = "" + ds.child("pImage").getValue();
                    hisDp = "" + ds.child("uDp").getValue();
                    hisUid = "" + ds.child("uid").getValue();
                    String uEmail = "" + ds.child("uEmail").getValue();
                    hisName = "" + ds.child("uName").getValue();
                    String commentCount = "" + ds.child("pComments").getValue();

                    // convert timestamp to dd/mm/yyyy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy, HH:mm aa", calendar).toString();

                    //set data
                    pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDescr);
                    pLikesTv.setText(pLikes + "Likes");
                    pTimeTiv.setText(pTime);
                    pCommentsTv.setText(commentCount + " Comments");

                    uNameTv.setText(hisName);

                    // set image of the user who posted
                    if (pImage.equals("noImage")) {
                        pImageIv.setVisibility(View.GONE);
                    } else {
                        // show imageview
                        pImageIv.setVisibility(View.VISIBLE);
                        try {
                            Picasso.get().load(pImage).into(pImageIv);
                        } catch (Exception e) {

                        }
                    }

                    // set user image in comment part
                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_face).into(uPictureIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_face).into(uPictureIv);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // user is signed in
            myEmail = user.getEmail();
            myUid = user.getUid();

        } else {
            // user not signed in, go to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // hide some menu items
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // get item id
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        uPictureIv = findViewById(R.id.uPictureIv);
        pImageIv = findViewById(R.id.pImageIv);
        uNameTv = findViewById(R.id.tv_uName);
        pTimeTiv = findViewById(R.id.tv_pTime);
        pTitleTv = findViewById(R.id.tv_pTitle);
        pLikesTv = findViewById(R.id.tv_pLikes);
        pCommentsTv = findViewById(R.id.tv_pComments);
        pDescriptionTv = findViewById(R.id.tv_pDescription);
        moreBtn = findViewById(R.id.btn_more);
        likeBtn = findViewById(R.id.btn_like);
        shareBtn = findViewById(R.id.btn_share);
        profileLayout = findViewById(R.id.profile_layout);
        recyclerView = findViewById(R.id.recyclerView);

        commentEt = findViewById(R.id.et_comment);
        senBtn = findViewById(R.id.btn_send);
        cAvatarIv = findViewById(R.id.iv_cAvatar);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}