package com.example.do_an;

// Các import cần thiết
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.do_an.adapter.TaskAdapter;
import com.example.do_an.database.DatabaseHelper;
import com.example.do_an.model.Task;
import com.example.do_an.util.NotificationHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Activity chính hiển thị danh sách công việc
 */
public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskDeleteListener {
    // Các mã yêu cầu
    private static final int REQUEST_ADD_TASK = 1;
    private static final int REQUEST_EDIT_TASK = 2;

    // Các hằng số lọc
    private static final int FILTER_ALL = 0;
    private static final int FILTER_COMPLETED = 1;
    private static final int FILTER_INCOMPLETE = 2;

    // Các hằng số sắp xếp
    private static final int SORT_DATE_ASC = 0;
    private static final int SORT_DATE_DESC = 1;
    private static final int SORT_NAME_ASC = 2;
    private static final int SORT_NAME_DESC = 3;

    // Khai báo biến giao diện
    private Toolbar toolbar;
    private RecyclerView recyclerViewTasks;
    private TextView textViewNoTasks;
    private FloatingActionButton fabAddTask;

    // Adapter, DB và dữ liệu
    private TaskAdapter taskAdapter;
    private DatabaseHelper databaseHelper;
    private int userId;
    private List<Task> taskList;
    private List<Task> filteredTaskList;

    private int currentFilter = FILTER_ALL;
    private int currentSort = SORT_DATE_ASC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo database
        databaseHelper = new DatabaseHelper(this);

        // Lấy userId từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("task_manager_prefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);

        // Nếu chưa đăng nhập thì chuyển về LoginActivity
        if (userId == -1) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Tạo notification channel
        NotificationHelper.createNotificationChannel(this);

        // Ánh xạ view
        toolbar = findViewById(R.id.toolbar);
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        textViewNoTasks = findViewById(R.id.textViewNoTasks);
        fabAddTask = findViewById(R.id.fabAddTask);

        // Thiết lập toolbar
        setSupportActionBar(toolbar);

        // Thiết lập RecyclerView
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));

        // Tải dữ liệu
        loadTasks();

        // Xử lý sự kiện nút thêm công việc
        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
                startActivityForResult(intent, REQUEST_ADD_TASK);
            }
        });
    }

    // Tải danh sách công việc từ database
    private void loadTasks() {
        taskList = databaseHelper.getAllTasks(userId);
        applyFilterAndSort();
    }

    // Áp dụng bộ lọc và sắp xếp danh sách công việc
    private void applyFilterAndSort() {
        filteredTaskList = new ArrayList<>();

        // Lọc theo trạng thái
        for (Task task : taskList) {
            switch (currentFilter) {
                case FILTER_ALL:
                    filteredTaskList.add(task);
                    break;
                case FILTER_COMPLETED:
                    if (task.isCompleted()) filteredTaskList.add(task);
                    break;
                case FILTER_INCOMPLETE:
                    if (!task.isCompleted()) filteredTaskList.add(task);
                    break;
            }
        }

        // Sắp xếp theo điều kiện
        switch (currentSort) {
            case SORT_DATE_ASC:
                Collections.sort(filteredTaskList, (t1, t2) -> Long.compare(t1.getDateTime(), t2.getDateTime()));
                break;
            case SORT_DATE_DESC:
                Collections.sort(filteredTaskList, (t1, t2) -> Long.compare(t2.getDateTime(), t1.getDateTime()));
                break;
            case SORT_NAME_ASC:
                Collections.sort(filteredTaskList, (t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName()));
                break;
            case SORT_NAME_DESC:
                Collections.sort(filteredTaskList, (t1, t2) -> t2.getName().compareToIgnoreCase(t1.getName()));
                break;
        }

        updateTaskList();
    }

    // Cập nhật giao diện danh sách công việc
    private void updateTaskList() {
        if (filteredTaskList.isEmpty()) {
            textViewNoTasks.setVisibility(View.VISIBLE);
            recyclerViewTasks.setVisibility(View.GONE);
        } else {
            textViewNoTasks.setVisibility(View.GONE);
            recyclerViewTasks.setVisibility(View.VISIBLE);

            if (taskAdapter == null) {
                taskAdapter = new TaskAdapter(this, filteredTaskList, this);
                recyclerViewTasks.setAdapter(taskAdapter);
            } else {
                taskAdapter.updateData(filteredTaskList);
            }
        }
    }

    // Nhận kết quả từ Add/Edit Task
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode == REQUEST_ADD_TASK || requestCode == REQUEST_EDIT_TASK)) {
            loadTasks();
        }
    }

    // Xóa công việc
    @Override
    public void onTaskDelete(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_task);
        builder.setMessage(getString(R.string.delete_task) + " '" + filteredTaskList.get(position).getName() + "'?");
        builder.setPositiveButton(R.string.delete_task, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Task taskToDelete = filteredTaskList.get(position);
                NotificationHelper.cancelAlarm(MainActivity.this, taskToDelete);
                databaseHelper.deleteTask(taskToDelete.getId());
                loadTasks();
                Toast.makeText(MainActivity.this, R.string.task_deleted, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    // Hiển thị thống kê công việc
    private void showStatisticsDialog() {
        int totalTasks = taskList.size();
        int completedTasks = 0, incompleteTasks = 0, upcomingTasks = 0, overdueTasks = 0;
        long currentTime = System.currentTimeMillis();

        for (Task task : taskList) {
            if (task.isCompleted()) {
                completedTasks++;
            } else {
                incompleteTasks++;
                if (task.getDateTime() > currentTime) {
                    upcomingTasks++;
                } else {
                    overdueTasks++;
                }
            }
        }

        float completedPercentage = totalTasks > 0 ? (float) completedTasks / totalTasks * 100 : 0;
        float incompletePercentage = totalTasks > 0 ? (float) incompleteTasks / totalTasks * 100 : 0;
        DecimalFormat df = new DecimalFormat("#.#");

        View view = getLayoutInflater().inflate(R.layout.dialog_statistics, null);
        TextView textViewStatsTotal = view.findViewById(R.id.textViewStatsTotal);
        TextView textViewStatsCompleted = view.findViewById(R.id.textViewStatsCompleted);
        TextView textViewStatsIncomplete = view.findViewById(R.id.textViewStatsIncomplete);
        TextView textViewStatsUpcoming = view.findViewById(R.id.textViewStatsUpcoming);
        TextView textViewStatsOverdue = view.findViewById(R.id.textViewStatsOverdue);
        ProgressBar progressBarCompleted = view.findViewById(R.id.progressBarCompleted);
        ProgressBar progressBarIncomplete = view.findViewById(R.id.progressBarIncomplete);

        textViewStatsTotal.setText(getString(R.string.stats_total, totalTasks));
        textViewStatsCompleted.setText(getString(R.string.stats_completed, completedTasks, df.format(completedPercentage)));
        textViewStatsIncomplete.setText(getString(R.string.stats_incomplete, incompleteTasks, df.format(incompletePercentage)));
        textViewStatsUpcoming.setText(getString(R.string.stats_upcoming, upcomingTasks));
        textViewStatsOverdue.setText(getString(R.string.stats_overdue, overdueTasks));

        progressBarCompleted.setMax(100);
        progressBarIncomplete.setMax(100);
        progressBarCompleted.setProgress((int) completedPercentage);
        progressBarIncomplete.setProgress((int) incompletePercentage);

        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton(R.string.save, null)
                .show();
    }

    // Tạo menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    // Xử lý lựa chọn menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        // Lọc
        if (itemId == R.id.filter_all) {
            currentFilter = FILTER_ALL;
        } else if (itemId == R.id.filter_completed) {
            currentFilter = FILTER_COMPLETED;
        } else if (itemId == R.id.filter_incomplete) {
            currentFilter = FILTER_INCOMPLETE;
        }

        // Sắp xếp
        else if (itemId == R.id.sort_date_asc) {
            currentSort = SORT_DATE_ASC;
        } else if (itemId == R.id.sort_date_desc) {
            currentSort = SORT_DATE_DESC;
        } else if (itemId == R.id.sort_name_asc) {
            currentSort = SORT_NAME_ASC;
        } else if (itemId == R.id.sort_name_desc) {
            currentSort = SORT_NAME_DESC;
        }

        // Thống kê
        else if (itemId == R.id.action_stats) {
            showStatisticsDialog();
            return true;
        }

        // Đồng bộ đám mây
        else if (itemId == R.id.action_cloud_sync) {
            startActivity(new Intent(MainActivity.this, CloudSyncActivity.class));
            return true;
        }

        // Đăng xuất
        else if (itemId == R.id.action_logout) {
            SharedPreferences sharedPreferences = getSharedPreferences("task_manager_prefs", MODE_PRIVATE);
            sharedPreferences.edit().clear().apply();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        applyFilterAndSort();
        return true;
    }
}
