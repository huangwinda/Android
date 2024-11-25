package com.example.clockapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ImageView clockImageView;
    private Button option1Button, option2Button, option3Button;
    private TextView scoreTextView;

    private int score = 0;
    private int questionCount = 0;  // 当前题目计数
    private final int totalQuestions = 3;  // 总题目数

    private final List<ClockItem> clocks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clockImageView = findViewById(R.id.clockImageView);
        option1Button = findViewById(R.id.option1Button);
        option2Button = findViewById(R.id.option2Button);
        option3Button = findViewById(R.id.option3Button);
        scoreTextView = findViewById(R.id.scoreTextView);

        initializeClocks();
        loadNewClock();

        option1Button.setOnClickListener(v -> checkAnswer(option1Button.getText().toString()));
        option2Button.setOnClickListener(v -> checkAnswer(option2Button.getText().toString()));
        option3Button.setOnClickListener(v -> checkAnswer(option3Button.getText().toString()));
    }

    private void initializeClocks() {
        Field[] fields = R.drawable.class.getDeclaredFields();

        for (Field field : fields) {
            try {
                String resourceName = field.getName();
                if (resourceName.startsWith("clock_") && resourceName.matches("clock_\\d{4}")) {  // 确保以 "clock_" 开头，后接四位数字
                    int resId = field.getInt(null);
                    String time = formatTimeFromFileName(resourceName);
                    clocks.add(new ClockItem(resId, time));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if (clocks.isEmpty()) {
            Toast.makeText(this, "No clock images found in drawable", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatTimeFromFileName(String fileName) {
        // 移除 "clock_" 前缀并获取时间部分
        String timePart = fileName.replace("clock_", "");
        if (timePart.length() == 4) {
            String hour = timePart.substring(0, 2);
            String minute = timePart.substring(2, 4);
            return hour + ":" + minute;
        }
        return "00:00";
    }

    private void loadNewClock() {
        if (clocks.isEmpty()) {
            return;
        }

        // 获取一个随机时钟项
        Random random = new Random();
        int index = random.nextInt(clocks.size());
        ClockItem clockItem = clocks.get(index);

        // 设置 ImageView 显示时钟图片
        clockImageView.setImageResource(clockItem.getImageRes());
        clockImageView.setTag(clockItem.getTime());  // 将正确时间设置为 Tag

        // 获取正确的时间
        String correctTime = clockItem.getTime();

        // 生成两个干扰项
        List<String> timeOptions = new ArrayList<>();
        timeOptions.add(correctTime);

        while (timeOptions.size() < 3) {
            int randomHours = random.nextInt(12);
            int randomMinutes = random.nextInt(60);
            String distractorTime = String.format(Locale.getDefault(), "%02d:%02d", randomHours, randomMinutes);

            // 确保干扰项不与正确时间重复
            if (!timeOptions.contains(distractorTime)) {
                timeOptions.add(distractorTime);
            }
        }

        // 打乱时间选项的顺序
        Collections.shuffle(timeOptions);

        // 设置三个按钮的文本为选项
        option1Button.setText(timeOptions.get(0));
        option2Button.setText(timeOptions.get(1));
        option3Button.setText(timeOptions.get(2));
    }

    private void checkAnswer(String selectedOption) {
        String correctAnswer = clockImageView.getTag().toString();  // 获取 ImageView 的 Tag 作为正确答案

        if (selectedOption.equals(correctAnswer)) {
            score++;
            Toast.makeText(this, getString(R.string.correct), Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, getString(R.string.wrong), Toast.LENGTH_SHORT).show();
        }

        scoreTextView.setText(getString(R.string.score_label, score));
        questionCount++;

        if (questionCount >= totalQuestions) {
            showGameOverDialog();
        } else {
            loadNewClock();
        }
    }

    private void showGameOverDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("Your final score is: " + score)
                .setCancelable(false)
                .setPositiveButton("Restart", (dialog, which) -> restartGame())
                .setNegativeButton("Exit", (dialog, which) -> finish())
                .show();
    }

    private void restartGame() {
        score = 0;
        questionCount = 0;
        scoreTextView.setText(getString(R.string.score_label, score));
        loadNewClock();
    }

    private static class ClockItem {
        private final int imageRes;
        private final String time;

        public ClockItem(int imageRes, String time) {
            this.imageRes = imageRes;
            this.time = time;
        }

        public int getImageRes() {
            return imageRes;
        }

        public String getTime() {
            return time;
        }
    }
}
