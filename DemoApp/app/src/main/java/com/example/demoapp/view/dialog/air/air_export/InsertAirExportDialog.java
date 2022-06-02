package com.example.demoapp.view.dialog.air.air_export;

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
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.demoapp.R;
import com.example.demoapp.databinding.FragmentDialogInsertAirBinding;
import com.example.demoapp.model.AirExport;
import com.example.demoapp.utilities.Constants;
import com.example.demoapp.view.activity.LoginActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
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


public class InsertAirExportDialog extends DialogFragment implements View.OnClickListener {

    private final String[] listStr = new String[2];
    private FragmentDialogInsertAirBinding insertAirBinding;
    private ArrayAdapter<String> adapterItemsMonth, adapterItemsContinent;

    private List<AirExport> airList;

    private FirebaseAuth mAuth;
    private DatabaseReference userDBRef;

    private ProgressDialog progressDialog;
    // user info
    String name, email, uid, dp;

    public static InsertAirExportDialog insertDiaLogAIR() {
        return new InsertAirExportDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        insertAirBinding = FragmentDialogInsertAirBinding.inflate(inflater, container, false);

        View view = insertAirBinding.getRoot();

//        mAirViewModel = new ViewModelProvider(this).get(AirExportViewModel.class);
//        mCommunicateViewModel = new ViewModelProvider(getActivity()).get(CommunicateViewModel.class);

        airList = new ArrayList<>();


        mAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        progressDialog = new ProgressDialog(getContext());

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

    private void showDatePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select date");

        final MaterialDatePicker<Long> materialDatePicker = builder.build();

        insertAirBinding.edtValid.setOnClickListener(view -> {
            materialDatePicker.show(getParentFragmentManager(), "Date_Picker");
            materialDatePicker.addOnPositiveButtonClickListener(selection -> {

                TimeZone timeZoneUTC = TimeZone.getDefault();
                // It will be negative, so that's the -1
                int offsetFromUTC = timeZoneUTC.getOffset(new Date().getTime()) * -1;
                // Create a date format, then a date object with our offset
                SimpleDateFormat simpleFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                Date date = new Date(selection + offsetFromUTC);

                Objects.requireNonNull(insertAirBinding.tfValid.getEditText()).setText(simpleFormat.format(date));
            });
        });
    }

    public boolean isFilled() {
        boolean result = true;

        if (TextUtils.isEmpty(insertAirBinding.insertAutoContinent.getText())) {
            result = false;
            insertAirBinding.insertAutoContinent.setError(Constants.ERROR_AUTO_COMPLETE_CONTINENT);
        }

        if (TextUtils.isEmpty(insertAirBinding.insertAutoMonth.getText())) {
            result = false;
            insertAirBinding.insertAutoMonth.setError(Constants.ERROR_AUTO_COMPLETE_MONTH);
        }


        if (TextUtils.isEmpty(Objects.requireNonNull(insertAirBinding.tfAol.getEditText()).getText().toString())) {
            result = false;
            insertAirBinding.tfAol.setError(Constants.ERROR_POL);
        }
        if (TextUtils.isEmpty(Objects.requireNonNull(insertAirBinding.tfAod.getEditText()).getText().toString())) {
            result = false;
            insertAirBinding.tfAol.setError(Constants.ERROR_POD);
        }

        if (TextUtils.isEmpty(Objects.requireNonNull(insertAirBinding.tfAod.getEditText()).getText().toString())) {
            result = false;
            insertAirBinding.tfValid.setError(Constants.ERROR_VALID);
        }

        return result;
    }

    private void initView() {
        adapterItemsMonth = new ArrayAdapter<String>(getContext(), R.layout.dropdown_item, Constants.ITEMS_MONTH);
        adapterItemsContinent = new ArrayAdapter<String>(getContext(), R.layout.dropdown_item, Constants.ITEMS_CONTINENT);

        insertAirBinding.insertAutoMonth.setAdapter(adapterItemsMonth);
        insertAirBinding.insertAutoContinent.setAdapter(adapterItemsContinent);

        insertAirBinding.btnFunctionAdd.setOnClickListener(this);
        insertAirBinding.btnFunctionCancel.setOnClickListener(this);

        insertAirBinding.insertAutoMonth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listStr[0] = adapterView.getItemAtPosition(i).toString();
            }
        });

        insertAirBinding.insertAutoContinent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listStr[1] = adapterView.getItemAtPosition(i).toString();
            }
        });

        setCancelable(false);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_function_add:
                if (isFilled()) {
                    insertAIR();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), Constants.INSERT_FAILED, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_function_cancel:
                dismiss();
                break;
        }
    }

    private void insertAIR() {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String stAol = insertAirBinding.tfAol.getEditText().getText().toString();
        String stAod = insertAirBinding.tfAod.getEditText().getText().toString();
        String stDim = insertAirBinding.tfDim.getEditText().getText().toString();
        String stGross = insertAirBinding.tfGross.getEditText().getText().toString();
        String stType = insertAirBinding.tfTypeofcargo.getEditText().getText().toString();
        String stFreight = insertAirBinding.tfAirfreight.getEditText().getText().toString();
        String stSurcharge = insertAirBinding.tfSurcharge.getEditText().getText().toString();
        String stLines = insertAirBinding.tfAirlines.getEditText().getText().toString();
        String stSchedule = insertAirBinding.tfSchedule.getEditText().getText().toString();
        String stTransittime = insertAirBinding.tfTfTransitTime.getEditText().getText().toString();
        String stValid = insertAirBinding.tfValid.getEditText().getText().toString();
        String stNote = insertAirBinding.tfNotes.getEditText().getText().toString();

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

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Air_Export");
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