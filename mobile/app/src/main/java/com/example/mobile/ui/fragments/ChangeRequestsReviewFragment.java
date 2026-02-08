package com.example.mobile.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mobile.R;
import com.example.mobile.dto.profile.ChangeDataRequestResponse;
import com.example.mobile.network.RetrofitClient;
import com.example.mobile.ui.adapters.AdminRequestsAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeRequestsReviewFragment extends Fragment implements AdminRequestsAdapter.OnActionListener {
    private List<ChangeDataRequestResponse> requestList = new ArrayList<>();
    private AdminRequestsAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyView;

    public ChangeRequestsReviewFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_requests_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.change_requests_rv_requests);
        emptyView = view.findViewById(R.id.change_requests_empty_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AdminRequestsAdapter(requestList, (AdminRequestsAdapter.OnActionListener) this);
        recyclerView.setAdapter(adapter);

        fetchRequests();
    }

    private void fetchRequests() {
        RetrofitClient.getChangeDataRequestService(requireContext())
                .getAllPendingRequests().enqueue(new Callback<List<ChangeDataRequestResponse>>() {
            @Override
            public void onResponse(Call<List<ChangeDataRequestResponse>> call, Response<List<ChangeDataRequestResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    requestList.clear();
                    requestList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    toggleEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<ChangeDataRequestResponse>> call, Throwable t) {
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toggleEmptyState() {
        if (requestList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onApprove(long id) {
        RetrofitClient.getChangeDataRequestService(requireContext())
                .approveRequest(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    removeRequestFromList(id);
                    Toast.makeText(getContext(), "Request approved", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onReject(long id) {
        RetrofitClient.getChangeDataRequestService(requireContext())
                .rejectRequest(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    removeRequestFromList(id);
                    Toast.makeText(getContext(), "Request rejected", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void removeRequestFromList(long id) {
        for (int i = 0; i < requestList.size(); i++) {
            if (requestList.get(i).getId() == id) {
                requestList.remove(i);
                adapter.notifyItemRemoved(i);
                break;
            }
        }
        toggleEmptyState();
    }
}