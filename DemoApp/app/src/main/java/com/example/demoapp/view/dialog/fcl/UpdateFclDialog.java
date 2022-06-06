package com.example.demoapp.view.dialog.fcl;

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

import com.example.demoapp.R;
import com.example.demoapp.databinding.FragmentDialogUpdateFclBinding;
import com.example.demoapp.model.FCLModel;
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

public class UpdateFclDialog extends DialogFragment implements View.OnClickListener {

    private final String[] listStr = new String[3];

    private FCLModel fcl;

    private FragmentDialogUpdateFclBinding binding;

    private FirebaseAuth mAuth;
    private DatabaseReference userDBRef;

    private ProgressDialog progressDialog;
    // user info
    String name, email, uid, dp;

    private Bundle bundle;


    public static UpdateFclDialog getInstance() {
        return new UpdateFclDialog();
    }

    /**
     * This method will set a view for insert dialog
     *
     * @param inflater           inflater
     * @param container          container
     * @param savedInstanceState save
     * @return view of this fragment dialog
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDialogUpdateFclBinding.inflate(inflater, container, false);

        View view = binding.getRoot();


        mAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        progressDialog = new ProgressDialog(getContext());

        bundle = getArguments();
        setInfo();


        initView();
        showDatePicker();

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

    public void setInfo() {

        if (bundle != null) {
            fcl = (FCLModel) bundle.getSerializable(Constants.FCL_UPDATE);

            binding.updateAutoMonth.setText(fcl.getMonth());
            binding.updateAutoContainer.setText(fcl.getType());
            binding.updateAutoContinent.setText(fcl.getContinent());
            Objects.requireNonNull(binding.tfPol.getEditText()).setText(fcl.getPol());
            Objects.requireNonNull(binding.tfPod.getEditText()).setText(fcl.getPod());
            Objects.requireNonNull(binding.tfOf20.getEditText()).setText(fcl.getOf20());
            Objects.requireNonNull(binding.tfOf40.getEditText()).setText(fcl.getOf40());
            Objects.requireNonNull(binding.tfOf45.getEditText()).setText(fcl.getOf45());
            Objects.requireNonNull(binding.tfSu20.getEditText()).setText(fcl.getSu20());
            Objects.requireNonNull(binding.tfSu40.getEditText()).setText(fcl.getSu40());
            Objects.requireNonNull(binding.tfLines.getEditText()).setText(fcl.getLine());
            Objects.requireNonNull(binding.tfNotes.getEditText()).setText(fcl.getNotes());
            Objects.requireNonNull(binding.tfValid.getEditText()).setText(fcl.getValid());
            Objects.requireNonNull(binding.tfNotes2.getEditText()).setText(fcl.getNote2());
        } else {
            Toast.makeText(getContext(), "GetData Failed", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * this method will init for all views and get a item of auto complete textview
     */
    public void initView() {

        // auto complete textview
        ArrayAdapter<String> adapterItemsType = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, Constants.ITEMS_FCL);
        ArrayAdapter<String> adapterItemsMonth = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, Constants.ITEMS_MONTH);
        ArrayAdapter<String> adapterItemsContinent = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, Constants.ITEMS_CONTINENT);

        binding.updateAutoContainer.setAdapter(adapterItemsType);
        binding.updateAutoMonth.setAdapter(adapterItemsMonth);
        binding.updateAutoContinent.setAdapter(adapterItemsContinent);

        listStr[0] = binding.updateAutoContainer.getText().toString();
        listStr[1] = binding.updateAutoMonth.getText().toString();
        listStr[2] = binding.updateAutoContinent.getText().toString();

        // buttons
        binding.btnFunctionUpdate.setOnClickListener(this);
        binding.btnFunctionCancel.setOnClickListener(this);

        binding.updateAutoContainer.setOnItemClickListener((adapterView, view, i, l) -> listStr[0] = adapterView.getItemAtPosition(i).toString());

        binding.updateAutoMonth.setOnItemClickListener((adapterView, view, i, l) -> listStr[1] = adapterView.getItemAtPosition(i).toString());

        binding.updateAutoContinent.setOnItemClickListener((adapterView, view, i, l) -> listStr[2] = adapterView.getItemAtPosition(i).toString());

        textWatcher();

        setCancelable(false);

    }

    /**
     * If this field is not empty, set null for error
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

    /**
     * This method set event for and and cancel buttons
     *
     * @param v view
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_function_update:
                if (isFilled()) {
                    updateFcl();
                    dismiss();
                } else
                    Toast.makeText(getContext(), Constants.UPDATE_FAILED, Toast.LENGTH_LONG).show();
                break;
            case R.id.btn_function_cancel:
                dismiss();
                break;
        }
    }

    public void showDatePicker() {

        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select date");

        final MaterialDatePicker<Long> materialDatePicker = builder.build();

        binding.edtValid.setOnClickListener(view -> {
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
     * This method used to get data user typing and insert them into database
     */
    public void updateFcl() {
        String pol = Objects.requireNonNull(binding.tfPol.getEditText()).getText().toString();
        String pod = Objects.requireNonNull(binding.tfPod.getEditText()).getText().toString();
        String of20 = Objects.requireNonNull(binding.tfOf20.getEditText()).getText().toString();
        String of40 = Objects.requireNonNull(binding.tfOf40.getEditText()).getText().toString();
        String of45 = Objects.requireNonNull(binding.tfOf45.getEditText()).getText().toString();
        String su20 = Objects.requireNonNull(binding.tfSu20.getEditText()).getText().toString();
        String su40 = Objects.requireNonNull(binding.tfSu40.getEditText()).getText().toString();
        String line = Objects.requireNonNull(binding.tfLines.getEditText()).getText().toString();
        String notes = Objects.requireNonNull(binding.tfNotes.getEditText()).getText().toString();
        String valid = Objects.requireNonNull(binding.tfValid.getEditText()).getText().toString();
        String note2 = Objects.requireNonNull(binding.tfNotes2.getEditText()).getText().toString();
        String timeStamp = fcl.getpTime();


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("pol", pol);
        hashMap.put("pod", pod);
        hashMap.put("of20", of20);
        hashMap.put("of40", of40);
        hashMap.put("of45", of45);
        hashMap.put("su20", su20);
        hashMap.put("su40", su40);
        hashMap.put("line", line);
        hashMap.put("of20", of20);
        hashMap.put("of40", of40);
        hashMap.put("of45", of45);
        hashMap.put("notes", notes);
        hashMap.put("valid", valid);
        hashMap.put("note2", note2);
        hashMap.put("type", listStr[0]);
        hashMap.put("month", listStr[1]);
        hashMap.put("continent", listStr[2]);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("FCL");
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

    public void updateFCL(){

    }

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
}
