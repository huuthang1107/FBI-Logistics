package com.example.demoapp.view.dialog.air.retailgoods;

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
import com.example.demoapp.databinding.FragmentInsertRetailGoodsDialogBinding;
import com.example.demoapp.model.RetailGoods;
import com.example.demoapp.utilities.Constants;
import com.example.demoapp.view.activity.LoginActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;


public class InsertRetailGoodsDialog extends DialogFragment implements View.OnClickListener {
    private final String[] listCM = new String[2];
    private FragmentInsertRetailGoodsDialogBinding mRetailGoodsDialogBinding;
    private ArrayAdapter<String> adapterItemsMonth, adapterItemsContinent;
    private List<RetailGoods> retailGoods;

    private FirebaseAuth mAuth;
    private DatabaseReference userDBRef;

    private ProgressDialog progressDialog;
    // user info
    String name, email, uid, dp;

    public static  InsertRetailGoodsDialog insertDialogRetailGoods(){
        return  new InsertRetailGoodsDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRetailGoodsDialogBinding = FragmentInsertRetailGoodsDialogBinding.inflate(inflater, container, false);
        View view = mRetailGoodsDialogBinding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        retailGoods = new ArrayList<>();
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
        setUpButtons();
        showDatePicker();
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

    private String getCreatedDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }
    public void showDatePicker() {

        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select date");

        final MaterialDatePicker<Long> materialDatePicker = builder.build();

        mRetailGoodsDialogBinding.edtValid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDatePicker.show(getParentFragmentManager(), "Date_Picker");
                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override
                    public void onPositiveButtonClick(Long selection) {

                        TimeZone timeZoneUTC = TimeZone.getDefault();
                        // It will be negative, so that's the -1
                        int offsetFromUTC = timeZoneUTC.getOffset(new Date().getTime()) * -1;
                        // Create a date format, then a date object with our offset
                        SimpleDateFormat simpleFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                        Date date = new Date(selection + offsetFromUTC);

                        Objects.requireNonNull(mRetailGoodsDialogBinding.tfValidRetailGoods.getEditText()).setText(simpleFormat.format(date));
                    }
                });
            }
        });

    }

    private void setUpButtons() {
        mRetailGoodsDialogBinding.btnFunctionAddRetailGoods.setOnClickListener(this);
        mRetailGoodsDialogBinding.btnFunctionCancelRetailGoods.setOnClickListener(this);
    }

    private void initView() {
        adapterItemsMonth = new ArrayAdapter<String>(getContext(), R.layout.dropdown_item, Constants.ITEMS_MONTH);
        adapterItemsContinent = new ArrayAdapter<String>(getContext(), R.layout.dropdown_item, Constants.ITEMS_CONTINENT);

        mRetailGoodsDialogBinding.insertAutoContinent.setAdapter(adapterItemsContinent);
        mRetailGoodsDialogBinding.insertAutoMonth.setAdapter(adapterItemsMonth);

        mRetailGoodsDialogBinding.insertAutoMonth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listCM[0] = adapterView.getItemAtPosition(i).toString();
            }
        });

        mRetailGoodsDialogBinding.insertAutoContinent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listCM[1] = adapterView.getItemAtPosition(i).toString();
            }
        });

        setCancelable(false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_function_add_retail_goods:
                if(isFilled()) {
                    insertRetailGoods();
                    dismiss();
                } else{
                    Toast.makeText(getContext(), Constants.INSERT_FAILED, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_function_cancel_retail_goods:
                dismiss();
                break;
        }
    }

    public boolean isFilled() {
        boolean result = true;

        if (TextUtils.isEmpty(mRetailGoodsDialogBinding.insertAutoContinent.getText())) {
            result = false;
            mRetailGoodsDialogBinding.insertAutoContinent.setError(Constants.ERROR_AUTO_COMPLETE_CONTINENT);
        }

        if (TextUtils.isEmpty(mRetailGoodsDialogBinding.insertAutoMonth.getText())) {
            result = false;
            mRetailGoodsDialogBinding.insertAutoMonth.setError(Constants.ERROR_AUTO_COMPLETE_MONTH);
        }


        if (TextUtils.isEmpty(Objects.requireNonNull(mRetailGoodsDialogBinding.tfPolRetailGoods.getEditText()).getText().toString())) {
            result = false;
            mRetailGoodsDialogBinding.tfPolRetailGoods.setError(Constants.ERROR_POL);
        }
        if (TextUtils.isEmpty(Objects.requireNonNull(mRetailGoodsDialogBinding.tfPodRetailGoods.getEditText()).getText().toString())) {
            result = false;
            mRetailGoodsDialogBinding.tfPodRetailGoods.setError(Constants.ERROR_POD);
        }

        if (TextUtils.isEmpty(Objects.requireNonNull(mRetailGoodsDialogBinding.tfValidRetailGoods.getEditText()).getText().toString())) {
            result = false;
            mRetailGoodsDialogBinding.tfValidRetailGoods.setError(Constants.ERROR_VALID);
        }

        return result;
    }

    private void insertRetailGoods() {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String strPol = mRetailGoodsDialogBinding.tfPolRetailGoods.getEditText().getText().toString();
        String strPod = mRetailGoodsDialogBinding.tfPodRetailGoods.getEditText().getText().toString();
        String strDim = mRetailGoodsDialogBinding.tfDimRetailGoods.getEditText().getText().toString();
        String strGross = mRetailGoodsDialogBinding.tfGrossRetailGoods.getEditText().getText().toString();
        String strType = mRetailGoodsDialogBinding.tfTypeofcargoRetailGoods.getEditText().getText().toString();
        String strOceanFreight = mRetailGoodsDialogBinding.tfOceanAirfreightRetailGoods.getEditText().getText().toString();
        String strLocalCharge = mRetailGoodsDialogBinding.tfLocalchargeAirImport.getEditText().getText().toString();
        String strCarrier = mRetailGoodsDialogBinding.tfCarrierRetailGoods.getEditText().getText().toString();
        String strSchedule = mRetailGoodsDialogBinding.tfScheduleRetailGoods.getEditText().getText().toString();
        String strTransittime = mRetailGoodsDialogBinding.tfTfTransitTimeRetailGoods.getEditText().getText().toString();
        String strValid = mRetailGoodsDialogBinding.tfValidRetailGoods.getEditText().getText().toString();
        String strNotes = mRetailGoodsDialogBinding.tfNotesRetailGoods.getEditText().getText().toString();


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("pol", strPol);
        hashMap.put("pod", strPod);
        hashMap.put("dim", strDim);
        hashMap.put("grossweight", strGross);
        hashMap.put("typeofcargo", strType);
        hashMap.put("oceanfreight", strOceanFreight);
        hashMap.put("localcharge", strLocalCharge);
        hashMap.put("carrier", strCarrier);
        hashMap.put("schedule", strSchedule);
        hashMap.put("transittime", strTransittime);
        hashMap.put("valid", strValid);
        hashMap.put("note", strNotes);
        hashMap.put("month", listCM[0]);
        hashMap.put("continent", listCM[1]);
        hashMap.put("date_created", getCreatedDate());
        hashMap.put("pTime", timeStamp);
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Retail_Goods_Air");
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