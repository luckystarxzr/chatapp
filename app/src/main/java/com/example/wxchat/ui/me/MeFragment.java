package com.example.wxchat.ui.me;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.wxchat.MainActivity;
import com.example.wxchat.R;
import com.example.wxchat.databinding.FragmentMeBinding;
import com.example.wxchat.model.User;

public class MeFragment extends Fragment {
    private FragmentMeBinding binding;
    private MeViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(MeViewModel.class);
        viewModel.getText().observe(getViewLifecycleOwner(), text -> {
            binding.statusText.setText(text != null ? text : "");
        });

        // 设置初始状态
        viewModel.setText("加载中..."); // 使用 setText 方法

        // 从数据库加载当前用户信息
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.getExecutorService().execute(() -> {
                User user = activity.getDatabase().userDao().getUserById("0"); // 当前用户 ID
                if (user != null) {
                    getActivity().runOnUiThread(() -> {
                        binding.nickname.setText(user.getName());
                        binding.wechatId.setText("微信号: " + user.getWechatId());
                        binding.avatar.setImageResource(user.getAvatarResId());
                        viewModel.setText(null); // 使用 setText 方法
                    });
                } else {
                    getActivity().runOnUiThread(() -> {
                        viewModel.setText("用户信息加载失败"); // 使用 setText 方法
                    });
                }
            });
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}