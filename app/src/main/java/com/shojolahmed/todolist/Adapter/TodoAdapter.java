package com.shojolahmed.todolist.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shojolahmed.todolist.AddNewTask;
import com.shojolahmed.todolist.MainActivity;
import com.shojolahmed.todolist.Model.TodoModel;
import com.shojolahmed.todolist.R;
import com.shojolahmed.todolist.TaskDetailsActivity;

import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.MyViewHolder> {

    private RecyclerViewClickListener listener;

    private List<TodoModel> todoList;
    private MainActivity activity;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private String userID;

    private ImageButton btnMore;

    public TodoAdapter(MainActivity mainActivity, List<TodoModel> todoList, RecyclerViewClickListener listener){
        this.todoList = todoList;
        activity = mainActivity;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.task_layout, parent, false);
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getUid();

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TodoModel todoModel = todoList.get(position);
        holder.textTask.setText(todoModel.getTask_name());
        holder.textDateCreated.setText("Created on " + todoModel.getDate_created());
        holder.checkBox.setChecked(toBoolean(todoModel.getStatus()));

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    firestore.collection("users").document(userID)
                            .collection("tasks").document(todoModel.TaskId).update("status", 1);
                    return;
                }

                firestore.collection("users").document(userID)
                        .collection("tasks").document(todoModel.TaskId).update("status", 0);
            }
        });

        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getContext(), v);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.itemEditTask:
                                editTask(position);
                                notifyDataSetChanged();
                                return true;
                            case R.id.itemDelete:
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setMessage("Are you sure?")
                                        .setTitle("Delete Task")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                deleteTask(position);
                                                Toast.makeText(getContext(), "Task Deleted", Toast.LENGTH_SHORT).show();
                                            }
                                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        notifyDataSetChanged();
                                    }
                                });

                                AlertDialog dialog = builder.create();
                                dialog.show();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.inflate(R.menu.longpress_menu);
                popupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textDateCreated, textTask;
        CheckBox checkBox;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            textDateCreated = itemView.findViewById(R.id.textViewDateCreated);
            textTask = itemView.findViewById(R.id.textViewTask);
            checkBox = itemView.findViewById(R.id.checkboxTask);

            btnMore = itemView.findViewById(R.id.btnMore);
            
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(itemView, getLayoutPosition());
                }
            });

            /*itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });*/
        }
    }

    public interface RecyclerViewClickListener{
        void onClick(View v, int position);
    }

    private void showDetails(int position) {
        TodoModel todoModel = todoList.get(position);

        Intent intent = new Intent(getContext(), TaskDetailsActivity.class);
        intent.putExtra("task_title", todoModel.getTask_name());
        intent.putExtra("task_description", todoModel.getDescription());
    }

    public void deleteTask(int position){
        TodoModel todoModel = todoList.get(position);
        firestore.collection("users").document(userID)
                .collection("tasks").document(todoModel.TaskId).delete();
        todoList.remove(position);
        notifyItemRemoved(position);
    }

    public void editTask(int position){
        TodoModel todoModel = todoList.get(position);

        Bundle bundle = new Bundle();
        bundle.putString("task_name", todoModel.getTask_name());
        bundle.putString("date_created", todoModel.getDate_created());
        bundle.putString("id", todoModel.TaskId);
        bundle.putString("description", todoModel.getDescription());
        bundle.putInt("has_description", todoModel.getHas_description());

        AddNewTask addNewTask = new AddNewTask();
        addNewTask.setArguments(bundle);
        addNewTask.show(activity.getSupportFragmentManager(), addNewTask.getTag());
    }

    public Context getContext(){
        return activity;
    }

    private boolean toBoolean(int status) {
        return status != 0;
    }
}
