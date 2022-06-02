package com.example.demoapp.view.dialog.log;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.demoapp.R;
import com.example.demoapp.databinding.FragmentInsertLogBinding;
import com.example.demoapp.model.Log;
import com.example.demoapp.utilities.Constants;
import com.example.demoapp.view.activity.LoginActivity;
import com.example.demoapp.viewmodel.CommunicateViewModel;
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

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InsertLogFragment extends DialogFragment implements View.OnClickListener{

    private static final int MY_REQUEST_CODE = 10;

    private final String[] itemsImportOrExport = {"Nhập Khẩu", "Xuất Khẩu"};

    private final String[] listStr = new String[3];

    private String type = "";

    private List<Log> logList;

    private FirebaseAuth mAuth;
    private DatabaseReference userDBRef;

    private ProgressDialog progressDialog;
    // user info
    String name, email, uid, dp;

    public static InsertLogFragment insertDiaLogLog(){
        return new InsertLogFragment();

    }
    private ArrayAdapter<String> adapterItemsMonth, adapterItemsImportAndExport, adapterItemsType;

    private FragmentInsertLogBinding logBinding;


    private Bitmap bitmap;

    private CommunicateViewModel mCommunicateViewModel;

    public static final String TAG = InsertLogFragment.class.getName();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        logBinding = FragmentInsertLogBinding.inflate(inflater,container, false);
        View view = logBinding.getRoot();

        mCommunicateViewModel = new ViewModelProvider(getActivity()).get(CommunicateViewModel.class);

        mAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        logList = new ArrayList<>();
        progressDialog = new ProgressDialog(getContext());

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

        initView();
        return view;
    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            email = user.getEmail();
            uid = user.getUid();
        } else {
            startActivity(new Intent(getContext(), LoginActivity.class));
            getActivity().finish();
        }
    }

    private void initView() {
        adapterItemsMonth = new ArrayAdapter<String>(getContext(), R.layout.dropdown_item, Constants.ITEMS_MONTH);
        adapterItemsImportAndExport = new ArrayAdapter<String>(getContext(), R.layout.dropdown_item, itemsImportOrExport);
        adapterItemsType = new ArrayAdapter<String>(getContext(), R.layout.dropdown_item, Constants.ITEMS_TYPE);

        logBinding.insertAutoMonth.setAdapter(adapterItemsMonth);
        logBinding.insertAutoShippingType.setAdapter(adapterItemsImportAndExport);
        logBinding.insertAutoLoaihinh.setAdapter(adapterItemsType);

        logBinding.btnFunctionAdd.setOnClickListener(this);
        logBinding.btnFunctionCancel.setOnClickListener(this);
//        logBinding.btnDinhkemhinhanh.setOnClickListener(this);

        logBinding.insertAutoMonth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listStr[0] = adapterView.getItemAtPosition(i).toString();
            }
        });

        logBinding.insertAutoShippingType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listStr[1] = adapterView.getItemAtPosition(i).toString();
            }
        });

        logBinding.insertAutoLoaihinh.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listStr[2] = adapterView.getItemAtPosition(i).toString();
            }
        });

        setCancelable(false);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_function_add:
                if(isFilled()) {
                    insertLog();
                    dismiss();
                }else{
                    Toast.makeText(getContext(), Constants.INSERT_FAILED, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_function_cancel:
                dismiss();
                break;
//            case R.id.btn_dinhkemhinhanh:
//                onClickRequestPermission();
        }
    }

    private String getCreatedDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    public boolean isFilled() {
        boolean result = true;


        if (TextUtils.isEmpty(logBinding.insertAutoShippingType.getText())) {
            result = false;
            logBinding.insertAutoShippingType.setError(Constants.ERROR_AUTO_COMPLETE_SHIPPING_TYPE);
        }
        if (TextUtils.isEmpty(logBinding.insertAutoLoaihinh.getText())) {
            result = false;
            logBinding.insertAutoLoaihinh.setError(Constants.ERROR_AUTO_COMPLETE_TYPE_LOG);
        }
        if (TextUtils.isEmpty(logBinding.insertAutoMonth.getText())) {
            result = false;
            logBinding.insertAutoMonth.setError(Constants.ERROR_AUTO_COMPLETE_MONTH);
        }

        return result;
    }


    private void insertLog() {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String strTenHang = logBinding.tfTenhang.getEditText().getText().toString();
        String strhscode = logBinding.tfHscode.getEditText().getText().toString();
        String strcongdung = logBinding.tfCongdung.getEditText().getText().toString();
        String strhinhanh = logBinding.tfHinhanh.getEditText().getText().toString();
        String strcangdi = logBinding.tfCangdi.getEditText().getText().toString();
        String strcangden = logBinding.tfCangden.getEditText().getText().toString();
        String strloaihang = logBinding.tfLoaihang.getEditText().getText().toString();
        String strsoluongcuthe = logBinding.tfSoluongcuthe.getEditText().getText().toString();
        String stryeucaudacbiet = logBinding.tfYeucaudacbiet.getEditText().getText().toString();
        String strPrice = logBinding.tfPrice.getEditText().getText().toString();


        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("tenhang", strTenHang);
        hashMap.put("hscode", strhscode);
        hashMap.put("congdung", strcongdung);
        hashMap.put("hinhanh", strhinhanh);
        hashMap.put("cangdi", strcangdi);
        hashMap.put("cangden", strcangden);
        hashMap.put("loaihang", strloaihang);
        hashMap.put("soluongcuthe", strsoluongcuthe);
        hashMap.put("yeucaudacbiet", stryeucaudacbiet);
        hashMap.put("price", strPrice);
        hashMap.put("importorexport", listStr[1] );
        hashMap.put("type", listStr[2]);
        hashMap.put("month", listStr[0]);
        hashMap.put("createdDate", getCreatedDate());
        hashMap.put("pTime", timeStamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LOG");
        // put data in this ref
        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }



    private void uploadImage(){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,75, byteArrayOutputStream);

        byte [] imageInByte = byteArrayOutputStream.toByteArray();

        String encodeImage = Base64.encodeToString(imageInByte, Base64.DEFAULT);

        Toast.makeText(getContext(), encodeImage,Toast.LENGTH_LONG).show();

    }
}