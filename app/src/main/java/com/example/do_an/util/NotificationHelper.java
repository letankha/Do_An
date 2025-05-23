package com.example.do_an.util;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.do_an.R;
import com.example.do_an.model.Task;
import com.example.do_an.receiver.AlarmReceiver;

/**
 * Lớp trợ giúp để tạo và xử lý thông báo nhắc nhở công việc
 */
public class NotificationHelper {
    // ID kênh thông báo (dùng cho Android 8.0+)
    private static final String CHANNEL_ID = "task_manager_channel";

    /**
     * Tạo kênh thông báo cho Android 8.0 trở lên (bắt buộc)
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Lấy tên và mô tả từ strings.xml
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;

            // Tạo kênh
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Đăng ký kênh với hệ thống
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Trả về ID của kênh thông báo
     */
    public static String getChannelId() {
        return CHANNEL_ID;
    }

    /**
     * Đặt báo thức (alarm) để thông báo nhắc nhở công việc
     */
    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleAlarm(Context context, Task task) {
        // Nếu công việc đã hoàn thành thì không cần nhắc
        if (task.isCompleted()) {
            return;
        }

        // Lấy dịch vụ báo thức
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        // Tạo intent gửi tới AlarmReceiver khi tới giờ hẹn
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("task_id", task.getId());
        intent.putExtra("task_name", task.getName());

        // Tạo PendingIntent để hệ thống gọi khi đến giờ
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getId(), // sử dụng ID công việc để phân biệt
                intent,
                flags
        );

        // Lấy thời điểm kích hoạt từ đối tượng Task (dạng milliseconds)
        long triggerTime = task.getDateTime();

        // Đặt báo thức chính xác
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        }
    }

    /**
     * Hủy báo thức đã đặt nếu người dùng xóa hoặc sửa task
     */
    public static void cancelAlarm(Context context, Task task) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, AlarmReceiver.class);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getId(), // phải khớp ID đã đặt khi schedule
                intent,
                flags
        );

        // Hủy báo thức
        alarmManager.cancel(pendingIntent);
    }
}
