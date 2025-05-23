package com.example.do_an;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.do_an.database.DatabaseHelper;
import com.example.do_an.model.Task;
import com.example.do_an.util.FirebaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity for cloud synchronization
 */
public class CloudSyncActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView textViewConnectionStatus;
    private TextView textViewLastSync;
    private Button buttonConnect;
    private CardView cardViewSyncOptions;
    private Switch switchAutoSync;
    private Button buttonUpload;
    private Button buttonDownload;
    private ProgressBar progressBar;
    private TextView textViewProgress;

    private FirebaseHelper firebaseHelper;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_sync);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        textViewConnectionStatus = findViewById(R.id.textViewConnectionStatus);
        textViewLastSync = findViewById(R.id.textViewLastSync);
        buttonConnect = findViewById(R.id.buttonConnect);
        cardViewSyncOptions = findViewById(R.id.cardViewSyncOptions);
        switchAutoSync = findViewById(R.id.switchAutoSync);
        buttonUpload = findViewById(R.id.buttonUpload);
        buttonDownload = findViewById(R.id.buttonDownload);
        progressBar = findViewById(R.id.progressBar);
        textViewProgress = findViewById(R.id.textViewProgress);

        // Set up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.cloud_sync);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize helpers
        firebaseHelper = FirebaseHelper.getInstance();
        databaseHelper = new DatabaseHelper(this);

        // Get user id from shared preferences
        sharedPreferences = getSharedPreferences("task_manager_prefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);

        // Load preferences
        boolean autoSync = sharedPreferences.getBoolean("auto_sync", false);
        switchAutoSync.setChecked(autoSync);

        // Load last sync time
        long lastSyncTime = sharedPreferences.getLong("last_sync_time", 0);
        if (lastSyncTime > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String lastSyncString = sdf.format(new Date(lastSyncTime));
            textViewLastSync.setText(getString(R.string.sync_last, lastSyncString));
            textViewLastSync.setVisibility(View.VISIBLE);
        }

        // Set button click listeners
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleConnection();
            }
        });

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadTasks();
            }
        });

        buttonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDownloadConfirmDialog();
            }
        });

        switchAutoSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save auto sync preference
                boolean autoSync = switchAutoSync.isChecked();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("auto_sync", autoSync);
                editor.apply();

                // Show status message
                Toast.makeText(CloudSyncActivity.this,
                        autoSync ? "Đã bật tự động đồng bộ" : "Đã tắt tự động đồng bộ",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Update UI based on connection status
        updateConnectionUI();
    }

    /**
     * Update UI based on connection status
     */
    private void updateConnectionUI() {
        boolean isConnected = firebaseHelper.isConnected();

        if (isConnected) {
            textViewConnectionStatus.setText(R.string.sync_connected);
            buttonConnect.setText(R.string.disconnect_cloud);
            cardViewSyncOptions.setVisibility(View.VISIBLE);
        } else {
            textViewConnectionStatus.setText(R.string.sync_not_connected);
            buttonConnect.setText(R.string.connect_cloud);
            cardViewSyncOptions.setVisibility(View.GONE);
        }
    }

    /**
     * Toggle cloud connection
     */
    private void toggleConnection() {
        if (firebaseHelper.isConnected()) {
            // Disconnect from cloud
            firebaseHelper.disconnect();
            updateConnectionUI();
            Toast.makeText(this, "Đã ngắt kết nối đám mây", Toast.LENGTH_SHORT).show();
        } else {
            // Connect to cloud
            showProgress(getString(R.string.sync_connecting));

            firebaseHelper.connect(this, new FirebaseHelper.OnConnectListener() {
                @Override
                public void onConnectSuccess() {
                    hideProgress();
                    updateConnectionUI();
                    Toast.makeText(CloudSyncActivity.this, "Đã kết nối đám mây", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onConnectFailure(String errorMessage) {
                    hideProgress();
                    Toast.makeText(CloudSyncActivity.this, "Kết nối thất bại: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Upload tasks to cloud
     */
    private void uploadTasks() {
        // Get tasks from database
        List<Task> tasks = databaseHelper.getAllTasks(userId);

        if (tasks.isEmpty()) {
            Toast.makeText(this, "Không có công việc để đồng bộ", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(getString(R.string.sync_uploading));

        firebaseHelper.uploadTasks(this, String.valueOf(userId), tasks, new FirebaseHelper.OnSyncListener() {
            @Override
            public void onSyncSuccess() {
                hideProgress();
                // Save last sync time
                SharedPreferences.Editor editor = sharedPreferences.edit();
                long currentTime = System.currentTimeMillis();
                editor.putLong("last_sync_time", currentTime);
                editor.apply();

                // Update last sync text
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String lastSyncString = sdf.format(new Date(currentTime));
                textViewLastSync.setText(getString(R.string.sync_last, lastSyncString));
                textViewLastSync.setVisibility(View.VISIBLE);

                Toast.makeText(CloudSyncActivity.this, R.string.sync_success, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSyncFailure(String errorMessage) {
                hideProgress();
                Toast.makeText(CloudSyncActivity.this, getString(R.string.sync_failure, errorMessage), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Show confirmation dialog for download
     */
    private void showDownloadConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tải xuống từ đám mây");
        builder.setMessage("Dữ liệu hiện tại sẽ bị ghi đè bởi dữ liệu từ đám mây. Bạn có chắc muốn tiếp tục?");
        builder.setPositiveButton("Tiếp tục", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadTasks();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    /**
     * Download tasks from cloud
     */
    private void downloadTasks() {
        showProgress(getString(R.string.sync_downloading));

        firebaseHelper.downloadTasks(this, String.valueOf(userId), new FirebaseHelper.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(List<Task> tasks) {
                hideProgress();

                if (tasks.isEmpty()) {
                    Toast.makeText(CloudSyncActivity.this, "Không có dữ liệu trên đám mây", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show confirmation with task count
                AlertDialog.Builder builder = new AlertDialog.Builder(CloudSyncActivity.this);
                builder.setTitle("Xác nhận ghi đè");
                builder.setMessage("Đã tìm thấy " + tasks.size() + " công việc. Ghi đè dữ liệu hiện tại?");
                builder.setPositiveButton("Ghi đè", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Replace local tasks with cloud tasks
                        replaceLocalTasks(tasks);
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.show();
            }

            @Override
            public void onDownloadFailure(String errorMessage) {
                hideProgress();
                Toast.makeText(CloudSyncActivity.this, getString(R.string.sync_failure, errorMessage), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Replace local tasks with cloud tasks
     */
    private void replaceLocalTasks(List<Task> cloudTasks) {
        showProgress("Đang cập nhật cơ sở dữ liệu...");

        // Run in background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Get all local tasks
                List<Task> localTasks = databaseHelper.getAllTasks(userId);

                // Delete all local tasks
                for (Task task : localTasks) {
                    databaseHelper.deleteTask(task.getId());
                }

                // Add all cloud tasks
                for (Task task : cloudTasks) {
                    databaseHelper.addTask(task, userId);
                }

                // Save last sync time
                SharedPreferences.Editor editor = sharedPreferences.edit();
                long currentTime = System.currentTimeMillis();
                editor.putLong("last_sync_time", currentTime);
                editor.apply();

                // Update UI on UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();

                        // Update last sync text
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        String lastSyncString = sdf.format(new Date(currentTime));
                        textViewLastSync.setText(getString(R.string.sync_last, lastSyncString));
                        textViewLastSync.setVisibility(View.VISIBLE);

                        Toast.makeText(CloudSyncActivity.this, "Đã cập nhật " + cloudTasks.size() + " công việc từ đám mây", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    /**
     * Show progress indicator
     */
    private void showProgress(String message) {
        progressBar.setVisibility(View.VISIBLE);
        textViewProgress.setText(message);
        textViewProgress.setVisibility(View.VISIBLE);

        // Disable buttons
        buttonConnect.setEnabled(false);
        buttonUpload.setEnabled(false);
        buttonDownload.setEnabled(false);
    }

    /**
     * Hide progress indicator
     */
    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
        textViewProgress.setVisibility(View.GONE);

        // Enable buttons
        buttonConnect.setEnabled(true);
        buttonUpload.setEnabled(true);
        buttonDownload.setEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}