package com.example.wxchat.ui.chats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import cn.leancloud.im.v2.LCIMConversation;
import cn.leancloud.im.v2.LCIMException;
import cn.leancloud.im.v2.callback.LCIMConversationCreatedCallback; // 使用新回调
import com.example.wxchat.MainActivity;
import com.example.wxchat.adapter.MessageAdapter;
import com.example.wxchat.databinding.FragmentChatDetailBinding;
import com.example.wxchat.model.Message;
import com.example.wxchat.model.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatDetailFragment extends Fragment {
    private FragmentChatDetailBinding binding;
    private RecyclerView messageListView;
    private EditText messageInput;
    private Button sendButton;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private String currentUserId = "0";
    private String otherUserId;
    private String otherUserName;
    private int otherUserAvatarResId;
    private MainActivity activity;
    private LCIMConversation conversation;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        activity = (MainActivity) getActivity();
        if (activity == null) return root;

        if (getArguments() != null) {
            otherUserId = getArguments().getString("userId");
            otherUserName = getArguments().getString("userName");
        }

        activity.getSupportActionBar().setTitle(otherUserName);

        activity.getExecutorService().execute(() -> {
            User user = activity.getDatabase().userDao().getUserById(otherUserId);
            if (user != null) {
                otherUserAvatarResId = user.getAvatarResId();
                getActivity().runOnUiThread(this::setupChat);
            }
        });

        List<String> members = Arrays.asList(currentUserId, otherUserId);
        activity.getImClient().createConversation(members, otherUserName, null, false, true, new LCIMConversationCreatedCallback() { // 修复：使用 LCIMConversationCreatedCallback
            @Override
            public void done(LCIMConversation conv, LCIMException e) { // 修复：接收 conversation 和 e
                if (e == null && conv != null) {
                    conversation = conv;
                    loadMessagesFromLocal();
                } else {
                    activity.runOnUiThread(() ->
                            Toast.makeText(activity, "创建会话失败: " + (e != null ? e.getMessage() : "未知错误"), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });

        return root;
    }

    private void setupChat() {
        messageListView = binding.messageList;
        messageInput = binding.messageInput;
        sendButton = binding.sendButton;
        messageList = new ArrayList<>();

        messageAdapter = new MessageAdapter(messageList, currentUserId, otherUserAvatarResId);
        messageListView.setAdapter(messageAdapter);

        sendButton.setOnClickListener(v -> {
            String content = messageInput.getText().toString().trim();
            if (!content.isEmpty()) {
                Message localMessage = new Message(currentUserId, otherUserId, content, System.currentTimeMillis());
                messageList.add(localMessage);
                messageAdapter.notifyItemInserted(messageList.size() - 1);
                messageListView.scrollToPosition(messageList.size() - 1);
                messageInput.setText("");

                activity.getExecutorService().execute(() ->
                        activity.getDatabase().messageDao().insert(localMessage)
                );
            }
        });
    }

    private void loadMessagesFromLocal() {
        activity.getExecutorService().execute(() -> {
            List<Message> localMessages = activity.getDatabase().messageDao().getMessagesBetween(currentUserId, otherUserId);
            messageList.clear();
            messageList.addAll(localMessages);
            getActivity().runOnUiThread(() -> {
                messageAdapter.notifyDataSetChanged();
                messageListView.scrollToPosition(messageList.size() - 1);
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}