package com.example.todo.task.frag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.todo.R;
import com.example.todo.task.RefreshListener;
import com.example.todo.task.TaskAdapter;

public class InboxFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private View view;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private TaskAdapter taskAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    RefreshListener refreshListener;
    ConstraintLayout relaxBanner;

    private boolean showBanner = false;

    public InboxFragment() {}

    public InboxFragment(TaskAdapter taskAdapter, RefreshListener refreshListener) {
        this.taskAdapter = taskAdapter;
        this.refreshListener = refreshListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_inbox, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(taskAdapter);

        swipeRefreshLayout = view.findViewById(R.id.simpleSwipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        relaxBanner = view.findViewById(R.id.relaxBanner);

        if (isShowBanner())
            relaxBanner.setVisibility(View.VISIBLE);
        else
            relaxBanner.setVisibility(View.INVISIBLE);

        layoutManager = new LinearLayoutManager(getContext());

        recyclerView.setLayoutManager(layoutManager);

        return view;
    }

    public boolean isShowBanner() {
        return showBanner;
    }

    public void setShowBanner(boolean showBanner) {
        this.showBanner = showBanner;
    }

    public void displayBanner() {
        if (relaxBanner == null)
            setShowBanner(true);
        else
            relaxBanner.setVisibility(View.VISIBLE);
    }

    public void hideBanner() {
        if (relaxBanner == null)
            setShowBanner(false);
        else
            relaxBanner.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onRefresh() {
        refreshListener.onInboxRefresh();
        swipeRefreshLayout.setRefreshing(false);
    }
}
