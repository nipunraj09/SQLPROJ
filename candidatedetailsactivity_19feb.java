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
    private Button saveButton, nextCandidateButton, clearButton, updateLogButton;
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
        updateLogButton = findViewById(R.id.updateLogButton);

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
        updateLogButton.setOnClickListener(v -> updateLog());

        updateQpCodeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleUpdateLogButton());
        updateAnswerBookletCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleUpdateLogButton());
    }

    private void toggleUpdateLogButton() {
        updateLogButton.setEnabled(updateQpCodeCheckBox.isChecked() || updateAnswerBookletCheckBox.isChecked());
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

        if (qpCode.isEmpty() || answerBookletCode.isEmpty()) {
            Toast.makeText(this, "Fill all fields before saving", Toast.LENGTH_SHORT).show();
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
                // **Check for duplicate Qpcode or Answerbookletcode in expjava**
                String duplicateCheckQuery = "SELECT COUNT(*) FROM expjava WHERE (Qpcode = ? OR Answerbookletcode = ?) AND ROLL <> ?";
                PreparedStatement duplicateCheckStmt = conn.prepareStatement(duplicateCheckQuery);
                duplicateCheckStmt.setString(1, qpCode);
                duplicateCheckStmt.setString(2, answerBookletCode);
                duplicateCheckStmt.setString(3, rollNumber);

                ResultSet rs = duplicateCheckStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    runOnUiThread(() -> Toast.makeText(CandidateDetailsActivity.this, "Duplicate QP Code or Answer Booklet Code found! Update canceled.", Toast.LENGTH_SHORT).show());
                } else {
                    // **Update expjava only if no duplicate is found**
                    String updateQuery = "UPDATE expjava SET Qpcode = ?, Answerbookletcode = ?, UpdatedAt = GETDATE() WHERE ROLL = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setString(1, qpCode);
                    updateStmt.setString(2, answerBookletCode);
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

                rs.close();
                duplicateCheckStmt.close();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(CandidateDetailsActivity.this, "Operation failed", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateLog() {
        if (!updateQpCodeCheckBox.isChecked() && !updateAnswerBookletCheckBox.isChecked()) {
            Toast.makeText(this, "Select at least one checkbox to update the log", Toast.LENGTH_SHORT).show();
            return;
        }

        String qpCode = qpCodeEditText.getText().toString().trim();
        String answerBookletCode = answerBookletCodeEditText.getText().toString().trim();
        String name = nameTextView.getText().toString();
        String dob = dobTextView.getText().toString();

        final String updateReason;
        if (updateQpCodeCheckBox.isChecked() && updateAnswerBookletCheckBox.isChecked()) {
            updateReason = "QP Code and Answer Booklet Code update needed";
        } else if (updateQpCodeCheckBox.isChecked()) {
            updateReason = "QP Code update needed";
        } else {
            updateReason = "Answer Booklet Code update needed";
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Connection conn = connectionClass.connect();
            if (conn == null) {
                runOnUiThread(() -> Toast.makeText(CandidateDetailsActivity.this, "Database Connection Failed", Toast.LENGTH_SHORT).show());
                return;
            }

            try {
                String insertLogQuery = "INSERT INTO expjava_log_new (ROLL, NAME, DOB, Qpcode, Answerbookletcode, Updatereason, UpdatedAt, Timelog) " +
                        "VALUES (?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())";

                PreparedStatement logStmt = conn.prepareStatement(insertLogQuery);
                logStmt.setString(1, rollNumber);
                logStmt.setString(2, name);
                logStmt.setString(3, dob);
                logStmt.setString(4, qpCode.isEmpty() ? null : qpCode);
                logStmt.setString(5, answerBookletCode.isEmpty() ? null : answerBookletCode);
                logStmt.setString(6, updateReason);

                int rowsInserted = logStmt.executeUpdate();
                logStmt.close();
                conn.close();

                runOnUiThread(() -> {
                    if (rowsInserted > 0) {
                        Toast.makeText(CandidateDetailsActivity.this, "Log updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CandidateDetailsActivity.this, "Log update failed", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(CandidateDetailsActivity.this, "Log insertion failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }


}
