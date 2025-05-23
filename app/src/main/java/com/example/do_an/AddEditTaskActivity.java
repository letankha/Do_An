package com.example.do_an;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.do_an.database.DatabaseHelper;
import com.example.do_an.model.Task;
import com.example.do_an.util.NotificationHelper;

import java.util.Calendar;

/**
 * Activity để thêm mới hoặc chỉnh sửa công việc (Task)
 */
public class AddEditTaskActivity extends AppCompatActivity {
    // Khai báo các view trong giao diện
    private TextView textViewTitle, textViewTaskId, textViewTaskIdValue;
    private EditText editTextTaskName, editTextTaskDescription;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private CheckBox checkBoxStatus;
    private Button buttonSave;

    private DatabaseHelper databaseHelper; // Đối tượng để thao tác với database
    private int userId;                   // ID người dùng hiện tại
    private Task existingTask;            // Công việc đang được chỉnh sửa (nếu có)
    private boolean isEditMode = false;   // Cờ xác định là đang sửa hay thêm mới

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        // Kích hoạt nút trở về (up navigation) trên thanh ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Khởi tạo đối tượng DatabaseHelper để thao tác dữ liệu
        databaseHelper = new DatabaseHelper(this);

        // Lấy userId đã lưu trong SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("task_manager_prefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Lỗi: Người dùng chưa đăng nhập", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Ánh xạ các view với layout
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewTaskId = findViewById(R.id.textViewTaskId);
        textViewTaskIdValue = findViewById(R.id.textViewTaskIdValue);
        editTextTaskName = findViewById(R.id.editTextTaskName);
        editTextTaskDescription = findViewById(R.id.editTextTaskDescription);
        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);
        checkBoxStatus = findViewById(R.id.checkBoxStatus);
        buttonSave = findViewById(R.id.buttonSave);

        // Thiết lập hiển thị giờ theo định dạng 24h
        timePicker.setIs24HourView(true);

        // Kiểm tra xem có task được truyền vào để chỉnh sửa hay không
        if (getIntent().hasExtra("task")) {
            isEditMode = true;
            existingTask = (Task) getIntent().getSerializableExtra("task");

            // Cập nhật tiêu đề và hiển thị ID công việc khi ở chế độ sửa
            textViewTitle.setText(R.string.edit_task);
            textViewTaskId.setVisibility(View.VISIBLE);
            textViewTaskIdValue.setVisibility(View.VISIBLE);
            textViewTaskIdValue.setText(String.valueOf(existingTask.getId()));

            // Điền thông tin công việc vào các trường nhập liệu
            editTextTaskName.setText(existingTask.getName());
            editTextTaskDescription.setText(existingTask.getDescription());
            checkBoxStatus.setChecked(existingTask.isCompleted());

            // Lấy thời gian của task hiện tại để cập nhật DatePicker và TimePicker
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(existingTask.getDateTime());

            // Cập nhật DatePicker theo thời gian công việc
            datePicker.updateDate(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            // Cập nhật TimePicker theo thời gian công việc, tùy theo SDK version
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
                timePicker.setMinute(calendar.get(Calendar.MINUTE));
            } else {
                timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
                timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
            }
        } else {
            // Nếu là thêm mới thì đặt tiêu đề phù hợp và ẩn ID task
            textViewTitle.setText(R.string.add_task);
            textViewTaskId.setVisibility(View.GONE);
            textViewTaskIdValue.setVisibility(View.GONE);
        }

        // Thiết lập sự kiện cho nút lưu công việc
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask();  // Gọi hàm lưu công việc khi nhấn nút
            }
        });
    }

    /**
     * Hàm lưu dữ liệu công việc mới hoặc cập nhật công việc cũ
     */
    private void saveTask() {
        // Lấy dữ liệu nhập từ người dùng
        String name = editTextTaskName.getText().toString().trim();
        String description = editTextTaskDescription.getText().toString().trim();
        boolean completed = checkBoxStatus.isChecked();

        // Kiểm tra bắt buộc phải nhập tên công việc
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.fields_required, Toast.LENGTH_SHORT).show();
            return;  // Nếu không nhập tên thì dừng và báo lỗi
        }

        // Lấy ngày giờ từ DatePicker và TimePicker
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, datePicker.getYear());
        calendar.set(Calendar.MONTH, datePicker.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());

        // Lấy giờ phút theo phiên bản SDK Android
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
            calendar.set(Calendar.MINUTE, timePicker.getMinute());
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
            calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
        }

        calendar.set(Calendar.SECOND, 0);  // Đặt giây là 0

        long dateTime = calendar.getTimeInMillis();  // Lấy timestamp tính bằng millis

        if (isEditMode) {
            // Nếu là sửa thì hủy thông báo nhắc nhở cũ trước
            NotificationHelper.cancelAlarm(this, existingTask);

            // Cập nhật dữ liệu công việc hiện tại
            existingTask.setName(name);
            existingTask.setDescription(description);
            existingTask.setDateTime(dateTime);
            existingTask.setCompleted(completed);

            // Cập nhật công việc vào database
            databaseHelper.updateTask(existingTask);

            // Nếu công việc chưa hoàn thành và thời gian còn trong tương lai thì đặt lại thông báo nhắc nhở mới
            if (!completed && dateTime > System.currentTimeMillis()) {
                NotificationHelper.scheduleAlarm(this, existingTask);
            }

            // Hiển thị thông báo cập nhật thành công
            Toast.makeText(this, R.string.task_updated, Toast.LENGTH_SHORT).show();
        } else {
            // Nếu là thêm mới thì tạo đối tượng task mới
            Task newTask = new Task(name, description, dateTime, completed);

            // Thêm task mới vào database, lấy id trả về
            long taskId = databaseHelper.addTask(newTask, userId);

            // Gán id vừa tạo cho đối tượng task
            newTask.setId((int) taskId);

            // Nếu công việc chưa hoàn thành và thời gian trong tương lai thì đặt thông báo nhắc nhở
            if (!completed && dateTime > System.currentTimeMillis()) {
                NotificationHelper.scheduleAlarm(this, newTask);
            }

            // Hiển thị thông báo thêm thành công
            Toast.makeText(this, R.string.task_added, Toast.LENGTH_SHORT).show();
        }

        // Trả về kết quả OK và đóng activity
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Xử lý khi nhấn nút quay lại trên ActionBar
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
