package com.example.uasmobileprograming

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val btn: TextView = findViewById(R.id.textMasuk)
        btn.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }

        emailEditText = findViewById(R.id.et_email)
        passwordEditText = findViewById(R.id.et_pass)

        val registerBtn: TextView = findViewById(R.id.btnRegister)
        registerBtn.setOnClickListener {
            register()
        }
    }

    private fun register() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(applicationContext, "Email tidak boleh kosong", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(applicationContext, "Masukkan email yang valid", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(applicationContext, "Password tidak boleh kosong", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(
                applicationContext,
                "Password harus lebih dari 6 karakter",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, Login::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Registrasi gagal: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
