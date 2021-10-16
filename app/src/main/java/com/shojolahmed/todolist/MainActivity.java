package com.shojolahmed.todolist;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.shojolahmed.todolist.Adapter.TodoAdapter;
import com.shojolahmed.todolist.Model.TodoModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnDialogCloseListener {

    private RecyclerView recyclerView;
    private TextView textNoTasks;
    private Button btnNewTask, btnLogout;
    private FirebaseFirestore firestore;
    private TodoAdapter adapter;
    private List<TodoModel> mList;
    private Query query;
    private ListenerRegistration listenerRegistration;

    private SwipeRefreshLayout refreshLayout;
    private FirebaseAuth firebaseAuth;
    private String userID;

    private TodoAdapter.RecyclerViewClickListener recyclerViewClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        textNoTasks = findViewById(R.id.textViewNoTasks);
        btnNewTask = findViewById(R.id.btnNewTask);
        btnLogout = findViewById(R.id.btnLogin);
        firestore = FirebaseFirestore.getInstance();

        refreshLayout = findViewById(R.id.refreshLayout);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mList.clear();
                showData();
                adapter.notifyDataSetChanged();
                refreshLayout.setRefreshing(false);
            }
        });

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        btnNewTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG);
            }
        });

        recyclerViewClickListener = new TodoAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                Intent intent = new Intent(getApplicationContext(), TaskDetailsActivity.class);
                intent.putExtra("task_title", mList.get(position).getTask_name());
                intent.putExtra("task_description", mList.get(position).getDescription());
                intent.putExtra("time", mList.get(position).getTime().toString());
                startActivity(intent);
            }
        };

        mList = new ArrayList<>();
        adapter = new TodoAdapter(MainActivity.this, mList, recyclerViewClickListener);

        /*ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);*/

        recyclerView.setAdapter(adapter);
        showData();

        /*btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        mList.clear();
        showData();
        adapter.notifyDataSetChanged();
    }

    private void showData() {
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getUid();
        query = firestore.collection("users").document(userID)
                .collection("tasks").orderBy("time", Query.Direction.ASCENDING);
        listenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error!=null){
                    Log.d("SHOWDATA","Error:"+error.getMessage());
                    return;
                }
                for (DocumentChange documentChange : value.getDocumentChanges()){
                    if (documentChange.getType() == DocumentChange.Type.ADDED){
                        String id = documentChange.getDocument().getId();
                        TodoModel todoModel = documentChange.getDocument().toObject(TodoModel.class).withId(id);

                        mList.add(todoModel);
                        adapter.notifyDataSetChanged();
                    }
                }
                listenerRegistration.remove();

                if (mList.isEmpty()){
                    recyclerView.setVisibility(View.GONE);
                    textNoTasks.setVisibility(View.VISIBLE);
                }
//                Collections.reverse(mList);
            }
        });
    }

    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        mList.clear();
        showData();
        adapter.notifyDataSetChanged();
    }

    public void userLogout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }
}