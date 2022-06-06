package com.example.demoapp.view.dialog.air.air_import;

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
import com.example.demoapp.databinding.FragmentUpdateAirImportDialogBinding;
import com.example.demoapp.model.AirImport;
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


public class UpdateAirImportDialog extends DialogFragment implements View.OnClickListener {
    private FragmentUpdateAirImportDialogBinding mAirImportDialogBinding;
    private final String[] listPriceAirImport = new String[2];
    private Bundle mBundle;
    private AirImport mAirImport;
    private List<AirImport> airList;
    private FirebaseAuth mAuth;
    private DatabaseReference userDBRef;

    private ProgressDialog progressDialog;
    // user info
    String name, email, uid, dp;
    public static UpdateAirImportDialog getInstance(){
        return  new UpdateAirImportDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAirImportDialogBinding = FragmentUpdateAirImportDialogBinding.inflate(inflater, container, false);
        View view = mAirImportDialogBinding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        airList = new ArrayList<>();
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
        updateInformationImport();
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

    public void showDatePicker() {

        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select date");

        final MaterialDatePicker<Long> materialDatePicker = builder.build();

        mAirImportDialogBinding.edtValid.setOnClickListener(new View.OnClickListener() {
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

                        Objects.requireNonNull(mAirImportDialogBinding.tfValidAirImport.getEditText()).setText(simpleFormat.format(date));
                    }
                });
            }
        });

    }

    private void updateInformationImport() {
        if(mBundle != null){
            mAirImport = (AirImport) mBundle.getSerializable(Constants.AIR_IMPORT_UPDATE);

            mAirImportDialogBinding.insertAutoMonth.setText(mAirImport.getMonth());
            mAirImportDialogBinding.insertAutoContinent.setText(mAirImport.getContinent());
            Objects.requireNonNull(mAirImportDialogBinding.tfPolAirImport.getEditText()).setText(mAirImport.getAol());
            Objects.requireNonNull(mAirImportDialogBinding.tfPodAirImport.getEditText()).setText(mAirImport.getAod());
            Objects.requireNonNull(mAirImportDialogBinding.tfDimAirImport.getEditText()).setText(mAirImport.getDim());
            Objects.requireNonNull(mAirImportDialogBinding.tfGrossAirImport.getEditText()).setText(mAirImport.getGrossweight());
            Objects.requireNonNull(mAirImportDialogBinding.tfTypeofcargoAirImport.getEditText()).setText(mAirImport.getTypeofcargo());
            Objects.requireNonNull(mAirImportDialogBinding.tfAirfreightAirImport.getEditText()).setText(mAirImport.getAirfreight());
            Objects.requireNonNull(mAirImportDialogBinding.tfSurchargeAirImport.getEditText()).setText(mAirImport.getSurcharge());
            Objects.requireNonNull(mAirImportDialogBinding.tfAirlinesAirImport.getEditText()).setText(mAirImport.getAirlines());
            Objects.requireNonNull(mAirImportDialogBinding.tfScheduleAirImport.getEditText()).setText(mAirImport.getSchedule());
            Objects.requireNonNull(mAirImportDialogBinding.tfTfTransitTimeAirImport.getEditText()).setText(mAirImport.getTransittime());
            Objects.requireNonNull(mAirImportDialogBinding.tfValidAirImport.getEditText()).setText(mAirImport.getValid());
            Objects.requireNonNull(mAirImportDialogBinding.tfNotesAirImport.getEditText()).setText(mAirImport.getNote());



        }
    }

    private void unit() {
        ArrayAdapter<String> arrayAdapterItemsMonth = new ArrayAdapter<String>(getContext(),
                R.layout.dropdown_item, Constants.ITEMS_MONTH);
        ArrayAdapter<String> arrayAdapterItemsContinent = new ArrayAdapter<String>(getContext(),
                R.layout.dropdown_item,Constants.ITEMS_CONTINENT);


        mAirImportDialogBinding.insertAutoMonth.setAdapter(arrayAdapterItemsMonth);
        mAirImportDialogBinding.insertAutoContinent.setAdapter(arrayAdapterItemsContinent);

        listPriceAirImport[0] = mAirImportDialogBinding.insertAutoMonth.getText().toString();
        listPriceAirImport[1] = mAirImportDialogBinding.insertAutoContinent.getText().toString();

        mAirImportDialogBinding.insertAutoMonth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listPriceAirImport[0]= adapterView.getItemAtPosition(i).toString();
            }
        });

        mAirImportDialogBinding.insertAutoContinent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listPriceAirImport[1] = adapterView.getItemAtPosition(i).toString();
            }
        });


    }

    private void setUpButtons() {
        mAirImportDialogBinding.btnFunctionAddAirImport.setOnClickListener(this);
        mAirImportDialogBinding.btnFunctionUpdateAirImport.setOnClickListener(this);
        mAirImportDialogBinding.btnFunctionCancelAirImport.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_function_update_air_import:
                updateAirImport();
                dismiss();
                break;
            case R.id.btn_function_add_air_import:
                insertAirImport();
                dismiss();
                break;
            case R.id.btn_function_cancel_air_import:
                dismiss();
                break;
        }
    }

    private String getCreatedDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }


    private void insertAirImport() {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String strAol = Objects.requireNonNull(mAirImportDialogBinding.tfPolAirImport.getEditText()).getText().toString();
        String strAod = Objects.requireNonNull(mAirImportDialogBinding.tfPodAirImport.getEditText()).getText().toString();
        String strDim = Objects.requireNonNull(mAirImportDialogBinding.tfDimAirImport.getEditText()).getText().toString();
        String strGross = Objects.requireNonNull(mAirImportDialogBinding.tfGrossAirImport.getEditText()).getText().toString();
        String strType = Objects.requireNonNull(mAirImportDialogBinding.tfTypeofcargoAirImport.getEditText()).getText().toString();
        String strFreight = Objects.requireNonNull(mAirImportDialogBinding.tfAirfreightAirImport.getEditText()).getText().toString();
        String strSurcharge = Objects.requireNonNull(mAirImportDialogBinding.tfSurchargeAirImport.getEditText()).getText().toString();
        String strLine = Objects.requireNonNull(mAirImportDialogBinding.tfAirlinesAirImport.getEditText()).getText().toString();
        String strSchedule = Objects.requireNonNull(mAirImportDialogBinding.tfScheduleAirImport.getEditText()).getText().toString();
        String strTransittime = Objects.requireNonNull(mAirImportDialogBinding.tfTfTransitTimeAirImport.getEditText()).getText().toString();
        String strValid = Objects.requireNonNull(mAirImportDialogBinding.tfValidAirImport.getEditText()).getText().toString();
        String strNotes = Objects.requireNonNull(mAirImportDialogBinding.tfNotesAirImport.getEditText()).getText().toString();

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("aol", strAol);
        hashMap.put("aod", strAod);
        hashMap.put("dim", strDim);
        hashMap.put("grossweight", strGross);
        hashMap.put("typeofcargo", strType);
        hashMap.put("airfreight", strFreight);
        hashMap.put("surcharge", strSurcharge);
        hashMap.put("airlines", strLine);
        hashMap.put("schedule", strSchedule);
        hashMap.put("transittime", strTransittime);
        hashMap.put("valid", strValid);
        hashMap.put("note", strNotes);
        hashMap.put("month", listPriceAirImport[0]);
        hashMap.put("continent", listPriceAirImport[1]);
        hashMap.put("date_created", getCreatedDate());
        hashMap.put("pTime", timeStamp);
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Air_Import");
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

    private void updateAirImport() {
        String timeStamp = mAirImport.getpTime();
        String strAol = Objects.requireNonNull(mAirImportDialogBinding.tfPolAirImport.getEditText()).getText().toString();
        String strAod = Objects.requireNonNull(mAirImportDialogBinding.tfPodAirImport.getEditText()).getText().toString();
        String strDim = Objects.requireNonNull(mAirImportDialogBinding.tfDimAirImport.getEditText()).getText().toString();
        String strGross = Objects.requireNonNull(mAirImportDialogBinding.tfGrossAirImport.getEditText()).getText().toString();
        String strType = Objects.requireNonNull(mAirImportDialogBinding.tfTypeofcargoAirImport.getEditText()).getText().toString();
        String strFreight = Objects.requireNonNull(mAirImportDialogBinding.tfAirfreightAirImport.getEditText()).getText().toString();
        String strSurcharge = Objects.requireNonNull(mAirImportDialogBinding.tfSurchargeAirImport.getEditText()).getText().toString();
        String strLine = Objects.requireNonNull(mAirImportDialogBinding.tfAirlinesAirImport.getEditText()).getText().toString();
        String strSchedule = Objects.requireNonNull(mAirImportDialogBinding.tfScheduleAirImport.getEditText()).getText().toString();
        String strTransittime = Objects.requireNonNull(mAirImportDialogBinding.tfTfTransitTimeAirImport.getEditText()).getText().toString();
        String strValid = Objects.requireNonNull(mAirImportDialogBinding.tfValidAirImport.getEditText()).getText().toString();
        String strNotes = Objects.requireNonNull(mAirImportDialogBinding.tfNotesAirImport.getEditText()).getText().toString();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("aol", strAol);
        hashMap.put("aod", strAod);
        hashMap.put("dim", strDim);
        hashMap.put("grossweight", strGross);
        hashMap.put("typeofcargo", strType);
        hashMap.put("airfreight", strFreight);
        hashMap.put("surcharge", strSurcharge);
        hashMap.put("airlines", strLine);
        hashMap.put("schedule", strSchedule);
        hashMap.put("transittime", strTransittime);
        hashMap.put("valid", strValid);
        hashMap.put("note", strNotes);
        hashMap.put("month", listPriceAirImport[0]);
        hashMap.put("continent", listPriceAirImport[1]);
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Air_Import");
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