package com.example.husnaoli

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.husnaoli.databinding.ActivityLoginBinding

class login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbHelper: DBHusnaOli

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHusnaOli(this)

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Harap isi username dan password!", Toast.LENGTH_SHORT).show()
            } else {
                val db = dbHelper.readableDatabase
                val cursor = db.rawQuery("SELECT nama, role FROM user WHERE username = ? AND password = ?", arrayOf(username, password))
                
                if (cursor.moveToFirst()) {
                    val nama = cursor.getString(0)
                    val role = cursor.getString(1)
                    cursor.close()

                    Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                    
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.putExtra("USER_NAMA", nama)
                    intent.putExtra("USER_ROLE", role)
                    startActivity(intent)
                    finish()
                } else {
                    cursor.close()
                    Toast.makeText(this, "Username atau Password salah!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}