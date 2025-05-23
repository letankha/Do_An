package com.example.do_an;

import android.content.Intent;
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
 * Activity dành cho đăng ký người dùng mới
 */
public class RegisterActivity extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private TextView textViewLogin;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo đối tượng hỗ trợ cơ sở dữ liệu
        databaseHelper = new DatabaseHelper(this);

        // Ánh xạ các thành phần giao diện
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);

        // Xử lý khi nhấn nút "Đăng ký"
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser(); // Gọi hàm xử lý đăng ký
            }
        });

        // Xử lý khi nhấn "Đã có tài khoản? Đăng nhập"
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Trở lại màn hình đăng nhập
            }
        });
    }

    /**
     * Hàm xử lý đăng ký người dùng mới
     */
    private void registerUser() {
        // Lấy thông tin người dùng nhập
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Kiểm tra xem các trường có bị bỏ trống không
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, R.string.fields_required, Toast.LENGTH_SHORT).show(); // "Vui lòng điền đầy đủ thông tin"
            return;
        }

        // Kiểm tra xem mật khẩu và xác nhận mật khẩu có khớp không
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, R.string.passwords_not_match, Toast.LENGTH_SHORT).show(); // "Mật khẩu không khớp"
            return;
        }

        // Kiểm tra xem tên người dùng đã tồn tại trong cơ sở dữ liệu chưa
        if (databaseHelper.isUsernameExists(username)) {
            Toast.makeText(this, R.string.username_exists, Toast.LENGTH_SHORT).show(); // "Tên đăng nhập đã tồn tại"
            return;
        }

        // Tạo đối tượng người dùng mới
        User user = new User(username, password);

        // Thêm người dùng vào cơ sở dữ liệu
        long userId = databaseHelper.addUser(user);

        if (userId != -1) {
            // Thêm một số công việc mẫu cho người dùng mới
            databaseHelper.addSampleTasks((int) userId);

            // Hiển thị thông báo đăng ký thành công
            Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show(); // "Đăng ký thành công"

            // Chuyển về màn hình đăng nhập
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        } else {
            // Hiển thị lỗi nếu không thêm được người dùng
            Toast.makeText(this, R.string.register_failed, Toast.LENGTH_SHORT).show(); // "Đăng ký thất bại"
        }
    }
}
