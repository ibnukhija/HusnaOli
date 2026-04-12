package com.example.husnaoli

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.husnaoli.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner()

        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, login::class.java)
            startActivity(intent)
            finish()
        }

        binding.cardKelolaUser.setOnClickListener {
            val intent = Intent(this, KelolaUserActivity::class.java)
            startActivity(intent)
        }

//        binding.cardKelolaBarang.setOnClickListener {
//            Toast.makeText(this, "Membuka Kelola Barang", Toast.LENGTH_SHORT).show()
//        }
//
//        binding.cardLaporan.setOnClickListener {
//            Toast.makeText(this, "Membuka Laporan", Toast.LENGTH_SHORT).show()
//        }
    }

    private fun setupSpinner() {
        val options = arrayOf("Harian", "Mingguan", "Bulanan")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = adapter
    }
}