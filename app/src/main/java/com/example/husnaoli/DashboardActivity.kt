package com.example.husnaoli

import android.content.Intent
import android.os.Bundle
<<<<<<< Updated upstream
import android.widget.ArrayAdapter
=======
>>>>>>> Stashed changes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.husnaoli.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

<<<<<<< Updated upstream
        // Ambil data user
=======
>>>>>>> Stashed changes
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val namaUser = sharedPref.getString("USER_NAMA", "User")
        val roleUser = sharedPref.getString("USER_ROLE", "Guest")
        binding.tvUserGreeting.text = "Halo, $namaUser ($roleUser)"

<<<<<<< Updated upstream
        // Load default fragment
=======
>>>>>>> Stashed changes
        if (savedInstanceState == null) {
            replaceFragment(DashboardFragment())
        }

        setupBottomNavigation()

        binding.btnLogout.setOnClickListener {
            val sharedPrefEdit = getSharedPreferences("UserSession", MODE_PRIVATE).edit()
            sharedPrefEdit.clear().apply()
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

        // Card Laporan → pakai Fragment
        binding.cardLaporan.setOnClickListener {
            replaceFragment(LaporanFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
<<<<<<< Updated upstream
                R.id.nav_dashboard -> {
                    replaceFragment(DashboardFragment())
                    true
                }
                R.id.nav_user -> {
                    replaceFragment(UserFragment())
                    true
                }
                R.id.nav_barang -> {
                    replaceFragment(BarangFragment())
                    true
                }
                R.id.nav_laporan -> {
                    replaceFragment(LaporanFragment())
                    true
                }
=======
                R.id.nav_dashboard -> { replaceFragment(DashboardFragment()); true }
                R.id.nav_user -> { replaceFragment(UserFragment()); true }
                R.id.nav_barang -> { replaceFragment(BarangFragment()); true }
                R.id.nav_laporan -> { replaceFragment(LaporanFragment()); true }
>>>>>>> Stashed changes
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