package com.example.wxchat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.leancloud.im.v2.LCIMClient;
import cn.leancloud.im.v2.LCIMConversation;
import cn.leancloud.im.v2.LCIMMessage;
import cn.leancloud.im.v2.LCIMMessageHandler;
import cn.leancloud.im.v2.LCIMMessageManager;
import cn.leancloud.im.v2.callback.LCIMConversationCallback;
import cn.leancloud.im.v2.callback.LCIMConversationCreatedCallback;
import com.example.wxchat.database.AppDatabase;
import com.example.wxchat.model.Message;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatsFragment extends Fragment {

    private EditText etMessage;
    private Button btnSend;
    private RecyclerView rvChat;
    private MessageAdapter messageAdapter;
    private AppDatabase database;
    private ExecutorService executorService;
    private LCIMClient imClient;
    private String currentUser;
    private String targetUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        etMessage = view.findViewById(R.id.et_message);
        btnSend = view.findViewById(R.id.btn_send);
        rvChat = view.findViewById(R.id.rv_chat);

        database = AppDatabase.getInstance(requireContext());
        executorService = Executors.newSingleThreadExecutor();
        currentUser = requireActivity().getIntent().getStringExtra("username");
        targetUser = "targetUserId"; // 示例，实际从参数获取
        imClient = LCIMClient.getInstance(currentUser);

        rvChat.setLayoutManager(new LinearLayoutManager(getContext()));
        messageAdapter = new MessageAdapter(new ArrayList<>());
        rvChat.setAdapter(messageAdapter);

        loadChatHistory();
        setupMessageListener();

        btnSend.setOnClickListener(v -> sendMessage());

        return view;
    }

    private void loadChatHistory() {
        executorService.execute(() -> {
            List<Message> messages = database.messageDao().getChatHistory(currentUser, targetUser);
            requireActivity().runOnUiThread(() -> messageAdapter.updateMessages(messages));
        });
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        Message message = new Message(currentUser, targetUser, content, System.currentTimeMillis());
        executorService.execute(() -> database.messageDao().insert(message));

        imClient.createConversation(Arrays.asList(targetUser), "Chat with " + targetUser, null, false, true,
                new LCIMConversationCreatedCallback() {
                    @Override
                    public void done(LCIMConversation conversation, LCIMException e) {
                        if (e == null) {
                            LCIMMessage lcimMessage = new LCIMMessage();
                            lcimMessage.setContent(content);
                            conversation.sendMessage(lcimMessage, new LCIMConversationCallback() {
                                @Override
                                public void done(LCIMException e) {
                                    if (e == null) {
                                        etMessage.setText("");
                                        loadChatHistory();
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void setupMessageListener() {
        LCIMMessageManager.registerMessageHandler(new LCIMMessageHandler() {
            @Override
            public void onMessage(LCIMMessage message, LCIMConversation conversation, LCIMClient client) {
                String senderId = message.getFrom();
                String content = message.getContent();
                Message newMessage = new Message(senderId, currentUser, content, System.currentTimeMillis());
                executorService.execute(() -> {
                    database.messageDao().insert(newMessage);
                    requireActivity().runOnUiThread(() -> {
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            loadChatHistory();
                            if (!isFragmentVisible()) { // 应用在后台时发送通知
                                sendNotification(senderId, content);
                            }
                        }
                    });
                });
            }
        });
    }

    private boolean isFragmentVisible() {
        return isAdded() && !isHidden() && getView() != null && getView().getVisibility() == View.VISIBLE;
    }

    private void sendNotification(String senderId, String content) {
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "chat_notifications";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Chat Messages", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("新消息 from " + senderId)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LCIMMessageManager.unregisterMessageHandler();
        executorService.shutdown();
    }
}