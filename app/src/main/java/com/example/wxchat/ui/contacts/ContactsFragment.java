package com.example.wxchat.ui.contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wxchat.MainActivity;
import com.example.wxchat.R;
import com.example.wxchat.adapter.UserAdapter;
import com.example.wxchat.databinding.FragmentContactsBinding;
import com.example.wxchat.model.User;
import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment implements UserAdapter.OnUserClickListener {
    private FragmentContactsBinding binding;
    private RecyclerView userListView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private ContactsViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentContactsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        viewModel.getText().observe(getViewLifecycleOwner(), text -> {
            if (userList.isEmpty()) {
                binding.statusText.setText(text != null ? text : "暂无联系人");
            } else {
                binding.statusText.setText("");
            }
        });

        userListView = binding.userList;
        userList = new ArrayList<>();



        // 从数据库加载用户
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.getExecutorService().execute(() -> {
                List<User> users = activity.getDatabase().userDao().getAllUsers();
                userList.clear();
                for (User user : users) {
                    if (!user.getUserId().equals("0")) { // 排除当前用户
                        userList.add(user);
                    }
                }
                getActivity().runOnUiThread(() -> {
                    userAdapter = new UserAdapter(userList, this);
                    userListView.setAdapter(userAdapter);
                });
            });
        }

        return root;
    }

    @Override
    public void onUserClick(User user) {
        Bundle bundle = new Bundle();
        bundle.putString("userId", user.getUserId());
        bundle.putString("userName", user.getName());
        Navigation.findNavController(getView())
                .navigate(R.id.action_navigation_contacts_to_chat_detail, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}