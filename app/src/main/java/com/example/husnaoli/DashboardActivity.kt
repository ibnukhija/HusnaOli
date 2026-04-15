package com.example.husnaoli

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.husnaoli.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tampilkan Nama User dan Role di Header
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val namaUser = sharedPref.getString("USER_NAMA", "User")
        val roleUser = sharedPref.getString("USER_ROLE", "Guest")
        binding.tvUserGreeting.text = "Halo, $namaUser ($roleUser)"

        // Load Fragment (Cek intent extra jika diarahkan dari Riwayat)
        val target = intent.getStringExtra("TARGET_FRAGMENT")
        if (target == "INPUT_STOK") {
            replaceFragment(InputStokFragment())
        } else if (savedInstanceState == null) {
            replaceFragment(DashboardFragment())
        }

        setupBottomNavigation()

        // Tombol Logout
        binding.btnLogout.setOnClickListener {
            val sharedPrefEdit = getSharedPreferences("UserSession", MODE_PRIVATE).edit()
            sharedPrefEdit.clear().apply()

            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> { replaceFragment(DashboardFragment()); true }
                R.id.nav_user -> { replaceFragment(UserFragment()); true }
                R.id.nav_barang -> { replaceFragment(BarangFragment()); true }
                R.id.nav_laporan -> { replaceFragment(LaporanFragment()); true }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}