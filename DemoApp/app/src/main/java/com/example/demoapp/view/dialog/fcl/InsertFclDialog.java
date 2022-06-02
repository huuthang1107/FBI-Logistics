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
import com.example.demoapp.databinding.FragmentDialogInsertFclBinding;
import com.example.demoapp.model.FCLModel;
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

public class InsertFclDialog extends DialogFragment implements View.OnClickListener {

    private final String[] listStr = new String[3];

    private FragmentDialogInsertFclBinding binding;

    private List<FCLModel> fclList;

    private FirebaseAuth mAuth;
    private DatabaseReference userDBRef;

    private ProgressDialog progressDialog;
    // user info
    String name, email, uid, dp;

    private Bundle bundle;


    public static InsertFclDialog insertDialog() {
        return new InsertFclDialog();
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
        binding = FragmentDialogInsertFclBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        fclList = new ArrayList<>();
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

        Bundle bundle = getArguments();
        if (bundle != null) {
            FCLModel fcl = (FCLModel) bundle.getSerializable(Constants.FCL_UPDATE);
            if ("YES".equalsIgnoreCase(bundle.getString(Constants.FCL_ADD_NEW))) {
                setData(fcl);
            }
        }
        showDatePicker();
        return root;
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

    @Override
    public void onStart() {
        checkUserStatus();
        super.onStart();
    }

    public void setData(FCLModel fcl) {
        listStr[0] = fcl.getType();
        listStr[1] = fcl.getMonth();
        listStr[2] = fcl.getContinent();

        binding.insertAutoMonth.setText(listStr[1], false);
        binding.insertAutoContainer.setText(listStr[0], false);

        changeContName(fcl.getType());
        binding.insertAutoContinent.setText(listStr[2], false);

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
    }


    /**
     * this method will init for all views and get a item of auto complete textview
     */
    public void initView() {

        // auto complete textview
        ArrayAdapter<String> adapterItemsType = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, Constants.ITEMS_FCL);
        ArrayAdapter<String> adapterItemsMonth = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, Constants.ITEMS_MONTH);
        ArrayAdapter<String> adapterItemsContinent = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, Constants.ITEMS_CONTINENT);

        binding.insertAutoContainer.setAdapter(adapterItemsType);
        binding.insertAutoMonth.setAdapter(adapterItemsMonth);
        binding.insertAutoContinent.setAdapter(adapterItemsContinent);

        // buttons
        binding.btnFunctionAdd.setOnClickListener(this);
        binding.btnFunctionCancel.setOnClickListener(this);

        binding.insertAutoContainer.setOnItemClickListener((adapterView, view, i, l) -> {
            listStr[0] = adapterView.getItemAtPosition(i).toString();
            changeContName(listStr[0]);
        });

        binding.insertAutoMonth.setOnItemClickListener((adapterView, view, i, l) -> listStr[1] = adapterView.getItemAtPosition(i).toString());

        binding.insertAutoContinent.setOnItemClickListener((adapterView, view, i, l) -> listStr[2] = adapterView.getItemAtPosition(i).toString());

        textWatcher();

        setCancelable(false);

    }

    /**
     * Reset name for when user select item in auto complete
     *
     * @param name type of cont
     */
    @SuppressLint("ResourceAsColor")
    public void changeContName(String name) {
        String currentNameOf20 = getString(R.string.col_of2);
        String currentNameOf40 = getString(R.string.col_of4);
        String currentNameOf45 = getString(R.string.col_of45);

        String newNameOf20 = currentNameOf20.concat("_" + name);
        String newNameOf40 = currentNameOf40.concat("_" + name);
        String newNameOf45 = currentNameOf45.concat("_" + name);

        binding.tfOf20.setHint(newNameOf20);
        binding.tfOf40.setHint(newNameOf40);
        binding.tfOf45.setHint(newNameOf45);
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

        binding.insertAutoContainer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(binding.insertAutoContainer.getText())) {
                    binding.insertAutoContainer.setError(null);
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

    /**
     * This method set event for and and cancel buttons
     *
     * @param v view
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_function_add:
                if (isFilled()) {
                    insertFcl();
                    dismiss();
                } else
                    Toast.makeText(getContext(), Constants.INSERT_FAILED, Toast.LENGTH_LONG).show();
                break;
            case R.id.btn_function_cancel:
                dismiss();
                break;
        }
    }

    /**
     * This method used to get data user typing and insert them into database
     */
    public void insertFcl() {
        String timeStamp = String.valueOf(System.currentTimeMillis());
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

        HashMap<Object, String> hashMap = new HashMap<>();
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
        hashMap.put("createdDate", getCreatedDate());
        hashMap.put("pTime", timeStamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("FCL");
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
     * Get current date and time
     *
     * @return current date and time
     */
    private String getCreatedDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    /**
     * @return false if the user does not select auto complete
     */

    public boolean isFilled() {
        boolean result = true;

        if (TextUtils.isEmpty(binding.insertAutoContainer.getText())) {
            result = false;
            binding.insertAutoContainer.setError(Constants.ERROR_AUTO_COMPLETE_TYPE);
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

}
