package com.example.biometriaapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class AttendanceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        int userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Error al obtener el ID del usuario", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Button exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(v -> {
            Intent intent = new Intent(AttendanceActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        authenticateBiometrically(userId);
    }

    private void authenticateBiometrically(int userId) {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(AttendanceActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                registerAttendance(userId);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(AttendanceActivity.this, "Fallo en la autenticación", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Registro de Asistencia")
                .setDescription("Usa tu huella para registrar tu asistencia")
                .setNegativeButtonText("Cancelar")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void registerAttendance(int userId) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://192.168.1.70/api_asistencia/attendance.php",
                response -> {
                    if (response.trim().equals("Asistencia registrada")) {
                        showSuccessDialogAndRedirect();
                    } else {
                        Toast.makeText(AttendanceActivity.this, "Error: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(AttendanceActivity.this, "Error de conexión: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void showSuccessDialogAndRedirect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✅ Asistencia registrada")
                .setMessage("Gracias por registrar tu asistencia.")
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();

        new Handler().postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent(AttendanceActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, 2500);
    }
}
