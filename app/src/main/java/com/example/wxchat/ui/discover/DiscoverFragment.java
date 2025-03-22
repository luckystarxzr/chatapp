package com.example.wxchat.ui.discover;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.wxchat.databinding.FragmentDiscoverBinding;

public class DiscoverFragment extends Fragment {
    private FragmentDiscoverBinding binding;
    private DiscoverViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(DiscoverViewModel.class);
        viewModel.getText().observe(getViewLifecycleOwner(), text -> {
            binding.statusText.setText(text != null ? text : "发现页面（占位）");
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}