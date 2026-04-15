package com.example.husnaoli

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.husnaoli.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mengambil data user dari SharedPreferences (sesuai versi teman kamu)
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val namaUser = sharedPref.getString("USER_NAMA", "User")
        val roleUser = sharedPref.getString("USER_ROLE", "Guest")

        binding.tvUserGreeting.text = "Halo, $namaUser ($roleUser)"

        setupSpinner()
        setupBottomNavigation()

        // Tombol Logout
        binding.btnLogout.setOnClickListener {
            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Card Kelola User
        binding.cardKelolaUser.setOnClickListener {
            startActivity(Intent(this, KelolaUserActivity::class.java))
        }

        // Card Kelola Barang
        binding.cardKelolaBarang.setOnClickListener {
            startActivity(Intent(this, KelolaBarangActivity::class.java))
        }

        // Card Laporan
        binding.cardLaporan.setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
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
                    startActivity(Intent(this, KelolaBarangActivity::class.java))
                    true
                }
                R.id.nav_laporan -> {
                    startActivity(Intent(this, LaporanActivity::class.java))
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