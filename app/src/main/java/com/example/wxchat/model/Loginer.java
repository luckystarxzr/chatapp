package com.example.wxchat.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Entity(tableName = "loginers")
public class Loginer {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String username;
    private String password; // 存储加密后的密码

    public Loginer(String username, String password) {
        this.username = username;
        this.password = hashPassword(password); // 构造时加密密码
    }

    // SHA-256 加密方法
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // 出错时返回原密码（仅测试用）
        }
    }

    // Getters 和 Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = hashPassword(password);
    }
}