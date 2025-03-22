package com.example.wxchat;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import cn.leancloud.im.v2.LCIMClient;
import com.example.wxchat.database.AppDatabase;
import com.example.wxchat.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private AppDatabase database;
    private ExecutorService executorService;
    private LCIMClient imClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化数据库
        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        // 从 Intent 获取用户名并初始化 IM 客户端
        String username = getIntent().getStringExtra("username");
        imClient = LCIMClient.getInstance(username);

        // 设置工具栏
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_chats, R.id.navigation_contacts, R.id.navigation_discover, R.id.navigation_me)
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navView, navController);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destinationId = destination.getId();
                if (destinationId == R.id.navigation_chats) {
                    toolbar.setTitle("微信消息");
                } else if (destinationId == R.id.navigation_contacts) {
                    toolbar.setTitle("通讯录");
                } else if (destinationId == R.id.navigation_discover) {
                    toolbar.setTitle("发现");
                } else if (destinationId == R.id.navigation_me) {
                    executorService.execute(() -> {
                        User user = database.userDao().findUserByUsername(username); // 使用 username 查询
                        runOnUiThread(() -> toolbar.setTitle(user != null ? user.getName() : "我"));
                    });
                } else {
                    toolbar.setTitle("WxChat");
                }
            });
        } else {
            throw new IllegalStateException("NavHostFragment with ID nav_host_fragment not found");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            Toast.makeText(this, "搜索", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_add) {
            Toast.makeText(this, "添加", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
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