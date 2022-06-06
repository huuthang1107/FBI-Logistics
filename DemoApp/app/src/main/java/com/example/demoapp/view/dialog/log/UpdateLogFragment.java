package com.example.demoapp.view.dialog.log;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.demoapp.R;
import com.example.demoapp.databinding.FragmentUpdateLogBinding;
import com.example.demoapp.model.Log;
import com.example.demoapp.utilities.Constants;
import com.example.demoapp.view.activity.LoginActivity;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class UpdateLogFragment extends DialogFragment implements View.OnClickListener {

    private FragmentUpdateLogBinding mLogBinding;
    private String[] listPriceLog = new String[3];
    private Bundle mBundle;
    private Log mLog;
    private List<Log> logList;

    private FirebaseAuth mAuth;
    private DatabaseReference userDBRef;

    private ProgressDialog progressDialog;
    // user info
    String name, email, uid, dp;


    public static UpdateLogFragment getInstance(){
        return new UpdateLogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mLogBinding = FragmentUpdateLogBinding.inflate(inflater, container, false);
        View view = mLogBinding.getRoot();


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
        mBundle = getArguments();
        updateInformationLog();
        unit();
        setUpButtons();
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

    private void setUpButtons() {
        mLogBinding.btnFunctionAddLog.setOnClickListener(this);
        mLogBinding.btnFunctionUpdateLog.setOnClickListener(this);
        mLogBinding.btnFunctionCancelLog.setOnClickListener(this);
    }
    private String getCreatedDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    public boolean isFilled() {
        boolean result = true;


        if (TextUtils.isEmpty(mLogBinding.insertAutoImportorexport.getText())) {
            result = false;
            mLogBinding.insertAutoImportorexport.setError(Constants.ERROR_AUTO_COMPLETE_SHIPPING_TYPE);
        }
        if (TextUtils.isEmpty(mLogBinding.insertAutoLoaihinh.getText())) {
            result = false;
            mLogBinding.insertAutoLoaihinh.setError(Constants.ERROR_AUTO_COMPLETE_TYPE_LOG);
        }
        if (TextUtils.isEmpty(mLogBinding.insertAutoMonth.getText())) {
            result = false;
            mLogBinding.insertAutoMonth.setError(Constants.ERROR_AUTO_COMPLETE_MONTH);
        }

        return result;
    }

    private void unit() {
        ArrayAdapter<String> arrayAdapterItemsMonth = new ArrayAdapter<String>(getContext(), R.layout.dropdown_item, Constants.ITEMS_MONTH);
        ArrayAdapter<String> arrayAdapterItemsImportOrExport = new ArrayAdapter<String>(getContext(), R.layout.dropdown_item, Constants.ITEMS_IMPORTANDEXPORT);
        ArrayAdapter<String> arrayAdapterItemsType = new ArrayAdapter<String>(getContext(), R.layout.dropdown_item, Constants.ITEMS_TYPE);

        mLogBinding.insertAutoMonth.setAdapter(arrayAdapterItemsMonth);
        mLogBinding.insertAutoImportorexport.setAdapter(arrayAdapterItemsImportOrExport);
        mLogBinding.insertAutoLoaihinh.setAdapter(arrayAdapterItemsType);

        listPriceLog[0] = mLogBinding.insertAutoMonth.getText().toString();
        listPriceLog[1] = mLogBinding.insertAutoImportorexport.getText().toString();
        listPriceLog[2] = mLogBinding.insertAutoLoaihinh.getText().toString();



        mLogBinding.insertAutoMonth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listPriceLog[0] = adapterView.getItemAtPosition(i).toString();
            }
        });

        mLogBinding.insertAutoImportorexport.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listPriceLog[1] = adapterView.getItemAtPosition(i).toString();
            }
        });

        mLogBinding.insertAutoLoaihinh.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listPriceLog[2] = adapterView.getItemAtPosition(i).toString();
            }
        });
        setCancelable(false);
    }

    private void updateInformationLog() {
        if(mBundle != null){
            mLog = (Log) mBundle.getSerializable(Constants.LOG_UPDATE);
            mLogBinding.insertAutoMonth.setText(mLog.getMonth());
            mLogBinding.insertAutoImportorexport.setText(mLog.getImportorexport());
            mLogBinding.insertAutoLoaihinh.setText(mLog.getType());
            Objects.requireNonNull(mLogBinding.tfTenhang.getEditText()).setText(mLog.getTenhang());
            Objects.requireNonNull(mLogBinding.tfHscode.getEditText()).setText(mLog.getHscode());
            Objects.requireNonNull(mLogBinding.tfCongdung.getEditText()).setText(mLog.getCongdung());
            Objects.requireNonNull(mLogBinding.tfHinhanh.getEditText()).setText(mLog.getHinhanh());
            Objects.requireNonNull(mLogBinding.tfCangdi.getEditText()).setText(mLog.getCangdi());
            Objects.requireNonNull(mLogBinding.tfCangden.getEditText()).setText(mLog.getCangden());
            Objects.requireNonNull(mLogBinding.tfLoaihang.getEditText()).setText(mLog.getLoaihang());
            Objects.requireNonNull(mLogBinding.tfSoluongcuthe.getEditText()).setText(mLog.getSoluongcuthe());
            Objects.requireNonNull(mLogBinding.tfYeucaudacbiet.getEditText()).setText(mLog.getYeucaudacbiet());
            Objects.requireNonNull(mLogBinding.tfPrice.getEditText()).setText(mLog.getPrice());



        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_function_add_log:
                insertLog();
                dismiss();
                break;
            case R.id.btn_function_update_log:
                updateLog();
                dismiss();
                break;
            case R.id.btn_function_cancel_log:
                dismiss();
                break;
        }
    }

    private void updateLog() {
        String strTenHang = Objects.requireNonNull(mLogBinding.tfTenhang.getEditText()).getText().toString();
        String strHSCode = Objects.requireNonNull(mLogBinding.tfHscode.getEditText()).getText().toString();
        String strCondung = Objects.requireNonNull(mLogBinding.tfCongdung.getEditText()).getText().toString();
        String strHinhAnh = Objects.requireNonNull(mLogBinding.tfHinhanh.getEditText()).getText().toString();
        String strCangDi = Objects.requireNonNull(mLogBinding.tfCangdi.getEditText()).getText().toString();
        String strCangDen = Objects.requireNonNull(mLogBinding.tfCangden.getEditText()).getText().toString();
        String strLoaiHang = Objects.requireNonNull(mLogBinding.tfLoaihang.getEditText()).getText().toString();
        String strSoLuongCuThe = Objects.requireNonNull(mLogBinding.tfSoluongcuthe.getEditText()).getText().toString();
        String strYeuCauDacBiet = Objects.requireNonNull(mLogBinding.tfYeucaudacbiet.getEditText()).getText().toString();
        String strPrice = Objects.requireNonNull(mLogBinding.tfPrice.getEditText()).getText().toString();
        String timeStamp = mLog.getpTime();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("tenhang", strTenHang);
        hashMap.put("hscode", strHSCode);
        hashMap.put("congdung", strCondung);
        hashMap.put("hinhanh", strHinhAnh);
        hashMap.put("cangdi", strCangDi);
        hashMap.put("cangden", strCangDen);
        hashMap.put("loaihang", strLoaiHang);
        hashMap.put("soluongcuthe", strSoLuongCuThe);
        hashMap.put("yeucaudacbiet", strYeuCauDacBiet);
        hashMap.put("price", strPrice);
        hashMap.put("importorexport", listPriceLog[1] );
        hashMap.put("type", listPriceLog[2]);
        hashMap.put("month", listPriceLog[0]);
        hashMap.put("createdDate", getCreatedDate());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LOG");
        ref.child(timeStamp)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void insertLog() {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String strTenHang = mLogBinding.tfTenhang.getEditText().getText().toString();
        String strHSCode = mLogBinding.tfHscode.getEditText().getText().toString();
        String strCondung = mLogBinding.tfCongdung.getEditText().getText().toString();
        String strHinhAnh = mLogBinding.tfHinhanh.getEditText().getText().toString();
        String strCangDi = mLogBinding.tfCangdi.getEditText().getText().toString();
        String strCangDen = mLogBinding.tfCangden.getEditText().getText().toString();
        String strLoaiHang = mLogBinding.tfLoaihang.getEditText().getText().toString();
        String strSoLuongCuThe = mLogBinding.tfSoluongcuthe.getEditText().getText().toString();
        String strYeuCauDacBiet = mLogBinding.tfYeucaudacbiet.getEditText().getText().toString();
        String strPrice = mLogBinding.tfPrice.getEditText().getText().toString();


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("tenhang", strTenHang);
        hashMap.put("hscode", strHSCode);
        hashMap.put("congdung", strCondung);
        hashMap.put("hinhanh", strHinhAnh);
        hashMap.put("cangdi", strCangDi);
        hashMap.put("cangden", strCangDen);
        hashMap.put("loaihang", strLoaiHang);
        hashMap.put("soluongcuthe", strSoLuongCuThe);
        hashMap.put("yeucaudacbiet", strYeuCauDacBiet);
        hashMap.put("price", strPrice);
        hashMap.put("importorexport", listPriceLog[1] );
        hashMap.put("type", listPriceLog[2]);
        hashMap.put("month", listPriceLog[0]);
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
}