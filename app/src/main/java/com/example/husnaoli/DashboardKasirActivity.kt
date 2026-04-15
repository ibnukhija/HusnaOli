package com.example.husnaoli

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.husnaoli.databinding.ActivityDashboardKasirBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DashboardKasirActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardKasirBinding
    private lateinit var dbHelper: DBHusnaOli

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardKasirBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHusnaOli(this)

        setupBottomNav()
        loadSummary()
    }

    private fun loadSummary() {
        val db = dbHelper.readableDatabase
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // 1. Hitung Total Pendapatan & Jumlah Transaksi Hari Ini
        val cursor = db.rawQuery(
            "SELECT SUM(total_harga), COUNT(transaksi_id) FROM transaksi WHERE tanggal_transaksi LIKE ?",
            arrayOf("$today%")
        )

        if (cursor.moveToFirst()) {
            val total = cursor.getInt(0)
            val count = cursor.getInt(1)

            binding.tvTotalPendapatan.text = "Rp ${formatRupiah(total)}"
            binding.tvJumlahTransaksi.text = "$count Transaksi Terproses"
        }
        cursor.close()

        // 2. Load 5 Transaksi Terakhir untuk Riwayat Singkat
        val listRiwayat = mutableListOf<String>()
        val cursorRiwayat = db.rawQuery(
            "SELECT transaksi_id, total_harga, tanggal_transaksi FROM transaksi ORDER BY transaksi_id DESC LIMIT 5",
            null
        )

        if (cursorRiwayat.moveToFirst()) {
            do {
                val id = cursorRiwayat.getInt(0)
                val total = cursorRiwayat.getInt(1)
                val tgl = cursorRiwayat.getString(2)
                listRiwayat.add("ID #$id - Rp ${formatRupiah(total)}\n$tgl")
            } while (cursorRiwayat.moveToNext())
        } else {
            listRiwayat.add("Belum ada transaksi hari ini.")
        }
        cursorRiwayat.close()

        binding.lvRiwayatSingkat.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listRiwayat)
    }

    private fun setupBottomNav() {
        binding.bottomNavigationKasir.selectedItemId = R.id.nav_dashboard_kasir
        binding.bottomNavigationKasir.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_kasir -> {
                    startActivity(Intent(this, TransaksiKasirActivity::class.java))
                    true
                }
                R.id.nav_dashboard_kasir -> true
                else -> false
            }
        }
    }

    private fun formatRupiah(number: Int): String {
        return NumberFormat.getNumberInstance(Locale("in", "ID")).format(number)
    }

    // Refresh data saat kembali ke dashboard
    override fun onResume() {
        super.onResume()
        loadSummary()
    }
}