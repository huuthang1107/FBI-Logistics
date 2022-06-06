package com.example.demoapp.view.dialog.air.retailgoods;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.demoapp.R;
import com.example.demoapp.databinding.FragmentUpdateRetailGoodsDialogBinding;
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


public class UpdateRetailGoodsDialog extends DialogFragment implements View.OnClickListener {
    private FragmentUpdateRetailGoodsDialogBinding mRetailGoodsDialogBinding;
    private final String[] listPriceRetailGoods = new String[2];
    private Bundle mBundle;
    private RetailGoods mRetailGoods;
    private FirebaseAuth mAuth;
    private DatabaseReference userDBRef;
    private List<RetailGoods> retailGoodsList;

    private ProgressDialog progressDialog;
    // user info
    String name, email, uid, dp;


    public static UpdateRetailGoodsDialog getInstance(){
        return new UpdateRetailGoodsDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRetailGoodsDialogBinding = FragmentUpdateRetailGoodsDialogBinding.inflate(inflater, container, false);
        View view = mRetailGoodsDialogBinding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        retailGoodsList = new ArrayList<>();
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
        updateInformationRetailGoods();
        unit();
        showDatePicker();
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
        mRetailGoodsDialogBinding.btnFunctionUpdateRetailGoods.setOnClickListener(this);
        mRetailGoodsDialogBinding.btnFunctionInsertRetailGoods.setOnClickListener(this);
        mRetailGoodsDialogBinding.btnFunctionCancelRetailGoods.setOnClickListener(this);
    }

    private void unit() {
        ArrayAdapter<String> arrayAdapterItemsMonth = new ArrayAdapter<String>(getContext(),
                R.layout.dropdown_item, Constants.ITEMS_MONTH);
        ArrayAdapter<String> arrayAdapterItemsContinent = new ArrayAdapter<String>(getContext(),
                R.layout.dropdown_item,Constants.ITEMS_CONTINENT);

        mRetailGoodsDialogBinding.insertAutoContinent.setAdapter(arrayAdapterItemsContinent);
        mRetailGoodsDialogBinding.insertAutoMonth.setAdapter(arrayAdapterItemsMonth);

        listPriceRetailGoods[0] = mRetailGoodsDialogBinding.insertAutoMonth.getText().toString();
        listPriceRetailGoods[1] = mRetailGoodsDialogBinding.insertAutoContinent.getText().toString();

        mRetailGoodsDialogBinding.insertAutoMonth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listPriceRetailGoods[0] = adapterView.getItemAtPosition(i).toString();
            }
        });

        mRetailGoodsDialogBinding.insertAutoContinent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listPriceRetailGoods[1] = adapterView.getItemAtPosition(i).toString();
            }
        });

        setCancelable(false);
    }

    private void updateInformationRetailGoods() {
        if(mBundle != null){
            mRetailGoods = (RetailGoods) mBundle.getSerializable(Constants.RETAIL_GOODS_UPDATE);

            mRetailGoodsDialogBinding.insertAutoMonth.setText(mRetailGoods.getMonth());
            mRetailGoodsDialogBinding.insertAutoContinent.setText(mRetailGoods.getContinent());
            Objects.requireNonNull(mRetailGoodsDialogBinding.tfPolRetailGoods.getEditText()).setText(mRetailGoods.getPol());
            Objects.requireNonNull(mRetailGoodsDialogBinding.tfPodRetailGoods.getEditText()).setText(mRetailGoods.getPod());
            Objects.requireNonNull(mRetailGoodsDialogBinding.tfDimRetailGoods.getEditText()).setText(mRetailGoods.getDim());
            Objects.requireNonNull(mRetailGoodsDialogBinding.tfGrossRetailGoods.getEditText()).setText(mRetailGoods.getGrossweight());
            Objects.requireNonNull(mRetailGoodsDialogBinding.tfTypeofcargoRetailGoods.getEditText()).setText(mRetailGoods.getTypeofcargo());
            Objects.requireNonNull(mRetailGoodsDialogBinding.tfOceanAirfreightRetailGoods.getEditText()).setText(mRetailGoods.getOceanfreight());
            Objects.requireNonNull(mRetailGoodsDialogBinding.tfLocalchargeAirImport.getEditText()).setText(mRetailGoods.getLocalcharge());
            Objects.requireNonNull(mRetailGoodsDialogBinding.tfCarrierRetailGoods.getEditText()).setText(mRetailGoods.getCarrier());
            Objects.requireNonNull(mRetailGoodsDialogBinding.tfScheduleRetailGoods.getEditText()).setText(mRetailGoods.getSchedule());
            Objects.requireNonNull(mRetailGoodsDialogBinding.tfTfTransitTimeRetailGoods.getEditText()).setText(mRetailGoods.getTransittime());
            Objects.requireNonNull(mRetailGoodsDialogBinding.tfValidRetailGoods.getEditText()).setText(mRetailGoods.getValid());
            Objects.requireNonNull(mRetailGoodsDialogBinding.tfNotesRetailGoods.getEditText()).setText(mRetailGoods.getNote());


        }
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_function_update_retail_goods:
                updateRetailGoods();
                dismiss();
                break;
            case  R.id.btn_function_insert_retail_goods:
                insertRetailGoods();
                dismiss();
                break;
            case R.id.btn_function_cancel_retail_goods:
                dismiss();
                break;
        }
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
        hashMap.put("month", listPriceRetailGoods[0]);
        hashMap.put("continent", listPriceRetailGoods[1]);
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
    private String getCreatedDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    private void updateRetailGoods() {
        String strPol = Objects.requireNonNull(mRetailGoodsDialogBinding.tfPolRetailGoods.getEditText()).getText().toString();
        String strPod = Objects.requireNonNull(mRetailGoodsDialogBinding.tfPodRetailGoods.getEditText()).getText().toString();
        String strDim = Objects.requireNonNull(mRetailGoodsDialogBinding.tfDimRetailGoods.getEditText()).getText().toString();
        String strGross = Objects.requireNonNull(mRetailGoodsDialogBinding.tfGrossRetailGoods.getEditText()).getText().toString();
        String strType = Objects.requireNonNull(mRetailGoodsDialogBinding.tfTypeofcargoRetailGoods.getEditText()).getText().toString();
        String strOceanFreight = Objects.requireNonNull(mRetailGoodsDialogBinding.tfOceanAirfreightRetailGoods.getEditText()).getText().toString();
        String strLocalCharge = Objects.requireNonNull(mRetailGoodsDialogBinding.tfLocalchargeAirImport.getEditText()).getText().toString();
        String strCarrier = Objects.requireNonNull(mRetailGoodsDialogBinding.tfCarrierRetailGoods.getEditText()).getText().toString();
        String strSchedule = Objects.requireNonNull(mRetailGoodsDialogBinding.tfScheduleRetailGoods.getEditText()).getText().toString();
        String strTransittime = Objects.requireNonNull(mRetailGoodsDialogBinding.tfTfTransitTimeRetailGoods.getEditText()).getText().toString();
        String strValid = Objects.requireNonNull(mRetailGoodsDialogBinding.tfValidRetailGoods.getEditText()).getText().toString();
        String strNotes = Objects.requireNonNull(mRetailGoodsDialogBinding.tfNotesRetailGoods.getEditText()).getText().toString();
        String timeStamp = mRetailGoods.getpTime();

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
        hashMap.put("month", listPriceRetailGoods[0]);
        hashMap.put("continent", listPriceRetailGoods[1]);
        hashMap.put("pTime", timeStamp);
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Retail_Goods_Air");
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
}