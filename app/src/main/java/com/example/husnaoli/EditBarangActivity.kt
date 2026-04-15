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

    // Variabel untuk menyimpan ID barang yang sedang diedit (item_id di database)
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
        binding = ActivityEditBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHusnaOli(this)

        // 1. Setup Spinner Kategori
        setupSpinners()

        // 2. Ambil ID dari Intent (Dikirim dari KelolaBarangActivity)
        // Kuncinya harus sama dengan yang dikirim yaitu "id"
        sparepartId = intent.getIntExtra("id", -1)

        // 3. Load Data Lama jika ID valid
        if (sparepartId != -1) {
            loadDataLama(sparepartId)
        } else {
            Toast.makeText(this, "Error: Data barang tidak ditemukan!", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 4. Setup Tombol-tombol
        setupListeners()
    }

    private fun setupSpinners() {
        val adapterKategori = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listKategori
        )
        adapterKategori.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKategori.adapter = adapterKategori

        // Spinner Foto (Opsional untuk tampilan)
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
        binding.btnBackHeader.setOnClickListener { finish() }
        binding.btnBack.setOnClickListener { finish() }

        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.btnUpdate.setOnClickListener {
            updateDataBarang()
        }
    }

    private fun loadDataLama(id: Int) {
        val db = dbHelper.readableDatabase
        // Perbaikan: Tabel 'items', Kolom 'item_id'
        val cursor = db.rawQuery("SELECT * FROM items WHERE item_id = ?", arrayOf(id.toString()))

        if (cursor.moveToFirst()) {
            try {
                // Ambil data menggunakan nama kolom yang ada di DBHusnaOli
                binding.etNamaItem.setText(cursor.getString(cursor.getColumnIndexOrThrow("nama_item")))
                binding.etStokSaatIni.setText(cursor.getInt(cursor.getColumnIndexOrThrow("stok")).toString())
                binding.etHargaBeli.setText(cursor.getInt(cursor.getColumnIndexOrThrow("harga_beli")).toString())
                binding.etHargaJual.setText(cursor.getInt(cursor.getColumnIndexOrThrow("harga_jual")).toString())

                // Karena di DBHusnaOli kategori disimpan sebagai kategori_id (INTEGER),
                // sementara kita set default selection dulu
                binding.spinnerKategori.setSelection(0)

            } catch (e: Exception) {
                Toast.makeText(this, "Gagal memuat: Nama kolom tidak sesuai", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Data tidak ditemukan di database", Toast.LENGTH_SHORT).show()
        }
        cursor.close()
    }

    private fun updateDataBarang() {
        val namaItem = binding.etNamaItem.text.toString().trim()
        val stokStr = binding.etStokSaatIni.text.toString().trim()
        val hargaBeliStr = binding.etHargaBeli.text.toString().trim()
        val hargaJualStr = binding.etHargaJual.text.toString().trim()

        if (namaItem.isEmpty() || stokStr.isEmpty() || hargaBeliStr.isEmpty() || hargaJualStr.isEmpty()) {
            Toast.makeText(this, "Semua kolom form harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        val stok = stokStr.toIntOrNull() ?: 0
        val hargaBeli = hargaBeliStr.toIntOrNull() ?: 0
        val hargaJual = hargaJualStr.toIntOrNull() ?: 0

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nama_item", namaItem)
            put("kategori_id", 1) // Sesuaikan relasi kategori (Oli)
            put("stok", stok)
            put("harga_beli", hargaBeli)
            put("harga_jual", hargaJual)
        }

        // Perbaikan: Update ke tabel 'items' berdasarkan 'item_id'
        val result = db.update(
            "items",
            values,
            "item_id = ?",
            arrayOf(sparepartId.toString())
        )

        if (result > 0) {
            Toast.makeText(this, "Data Husna Oli berhasil diperbarui", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Gagal memperbarui data di tabel items", Toast.LENGTH_SHORT).show()
        }
    }
}