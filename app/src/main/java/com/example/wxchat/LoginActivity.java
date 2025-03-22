package com.example.wxchat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import cn.leancloud.LeanCloud;
import cn.leancloud.LCLogger;
import cn.leancloud.im.v2.LCIMClient;
import cn.leancloud.im.v2.callback.LCIMClientCallback;
import cn.leancloud.im.v2.LCIMException;
import com.example.wxchat.database.AppDatabase;
import com.example.wxchat.model.Loginer;
import com.example.wxchat.model.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private AppDatabase database;
    private ExecutorService executorService;
    private LCIMClient imClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化 LeanCloud
        LeanCloud.initialize(this, "XecI0qmFfrRrAjYHKmUaVKMU-gzGzoHsz", "WQO0EEk01N3v8T1ruOQCeJPv", "https://xeci0qmf.lc-cn-n1-shared.com");
        LeanCloud.setLogLevel(LCLogger.Level.DEBUG);

        // 初始化控件
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);

        // 初始化 Room 数据库和线程池
        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            } else {
                // 加密密码
                String hashedPassword = Loginer.hashPassword(password);
                // 查询本地数据库
                executorService.execute(() -> {
                    Loginer loginer = database.loginerDao().findLoginerByUsernameAndPassword(username, hashedPassword);
                    if (loginer != null) {
                        User user = database.userDao().findUserByUsername(username); // 通过 username 查询 User
                        runOnUiThread(() -> {
                            if (user != null) {
                                // 登录 LeanCloud IM 客户端
                                imClient = LCIMClient.getInstance(username);
                                imClient.open(new LCIMClientCallback() {
                                    @Override
                                    public void done(LCIMClient client, LCIMException e) {
                                        if (e == null) {
                                            Toast.makeText(LoginActivity.this, "IM 客户端登录成功", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            intent.putExtra("username", username);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "IM 客户端登录失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(this, "用户信息未找到", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imClient != null) {
            imClient.close(null);
        }
        executorService.shutdown();
    }
}