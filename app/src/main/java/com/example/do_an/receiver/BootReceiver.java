package com.example.do_an.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.do_an.database.DatabaseHelper;
import com.example.do_an.model.Task;
import com.example.do_an.util.NotificationHelper;

import java.util.List;

/**
 * BroadcastReceiver dùng để xử lý khi thiết bị khởi động lại (BOOT_COMPLETED)
 * Mục đích: Đặt lại (reschedule) tất cả các thông báo nhắc nhở (alarm) sau khi thiết bị được khởi động lại.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Kiểm tra xem Intent có hành động là BOOT_COMPLETED không
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            // Lấy user_id đã lưu trong SharedPreferences (để biết người dùng nào đang đăng nhập)
            SharedPreferences sharedPreferences = context.getSharedPreferences("task_manager_prefs", Context.MODE_PRIVATE);
            int userId = sharedPreferences.getInt("user_id", -1);

            // Nếu có người dùng đăng nhập (userId hợp lệ)
            if (userId != -1) {
                // Khởi tạo đối tượng thao tác cơ sở dữ liệu
                DatabaseHelper databaseHelper = new DatabaseHelper(context);

                // Lấy danh sách tất cả các công việc của người dùng
                List<Task> taskList = databaseHelper.getAllTasks(userId);

                // Lấy thời gian hiện tại để so sánh
                long currentTime = System.currentTimeMillis();

                // Duyệt qua tất cả các công việc
                for (Task task : taskList) {
                    // Nếu công việc chưa hoàn thành và thời gian diễn ra trong tương lai
                    if (!task.isCompleted() && task.getDateTime() > currentTime) {
                        // Đặt lại (lên lịch lại) thông báo nhắc nhở cho công việc đó
                        NotificationHelper.scheduleAlarm(context, task);
                    }
                }
            }
        }
    }
}
