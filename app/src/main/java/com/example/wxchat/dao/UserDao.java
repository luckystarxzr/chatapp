package com.example.wxchat.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.wxchat.model.User;
import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Query("SELECT * FROM users WHERE userId = :userId")
    User findUserByUserId(String userId);

    @Query("SELECT * FROM users WHERE userId = :username")
    User findUserByUsername(String username);

    @Query("SELECT * FROM users WHERE name LIKE :query OR wechatId LIKE :query")
    List<User> searchUsers(String query);

    @Query("SELECT * FROM users")
    List<User> getAllUsers(); // 示例方法，实际应查询好友
}