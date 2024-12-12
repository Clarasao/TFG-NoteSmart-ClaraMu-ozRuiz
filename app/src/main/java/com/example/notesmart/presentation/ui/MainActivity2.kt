package com.example.notesmart.presentation.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.notesmart.databinding.ActivityMainBinding
import org.json.JSONException
import org.json.JSONObject

class MainActivity2 : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val url = "http://192.168.0.178/school/login.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.activityMainBtnAccess.setOnClickListener {
            val email = binding.activityMainEditTextEmail.text.toString().trim()
            val password = binding.activityMainEditTextPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                validatingUser(email, password)
            } else {
                Toast.makeText(this, "Por favor ingrese el correo y la contraseña", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun validatingUser(email: String, password: String) {
        val params = HashMap<String, String>()
        params["email"] = email
        params["password"] = password

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    // Parsear la respuesta como JSON
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.getString("status")
                    when (status) {
                        "success" -> {
                            Toast.makeText(this, "Acceso concedido", Toast.LENGTH_SHORT).show()
                        }
                        "error" -> {
                            val message = jsonResponse.getString("message")
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            binding.activityMainEditTextEmail.setText("")
                            binding.activityMainEditTextPassword.setText("")
                            binding.activityMainEditTextEmail.requestFocus()
                        }
                        else -> {
                            Toast.makeText(this, "Respuesta inesperada del servidor: $status", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error procesando la respuesta JSON", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                val errorMessage = error.message ?: "Sin mensaje de error"
                Toast.makeText(this, "Error de conexión: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }


}