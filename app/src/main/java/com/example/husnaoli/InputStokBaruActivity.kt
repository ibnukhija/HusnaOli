package com.example.husnaoli

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.husnaoli.databinding.ActivityInputStokBaruBinding
import java.util.Calendar

class InputStokBaruActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInputStokBaruBinding
    private lateinit var dbHelper: DBHusnaOli

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Pastikan nama binding sesuai dengan file XML Anda (activity_input_stok_baru.xml)
        binding = ActivityInputStokBaruBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHusnaOli(this)

        setupListeners()
    }

    private fun setupListeners() {
        // --- 1. NAVIGASI HEADER ---
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.tvLihatHistory.setOnClickListener {
            val intent = Intent(this, RiwayatRestockActivity::class.java)
            startActivity(intent)
        }


        // --- 2. DATE PICKER (Pilih Tanggal) ---
        binding.etTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                // Format tanggal: YYYY-MM-DD (Sesuai format di web/database standar)
                val formattedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                binding.etTanggal.setText(formattedDate)
            }, year, month, day)

            datePickerDialog.show()
        }


        // --- 3. LOGIKA HAPUS BARIS ---
        // Karena di XML hanya ada 1 baris statis, tombol hapus ini akan mengosongkan formnya saja
        binding.btnHapusBaris.setOnClickListener {
            binding.etNamaBarang.text.clear()
            binding.etHargaBeli.text.clear()
            binding.etJumlahMasuk.text.clear()
        }


        // --- 4. SIMPAN DATA ---
        binding.btnSimpanStok.setOnClickListener {
            simpanStokMasuk()
        }
    }

    private fun simpanStokMasuk() {
        // Ambil Data Header
        val supplier = binding.etSupplier.text.toString().trim()
        val tanggal = binding.etTanggal.text.toString().trim()
        val keterangan = binding.etKeterangan.text.toString().trim()

        // Ambil Data Barang
        val namaBarang = binding.etNamaBarang.text.toString().trim()
        val hargaBeliStr = binding.etHargaBeli.text.toString().trim()
        val jumlahMasukStr = binding.etJumlahMasuk.text.toString().trim()

        // Validasi Form
        if (supplier.isEmpty() || tanggal.isEmpty() || namaBarang.isEmpty() || hargaBeliStr.isEmpty() || jumlahMasukStr.isEmpty()) {
            Toast.makeText(this, "Harap lengkapi Supplier, Tanggal, dan Data Barang!", Toast.LENGTH_SHORT).show()
            return
        }

        val hargaBeli = hargaBeliStr.toIntOrNull() ?: 0
        val jumlahMasuk = jumlahMasukStr.toIntOrNull() ?: 0

        if (jumlahMasuk <= 0) {
            Toast.makeText(this, "Jumlah masuk harus lebih dari 0", Toast.LENGTH_SHORT).show()
            return
        }

        val db = dbHelper.writableDatabase

        // CATATAN DATABASE:
        // Anda perlu menyesuaikan query ini dengan struktur tabel riwayat Anda.
        // Asumsi nama tabelnya adalah 'riwayat_restock'
        try {
            val values = ContentValues().apply {
                put("tanggal", tanggal)
                put("supplier", supplier)
                put("detail_barang", "$namaBarang (x$jumlahMasuk)")
                put("keterangan", keterangan)
            }

            val resultRiwayat = db.insert("riwayat_restock", null, values)

            if (resultRiwayat != -1L) {
                // OPSIONAL: Tambahkan logika untuk UPDATE stok di tabel 'sparepart'
                // Misalnya: db.execSQL("UPDATE sparepart SET stok = stok + ? WHERE nama_item = ?", arrayOf(jumlahMasuk, namaBarang))

                Toast.makeText(this, "Stok Masuk Berhasil Disimpan", Toast.LENGTH_SHORT).show()

                // Kosongkan form setelah berhasil
                kosongkanForm()
            } else {
                Toast.makeText(this, "Gagal menyimpan riwayat", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun kosongkanForm() {
        binding.etSupplier.text.clear()
        binding.etTanggal.text.clear()
        binding.etKeterangan.text.clear()
        binding.etNamaBarang.text.clear()
        binding.etHargaBeli.text.clear()
        binding.etJumlahMasuk.text.clear()
    }
}