package com.example.husnaoli

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.husnaoli.databinding.ActivityEditBarangBinding

class EditBarangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBarangBinding
    private lateinit var dbHelper: DBHusnaOli

    // Variabel untuk menyimpan ID barang yang sedang diedit
    private var sparepartId: Int = -1

    // Daftar Kategori (Samakan dengan yang ada di TambahBarangActivity)
    private val listKategori = arrayOf(
        "Oli & Pelumas",
        "Ban & Velg",
        "Sistem Pengereman",
        "Mesin & Transmisi",
        "Kelistrikan",
        "Aksesoris"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Pastikan nama binding sesuai dengan file XML Anda (activity_edit_barang.xml)
        binding = ActivityEditBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHusnaOli(this)

        // 1. Setup Spinner Kategori
        setupSpinners()

        // 2. Ambil ID dari Intent (Dikirim dari KelolaBarangActivity)
        sparepartId = intent.getIntExtra("id", -1)

        // 3. Load Data Lama jika ID valid
        if (sparepartId != -1) {
            loadDataLama(sparepartId)
        } else {
            Toast.makeText(this, "Error: Data barang tidak ditemukan!", Toast.LENGTH_SHORT).show()
            finish() // Tutup halaman jika ID tidak valid
        }

        // 4. Setup Tombol-tombol
        setupListeners()
    }

    private fun setupSpinners() {
        // --- Spinner Kategori ---
        val adapterKategori = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listKategori
        )
        adapterKategori.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKategori.adapter = adapterKategori

        // --- Spinner Foto (Simulasi) ---
        // Karena di UI Anda menggunakan dropdown untuk memilih foto, ini datanya
        val listFoto = arrayOf("Pilih Foto", "Oli Yamalube", "Oli MPX", "Kampas Rem Honda")
        val adapterFoto = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listFoto
        )
        adapterFoto.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFoto.adapter = adapterFoto
    }

    private fun setupListeners() {
        // Tombol Kembali di Header
        binding.btnBackHeader.setOnClickListener {
            finish()
        }

        // Tombol Kembali berupa teks
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

        // Tombol Update Data
        binding.btnUpdate.setOnClickListener {
            updateDataBarang()
        }
    }

    private fun loadDataLama(id: Int) {
        val db = dbHelper.readableDatabase
        // Asumsi nama tabel adalah 'sparepart'
        val cursor = db.rawQuery("SELECT * FROM sparepart WHERE id = ?", arrayOf(id.toString()))

        if (cursor.moveToFirst()) {
            try {
                // Set EditText
                binding.etNamaItem.setText(cursor.getString(cursor.getColumnIndexOrThrow("nama_item")))
                binding.etStokSaatIni.setText(cursor.getString(cursor.getColumnIndexOrThrow("stok")))
                binding.etHargaBeli.setText(cursor.getString(cursor.getColumnIndexOrThrow("harga_beli")))
                binding.etHargaJual.setText(cursor.getString(cursor.getColumnIndexOrThrow("harga_jual")))

                // Set Spinner Kategori ke posisi yang sesuai dengan data database
                val kategoriDB = cursor.getString(cursor.getColumnIndexOrThrow("kategori"))
                val kategoriPosition = listKategori.indexOf(kategoriDB)
                if (kategoriPosition >= 0) {
                    binding.spinnerKategori.setSelection(kategoriPosition)
                }

                // Catatan: Jika ada data foto di database, Anda bisa set Spinner Foto dan ImageView (Pratinjau) di sini

            } catch (e: Exception) {
                Toast.makeText(this, "Gagal memuat data: Kolom tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Data barang tidak ditemukan di database", Toast.LENGTH_SHORT).show()
        }
        cursor.close()
    }

    private fun updateDataBarang() {
        // Ambil data terbaru dari input
        val namaItem = binding.etNamaItem.text.toString().trim()
        val kategori = binding.spinnerKategori.selectedItem.toString()
        val stokStr = binding.etStokSaatIni.text.toString().trim()
        val hargaBeliStr = binding.etHargaBeli.text.toString().trim()
        val hargaJualStr = binding.etHargaJual.text.toString().trim()

        // Validasi form tidak boleh kosong
        if (namaItem.isEmpty() || stokStr.isEmpty() || hargaBeliStr.isEmpty() || hargaJualStr.isEmpty()) {
            Toast.makeText(this, "Semua kolom form harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        // Konversi ke integer dengan aman
        val stok = stokStr.toIntOrNull() ?: 0
        val hargaBeli = hargaBeliStr.toIntOrNull() ?: 0
        val hargaJual = hargaJualStr.toIntOrNull() ?: 0

        // Siapkan data untuk diupdate ke SQLite
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nama_item", namaItem)
            put("kategori", kategori)
            put("stok", stok)
            put("harga_beli", hargaBeli)
            put("harga_jual", hargaJual)
        }

        // Jalankan perintah UPDATE
        val result = db.update(
            "sparepart",
            values,
            "id = ?",
            arrayOf(sparepartId.toString())
        )

        if (result > 0) {
            Toast.makeText(this, "Data sparepart berhasil diperbarui", Toast.LENGTH_SHORT).show()
            finish() // Kembali ke halaman KelolaBarangActivity
        } else {
            Toast.makeText(this, "Gagal memperbarui data", Toast.LENGTH_SHORT).show()
        }
    }
}