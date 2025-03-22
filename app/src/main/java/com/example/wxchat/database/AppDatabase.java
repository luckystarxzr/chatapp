package com.example.wxchat.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.wxchat.dao.LoginerDao;
import com.example.wxchat.dao.MessageDao;
import com.example.wxchat.dao.UserDao;
import com.example.wxchat.model.Loginer;
import com.example.wxchat.model.Message;
import com.example.wxchat.model.User;

@Database(entities = {Loginer.class, User.class, Message.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LoginerDao loginerDao();
    public abstract UserDao userDao();
    public abstract MessageDao messageDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "wxchat_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}