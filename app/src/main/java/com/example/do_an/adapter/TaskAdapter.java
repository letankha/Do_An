package com.example.do_an.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.do_an.AddEditTaskActivity;
import com.example.do_an.R;
import com.example.do_an.database.DatabaseHelper;
import com.example.do_an.model.Task;
import com.example.do_an.util.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter dùng để hiển thị danh sách công việc trong RecyclerView
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private Context context;
    private List<Task> taskList;
    private DatabaseHelper databaseHelper;
    private OnTaskDeleteListener onTaskDeleteListener;

    /**
     * Giao diện callback khi người dùng xóa công việc
     */
    public interface OnTaskDeleteListener {
        void onTaskDelete(int position);
    }

    /**
     * Constructor của adapter
     * @param context context của Activity/Fragment
     * @param taskList danh sách công việc
     * @param listener callback xử lý xóa công việc
     */
    public TaskAdapter(Context context, List<Task> taskList, OnTaskDeleteListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.databaseHelper = new DatabaseHelper(context);
        this.onTaskDeleteListener = listener;
    }

    /**
     * Cập nhật lại dữ liệu adapter với danh sách mới
     * @param newTaskList danh sách công việc mới
     */
    public void updateData(List<Task> newTaskList) {
        this.taskList = newTaskList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tạo view item từ layout item_task.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        // Hiển thị tên công việc
        holder.textViewTaskName.setText(task.getName());

        // Định dạng ngày và giờ cho dễ nhìn
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date taskDate = new Date(task.getDateTime());

        holder.textViewTaskDate.setText(dateFormat.format(taskDate));
        holder.textViewTaskTime.setText(timeFormat.format(taskDate));

        // Hiển thị mô tả nếu có, ẩn nếu không có
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            holder.textViewTaskDescription.setText(task.getDescription());
            holder.textViewTaskDescription.setVisibility(View.VISIBLE);
        } else {
            holder.textViewTaskDescription.setVisibility(View.GONE);
        }

        // Xóa listener trước khi đặt trạng thái checkbox để tránh gọi không mong muốn
        holder.checkBoxTaskStatus.setOnCheckedChangeListener(null);
        holder.checkBoxTaskStatus.setChecked(task.isCompleted());

        // Nếu công việc đã hoàn thành thì gạch ngang tên
        if (task.isCompleted()) {
            holder.textViewTaskName.setPaintFlags(holder.textViewTaskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.textViewTaskName.setPaintFlags(holder.textViewTaskName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Đặt listener khi checkbox thay đổi trạng thái
        holder.checkBoxTaskStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    Task task = taskList.get(adapterPosition);

                    // Hủy thông báo cũ
                    NotificationHelper.cancelAlarm(context, task);

                    // Cập nhật trạng thái hoàn thành trong database
                    task.setCompleted(isChecked);
                    databaseHelper.updateTask(task);

                    // Nếu task chưa hoàn thành và thời gian trong tương lai, đặt lại thông báo
                    if (!isChecked && task.getDateTime() > System.currentTimeMillis()) {
                        NotificationHelper.scheduleAlarm(context, task);
                    }

                    // Cập nhật hiệu ứng gạch ngang
                    if (isChecked) {
                        holder.textViewTaskName.setPaintFlags(holder.textViewTaskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    } else {
                        holder.textViewTaskName.setPaintFlags(holder.textViewTaskName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    }

                    // Thông báo adapter cập nhật item
                    notifyItemChanged(adapterPosition);
                }
            }
        });

        // Xử lý sự kiện click vào item để mở màn hình chỉnh sửa
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    Task task = taskList.get(adapterPosition);

                    // Mở activity AddEditTaskActivity với dữ liệu task truyền đi
                    Intent intent = new Intent(context, AddEditTaskActivity.class);
                    intent.putExtra("task", task);
                    context.startActivity(intent);
                }
            }
        });

        // Xử lý sự kiện nhấn nút xóa công việc
        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && onTaskDeleteListener != null) {
                    onTaskDeleteListener.onTaskDelete(adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    /**
     * ViewHolder lưu giữ các view của một item task
     */
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTaskName;
        TextView textViewTaskDescription;
        TextView textViewTaskDate;
        TextView textViewTaskTime;
        CheckBox checkBoxTaskStatus;
        ImageButton buttonDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTaskName = itemView.findViewById(R.id.textViewTaskName);
            textViewTaskDescription = itemView.findViewById(R.id.textViewTaskDescription);
            textViewTaskDate = itemView.findViewById(R.id.textViewTaskDate);
            textViewTaskTime = itemView.findViewById(R.id.textViewTaskTime);
            checkBoxTaskStatus = itemView.findViewById(R.id.checkBoxTaskStatus);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
