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

        // Mengambil data dari Intent
        val namaUser = intent.getStringExtra("USER_NAMA") ?: "User"
        val roleUser = intent.getStringExtra("USER_ROLE") ?: "Guest"

        // Menampilkan ke TextView
        binding.tvUserGreeting.text = "Halo, $namaUser ($roleUser)"

        setupSpinner()
        setupBottomNavigation()

        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.cardKelolaUser.setOnClickListener {
            val intent = Intent(this, KelolaUserActivity::class.java)
            startActivity(intent)
        }

        binding.cardKelolaBarang.setOnClickListener {
            Toast.makeText(this, "Membuka Kelola Barang", Toast.LENGTH_SHORT).show()
        }

        binding.cardLaporan.setOnClickListener {
            Toast.makeText(this, "Membuka Laporan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_user -> {
                    startActivity(Intent(this, KelolaUserActivity::class.java))
                    true
                }
                R.id.nav_barang -> {
                    Toast.makeText(this, "Kelola Barang segera hadir", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_laporan -> {
                    Toast.makeText(this, "Laporan segera hadir", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupSpinner() {
        val options = arrayOf("Harian", "Mingguan", "Bulanan")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = adapter
    }
}