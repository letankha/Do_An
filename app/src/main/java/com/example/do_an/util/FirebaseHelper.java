package com.example.do_an.util;

import android.content.Context;

import com.example.do_an.R;
import com.example.do_an.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp trợ giúp để đồng bộ dữ liệu lên "đám mây" (giả lập)
 * Ghi chú: Triển khai này chỉ sử dụng bộ nhớ tạm trong máy (giả lập lưu trữ cloud)
 * Trong ứng dụng thực tế, bạn sẽ dùng Firebase Firestore hoặc Realtime Database
 */
public class FirebaseHelper {

    // Biến Singleton để chỉ có một instance của lớp này
    private static FirebaseHelper instance;

    // Bộ nhớ cache tạm để giả lập lưu trữ đám mây theo từng user
    private Map<String, List<Task>> userTasksMap;

    // Trạng thái kết nối tới "cloud"
    private boolean isConnected;

    // Constructor riêng để áp dụng Singleton
    private FirebaseHelper() {
        userTasksMap = new HashMap<>();
        isConnected = false;
    }

    /**
     * Lấy instance Singleton
     */
    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    /**
     * Kiểm tra đã kết nối cloud chưa
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Giả lập kết nối tới Firebase
     */
    public void connect(Context context, final OnConnectListener listener) {
        // Trong thực tế sẽ thực hiện đăng nhập vào Firebase ở đây

        // Giả lập độ trễ kết nối
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000); // đợi 1 giây

                    isConnected = true;

                    if (listener != null && context != null) {
                        listener.onConnectSuccess(); // gọi callback khi thành công
                    }
                } catch (InterruptedException e) {
                    isConnected = false;
                    if (listener != null && context != null) {
                        listener.onConnectFailure(e.getMessage()); // báo lỗi nếu thất bại
                    }
                }
            }
        }).start();
    }

    /**
     * Ngắt kết nối khỏi Firebase (giả lập)
     */
    public void disconnect() {
        isConnected = false;
    }

    /**
     * Đồng bộ (upload) danh sách công việc lên "cloud"
     */
    public void uploadTasks(final Context context, final String userId, final List<Task> tasks,
                            final OnSyncListener listener) {
        if (!isConnected) {
            if (listener != null) {
                listener.onSyncFailure(context.getString(R.string.error_not_connected));
            }
            return;
        }

        // Giả lập thao tác mạng trên luồng nền
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500); // độ trễ 1.5s

                    // Lưu danh sách task vào bộ nhớ cache theo user
                    userTasksMap.put(userId, new ArrayList<>(tasks));

                    if (listener != null && context != null) {
                        listener.onSyncSuccess(); // callback khi thành công
                    }
                } catch (InterruptedException e) {
                    if (listener != null && context != null) {
                        listener.onSyncFailure(e.getMessage());
                    }
                }
            }
        }).start();
    }

    /**
     * Tải xuống danh sách công việc từ "cloud"
     */
    public void downloadTasks(final Context context, final String userId, final OnDownloadListener listener) {
        if (!isConnected) {
            if (listener != null) {
                listener.onDownloadFailure(context.getString(R.string.error_not_connected));
            }
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500); // giả lập tải dữ liệu

                    final List<Task> tasks = userTasksMap.get(userId);

                    if (listener != null && context != null) {
                        if (tasks != null) {
                            listener.onDownloadSuccess(tasks); // có dữ liệu thì trả về
                        } else {
                            listener.onDownloadSuccess(new ArrayList<Task>()); // không có thì trả list rỗng
                        }
                    }
                } catch (InterruptedException e) {
                    if (listener != null && context != null) {
                        listener.onDownloadFailure(e.getMessage());
                    }
                }
            }
        }).start();
    }

    /**
     * Xóa toàn bộ dữ liệu người dùng khỏi "cloud"
     */
    public void deleteUserData(final Context context, final String userId, final OnDeleteListener listener) {
        if (!isConnected) {
            if (listener != null) {
                listener.onDeleteFailure(context.getString(R.string.error_not_connected));
            }
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000); // giả lập thời gian xử lý

                    userTasksMap.remove(userId); // xóa dữ liệu người dùng

                    if (listener != null && context != null) {
                        listener.onDeleteSuccess(); // callback thành công
                    }
                } catch (InterruptedException e) {
                    if (listener != null && context != null) {
                        listener.onDeleteFailure(e.getMessage());
                    }
                }
            }
        }).start();
    }

    /**
     * Giao diện callback khi kết nối
     */
    public interface OnConnectListener {
        void onConnectSuccess();                // Kết nối thành công
        void onConnectFailure(String errorMessage); // Kết nối thất bại
    }

    /**
     * Giao diện callback khi upload (đồng bộ)
     */
    public interface OnSyncListener {
        void onSyncSuccess();                  // Đồng bộ thành công
        void onSyncFailure(String errorMessage);   // Thất bại
    }

    /**
     * Giao diện callback khi download dữ liệu
     */
    public interface OnDownloadListener {
        void onDownloadSuccess(List<Task> tasks);  // Tải dữ liệu thành công
        void onDownloadFailure(String errorMessage); // Lỗi khi tải
    }

    /**
     * Giao diện callback khi xóa dữ liệu
     */
    public interface OnDeleteListener {
        void onDeleteSuccess();               // Xóa thành công
        void onDeleteFailure(String errorMessage); // Lỗi khi xóa
    }
}
