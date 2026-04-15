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
    private var sparepartId: Int = -1
    private val kategoriIds = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHusnaOli(this)

        // Ambil ID dari Intent
        sparepartId = intent.getIntExtra("ITEM_ID", -1)

        if (sparepartId != -1) {
            setupKategoriSpinner()
            loadDataLama(sparepartId)
        } else {
            Toast.makeText(this, "Error: Data barang tidak ditemukan!", Toast.LENGTH_SHORT).show()
            finish()
        }
        setupListeners()
    }

    // Fungsi untuk mengambil data kategori dari database dan mengisi spinner
    private fun setupKategoriSpinner() {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT kategori_id, nama_kategori FROM kategori", null)
        
        val categories = mutableListOf<String>()
        kategoriIds.clear()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("kategori_id"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("nama_kategori"))
                categories.add(name)
                kategoriIds.add(id)
            } while (cursor.moveToNext())
        }
        cursor.close()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKategori.adapter = adapter
    }

    // Fungsi untuk mengatur listener
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

    // Fungsi untuk mengambil data lama dari database
    private fun loadDataLama(id: Int) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM items WHERE item_id = ?", arrayOf(id.toString()))

        if (cursor.moveToFirst()) {
            binding.apply {
                etNamaItem.setText(cursor.getString(cursor.getColumnIndexOrThrow("nama_item")))
                etStokSaatIni.setText(cursor.getInt(cursor.getColumnIndexOrThrow("stok")).toString())
                etHargaBeli.setText(cursor.getInt(cursor.getColumnIndexOrThrow("harga_beli")).toString())
                etHargaJual.setText(cursor.getInt(cursor.getColumnIndexOrThrow("harga_jual")).toString())

                val currentKategoriId = cursor.getInt(cursor.getColumnIndexOrThrow("kategori_id"))
                val spinnerPosition = kategoriIds.indexOf(currentKategoriId)
                if (spinnerPosition != -1) {
                    spinnerKategori.setSelection(spinnerPosition)
                }
            }
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

        val selectedIndex = binding.spinnerKategori.selectedItemPosition
        val kategoriId = if (selectedIndex != -1) kategoriIds[selectedIndex] else 1

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nama_item", namaItem)
            put("kategori_id", kategoriId)
            put("stok", stokStr.toIntOrNull() ?: 0)
            put("harga_beli", hargaBeliStr.toIntOrNull() ?: 0)
            put("harga_jual", hargaJualStr.toIntOrNull() ?: 0)
        }

        val result = db.update("items", values, "item_id = ?", arrayOf(sparepartId.toString()))
        if (result > 0) {
            Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Gagal memperbarui data", Toast.LENGTH_SHORT).show()
        }
    }
}
