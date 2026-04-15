package com.example.husnaoli

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.husnaoli.databinding.ActivityDashboardKasirBinding

class DashboardKasirActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardKasirBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardKasirBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Menampilkan nama kasir
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val namaKasir = sharedPref.getString("USER_NAMA", "Kasir")
        binding.tvUserGreeting.text = "Halo, $namaKasir"

        if (savedInstanceState == null) {
            replaceFragment(DashboardKasirFragment())
        }
        setupBottomNav()
        setupLogout()
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            // Kembali ke halaman Login
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupBottomNav() {
        binding.bottomNavigationKasir.selectedItemId = R.id.nav_dashboard_kasir
        binding.bottomNavigationKasir.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard_kasir -> {
                    replaceFragment(DashboardKasirFragment())
                    true
                }
                R.id.nav_kasir -> {
                    // Pindah ke Fragment Transaksi
                    replaceFragment(TransaksiKasirFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_kasir, fragment)
            .commit()
    }
}
