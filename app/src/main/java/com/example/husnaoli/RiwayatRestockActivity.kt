package com.example.husnaoli

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.husnaoli.databinding.ActivityRiwayatRestockBinding

class RiwayatRestockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiwayatRestockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Memasang ViewBinding (Pastikan nama file XML-nya sudah activity_riwayat_restock.xml)
        binding = ActivityRiwayatRestockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        // Tombol Kembali
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Tombol Logout
        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Tombol + Input Stok Baru
        binding.btnInputStok.setOnClickListener {
            val intent = Intent(this, InputStokBaruActivity::class.java)
            startActivity(intent)
        }
    }
}