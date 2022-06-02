package com.example.demoapp.view.dialog.imp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.demoapp.R;
import com.example.demoapp.databinding.FragmentDialogInsertImportLclBinding;
import com.example.demoapp.model.ImportLcl;
import com.example.demoapp.utilities.Constants;
import com.example.demoapp.view.activity.LoginActivity;
import com.example.demoapp.viewmodel.CommunicateViewModel;
import com.example.demoapp.viewmodel.ImportLclViewModel;
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

public class InsertImportLclDialog extends DialogFragment implements View.OnClickListener {

    private final String[] listStr = new String[3];

    private FragmentDialogInsertImportLclBinding binding;
    private CommunicateViewModel mCommunicateViewModel;
    private ImportLclViewModel mImportViewModel;


    private List<ImportLcl> importLCLList;

    private FirebaseAuth mAuth;
    private DatabaseReference userDBRef;

    private ProgressDialog progressDialog;
    // user info
    String name, email, uid, dp;

    private Bundle bundle;

    /**
     * This method will set up view for insert dialog
     *
     * @param inflater           inflater
     * @param container          container
     * @param savedInstanceState save
     * @return view of Import insert dialog
     */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDialogInsertImportLclBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        mImportViewModel = new ViewModelProvider(this).get(ImportLclViewModel.class);
        mCommunicateViewModel = new ViewModelProvider(requireActivity()).get(CommunicateViewModel.class);

        mAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        importLCLList = new ArrayList<>();
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
        showDatePicker();

        bundle = getArguments();
        setData();

        return root;
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

    public void setData() {
        if (bundle != null) {
            ImportLcl imp = (ImportLcl) bundle.getSerializable(Constants.IMPORT_LCL_UPDATE);
            if ("YES".equalsIgnoreCase(bundle.getString(Constants.IMPORT_LCL_ADD_NEW))) {
                listStr[0] = imp.getCargo();
                listStr[1] = imp.getMonth();
                listStr[2] = imp.getContinent();

                binding.insertAutoCargo.setText(imp.getCargo(), false);
                binding.insertAutoMonth.setText(imp.getMonth(), false);
                binding.insertAutoContinent.setText(imp.getContinent(), false);

                Objects.requireNonNull(binding.tfTerm.getEditText()).setText(imp.getTerm());
                Objects.requireNonNull(binding.tfPol.getEditText()).setText(imp.getPol());
                Objects.requireNonNull(binding.tfPod.getEditText()).setText(imp.getPod());

                Objects.requireNonNull(binding.tfOf.getEditText()).setText(imp.getOf());
                Objects.requireNonNull(binding.tfLocalPol.getEditText()).setText(imp.getLocalPol());
                Objects.requireNonNull(binding.tfLocalPod.getEditText()).setText(imp.getLocalPod());

                Objects.requireNonNull(binding.tfCarrier.getEditText()).setText(imp.getCarrier());
                Objects.requireNonNull(binding.tfSchedule.getEditText()).setText(imp.getSchedule());
                Objects.requireNonNull(binding.tfTransitTime.getEditText()).setText(imp.getTransitTime());
                Objects.requireNonNull(binding.tfValid.getEditText()).setText(imp.getValid());
                Objects.requireNonNull(binding.tfNote.getEditText()).setText(imp.getNote());
            }
        }

    }

    public void showDatePicker() {

        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select date");

        final MaterialDatePicker<Long> materialDatePicker = builder.build();

        binding.importEdtValid.setOnClickListener(view -> {
            materialDatePicker.show(getParentFragmentManager(), "Date_Picker");
            materialDatePicker.addOnPositiveButtonClickListener(selection -> {

                TimeZone timeZoneUTC = TimeZone.getDefault();
                // It will be negative, so that's the -1
                int offsetFromUTC = timeZoneUTC.getOffset(new Date().getTime()) * -1;
                // Create a date format, then a date object with our offset
                SimpleDateFormat simpleFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                Date date = new Date(selection + offsetFromUTC);

                Objects.requireNonNull(binding.tfValid.getEditText()).setText(simpleFormat.format(date));
            });
        });

    }

    /**
     * This method will init for views and get item from auto complete text view
     */
    public void initView() {

        // auto complete textview
        ArrayAdapter<String> adapterItemsCargo = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, Constants.ITEMS_CARGO);
        ArrayAdapter<String> adapterItemsMonth = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, Constants.ITEMS_MONTH);
        ArrayAdapter<String> adapterItemsContinent = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, Constants.ITEMS_CONTINENT);

        binding.insertAutoCargo.setAdapter(adapterItemsCargo);
        binding.insertAutoMonth.setAdapter(adapterItemsMonth);
        binding.insertAutoContinent.setAdapter(adapterItemsContinent);

        // buttons
        binding.btnFunctionAdd.setOnClickListener(this);
        binding.btnFunctionCancel.setOnClickListener(this);

        binding.insertAutoCargo.setOnItemClickListener((adapterView, view, i, l) -> {
            listStr[0] = adapterView.getItemAtPosition(i).toString();
        });

        binding.insertAutoMonth.setOnItemClickListener((adapterView, view, i, l) -> {
            listStr[1] = adapterView.getItemAtPosition(i).toString();
        });

        binding.insertAutoContinent.setOnItemClickListener((adapterView, view, i, l) -> {
            listStr[2] = adapterView.getItemAtPosition(i).toString();
        });

        textWatcher();

        setCancelable(false);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_function_add:
                if (isFilled()) {
                    process();
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

    public static InsertImportLclDialog getInstance() {
        return new InsertImportLclDialog();
    }

    /**
     * This method will get information user typing and insert them into database
     */

    public void process() {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String term = Objects.requireNonNull(binding.tfTerm.getEditText()).getText().toString();
        String pol = Objects.requireNonNull(binding.tfPol.getEditText()).getText().toString();
        String pod = Objects.requireNonNull(binding.tfPod.getEditText()).getText().toString();

        String of = Objects.requireNonNull(binding.tfOf.getEditText()).getText().toString();
        String localPol = Objects.requireNonNull(binding.tfLocalPol.getEditText()).getText().toString();
        String localPod = Objects.requireNonNull(binding.tfLocalPod.getEditText()).getText().toString();

        String carrier = Objects.requireNonNull(binding.tfCarrier.getEditText()).getText().toString();
        String schedule = Objects.requireNonNull(binding.tfSchedule.getEditText()).getText().toString();
        String transit = Objects.requireNonNull(binding.tfTransitTime.getEditText()).getText().toString();
        String valid = Objects.requireNonNull(binding.tfValid.getEditText()).getText().toString();
        String note = Objects.requireNonNull(binding.tfNote.getEditText()).getText().toString();


        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("pol", pol);
        hashMap.put("pod", pod);
        hashMap.put("term", term);
        hashMap.put("cargo", listStr[0]);
        hashMap.put("of", of);
        hashMap.put("localPol", localPol);
        hashMap.put("localPod", localPod);
        hashMap.put("carrier", carrier);
        hashMap.put("schedule", schedule);
        hashMap.put("transitTime", transit);
        hashMap.put("valid", valid);
        hashMap.put("note", note);
        hashMap.put("month", listStr[1]);
        hashMap.put("continent", listStr[2]);
        hashMap.put("createdDate", getCreatedDate());
        hashMap.put("pTime", timeStamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Import_LCL");
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

    /**
     * get current date
     *
     * @return date
     */
    private String getCreatedDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    public boolean isFilled() {
        boolean result = true;

        if (TextUtils.isEmpty(binding.insertAutoCargo.getText())) {
            result = false;
            binding.insertAutoCargo.setError(Constants.ERROR_AUTO_COMPLETE_TYPE);
        }

        if (TextUtils.isEmpty(binding.insertAutoMonth.getText())) {
            result = false;
            binding.insertAutoMonth.setError(Constants.ERROR_AUTO_COMPLETE_MONTH);
        }

        if (TextUtils.isEmpty(binding.insertAutoContinent.getText())) {
            result = false;
            binding.insertAutoContinent.setError(Constants.ERROR_AUTO_COMPLETE_CONTINENT);
        }

        if (TextUtils.isEmpty(Objects.requireNonNull(binding.tfPol.getEditText()).getText().toString())) {
            result = false;
            binding.tfPol.setError(Constants.ERROR_POL);
        }

        if (TextUtils.isEmpty(Objects.requireNonNull(binding.tfValid.getEditText()).getText().toString())) {
            result = false;
            binding.tfValid.setError(Constants.ERROR_VALID);
        }

        if (TextUtils.isEmpty(Objects.requireNonNull(binding.tfPod.getEditText()).getText().toString())) {
            result = false;
            binding.tfPod.setError(Constants.ERROR_POD);
        }

        return result;
    }

    /**
     * watcher for fields
     */
    public void textWatcher() {
        Objects.requireNonNull(binding.tfPol.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(binding.tfPol.getEditText().getText().toString())) {
                    binding.tfPol.setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        Objects.requireNonNull(binding.tfPod.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(binding.tfPod.getEditText().getText().toString())) {
                    binding.tfPod.setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        Objects.requireNonNull(binding.tfValid.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(binding.tfValid.getEditText().getText().toString())) {
                    binding.tfValid.setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.insertAutoMonth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(binding.insertAutoMonth.getText())) {
                    binding.insertAutoMonth.setError(null);

                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.insertAutoCargo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(binding.insertAutoCargo.getText())) {
                    binding.insertAutoCargo.setError(null);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.insertAutoContinent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(binding.insertAutoContinent.getText())) {
                    binding.insertAutoContinent.setError(null);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }
}
