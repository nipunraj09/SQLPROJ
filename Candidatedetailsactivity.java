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
        answerBookletCodeEditText.setInputType(InputType.TYPE_NULL);

        connectionClass = new ConnectionClass();

        Intent intent = getIntent();
        rollNumber = intent.getStringExtra("rollNumber");
        nameTextView.setText(intent.getStringExtra("name"));
        dobTextView.setText(intent.getStringExtra("dob"));

        saveButton.setOnClickListener(v -> saveStudentData());
        nextCandidateButton.setOnClickListener(v -> startActivity(new Intent(CandidateDetailsActivity.this, RollNumberActivity.class)));
        clearButton.setOnClickListener(v -> clearData());
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
                String checkExistingQuery = "SELECT Qpcode, Answerbookletcode FROM RSSBROLL WHERE ROLL = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkExistingQuery);
                checkStmt.setString(1, rollNumber);
                ResultSet rs = checkStmt.executeQuery();
                String existingQpCode = "";
                String existingAnswerBookletCode = "";
                if (rs.next()) {
                    existingQpCode = rs.getString("Qpcode");
                    existingAnswerBookletCode = rs.getString("Answerbookletcode");
                }
                rs.close();
                checkStmt.close();

                if (!existingQpCode.isEmpty() && !existingAnswerBookletCode.isEmpty()) {
                    if (!updateQpCodeCheckBox.isChecked() && !updateAnswerBookletCheckBox.isChecked()) {
                        runOnUiThread(() -> Toast.makeText(CandidateDetailsActivity.this, "Select at least one field to update", Toast.LENGTH_SHORT).show());
                        conn.close();
                        return;
                    }
                }

                String checkDuplicateQuery = "SELECT COUNT(*) FROM RSSBROLL WHERE (Qpcode = ? OR Answerbookletcode = ?) AND ROLL <> ?";
                PreparedStatement checkDuplicateStmt = conn.prepareStatement(checkDuplicateQuery);
                checkDuplicateStmt.setString(1, qpCode);
                checkDuplicateStmt.setString(2, answerBookletCode);
                checkDuplicateStmt.setString(3, rollNumber);
                ResultSet rsDuplicate = checkDuplicateStmt.executeQuery();
                rsDuplicate.next();
                int duplicateCount = rsDuplicate.getInt(1);
                rsDuplicate.close();
                checkDuplicateStmt.close();

                if (duplicateCount > 0) {
                    runOnUiThread(() -> Toast.makeText(CandidateDetailsActivity.this, "Qpcode or Answerbookletcode already exists for another roll number", Toast.LENGTH_SHORT).show());
                    conn.close();
                    return;
                }

                String logQuery = "INSERT INTO RSSBROLLLOG (ROLL, NAME, DOB, Qpcode, Answerbookletcode, Updatereason, UpdatedAt) " +
                        "SELECT ROLL, NAME, DOB, Qpcode, Answerbookletcode, ?, GETDATE() FROM RSSBROLL WHERE ROLL = ?";
                PreparedStatement logStmt = conn.prepareStatement(logQuery);
                String updateReason = "";
                if (updateQpCodeCheckBox.isChecked() && updateAnswerBookletCheckBox.isChecked()) {
                    updateReason = "Issue of Answerbooklet and Qpcode both";
                } else if (updateQpCodeCheckBox.isChecked()) {
                    updateReason = "Qpcode issue";
                } else if (updateAnswerBookletCheckBox.isChecked()) {
                    updateReason = "Answerbooklet issue";
                }
                logStmt.setString(1, updateReason);
                logStmt.setString(2, rollNumber);
                logStmt.executeUpdate();
                logStmt.close();

                String updateQuery = "UPDATE RSSBROLL SET Qpcode = ?, Answerbookletcode = ?, UpdatedAt = GETDATE() WHERE ROLL = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, qpCode);
                updateStmt.setString(2, answerBookletCode);
                updateStmt.setString(3, rollNumber);
                updateStmt.executeUpdate();
                updateStmt.close();
                conn.close();
                runOnUiThread(() -> Toast.makeText(CandidateDetailsActivity.this, "Record updated successfully", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(CandidateDetailsActivity.this, "Operation failed", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void clearData() {
        qpCodeEditText.setText("");
        answerBookletCodeEditText.setText("");
        updateQpCodeCheckBox.setChecked(false);
        updateAnswerBookletCheckBox.setChecked(false);
    }
}
