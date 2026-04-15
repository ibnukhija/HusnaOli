package com.example.husnaoli

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.husnaoli.databinding.ActivityTambahUserBinding

class TambahUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTambahUserBinding
    private lateinit var dbHelper: DBHusnaOli

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHusnaOli(this)

        setupHeader()
        setupSpinner()

        binding.tvKembali.setOnClickListener { finish() }

        binding.btnSimpanUser.setOnClickListener {
            saveUser()
        }
    }

    private fun setupHeader() {
        binding.btnBackHeader.setOnClickListener { finish() }
        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun setupSpinner() {
        val roles = arrayOf("-- Pilih Jabatan --", "Admin", "Kasir")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = adapter
    }

    private fun saveUser() {
        val nama = binding.etNamaLengkap.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val role = binding.spinnerRole.selectedItem.toString()

        if (nama.isEmpty() || username.isEmpty() || password.isEmpty() || role == "-- Pilih Jabatan --") {
            Toast.makeText(this, "Harap lengkapi semua data!", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show()
            return
        }

        val db = dbHelper.writableDatabase
        val sql = "INSERT INTO user (nama, username, password, role) VALUES (?, ?, ?, ?)"
        db.execSQL(sql, arrayOf(nama, username, password, role.lowercase()))
        Toast.makeText(this, "User berhasil disimpan!", Toast.LENGTH_SHORT).show()
        finish()
    }
}