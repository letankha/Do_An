package com.example.do_an.model;

import java.io.Serializable;

/**
 * Lớp mô hình dữ liệu Task (Công việc)
 * Implements Serializable để có thể truyền đối tượng Task giữa các Activity thông qua Intent
 */
public class Task implements Serializable {
    private int id;                 // ID công việc (do database tạo tự động)
    private String name;            // Tên công việc
    private String description;     // Mô tả chi tiết công việc
    private long dateTime;          // Thời gian thực hiện công việc (dưới dạng timestamp, mili giây)
    private boolean completed;      // Trạng thái hoàn thành (true = đã hoàn thành, false = chưa)
    private int userId;             // ID người dùng sở hữu công việc này

    /**
     * Constructor khởi tạo công việc mới (chưa có ID)
     * Dùng khi tạo công việc mới trước khi lưu vào database
     */
    public Task(String name, String description, long dateTime, boolean completed) {
        this.name = name;
        this.description = description;
        this.dateTime = dateTime;
        this.completed = completed;
    }

    /**
     * Constructor khởi tạo công việc đầy đủ (có ID)
     * Dùng khi lấy dữ liệu công việc từ database
     */
    public Task(int id, String name, String description, long dateTime, boolean completed, int userId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.dateTime = dateTime;
        this.completed = completed;
        this.userId = userId;
    }

    // Các phương thức lấy (getter) và đặt (setter) giá trị thuộc tính

    /**
     * Lấy ID công việc
     */
    public int getId() {
        return id;
    }

    /**
     * Đặt ID công việc
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Lấy tên công việc
     */
    public String getName() {
        return name;
    }

    /**
     * Đặt tên công việc
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Lấy mô tả công việc
     */
    public String getDescription() {
        return description;
    }

    /**
     * Đặt mô tả công việc
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Lấy thời gian thực hiện công việc (timestamp mili giây)
     */
    public long getDateTime() {
        return dateTime;
    }

    /**
     * Đặt thời gian thực hiện công việc (timestamp mili giây)
     */
    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Kiểm tra trạng thái hoàn thành công việc
     * @return true nếu công việc đã hoàn thành, ngược lại false
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Đặt trạng thái hoàn thành công việc
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * Lấy ID người dùng sở hữu công việc
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Đặt ID người dùng sở hữu công việc
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }
}
