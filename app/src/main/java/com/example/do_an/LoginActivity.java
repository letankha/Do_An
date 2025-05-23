package com.example.do_an;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.do_an.database.DatabaseHelper;
import com.example.do_an.model.User;

/**
 * Màn hình đăng nhập người dùng
 */
public class LoginActivity extends AppCompatActivity {

    // Khai báo các view
    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;

    // Đối tượng hỗ trợ cơ sở dữ liệu
    private DatabaseHelper databaseHelper;

    // Đối tượng SharedPreferences để lưu dữ liệu đăng nhập
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo đối tượng trợ giúp cơ sở dữ liệu
        databaseHelper = new DatabaseHelper(this);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("task_manager_prefs", MODE_PRIVATE);

        // Kiểm tra nếu người dùng đã đăng nhập từ trước
        if (sharedPreferences.getInt("user_id", -1) != -1) {
            // Nếu đã đăng nhập, chuyển đến màn hình chính
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        // Ánh xạ các view từ layout
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);

        // Xử lý sự kiện khi nhấn nút đăng nhập
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser(); // Gọi hàm xử lý đăng nhập
            }
        });

        // Xử lý sự kiện khi nhấn vào "Đăng ký"
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang màn hình đăng ký
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    /**
     * Hàm xử lý đăng nhập người dùng
     */
    private void loginUser() {
        // Lấy tên đăng nhập và mật khẩu từ EditText
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Kiểm tra nếu bỏ trống tên hoặc mật khẩu
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.fields_required, Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra thông tin đăng nhập từ cơ sở dữ liệu
        User user = databaseHelper.getUser(username, password);

        if (user != null) {
            // Nếu đúng, lưu thông tin người dùng vào SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("user_id", user.getId());
            editor.putString("username", user.getUsername());
            editor.apply();

            // Thông báo đăng nhập thành công
            Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();

            // Chuyển sang màn hình chính
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            // Nếu sai thông tin đăng nhập, thông báo lỗi
            Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
