package com.example.wxchat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wxchat.database.AppDatabase;
import com.example.wxchat.model.Loginer;
import com.example.wxchat.model.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirmPassword;
    private Button btnSubmit;
    private AppDatabase database;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSubmit = findViewById(R.id.btn_submit);

        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        btnSubmit.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
            } else {
                String hashedPassword = Loginer.hashPassword(password);
                executorService.execute(() -> {
                    // 检查用户是否已存在
                    Loginer existingLoginer = database.loginerDao().findLoginerByUsernameAndPassword(username, hashedPassword);
                    if (existingLoginer != null) {
                        runOnUiThread(() -> Toast.makeText(this, "用户名已存在", Toast.LENGTH_SHORT).show());
                    } else {
                        // 插入登录信息
                        Loginer newLoginer = new Loginer(username, password);
                        database.loginerDao().insert(newLoginer);
                        // 插入用户信息（示例数据）
                        User newUser = new User(username, username, "wxid_" + username, 0); // userId 与 username 一致
                        database.userDao().insert(newUser);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}