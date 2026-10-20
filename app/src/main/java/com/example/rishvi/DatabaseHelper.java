
package com.example.rishvi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "calorie_buddy.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String TABLE_FOODS = "food_records";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createUsers = "CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, " +
                "password_hash TEXT NOT NULL, " +
                "daily_goal INTEGER NOT NULL DEFAULT 2000" +
                ")";

        String createFoods = "CREATE TABLE " + TABLE_FOODS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "food_name TEXT NOT NULL, " +
                "calories INTEGER NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "meal_type TEXT NOT NULL, " +
                "food_date TEXT NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES users(id)" +
                ")";

        db.execSQL(createUsers);
        db.execSQL(createFoods);

        db.execSQL(
                "CREATE INDEX idx_food_user_date ON " +
                        TABLE_FOODS + "(user_id, food_date)"
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        // Preserve existing records when the schema changes.
        // Add explicit migration steps here for future versions.
    }

    private String today() {
        return new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
        ).format(new Date());
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] bytes = digest.digest(
                    password.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );

            StringBuilder result = new StringBuilder();

            for (byte b : bytes) {
                result.append(String.format(
                        Locale.US, "%02x", b & 0xff
                ));
            }

            return result.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable", e
            );
        }
    }

    public long registerUser(
            String name,
            String email,
            String password,
            int dailyGoal) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email.toLowerCase(Locale.ROOT));
        values.put("password_hash", hashPassword(password));
        values.put("daily_goal", dailyGoal);

        try {
            return db.insertOrThrow(TABLE_USERS, null, values);
        } catch (android.database.SQLException e) {
            // A duplicate email or another database constraint failed.
            return -1;
        }
    }

    public Cursor loginUser(String email, String password) {

        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_USERS,
                new String[]{
                        "id",
                        "name",
                        "email",
                        "daily_goal"
                },
                "email = ? AND password_hash = ?",
                new String[]{
                        email.toLowerCase(Locale.ROOT),
                        hashPassword(password)
                },
                null,
                null,
                null
        );
    }

    public boolean addFood(
            int userId,
            String foodName,
            int calories,
            int quantity,
            String mealType) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("food_name", foodName);
        values.put("calories", calories);
        values.put("quantity", quantity);
        values.put("meal_type", mealType);
        values.put("food_date", today());

        return db.insert(TABLE_FOODS, null, values) != -1;
    }

    public Cursor getFoodsForDate(int userId, String date) {

        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_FOODS,
                new String[]{
                        "id",
                        "food_name",
                        "calories",
                        "quantity",
                        "meal_type",
                        "food_date"
                },
                "user_id = ? AND food_date = ?",
                new String[]{
                        String.valueOf(userId),
                        date
                },
                null,
                null,
                "id DESC"
        );
    }

    public Cursor getTodayFoods(int userId) {
        return getFoodsForDate(userId, today());
    }

    public int getCaloriesForDate(int userId, String date) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(calories * quantity), 0) " +
                        "FROM " + TABLE_FOODS +
                        " WHERE user_id = ? AND food_date = ?",
                new String[]{
                        String.valueOf(userId),
                        date
                }
        );

        int total = 0;

        try {
            if (cursor.moveToFirst()) {
                total = cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }

        return total;
    }

    public Cursor getDailyHistory(int userId) {

        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT food_date, " +
                        "SUM(calories * quantity) AS total_calories, " +
                        "COUNT(*) AS food_entries " +
                        "FROM " + TABLE_FOODS +
                        " WHERE user_id = ? " +
                        "GROUP BY food_date " +
                        "ORDER BY food_date DESC",
                new String[]{String.valueOf(userId)}
        );
    }

    public boolean deleteFood(int foodId, int userId) {

        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_FOODS,
                "id = ? AND user_id = ?",
                new String[]{
                        String.valueOf(foodId),
                        String.valueOf(userId)
                }
        ) > 0;
    }

    public int clearTodayFoods(int userId) {

        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_FOODS,
                "user_id = ? AND food_date = ?",
                new String[]{
                        String.valueOf(userId),
                        today()
                }
        );
    }

    public String getTodayDate() {
        return today();
    }
}
