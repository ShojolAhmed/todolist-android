package com.shojolahmed.todolist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class TaskDetailsActivity extends AppCompatActivity {

    private TextView taskTitle, taskDescription, taskDateCreated;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        taskTitle = findViewById(R.id.textTaskTitle);
        taskDescription = findViewById(R.id.textTaskDescription);
        btnBack = findViewById(R.id.btnBack);

        taskDateCreated = findViewById(R.id.textTaskDateCreated);

        Bundle bundle = getIntent().getExtras();
        String title = bundle.getString("task_title");
        String description = bundle.getString("task_description", "Descrtiption Not Provided.");
        String dateCreated = bundle.getString("time");

        taskTitle.setText(title);
        taskDescription.setText(description);
        taskDateCreated.setText(dateCreated);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}