package com.example.demoapp.view.activity.chat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.demoapp.R;
import com.example.demoapp.databinding.ActivityAddPostBinding;
import com.example.demoapp.view.activity.LoginActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    private ActivityAddPostBinding binding;
    private ActionBar actionBar;
    private FirebaseAuth mAuth;
    private DatabaseReference userDBRef;

    // user info
    String name, email, uid, dp;

    // permission constanst
    public static final int CAMERA_REQUEST_CODE = 100;
    public static final int STORAGE_REQUEST_CODE = 200;
    public static final int IMAGE_PICK_GALLERY_CODE = 300;
    public static final int IMAGE_PICK_CAMERA_CODE = 400;
    // permission array
    String[] cameraPermissions;
    String[] storagePermissions;

    // info of post to be edited
    private String editTitle, editDescription, editImage;

    private Uri image_uri = null;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");

        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setSubtitle(email);

        // init  permissions arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        // get data through intent form previous activities adapter
        Intent intent = getIntent();
        String isUpdateKey = "" + intent.getStringExtra("key");
        String editPostId = "" + intent.getStringExtra("editPostId");
        // validate if we came here to update post i.e camera from AdapterPost
        if (isUpdateKey.equals("editPost")) {
            // Update
            actionBar.setTitle("Update Post");
            binding.btnUpload.setText("Update");
            loadPostData(editPostId);
        } else {
            // add
            actionBar.setTitle("Add new Post");
            binding.btnUpload.setText("Upload");
        }

        // get some info of current user to include in post
        userDBRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDBRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    name = "" + ds.child("name").getValue();
                    email = "" + ds.child("email").getValue();
                    dp = "" + ds.child("image").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Get image from camera/ gallery on click
        binding.imgIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show image pick dialog
                showImagePickDialog();
            }
        });

        // upload button click listener
        binding.btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get data from edittexts
                String title = binding.edtTitle.getText().toString().trim();
                String description = binding.edtDescription.getText().toString().trim();

                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(AddPostActivity.this, "Enter title...", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(description)) {
                    Toast.makeText(AddPostActivity.this, "Enter description...", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isUpdateKey.equals("editPost")) {
                    beginUpdate(title, description, editPostId);
                } else {
                    uploadData(title, description);
                }

            }
        });

    }

    private void beginUpdate(String title, String description, String editPostId) {
        progressDialog.setMessage("Updating Post...");
        progressDialog.show();
        if (!editImage.equals("noImage")) {
            //without image
            updateWasWithoutImage(title, description, editPostId);
        } else if(binding.imgIv.getDrawable() != null){
            //with image
            updateWithoutNowImage(title, description, editPostId);
        } else{
            // without image
            updateWithoutImage(title, description, editPostId);
        }
    }

    private void updateWithoutImage(String title, String description, String editPostId) {
        HashMap<String, Object> hashMap = new HashMap<>();
        // put post info
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("uDp", dp);
        hashMap.put("pTitle", title);
        hashMap.put("pDescrip", description);
        hashMap.put("pImage", "noImage");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(editPostId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, "Updated....", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateWithoutNowImage(String title, String description, String editPostId) {
        // image deleted, upload new image
        // for post-image name, post-id, publish-time
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + timeStamp;
        // get image from imageview
        Bitmap bitmap = ((BitmapDrawable) binding.imgIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // image compress
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // image uploaded get its url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()){
                            String downloadUri = uriTask.getResult().toString();
                            if(uriTask.isSuccessful()){
                                // url is recieved, upload to firebase database
                                HashMap<String, Object> hashMap = new HashMap<>();
                                // put post info
                                hashMap.put("uid", uid);
                                hashMap.put("uName", name);
                                hashMap.put("uEmail", email);
                                hashMap.put("uDp", dp);
                                hashMap.put("pTitle", title);
                                hashMap.put("pDescrip", description);
                                hashMap.put("pImage", downloadUri);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                ref.child(editPostId)
                                        .updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog.dismiss();
                                                Toast.makeText(AddPostActivity.this, "Updated....", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void updateWasWithoutImage(String title, String description, String editPostId) {
        // post is with image, delete previous image first
        StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPictureRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // image deleted, upload new image
                        // for post-image name, post-id, publish-time
                        String timeStamp = String.valueOf(System.currentTimeMillis());
                        String filePathAndName = "Posts/" + "post_" + timeStamp;
                            // get image from imageview
                            Bitmap bitmap = ((BitmapDrawable) binding.imgIv.getDrawable()).getBitmap();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            // image compress
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                            byte[] data = baos.toByteArray();

                            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                            ref.putBytes(data)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            // image uploaded get its url
                                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                            while (!uriTask.isSuccessful()){
                                                String downloadUri = uriTask.getResult().toString();
                                                if(uriTask.isSuccessful()){
                                                    // url is recieved, upload to firebase database
                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                    // put post info
                                                    hashMap.put("uid", uid);
                                                    hashMap.put("uName", name);
                                                    hashMap.put("uEmail", email);
                                                    hashMap.put("uDp", dp);
                                                    hashMap.put("pTitle", title);
                                                    hashMap.put("pDescrip", description);
                                                    hashMap.put("pImage", downloadUri);

                                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                                    ref.child(editPostId)
                                                            .updateChildren(hashMap)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(AddPostActivity.this, "Updated....", Toast.LENGTH_SHORT).show();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure (@NonNull Exception e){
                            // image not uploaded
                            progressDialog.dismiss();
                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

        private void loadPostData (String editPostId){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
            // get detail of post using id of post
            Query fQuery = reference.orderByChild("pId").equalTo(editPostId);
            fQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        // get data
                        editTitle = "" + ds.child("pTitle").getValue();
                        editDescription = "" + ds.child("pDescrip").getValue();
                        editImage = "" + ds.child("pImage").getValue();

                        // set data to views
                        binding.edtTitle.setText(editTitle);
                        binding.edtDescription.setText(editDescription);

                        // set image
                        if (!editImage.equals("noImage")) {
                            try {
                                Picasso.get().load(editImage).into(binding.imgIv);
                            } catch (Exception e) {

                            }
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        private void uploadData (String title, String description){
            progressDialog.setMessage("Publishing post...");
            progressDialog.show();
            // for post-image name, post-id, post-publish-time
            String timeStamp = String.valueOf(System.currentTimeMillis());
            String filePathAndName = "Posts/" + "post_" + timeStamp;
            if (binding.imgIv.getDrawable() != null) {
                // get image from imageview
                Bitmap bitmap = ((BitmapDrawable) binding.imgIv.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // image compress
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] data = baos.toByteArray();
                // post with image
                StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                ref.putBytes(data)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // image is uploaded to firebase storage, now get it's url
                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                                while (!uriTask.isSuccessful()) ;

                                String downloadUri = uriTask.getResult().toString();

                                if (uriTask.isSuccessful()) {
                                    // url is received upload post to firebase database
                                    HashMap<Object, String> hashMap = new HashMap<>();
                                    // put post  info
                                    hashMap.put("uid", uid);
                                    hashMap.put("uName", name);
                                    hashMap.put("uEmail", email);
                                    hashMap.put("uDp", dp);
                                    hashMap.put("pId", timeStamp);
                                    hashMap.put("pTitle", title);
                                    hashMap.put("pDescrip", description);
                                    hashMap.put("pImage", downloadUri);
                                    hashMap.put("pTime", timeStamp);
                                    hashMap.put("pLikes", "0");
                                    hashMap.put("pComments", "0");

                                    // path to store post data
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                    // put data in this ref
                                    ref.child(timeStamp).setValue(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    // added in database
                                                    progressDialog.dismiss();
                                                    Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                                    // reset views
                                                    binding.edtTitle.setText("");
                                                    binding.edtDescription.setText("");
                                                    binding.imgIv.setImageURI(null);
                                                    image_uri = null;

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // failed adding post in database
                                                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // failed uploading image
                                progressDialog.dismiss();
                                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // post with image
                HashMap<Object, String> hashMap = new HashMap<>();
                // put post  info
                hashMap.put("uid", uid);
                hashMap.put("uName", name);
                hashMap.put("uEmail", email);
                hashMap.put("uDp", dp);
                hashMap.put("pId", timeStamp);
                hashMap.put("pTitle", title);
                hashMap.put("pDescrip", description);
                hashMap.put("pImage", "noImage");
                hashMap.put("pTime", timeStamp);

                // path to store post data
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                // put data in this ref
                ref.child(timeStamp).setValue(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // added in database
                                progressDialog.dismiss();
                                Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                                // reset views
                                binding.edtTitle.setText("");
                                binding.edtDescription.setText("");
                                binding.imgIv.setImageURI(null);
                                image_uri = null;

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // failed adding post in database
                                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        }

        private void showImagePickDialog () {
            // option (Camera, gallery) to show in dialog
            String[] option = {"Camera", "Gallery"};

            // dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Choose Image From");
            // set option to dialog
            builder.setItems(option, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // item click handle
                    if (which == 0) {
                        // camera clicked
                        if (!checkCameraPermissions()) {
                            requestCameraPermission();
                        } else {
                            pickFromCamera();
                        }
                    }
                    if (which == 1) {
                        // gallery clicked
                        if (!checkStoragePermissions()) {
                            requestStoragePermission();
                        } else {
                            pickFromGallery();
                        }
                    }
                }
            });
            // create and show dialog
            builder.create().show();
        }

        private void pickFromGallery () {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
        }

        private void pickFromCamera () {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "Temp Pick");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

            image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
            startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
        }

        @Override
        public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults){

            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            switch (requestCode) {
                case CAMERA_REQUEST_CODE: {
                    if (grantResults.length > 0) {
                        boolean isCameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                        boolean isWriteStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                        if (isCameraAccepted && isWriteStorageAccepted) {
                            pickFromCamera();
                        } else {
                            Toast.makeText(this, "Please enable camera & storage permisstion", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
                case STORAGE_REQUEST_CODE: {
                    if (grantResults.length > 0) {
                        boolean isWriteStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                        if (isWriteStorageAccepted) {
                            pickFromGallery();
                        } else {
                            Toast.makeText(this, "Please enable storage permisstion", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;

            }


        }

        private boolean checkStoragePermissions () {
            // check if storage permission is enabled or not
            // return true if enabled
            // return fales if not enabled
            boolean result = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
            return result;
        }

        private void requestStoragePermission () {
            //request runtime storage permission
            ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
        }

        private boolean checkCameraPermissions () {
            // check if camera permission is enabled or not
            // return true if enabled
            // return fales if not enabled
            boolean result = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
            boolean result1 = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
            return result && result1;
        }

        private void requestCameraPermission () {
            //request runtime camera permission
            ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
        }

        @Override
        protected void onStart () {
            checkUserStatus();
            super.onStart();
        }

        @Override
        protected void onResume () {
            super.onResume();
            checkUserStatus();
        }

        @Override
        public boolean onSupportNavigateUp () {
            onBackPressed();
            return super.onSupportNavigateUp();
        }

        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            getMenuInflater().inflate(R.menu.menu_main, menu);
            menu.findItem(R.id.action_add_post).setVisible(false);
            menu.findItem(R.id.action_search).setVisible(false);
            return super.onCreateOptionsMenu(menu);
        }

        @Override
        public boolean onOptionsItemSelected (@NonNull MenuItem item){
            int id = item.getItemId();
            if (id == R.id.action_logout) {
                mAuth.signOut();
                checkUserStatus();
            }
            return super.onOptionsItemSelected(item);
        }

        private void checkUserStatus () {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                email = user.getEmail();
                uid = user.getUid();
            } else {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        }

        @Override
        public void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){

            if (resultCode == RESULT_OK) {
                if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                    image_uri = data.getData();

                    binding.imgIv.setImageURI(image_uri);

                }
                if (requestCode == IMAGE_PICK_CAMERA_CODE) {

                }
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }