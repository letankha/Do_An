package com.example.do_an.model;

/**
 * Lớp mô hình dữ liệu User (Người dùng)
 */
public class User {
    private int id;             // ID của người dùng (được tạo tự động trong database)
    private String username;    // Tên đăng nhập của người dùng
    private String password;    // Mật khẩu của người dùng

    /**
     * Constructor khởi tạo đối tượng User mới (chưa có ID)
     * Dùng khi tạo người dùng mới trước khi lưu vào database
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Constructor khởi tạo đối tượng User đầy đủ (có ID)
     * Dùng khi lấy dữ liệu người dùng từ database
     */
    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    // Các phương thức lấy (getter) và đặt (setter) giá trị thuộc tính

    /**
     * Lấy ID của người dùng
     */
    public int getId() {
        return id;
    }

    /**
     * Đặt ID cho người dùng
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Lấy tên đăng nhập
     */
    public String getUsername() {
        return username;
    }

    /**
     * Đặt tên đăng nhập
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Lấy mật khẩu
     */
    public String getPassword() {
        return password;
    }

    /**
     * Đặt mật khẩu
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
