package com.example.wxchat.model;

public class ChatSession {
    private String userId;
    private String userName;
    private String lastMessage;
    private long lastMessageTime;
    private int avatarResId;
    private int unreadCount; // 新增未读消息数量

    public ChatSession(String userId, String userName, String lastMessage, long lastMessageTime, int avatarResId, int unreadCount) {
        this.userId = userId;
        this.userName = userName;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.avatarResId = avatarResId;
        this.unreadCount = unreadCount;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public int getAvatarResId() {
        return avatarResId;
    }

    public int getUnreadCount() {
        return unreadCount;
    }
}