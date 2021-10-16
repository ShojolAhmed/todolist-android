package com.shojolahmed.todolist;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddNewTask extends BottomSheetDialogFragment {

    /* CONSTANTS FOR DATABASE */
    private String TASK_NAME = "task_name";
    private String DATE_CREATED = "date_created";
    private String STATUS = "status";
    private String TIME = "time";
    private String HAS_DESCRIPTION = "has_description";
    private String DESCRIPTION = "description";

    public static final String TAG = "AddNewTask";

    private EditText editNewTask;
    private EditText editDescription;
    private CheckBox checkDescription;
    private Button btnSave;

    private FirebaseFirestore firestore;
    private Context context;
    private String id = "";
    private FirebaseAuth firebaseAuth;
    private String userID;

    public static AddNewTask newInstance(){
        return new AddNewTask();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_task, container, false);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editNewTask = view.findViewById(R.id.editTextNewTask);
        editDescription = view.findViewById(R.id.editTaskDescription);
        checkDescription = view.findViewById(R.id.checkDescription);
        btnSave = view.findViewById(R.id.btnSave);

        firestore = FirebaseFirestore.getInstance();

        editDescription.setVisibility(View.GONE);

        checkDescription.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    editDescription.setVisibility(View.VISIBLE);
                    /*saveBtnSetEnabled(false);*/
                    return;
                }
                editDescription.setVisibility(View.GONE);
                /*if (editNewTask.toString().equals("")){
                    saveBtnSetEnabled(false);;
                    return;
                }*/
                /*saveBtnSetEnabled(true);*/
            }
        });

        boolean isUpdate = false;

        /* Disable submit button if there is text */
        final Bundle bundle = getArguments();
        if (bundle != null){
            isUpdate = true;
            String task = bundle.getString(TASK_NAME);
            int hasDescription = bundle.getInt(HAS_DESCRIPTION);
            id = bundle.getString("id");

            editNewTask.setText(task);
            editNewTask.setSelection(editNewTask.getText().length());

            if (hasDescription == 1){
                checkDescription.setChecked(true);
                String description = bundle.getString(DESCRIPTION);
                editDescription.setText(description);
                editDescription.setSelection(editDescription.getText().length());
            }

            /*if (task.length() > 0){
                saveBtnSetEnabled(false);
            }*/
        }

        /* Disable submit button if text don't change */
        /*editNewTask.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")){
                    saveBtnSetEnabled(false);;
                    return;
                }

                saveBtnSetEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")){
                    saveBtnSetEnabled(false);;
                    return;
                }

                saveBtnSetEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });*/

        boolean finalIsUpdate = isUpdate;
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String task = editNewTask.getText().toString();
                String description = editDescription.getText().toString();
                String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

                firebaseAuth = FirebaseAuth.getInstance();
                userID = firebaseAuth.getUid();

                if (finalIsUpdate){
                    Map<String, Object> updateTaskMap = new HashMap<>();
                    updateTaskMap.put(TASK_NAME, task);

                    if (checkDescription.isChecked()){
                        updateTaskMap.put(HAS_DESCRIPTION, 1);
                        updateTaskMap.put(DESCRIPTION, description);
                    }else{
                        updateTaskMap.put(HAS_DESCRIPTION, 0);
                    }

                    firestore.collection("users").document(userID)
                            .collection("tasks").document(id).update(updateTaskMap);
                    Toast.makeText(context, "Task Updated", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (task.isEmpty()){
                        Toast.makeText(context, "Empty task not allowed!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> taskMap = new HashMap<>();

                    taskMap.put(TASK_NAME, task);
                    taskMap.put(DATE_CREATED, currentDate);
                    taskMap.put(STATUS, 0);
                    taskMap.put(TIME, FieldValue.serverTimestamp());

                    if (checkDescription.isChecked()){
                        if (description.isEmpty()){
                            Toast.makeText(context, "Empty description not allowed!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        taskMap.put(HAS_DESCRIPTION, 1);
                        taskMap.put(DESCRIPTION, description);
                    } else{
                        taskMap.put(HAS_DESCRIPTION, 0);
                    }

                    firestore.collection("users").document(userID)
                            .collection("tasks").add(taskMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(context, "Task Saved", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                dismiss();
            }
        });
    }

    /*private void saveBtnSetEnabled(boolean b) {
        if (!b){
            btnSave.setEnabled(false);
            btnSave.setBackgroundColor(getResources().getColor(R.color.gray));
            return;
        }
        btnSave.setEnabled(true);
        btnSave.setBackgroundColor(getResources().getColor(R.color.orange));
    }*/

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = new Activity();
        if (activity instanceof OnDialogCloseListener){
            ((OnDialogCloseListener)activity).onDialogClose(dialog);
        }
    }
}
