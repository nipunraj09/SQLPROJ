package com.example.javasqlapp;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponent;
    private String deviceInfo;  // Stores unique device info
    private String ipAddress;   // Stores device IP Address

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemUI();
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        // Get unique device info (Android ID)
        deviceInfo = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Get device IP address
        ipAddress = getLocalIpAddress();

        // Lock Task Mode setup
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);

        if (devicePolicyManager.isDeviceOwnerApp(getPackageName())) {
            devicePolicyManager.setLockTaskPackages(adminComponent, new String[]{getPackageName()});
            startLockTask();
            devicePolicyManager.setKeyguardDisabled(adminComponent, true);
        }

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            new AuthenticateUser().execute(username, password);
        });
    }

    private class AuthenticateUser extends AsyncTask<String, Void, Boolean> {
        private String errorMessage = null;

        @Override
        protected Boolean doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            boolean isAuthenticated = false;

            try {
                Connection connection = DatabaseConnection.getConnection(); // Use your connection class
                if (connection != null) {
                    // Check if the username and password match
                    String query = "SELECT COUNT(*) FROM RSSB_Login WHERE Username = ? AND Password = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, username);
                    statement.setString(2, password);
                    ResultSet resultSet = statement.executeQuery();

                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        isAuthenticated = true;

                        // Update device info, IP, and LoginTime in the database
                        String updateQuery = "UPDATE RSSB_Login SET DeviceInfo = ?, IP = ?, LoginTime = GETDATE() WHERE Username = ?";
                        PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                        updateStatement.setString(1, deviceInfo);
                        updateStatement.setString(2, ipAddress);
                        updateStatement.setString(3, username);
                        updateStatement.executeUpdate();
                        updateStatement.close();
                    }

                    resultSet.close();
                    statement.close();
                    connection.close();
                }
            } catch (Exception e) {
                errorMessage = "Database Error: " + e.getMessage();
            }
            return isAuthenticated;
        }

        @Override
        protected void onPostExecute(Boolean isAuthenticated) {
            if (isAuthenticated) {
                Intent intent = new Intent(LoginActivity.this, RollNumberActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, (errorMessage != null) ? errorMessage : "Invalid Credentials", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    @Override
    public void onBackPressed() {
        // Disable back button
    }

    // Method to get the local IP address
    private String getLocalIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                List<InetAddress> addresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress address : addresses) {
                    if (!address.isLoopbackAddress() && address.getHostAddress().indexOf(':') == -1) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            return "Unknown";
        }
        return "Unknown";
    }
}
