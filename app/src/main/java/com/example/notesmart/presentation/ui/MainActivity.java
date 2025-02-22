package com.example.notesmart.presentation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.notesmart.databinding.ActivityMainBinding;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final String urlLogin = "http://10.0.2.2/school/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.activityMainBtnAccess.setOnClickListener(v -> {
            String email = binding.activityMainEditTextEmail.getText().toString().trim();
            String password = binding.activityMainEditTextPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                validatingUser(email, password);
            } else {
                Toast.makeText(this, "Por favor ingrese el correo y la contraseña", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validatingUser(String email, String password) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                urlLogin,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if ("success".equals(status)) {
                            String name = jsonResponse.getString("name");
                            String lastName = jsonResponse.getString("surname");
                            int courseId = jsonResponse.getInt("courseId");
                            String userType = jsonResponse.getString("userType");

                            Toast.makeText(this, "Acceso concedido", Toast.LENGTH_SHORT).show();

                            Intent intent;
                            if ("student".equals(userType)) {
                                intent = new Intent(this, StudentActivity.class);
                                intent.putExtra("name", name);
                                intent.putExtra("surname", lastName);
                                intent.putExtra("grade", courseId);
                                intent.putExtra("email", email);
                            } else {
                                intent = new Intent(this, TeacherActivity.class);
                                intent.putExtra("name", name);
                                intent.putExtra("surname", lastName);
                                intent.putExtra("courseId", courseId);
                            }

                            startActivity(intent);
                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            binding.activityMainEditTextEmail.setText("");
                            binding.activityMainEditTextPassword.setText("");
                            binding.activityMainEditTextEmail.requestFocus();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error procesando la respuesta JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    String errorMessage = error.getMessage() != null ? error.getMessage() : "Sin mensaje de error";
                    Toast.makeText(this, "Error de conexión: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}