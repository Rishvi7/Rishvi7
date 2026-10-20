
package com.example.rishvi;

import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView txtGreeting;
    private TextView txtConsumed;
    private TextView txtGoal;
    private TextView txtRemaining;

    private TextView profileGoal;
    private TextView profileConsumed;
    private TextView profileRemaining;
    private TextView profileBreakfast;
    private TextView profileLunch;
    private TextView profileDinner;

    private ProgressBar calorieProgress;

    private LinearLayout breakfastMeal;
    private LinearLayout lunchMeal;
    private LinearLayout dinnerMeal;

    private ScrollView homeScreen;
    private ScrollView profileScreen;

    private DatabaseHelper database;

    private int userId;
    private int dailyGoal = 2000;
    private String userName = "there";

    private final ArrayList<FoodItem> breakfastFoods =
            new ArrayList<>();
    private final ArrayList<FoodItem> lunchFoods =
            new ArrayList<>();
    private final ArrayList<FoodItem> dinnerFoods =
            new ArrayList<>();

    private static class FoodItem {
        int id;
        String name;
        int calories;
        int quantity;
        String mealType;
        String date;

        FoodItem(
                int id,
                String name,
                int calories,
                int quantity,
                String mealType,
                String date) {
            this.id = id;
            this.name = name;
            this.calories = calories;
            this.quantity = quantity;
            this.mealType = mealType;
            this.date = date;
        }

        int getTotalCalories() {
            return calories * quantity;
        }
    }

    private final String[] foodNames = {
            "Idli", "Dosa", "Chapati", "Rice", "Roti",
            "Banana", "Apple", "Egg", "Bread", "Oats",
            "Milk", "Curd", "Chicken", "Salad", "Sandwich",
            "Upma", "Poha", "Pongal", "Paneer", "Dal"
    };

    private final int[] foodCalories = {
            60, 168, 120, 206, 120,
            105, 95, 78, 75, 150,
            122, 100, 250, 80, 250,
            200, 180, 250, 265, 180
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userId = getIntent().getIntExtra("USER_ID", -1);
        userName = getIntent().getStringExtra("USER_NAME");
        dailyGoal = getIntent().getIntExtra("DAILY_GOAL", 2000);

        if (userId < 1) {
            Toast.makeText(this,
                    "Please log in again",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (userName == null || userName.trim().isEmpty()) {
            userName = "there";
        }

        database = new DatabaseHelper(this);

        initializeViews();
        setupButtons();
        loadTodayFoods();
    }

    private void initializeViews() {
        txtGreeting = findViewById(R.id.txtGreeting);
        txtConsumed = findViewById(R.id.txtConsumed);
        txtGoal = findViewById(R.id.txtGoal);
        txtRemaining = findViewById(R.id.txtRemaining);

        calorieProgress = findViewById(R.id.calorieProgress);

        breakfastMeal = findViewById(R.id.breakfastMeal);
        lunchMeal = findViewById(R.id.lunchMeal);
        dinnerMeal = findViewById(R.id.dinnerMeal);

        homeScreen = findViewById(R.id.homeScreen);
        profileScreen = findViewById(R.id.profileScreen);

        profileGoal = findViewById(R.id.profileGoal);
        profileConsumed = findViewById(R.id.profileConsumed);
        profileRemaining = findViewById(R.id.profileRemaining);
        profileBreakfast = findViewById(R.id.profileBreakfast);
        profileLunch = findViewById(R.id.profileLunch);
        profileDinner = findViewById(R.id.profileDinner);
    }

    private void setupButtons() {
        findViewById(R.id.btnAddFood)
                .setOnClickListener(v -> showMealSelection());

        findViewById(R.id.btnClear)
                .setOnClickListener(v -> clearTodayFoods());

        findViewById(R.id.btnHistory)
                .setOnClickListener(v -> showDailyHistory());

        findViewById(R.id.btnLogout)
                .setOnClickListener(v -> logout());

        findViewById(R.id.homeButton)
                .setOnClickListener(v -> showHome());

        findViewById(R.id.profileButton)
                .setOnClickListener(v -> showProfile());
    }

    private void loadTodayFoods() {
        breakfastFoods.clear();
        lunchFoods.clear();
        dinnerFoods.clear();

        Cursor cursor = database.getTodayFoods(userId);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(
                            cursor.getColumnIndexOrThrow("id")
                    );
                    String name = cursor.getString(
                            cursor.getColumnIndexOrThrow("food_name")
                    );
                    int calories = cursor.getInt(
                            cursor.getColumnIndexOrThrow("calories")
                    );
                    int quantity = cursor.getInt(
                            cursor.getColumnIndexOrThrow("quantity")
                    );
                    String meal = cursor.getString(
                            cursor.getColumnIndexOrThrow("meal_type")
                    );
                    String date = cursor.getString(
                            cursor.getColumnIndexOrThrow("food_date")
                    );

                    FoodItem item = new FoodItem(
                            id, name, calories, quantity, meal, date
                    );

                    if ("Breakfast".equals(meal)) {
                        breakfastFoods.add(item);
                    } else if ("Lunch".equals(meal)) {
                        lunchFoods.add(item);
                    } else if ("Dinner".equals(meal)) {
                        dinnerFoods.add(item);
                    }

                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        updateAll();
    }

    private void showMealSelection() {
        String[] meals = {
                "Breakfast", "Lunch", "Dinner"
        };

        new AlertDialog.Builder(this)
                .setTitle("Choose your meal")
                .setItems(meals, (dialog, which) -> {
                    showFoodSelection(meals[which]);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showFoodSelection(String mealType) {
        String[] options = new String[foodNames.length];

        for (int i = 0; i < foodNames.length; i++) {
            options[i] = foodNames[i]
                    + " — " + foodCalories[i] + " kcal";
        }

        new AlertDialog.Builder(this)
                .setTitle("Select food")
                .setItems(options, (dialog, which) -> {
                    showQuantityDialog(
                            mealType,
                            foodNames[which],
                            foodCalories[which]
                    );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showQuantityDialog(
            String mealType,
            String foodName,
            int calories) {

        EditText quantityInput = new EditText(this);
        quantityInput.setHint("Quantity (1 to 10)");
        quantityInput.setText("1");
        quantityInput.setSelectAllOnFocus(true);
        quantityInput.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
        );
        quantityInput.setPadding(40, 20, 40, 20);

        new AlertDialog.Builder(this)
                .setTitle("Add " + foodName)
                .setMessage(
                        "Calories per serving: " + calories + " kcal"
                )
                .setView(quantityInput)
                .setPositiveButton("Add Food", (dialog, which) -> {
                    int quantity;

                    try {
                        quantity = Integer.parseInt(
                                quantityInput.getText()
                                        .toString().trim()
                        );
                    } catch (NumberFormatException e) {
                        Toast.makeText(this,
                                "Enter a valid quantity",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (quantity < 1 || quantity > 10) {
                        Toast.makeText(this,
                                "Quantity must be between 1 and 10",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean saved = database.addFood(
                            userId,
                            foodName,
                            calories,
                            quantity,
                            mealType
                    );

                    if (saved) {
                        loadTodayFoods();
                        Toast.makeText(this,
                                "Food saved to database",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Could not save food",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int getTotalCalories() {
        return database.getCaloriesForDate(
                userId,
                database.getTodayDate()
        );
    }

    private void updateAll() {
        int consumed = getTotalCalories();
        int remaining = dailyGoal - consumed;

        txtGreeting.setText("Hello, " + userName + "!");
        txtConsumed.setText(consumed + " kcal");
        txtGoal.setText("Goal: " + dailyGoal + " kcal");

        if (remaining >= 0) {
            txtRemaining.setText(remaining + " kcal left");
        } else {
            txtRemaining.setText(
                    (-remaining) + " kcal over goal"
            );
        }

        calorieProgress.setMax(Math.max(1, dailyGoal));
        calorieProgress.setProgress(
                Math.min(consumed, dailyGoal)
        );

        updateMealList(breakfastMeal, breakfastFoods);
        updateMealList(lunchMeal, lunchFoods);
        updateMealList(dinnerMeal, dinnerFoods);

        profileGoal.setText(dailyGoal + " kcal");
        profileConsumed.setText(consumed + " kcal");

        if (remaining >= 0) {
            profileRemaining.setText(remaining + " kcal");
        } else {
            profileRemaining.setText(
                    (-remaining) + " kcal over goal"
            );
        }

        profileBreakfast.setText(formatFoods(breakfastFoods));
        profileLunch.setText(formatFoods(lunchFoods));
        profileDinner.setText(formatFoods(dinnerFoods));
    }

    private void updateMealList(
            LinearLayout container,
            ArrayList<FoodItem> foods) {

        container.removeAllViews();

        if (foods.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No food added yet");
            empty.setTextColor(
                    getResources().getColor(R.color.text_muted)
            );
            empty.setTextSize(14);
            empty.setPadding(4, 10, 4, 10);
            container.addView(empty);
            return;
        }

        for (FoodItem food : foods) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(4, 10, 4, 10);

            TextView foodText = new TextView(this);
            foodText.setText(food.name + " × " + food.quantity);
            foodText.setTextColor(
                    getResources().getColor(R.color.text_dark)
            );
            foodText.setTextSize(14);
            foodText.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    )
            );

            TextView caloriesText = new TextView(this);
            caloriesText.setText(
                    food.getTotalCalories() + " kcal"
            );
            caloriesText.setTextColor(
                    getResources().getColor(R.color.green)
            );
            caloriesText.setTypeface(null, Typeface.BOLD);
            caloriesText.setTextSize(13);

            row.addView(foodText);
            row.addView(caloriesText);
            container.addView(row);
        }
    }

    private String formatFoods(ArrayList<FoodItem> foods) {
        if (foods.isEmpty()) {
            return "No food added yet";
        }

        StringBuilder result = new StringBuilder();

        for (FoodItem food : foods) {
            result.append("• ")
                    .append(food.name)
                    .append(" × ")
                    .append(food.quantity)
                    .append(" — ")
                    .append(food.getTotalCalories())
                    .append(" kcal\n");
        }

        return result.toString().trim();
    }

    private void clearTodayFoods() {
        new AlertDialog.Builder(this)
                .setTitle("Clear today's food?")
                .setMessage(
                        "This deletes today's saved food records."
                )
                .setPositiveButton("Clear", (dialog, which) -> {
                    database.clearTodayFoods(userId);
                    loadTodayFoods();

                    Toast.makeText(this,
                            "Today's food has been cleared",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDailyHistory() {
        Cursor cursor = database.getDailyHistory(userId);

        try {
            if (cursor == null || !cursor.moveToFirst()) {
                new AlertDialog.Builder(this)
                        .setTitle("Daily History")
                        .setMessage("No saved food history yet.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            ArrayList<String> dates = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();

            do {
                String date = cursor.getString(
                        cursor.getColumnIndexOrThrow("food_date")
                );
                int total = cursor.getInt(
                        cursor.getColumnIndexOrThrow("total_calories")
                );
                int entries = cursor.getInt(
                        cursor.getColumnIndexOrThrow("food_entries")
                );

                dates.add(date);
                labels.add(
                        date + "  •  " + total + " kcal  •  "
                                + entries + " food entries"
                );

            } while (cursor.moveToNext());

            String[] options = labels.toArray(new String[0]);

            new AlertDialog.Builder(this)
                    .setTitle("Daily Calorie History")
                    .setItems(options, (dialog, which) -> {
                        showHistoryForDate(dates.get(which));
                    })
                    .setPositiveButton("Close", null)
                    .show();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void showHistoryForDate(String date) {
        Cursor cursor = database.getFoodsForDate(userId, date);

        try {
            StringBuilder details = new StringBuilder();
            int total = 0;

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(
                            cursor.getColumnIndexOrThrow("food_name")
                    );
                    int calories = cursor.getInt(
                            cursor.getColumnIndexOrThrow("calories")
                    );
                    int quantity = cursor.getInt(
                            cursor.getColumnIndexOrThrow("quantity")
                    );
                    String meal = cursor.getString(
                            cursor.getColumnIndexOrThrow("meal_type")
                    );

                    int itemTotal = calories * quantity;
                    total += itemTotal;

                    details.append(meal)
                            .append(": ")
                            .append(name)
                            .append(" × ")
                            .append(quantity)
                            .append(" — ")
                            .append(itemTotal)
                            .append(" kcal\n");

                } while (cursor.moveToNext());
            }

            details.append("\nTotal: ").append(total).append(" kcal");

            new AlertDialog.Builder(this)
                    .setTitle("History: " + date)
                    .setMessage(details.toString())
                    .setPositiveButton("OK", null)
                    .show();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void showHome() {
        homeScreen.setVisibility(View.VISIBLE);
        profileScreen.setVisibility(View.GONE);
        loadTodayFoods();
    }

    private void showProfile() {
        homeScreen.setVisibility(View.GONE);
        profileScreen.setVisibility(View.VISIBLE);
        loadTodayFoods();
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    Intent intent = new Intent(
                            MainActivity.this,
                            LoginActivity.class
                    );

                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        if (database != null) {
            database.close();
        }
        super.onDestroy();
    }
}
