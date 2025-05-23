package com.example.do_an.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.do_an.model.Task;
import com.example.do_an.model.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * SQLite database helper
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    // Database info
    private static final String DATABASE_NAME = "task_manager.db";
    private static final int DATABASE_VERSION = 1;

    // Tables
    private static final String TABLE_USERS = "users";
    private static final String TABLE_TASKS = "tasks";

    // Common columns
    private static final String COLUMN_ID = "id";

    // User table columns
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Task table columns
    private static final String COLUMN_TASK_NAME = "name";
    private static final String COLUMN_TASK_DESCRIPTION = "description";
    private static final String COLUMN_TASK_DATE_TIME = "date_time";
    private static final String COLUMN_TASK_COMPLETED = "completed";
    private static final String COLUMN_TASK_USER_ID = "user_id";

    // Create table statements
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USERNAME + " TEXT UNIQUE,"
            + COLUMN_PASSWORD + " TEXT"
            + ")";

    private static final String CREATE_TABLE_TASKS = "CREATE TABLE " + TABLE_TASKS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TASK_NAME + " TEXT,"
            + COLUMN_TASK_DESCRIPTION + " TEXT,"
            + COLUMN_TASK_DATE_TIME + " INTEGER,"
            + COLUMN_TASK_COMPLETED + " INTEGER,"
            + COLUMN_TASK_USER_ID + " INTEGER,"
            + "FOREIGN KEY(" + COLUMN_TASK_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")"
            + ")";

    /**
     * Constructor
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_TASKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Create tables again
        onCreate(db);
    }

    /**
     * Add a new user to the database
     */
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_PASSWORD, user.getPassword());

        // Insert row
        long id = db.insert(TABLE_USERS, null, values);

        db.close();

        return id;
    }

    /**
     * Get user by username and password (for login)
     */
    public User getUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID, COLUMN_USERNAME, COLUMN_PASSWORD},
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        User user = null;

        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            );
            cursor.close();
        }

        db.close();

        return user;
    }

    /**
     * Check if username already exists
     */
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean exists = false;

        if (cursor != null) {
            exists = cursor.getCount() > 0;
            cursor.close();
        }

        db.close();

        return exists;
    }

    /**
     * Add a new task to the database
     */
    public long addTask(Task task, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_NAME, task.getName());
        values.put(COLUMN_TASK_DESCRIPTION, task.getDescription());
        values.put(COLUMN_TASK_DATE_TIME, task.getDateTime());
        values.put(COLUMN_TASK_COMPLETED, task.isCompleted() ? 1 : 0);
        values.put(COLUMN_TASK_USER_ID, userId);

        // Insert row
        long id = db.insert(TABLE_TASKS, null, values);

        db.close();

        return id;
    }

    /**
     * Update an existing task
     */
    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_NAME, task.getName());
        values.put(COLUMN_TASK_DESCRIPTION, task.getDescription());
        values.put(COLUMN_TASK_DATE_TIME, task.getDateTime());
        values.put(COLUMN_TASK_COMPLETED, task.isCompleted() ? 1 : 0);

        // Update row
        int rowsAffected = db.update(
                TABLE_TASKS,
                values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(task.getId())}
        );

        db.close();

        return rowsAffected;
    }

    /**
     * Delete a task
     */
    public void deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(
                TABLE_TASKS,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(taskId)}
        );

        db.close();
    }

    /**
     * Get a task by id
     */
    public Task getTask(int taskId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_TASKS,
                new String[]{
                        COLUMN_ID,
                        COLUMN_TASK_NAME,
                        COLUMN_TASK_DESCRIPTION,
                        COLUMN_TASK_DATE_TIME,
                        COLUMN_TASK_COMPLETED,
                        COLUMN_TASK_USER_ID
                },
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(taskId)},
                null,
                null,
                null
        );

        Task task = null;

        if (cursor != null && cursor.moveToFirst()) {
            task = new Task(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DESCRIPTION)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TASK_DATE_TIME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_COMPLETED)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_USER_ID))
            );
            cursor.close();
        }

        db.close();

        return task;
    }

    /**
     * Get all tasks for a user
     */
    public List<Task> getAllTasks(int userId) {
        List<Task> taskList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_TASKS
                + " WHERE " + COLUMN_TASK_USER_ID + " = " + userId;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Loop through all rows and add to list
        if (cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DESCRIPTION)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TASK_DATE_TIME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_COMPLETED)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_USER_ID))
                );

                taskList.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return taskList;
    }

    /**
     * Add sample tasks for new users
     */
    public void addSampleTasks(int userId) {
        // Get current time
        Calendar calendar = Calendar.getInstance();
        long currentTime = calendar.getTimeInMillis();

        // Sample task 1 - Today
        Task task1 = new Task("Mua sắm", "Mua thực phẩm tại siêu thị", currentTime + 3600000, false);
        addTask(task1, userId);

        // Sample task 2 - Tomorrow
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Task task2 = new Task("Họp nhóm", "Họp nhóm dự án tại phòng họp", calendar.getTimeInMillis(), false);
        addTask(task2, userId);

        // Sample task 3 - Next week
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        Task task3 = new Task("Khám sức khỏe", "Khám sức khỏe định kỳ tại bệnh viện", calendar.getTimeInMillis(), false);
        addTask(task3, userId);

        // Sample task 4 - Completed
        calendar.setTimeInMillis(currentTime);
        calendar.add(Calendar.HOUR_OF_DAY, -2);
        Task task4 = new Task("Trả lời email", "Trả lời email công việc", calendar.getTimeInMillis(), true);
        addTask(task4, userId);
    }
}