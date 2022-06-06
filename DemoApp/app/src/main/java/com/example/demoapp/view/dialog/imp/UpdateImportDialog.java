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
import com.example.demoapp.databinding.FragmentUpdateImportDialogBinding;
import com.example.demoapp.model.Import;
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


public class UpdateImportDialog extends DialogFragment implements View.OnClickListener {

    private FragmentUpdateImportDialogBinding binding;

    private FirebaseAuth mAuth;
    private DatabaseReference userDBRef;

    private ProgressDialog progressDialog;
    // user info
    String name, email, uid, dp;

    private final String[] listStr = new String[3];

    private Bundle bundle;
    private Import imp;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentUpdateImportDialogBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        showDatePicker();

        bundle = getArguments();
        setInfo();

        progressDialog = new ProgressDialog(getContext());

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

    public static UpdateImportDialog getInstance() {
        return new UpdateImportDialog();
    }

    public void setInfo() {

        if (bundle != null) {
            imp = (Import) bundle.getSerializable(Constants.IMPORT_UPDATE);

            binding.updateAutoContainer.setText(imp.getType(), false);
            binding.updateAutoMonth.setText(imp.getMonth(), false);
            binding.updateAutoContinent.setText(imp.getContinent(), false);

            Objects.requireNonNull(binding.tfPol.getEditText()).setText(imp.getPol());
            Objects.requireNonNull(binding.tfPod.getEditText()).setText(imp.getPod());

            Objects.requireNonNull(binding.tfOf20.getEditText()).setText(imp.getOf20());
            Objects.requireNonNull(binding.tfOf40.getEditText()).setText(imp.getOf40());
            Objects.requireNonNull(binding.tfOf45.getEditText()).setText(imp.getOf45());

            Objects.requireNonNull(binding.tfSur20.getEditText()).setText(imp.getSur20());
            Objects.requireNonNull(binding.tfSur40.getEditText()).setText(imp.getSur40());
            Objects.requireNonNull(binding.tfSur45.getEditText()).setText(imp.getSur45());

            Objects.requireNonNull(binding.tfTotalFreight.getEditText()).setText(imp.getTotalFreight());
            Objects.requireNonNull(binding.tfCarrier.getEditText()).setText(imp.getCarrier());
            Objects.requireNonNull(binding.tfSchedule.getEditText()).setText(imp.getSchedule());
            Objects.requireNonNull(binding.tfTransitTime.getEditText()).setText(imp.getTransitTime());
            Objects.requireNonNull(binding.tfFreeTime.getEditText()).setText(imp.getFreeTime());
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
        ArrayAdapter<String> adapterItemsType = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, Constants.ITEMS_IMPORT);
        ArrayAdapter<String> adapterItemsMonth = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, Constants.ITEMS_MONTH);
        ArrayAdapter<String> adapterItemsContinent = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, Constants.ITEMS_CONTINENT);

        binding.updateAutoContainer.setAdapter(adapterItemsType);
        binding.updateAutoMonth.setAdapter(adapterItemsMonth);
        binding.updateAutoContinent.setAdapter(adapterItemsContinent);

        listStr[0] = binding.updateAutoContainer.getText().toString();
        listStr[1] = binding.updateAutoMonth.getText().toString();
        listStr[2] = binding.updateAutoContinent.getText().toString();

        // buttons
        binding.importUpdateBtnCancel.setOnClickListener(this);
        binding.importUpdateBtnUpdate.setOnClickListener(this);

        binding.updateAutoContainer.setOnItemClickListener((adapterView, view, i, l) -> {
            listStr[0] = adapterView.getItemAtPosition(i).toString();
            Toast.makeText(getContext(), listStr[0], Toast.LENGTH_LONG).show();
        });

        binding.updateAutoMonth.setOnItemClickListener((adapterView, view, i, l) -> {
            listStr[1] = adapterView.getItemAtPosition(i).toString();
            Toast.makeText(getContext(), listStr[1], Toast.LENGTH_LONG).show();
        });

        binding.updateAutoContinent.setOnItemClickListener((adapterView, view, i, l) -> {
            listStr[2] = adapterView.getItemAtPosition(i).toString();
            Toast.makeText(getContext(), listStr[2], Toast.LENGTH_LONG).show();
        });

        textWatcher();
        setCancelable(false);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.import_update_btn_cancel:
                dismiss();
                break;
            case R.id.import_update_btn_update:
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
        String pol = Objects.requireNonNull(binding.tfPol.getEditText()).getText().toString();
        String pod = Objects.requireNonNull(binding.tfPod.getEditText()).getText().toString();

        String of20 = Objects.requireNonNull(binding.tfOf20.getEditText()).getText().toString();
        String of40 = Objects.requireNonNull(binding.tfOf40.getEditText()).getText().toString();
        String of45 = Objects.requireNonNull(binding.tfOf45.getEditText()).getText().toString();

        String sur20 = Objects.requireNonNull(binding.tfSur20.getEditText()).getText().toString();
        String sur40 = Objects.requireNonNull(binding.tfSur40.getEditText()).getText().toString();
        String sur45 = Objects.requireNonNull(binding.tfSur45.getEditText()).getText().toString();

        String totalFreight = Objects.requireNonNull(binding.tfTotalFreight.getEditText()).getText().toString();
        String carrier = Objects.requireNonNull(binding.tfCarrier.getEditText()).getText().toString();
        String schedule = Objects.requireNonNull(binding.tfSchedule.getEditText()).getText().toString();
        String transit = Objects.requireNonNull(binding.tfTransitTime.getEditText()).getText().toString();
        String free = Objects.requireNonNull(binding.tfFreeTime.getEditText()).getText().toString();
        String valid = Objects.requireNonNull(binding.tfValid.getEditText()).getText().toString();
        String note = Objects.requireNonNull(binding.tfNote.getEditText()).getText().toString();


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("pol", pol);
        hashMap.put("pod", pod);
        hashMap.put("of20", of20);
        hashMap.put("of40", of40);
        hashMap.put("of45", of45);
        hashMap.put("sur20", sur20);
        hashMap.put("sur40", sur40);
        hashMap.put("sur45", sur45);
        hashMap.put("totalFreight", totalFreight);
        hashMap.put("carrier", carrier);
        hashMap.put("schedule", schedule);
        hashMap.put("transitTime", transit);
        hashMap.put("freeTime", free);
        hashMap.put("valid", valid);
        hashMap.put("note", note);
        hashMap.put("type", listStr[0]);
        hashMap.put("month", listStr[1]);
        hashMap.put("continent", listStr[2]);
        hashMap.put("pTime", timeStamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Import");
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

        Objects.requireNonNull(binding.tfOf20.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                try {

                    Objects.requireNonNull(binding.tfTotalFreight.getEditText()).setText(
                            totalFreight(binding.tfOf20.getEditText().getText().toString(),
                                    Objects.requireNonNull(binding.tfSur20.getEditText()).getText().toString()));

                } catch (Exception exception) {
                    //Toast.makeText(getContext(), exception.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

        Objects.requireNonNull(binding.tfSur20.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    Objects.requireNonNull(binding.tfTotalFreight.getEditText()).setText(
                            totalFreight(binding.tfOf20.getEditText().getText().toString(),
                                    Objects.requireNonNull(binding.tfSur20.getEditText()).getText().toString()));

                } catch (Exception e) {
                    //Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Get total of of20 and sur20
     *
     * @param of  of20
     * @param sur sur40
     * @return total of of20 and sur20
     */
    private String totalFreight(String of, String sur) {
        double numOf, numSur;

        if (TextUtils.isEmpty(of)) {
            numOf = 0;
        } else numOf = Double.parseDouble(of);

        if (TextUtils.isEmpty(sur)) {
            numSur = 0;
        } else numSur = Double.parseDouble(sur);

        double total = numOf + numSur;

        return String.valueOf(total);
    }
}