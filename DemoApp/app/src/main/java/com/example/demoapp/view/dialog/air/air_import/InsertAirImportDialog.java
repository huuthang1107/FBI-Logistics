package com.example.demoapp.view.dialog.air.air_import;

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
import androidx.lifecycle.ViewModelProvider;

import com.example.demoapp.R;
import com.example.demoapp.databinding.FragmentInsertAirImportDialogBinding;
import com.example.demoapp.model.AirImport;
import com.example.demoapp.utilities.Constants;
import com.example.demoapp.view.activity.LoginActivity;
import com.example.demoapp.viewmodel.AirImportViewModel;
import com.example.demoapp.viewmodel.CommunicateViewModel;
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


public class InsertAirImportDialog extends DialogFragment implements View.OnClickListener {
    private final String[] listStr = new String[2];
    private FragmentInsertAirImportDialogBinding mInsertAirImportDialogBinding;
    private ArrayAdapter<String> adapterItemsMonth, adapterItemsContinent;

    private AirImportViewModel mAirViewModel;
    private List<AirImport> airList ;
    private CommunicateViewModel mCommunicateViewModel;
    private Bundle bundle;
    private AirImport mAirImport;

    private FirebaseAuth mAuth;
    private DatabaseReference userDBRef;

    private ProgressDialog progressDialog;
    // user info
    String name, email, uid, dp;
    public static InsertAirImportDialog insertDiaLogAIRImport(){
        return new InsertAirImportDialog();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInsertAirImportDialogBinding = FragmentInsertAirImportDialogBinding.inflate(inflater, container, false);
        View view = mInsertAirImportDialogBinding.getRoot();

        mAirViewModel = new ViewModelProvider(this).get(AirImportViewModel.class);
        mCommunicateViewModel = new ViewModelProvider(getActivity()).get(CommunicateViewModel.class);

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
        bundle = getArguments();
        insertInformationImport();

        initView();
        eventOnclick();
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

    private void insertInformationImport() {
        if(bundle != null){
            mAirImport = (AirImport) bundle.getSerializable(Constants.AIR_IMPORT_UPDATE);

            mInsertAirImportDialogBinding.insertAutoMonth.setText(mAirImport.getMonth());
            mInsertAirImportDialogBinding.insertAutoContinent.setText(mAirImport.getContinent());
            Objects.requireNonNull(mInsertAirImportDialogBinding.tfAolAirImport.getEditText()).setText(mAirImport.getAol());
            Objects.requireNonNull(mInsertAirImportDialogBinding.tfAodAirImport.getEditText()).setText(mAirImport.getAod());
            Objects.requireNonNull(mInsertAirImportDialogBinding.tfDimAirImport.getEditText()).setText(mAirImport.getDim());
            Objects.requireNonNull(mInsertAirImportDialogBinding.tfGrossAirImport.getEditText()).setText(mAirImport.getGrossweight());
            Objects.requireNonNull(mInsertAirImportDialogBinding.tfTypeofcargoAirImport.getEditText()).setText(mAirImport.getTypeofcargo());
            Objects.requireNonNull(mInsertAirImportDialogBinding.tfAirfreightAirImport.getEditText()).setText(mAirImport.getAirfreight());
            Objects.requireNonNull(mInsertAirImportDialogBinding.tfSurchargeAirImport.getEditText()).setText(mAirImport.getSurcharge());
            Objects.requireNonNull(mInsertAirImportDialogBinding.tfAirlinesAirImport.getEditText()).setText(mAirImport.getAirlines());
            Objects.requireNonNull(mInsertAirImportDialogBinding.tfScheduleAirImport.getEditText()).setText(mAirImport.getSchedule());
            Objects.requireNonNull(mInsertAirImportDialogBinding.tfTfTransitTimeAirImport.getEditText()).setText(mAirImport.getTransittime());
            Objects.requireNonNull(mInsertAirImportDialogBinding.tfValidAirImport.getEditText()).setText(mAirImport.getValid());
            Objects.requireNonNull(mInsertAirImportDialogBinding.tfNotesAirImport.getEditText()).setText(mAirImport.getNote());


        }
    }
    private String getCreatedDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }


    private void eventOnclick() {
        mInsertAirImportDialogBinding.btnFunctionAddAirImport.setOnClickListener(this);
        mInsertAirImportDialogBinding.btnFunctionCancelAirImport.setOnClickListener(this);
        mInsertAirImportDialogBinding.tfValidAirImport.setOnClickListener(this);

    }

    private void initView() {
        adapterItemsMonth = new ArrayAdapter<String>(getContext(), R.layout.dropdown_item, Constants.ITEMS_MONTH);
        adapterItemsContinent = new ArrayAdapter<String>(getContext(), R.layout.dropdown_item, Constants.ITEMS_CONTINENT);

        mInsertAirImportDialogBinding.insertAutoMonth.setAdapter(adapterItemsMonth);
        mInsertAirImportDialogBinding.insertAutoContinent.setAdapter(adapterItemsContinent);


        mInsertAirImportDialogBinding.insertAutoMonth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listStr[0] = adapterView.getItemAtPosition(i).toString();
            }
        });

        mInsertAirImportDialogBinding.insertAutoContinent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listStr[1] = adapterView.getItemAtPosition(i).toString();
            }
        });

        setCancelable(false);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_function_add_air_import:
                if(isFilled()) {
                    insertAirImport();
                    dismiss();
                }else{
                    Toast.makeText(getContext(), Constants.INSERT_FAILED, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_function_cancel_air_import:
                dismiss();
                break;
        }
    }

    public void showDatePicker() {

        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select date");

        final MaterialDatePicker<Long> materialDatePicker = builder.build();

        mInsertAirImportDialogBinding.edtValid.setOnClickListener(new View.OnClickListener() {
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

                        Objects.requireNonNull(mInsertAirImportDialogBinding.tfValidAirImport.getEditText()).setText(simpleFormat.format(date));
                    }
                });
            }
        });

    }

    public boolean isFilled() {
        boolean result = true;

        if (TextUtils.isEmpty(mInsertAirImportDialogBinding.insertAutoContinent.getText())) {
            result = false;
            mInsertAirImportDialogBinding.insertAutoContinent.setError(Constants.ERROR_AUTO_COMPLETE_CONTINENT);
        }

        if (TextUtils.isEmpty(mInsertAirImportDialogBinding.insertAutoMonth.getText())) {
            result = false;
            mInsertAirImportDialogBinding.insertAutoMonth.setError(Constants.ERROR_AUTO_COMPLETE_MONTH);
        }


        if (TextUtils.isEmpty(Objects.requireNonNull(mInsertAirImportDialogBinding.tfAolAirImport.getEditText()).getText().toString())) {
            result = false;
            mInsertAirImportDialogBinding.tfAolAirImport.setError(Constants.ERROR_POL);
        }
        if (TextUtils.isEmpty(Objects.requireNonNull(mInsertAirImportDialogBinding.tfAodAirImport.getEditText()).getText().toString())) {
            result = false;
            mInsertAirImportDialogBinding.tfAodAirImport.setError(Constants.ERROR_POD);
        }

        if (TextUtils.isEmpty(Objects.requireNonNull(mInsertAirImportDialogBinding.tfValidAirImport.getEditText()).getText().toString())) {
            result = false;
            mInsertAirImportDialogBinding.tfValidAirImport.setError(Constants.ERROR_VALID);
        }

        return result;
    }

    private void insertAirImport() {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String stAol = mInsertAirImportDialogBinding.tfAolAirImport.getEditText().getText().toString();
        String stAod = mInsertAirImportDialogBinding.tfAodAirImport.getEditText().getText().toString();
        String stDim = mInsertAirImportDialogBinding.tfDimAirImport.getEditText().getText().toString();
        String stGross = mInsertAirImportDialogBinding.tfGrossAirImport.getEditText().getText().toString();
        String stType = mInsertAirImportDialogBinding.tfTypeofcargoAirImport.getEditText().getText().toString();
        String stFreight = mInsertAirImportDialogBinding.tfAirfreightAirImport.getEditText().getText().toString();
        String stSurcharge = mInsertAirImportDialogBinding.tfSurchargeAirImport.getEditText().getText().toString();
        String stLines = mInsertAirImportDialogBinding.tfAirlinesAirImport.getEditText().getText().toString();
        String stSchedule = mInsertAirImportDialogBinding.tfScheduleAirImport.getEditText().getText().toString();
        String stTransittime = mInsertAirImportDialogBinding.tfTfTransitTimeAirImport.getEditText().getText().toString();
        String stValid = mInsertAirImportDialogBinding.tfValidAirImport.getEditText().getText().toString();
        String stNote = mInsertAirImportDialogBinding.tfNotesAirImport.getEditText().getText().toString();


        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("aol", stAol);
        hashMap.put("aod", stAod);
        hashMap.put("dim", stDim);
        hashMap.put("grossweight", stGross);
        hashMap.put("typeofcargo", stType);
        hashMap.put("airfreight", stFreight);
        hashMap.put("surcharge", stSurcharge);
        hashMap.put("airlines", stLines);
        hashMap.put("schedule", stSchedule);
        hashMap.put("transittime", stTransittime);
        hashMap.put("valid", stValid);
        hashMap.put("note", stNote);
        hashMap.put("month", listStr[0]);
        hashMap.put("continent", listStr[1]);
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
}