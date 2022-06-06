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
import androidx.fragment.app.DialogFragment;

import com.example.demoapp.R;
import com.example.demoapp.databinding.FragmentUpdateImportLclDialogBinding;
import com.example.demoapp.model.ImportLcl;
import com.example.demoapp.utilities.Constants;
import com.example.demoapp.view.activity.LoginActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;


public class UpdateImportLclDialog extends DialogFragment implements View.OnClickListener {

    private FragmentUpdateImportLclDialogBinding binding;

    private FirebaseAuth mAuth;
    private DatabaseReference userDBRef;

    private ProgressDialog progressDialog;
    // user info
    String name, email, uid, dp;

    private final String[] listStr = new String[3];

    private Bundle bundle;
    private ImportLcl imp;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentUpdateImportLclDialogBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        showDatePicker();

        progressDialog = new ProgressDialog(getContext());

        bundle = getArguments();
        setInfo();

        initView();

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

    public static UpdateImportLclDialog getInstance() {
        return new UpdateImportLclDialog();
    }

    public void setInfo() {

        if (bundle != null) {
            imp = (ImportLcl) bundle.getSerializable(Constants.IMPORT_LCL_UPDATE);

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

        listStr[0] = binding.insertAutoCargo.getText().toString();
        listStr[1] = binding.insertAutoMonth.getText().toString();
        listStr[2] = binding.insertAutoContinent.getText().toString();

        // buttons
        binding.btnAddImportLcl.setOnClickListener(this);
        binding.btnCancelImportLcl.setOnClickListener(this);

        binding.insertAutoCargo.setOnItemClickListener((adapterView, view, i, l) -> listStr[0] = adapterView.getItemAtPosition(i).toString());

        binding.insertAutoMonth.setOnItemClickListener((adapterView, view, i, l) -> listStr[1] = adapterView.getItemAtPosition(i).toString());

        binding.insertAutoContinent.setOnItemClickListener((adapterView, view, i, l) -> listStr[2] = adapterView.getItemAtPosition(i).toString());

        textWatcher();

        setCancelable(false);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_cancel_import_lcl:
                dismiss();
                break;
            case R.id.btn_add_import_lcl:
                if (isFilled()) {
                    process();
                    dismiss();
                } else
                    Toast.makeText(getContext(), Constants.UPDATE_FAILED, Toast.LENGTH_LONG).show();
                break;
        }
    }

    /**
     * get data of fields and update
     */
    private void process() {

        String timeStamp = imp.getpTime();
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


        HashMap<String, Object> hashMap = new HashMap<>();
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
        hashMap.put("pTime", timeStamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Import_LCL");
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

    /**
     *
     * @return false if one of these fields is not filled
     */
    public boolean isFilled() {
        boolean result = true;

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
     * Using text watcher to check fields
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
    }
}