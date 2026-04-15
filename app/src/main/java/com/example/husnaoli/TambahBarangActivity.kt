package com.example.husnaoli

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.husnaoli.databinding.ActivityTambahBarangBinding

class TambahBarangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahBarangBinding
    private lateinit var dbHelper: DBHusnaOli
    private var selectedImageUri: Uri? = null
    
    // List untuk menyimpan ID kategori agar sinkron dengan spinner
    private val kategoriIds = mutableListOf<Int>()

    // Register launcher untuk mengambil gambar dari galeri
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            val fileName = getFileName(uri)
            binding.tvFileName.text = fileName ?: "Gambar dipilih"
        } else {
            binding.tvFileName.text = "No file chosen"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHusnaOli(this)

        setupKategoriSpinner()
        setupListeners()
    }

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

        // Tombol Pilih File Foto
        binding.btnChooseFile.setOnClickListener {
            getContent.launch("image/*")
        }

        // Tombol Simpan Data
        binding.btnSimpan.setOnClickListener {
            simpanBarang()
        }
    }

    private fun simpanBarang() {
        val namaItem = binding.etNamaItem.text.toString().trim()
        val stokAwal = binding.etStokAwal.text.toString().toIntOrNull() ?: 0
        val hargaBeli = binding.etHargaBeli.text.toString().toIntOrNull() ?: 0
        val hargaJual = binding.etHargaJual.text.toString().toIntOrNull() ?: 0

        // Ambil ID kategori dari spinner
        val selectedIndex = binding.spinnerKategori.selectedItemPosition
        if (selectedIndex == -1 || kategoriIds.isEmpty()) {
            Toast.makeText(this, "Silakan pilih kategori!", Toast.LENGTH_SHORT).show()
            return
        }
        val kategoriId = kategoriIds[selectedIndex]

        if (namaItem.isEmpty()) {
            Toast.makeText(this, "Nama item tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nama_item", namaItem)
            put("kategori_id", kategoriId)
            put("stok", stokAwal)
            put("harga_beli", hargaBeli)
            put("harga_jual", hargaJual)
            put("foto", selectedImageUri?.toString() ?: "")
        }

        val result = db.insert("items", null, values)

        if (result != -1L) {
            Toast.makeText(this, "Barang berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Gagal simpan ke tabel items", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Menutup helper saat activity dihancurkan adalah cara yang benar
        dbHelper.close()
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = it.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }
}
