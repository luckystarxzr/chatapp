package com.example.wxchat.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.wxchat.model.Loginer;

@Dao
public interface LoginerDao {
    @Insert
    void insert(Loginer loginer);

    @Query("SELECT * FROM loginers WHERE username = :username AND password = :password")
    Loginer findLoginerByUsernameAndPassword(String username, String password);
}