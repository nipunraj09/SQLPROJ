package com.example.javasqlapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.view.WindowManager;
import java.sql.Timestamp;

public class CandidateDetailsActivity extends AppCompatActivity {

    private TextView nameTextView, dobTextView;
    private EditText qpCodeEditText, answerBookletCodeEditText;
    private Button saveButton, nextCandidateButton, clearButton;
    private CheckBox updateQpCodeCheckBox, updateAnswerBookletCheckBox;
    private ConnectionClass connectionClass;
    private String rollNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_candidate_details);

        nameTextView = findViewById(R.id.nameTextView);
        dobTextView = findViewById(R.id.dobTextView);
        qpCodeEditText = findViewById(R.id.qpCodeEditText);
        answerBookletCodeEditText = findViewById(R.id.answerBookletCodeEditText);
        updateQpCodeCheckBox = findViewById(R.id.updateQpCodeCheckBox);
        updateAnswerBookletCheckBox = findViewById(R.id.updateAnswerBookletCheckBox);
        saveButton = findViewById(R.id.saveButton);
        nextCandidateButton = findViewById(R.id.nextCandidateButton);
        clearButton = findViewById(R.id.clearButton);

        qpCodeEditText.setInputType(InputType.TYPE_NULL);
        qpCodeEditText.setFocusable(true);
        answerBookletCodeEditText.setInputType(InputType.TYPE_NULL);
        answerBookletCodeEditText.setFocusable(true);

        connectionClass = new ConnectionClass();

        Intent intent = getIntent();
        rollNumber = intent.getStringExtra("rollNumber");
        nameTextView.setText(intent.getStringExtra("name"));
        dobTextView.setText(intent.getStringExtra("dob"));

        saveButton.setOnClickListener(v -> saveStudentData());
        nextCandidateButton.setOnClickListener(v -> startActivity(new Intent(CandidateDetailsActivity.this, RollNumberActivity.class)));
        clearButton.setOnClickListener(v -> clearData());
    }

    private void clearData() {
        qpCodeEditText.setText("");
        answerBookletCodeEditText.setText("");
        updateQpCodeCheckBox.setChecked(false);
        updateAnswerBookletCheckBox.setChecked(false);
    }

    private void saveStudentData() {
        String qpCode = qpCodeEditText.getText().toString().trim();
        String answerBookletCode = answerBookletCodeEditText.getText().toString().trim();
        boolean updateQpCodeChecked = updateQpCodeCheckBox.isChecked();
        boolean updateAnswerBookletChecked = updateAnswerBookletCheckBox.isChecked();

        // Ensure either fields are filled or checkboxes are ticked
        if ((qpCode.isEmpty() || answerBookletCode.isEmpty()) && !updateQpCodeChecked && !updateAnswerBookletChecked) {
            Toast.makeText(this, "Fill all fields or select a checkbox to update", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Connection conn = connectionClass.connect();
            if (conn == null) {
                runOnUiThread(() -> Toast.makeText(CandidateDetailsActivity.this, "Database Connection Failed", Toast.LENGTH_SHORT).show());
                return;
            }

            try {
                // Check if the roll number exists in expjava
                String checkQuery = "SELECT Qpcode, Answerbookletcode FROM expjava WHERE ROLL = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setString(1, rollNumber);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    String existingQpCode = rs.getString("Qpcode");
                    String existingAnswerBookletCode = rs.getString("Answerbookletcode");

                    // If both QP Code and Answer Booklet Code are empty, update expjava directly
                    if ((existingQpCode == null || existingQpCode.trim().isEmpty()) &&
                            (existingAnswerBookletCode == null || existingAnswerBookletCode.trim().isEmpty()) &&
                            !updateQpCodeChecked && !updateAnswerBookletChecked) {

                        String updateQuery = "UPDATE expjava SET Qpcode = ?, Answerbookletcode = ?, UpdatedAt = GETDATE() WHERE ROLL = ?";
                        PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                        updateStmt.setString(1, qpCode.isEmpty() ? "" : qpCode);  // Ensure empty string if no value
                        updateStmt.setString(2, answerBookletCode.isEmpty() ? "" : answerBookletCode);  // Ensure empty string if no value
                        updateStmt.setString(3, rollNumber);

                        int rowsUpdated = updateStmt.executeUpdate();
                        updateStmt.close();

                        runOnUiThread(() -> {
                            if (rowsUpdated > 0) {
                                Toast.makeText(CandidateDetailsActivity.this, "Record updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CandidateDetailsActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }


                    // If the checkboxes are ticked, insert into expjava_log for logging purposes
                    else if (updateQpCodeChecked || updateAnswerBookletChecked) {
                        String updateReason = "";
                        if (updateQpCodeChecked && updateAnswerBookletChecked) {
                            updateReason = "QP Code and Answer Booklet Code update needed";
                        } else if (updateQpCodeChecked) {
                            updateReason = "QP Code update needed";
                        } else if (updateAnswerBookletChecked) {
                            updateReason = "Answer Booklet Code update needed";
                        }

                        // Log the update in expjava_log with unique timestamp
                        String insertLogQuery = "EXEC dbo.InsertLogProcedure ?, ?, ?, ?, ?, ?, ?";
                        PreparedStatement logStmt = conn.prepareStatement(insertLogQuery);
                        logStmt.setString(1, rollNumber);
                        logStmt.setString(2, nameTextView.getText().toString());
                        logStmt.setString(3, dobTextView.getText().toString());
                        logStmt.setString(4, qpCode.isEmpty() ? null : qpCode);
                        logStmt.setString(5, answerBookletCode.isEmpty() ? null : answerBookletCode);
                        logStmt.setString(6, updateReason);
                        logStmt.setTimestamp(7, new Timestamp(System.currentTimeMillis())); // current timestamp

                        int rowsInserted = logStmt.executeUpdate();
                        logStmt.close();

                        runOnUiThread(() -> {
                            if (rowsInserted > 0) {
                                Toast.makeText(CandidateDetailsActivity.this, "Log updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CandidateDetailsActivity.this, "Log update failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } else {
                    runOnUiThread(() -> Toast.makeText(CandidateDetailsActivity.this, "Roll number not found", Toast.LENGTH_SHORT).show());
                }

                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(CandidateDetailsActivity.this, "Operation failed", Toast.LENGTH_SHORT).show());
            }
        });
    }


}



