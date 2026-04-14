package com.example.husnaoli

import android.app.Activity
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
        // Pastikan nama binding ini sesuai dengan nama file XML Anda (activity_tambah_barang.xml)
        binding = ActivityTambahBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHusnaOli(this)

        setupKategoriSpinner()
        setupListeners()
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
            // Membuka galeri untuk memilih gambar (hanya JPG dan PNG)
            getContent.launch("image/*")
        }

        // Tombol Simpan Data
        binding.btnSimpan.setOnClickListener {
            simpanBarang()
        }
    }

    private fun setupKategoriSpinner() {
        // Daftar kategori (Anda bisa mengganti ini dengan data dari database jika kategori dinamis)
        val listKategori = arrayOf(
            "Oli & Pelumas",
            "Ban & Velg",
            "Sistem Pengereman",
            "Mesin & Transmisi",
            "Kelistrikan",
            "Aksesoris"
        )

        // Membuat adapter untuk spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listKategori
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerKategori.adapter = adapter
    }

    private fun simpanBarang() {
        // Ambil data dari input
        val namaItem = binding.etNamaItem.text.toString().trim()
        val kategori = binding.spinnerKategori.selectedItem.toString()
        val stokAwalStr = binding.etStokAwal.text.toString().trim()
        val hargaBeliStr = binding.etHargaBeli.text.toString().trim()
        val hargaJualStr = binding.etHargaJual.text.toString().trim()

        // Validasi form tidak boleh kosong
        if (namaItem.isEmpty() || stokAwalStr.isEmpty() || hargaBeliStr.isEmpty() || hargaJualStr.isEmpty()) {
            Toast.makeText(this, "Semua kolom form harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi tambahan: Pastikan konversi ke angka aman
        val stokAwal = stokAwalStr.toIntOrNull() ?: 0
        val hargaBeli = hargaBeliStr.toIntOrNull() ?: 0
        val hargaJual = hargaJualStr.toIntOrNull() ?: 0

        if (hargaJual < hargaBeli) {
            Toast.makeText(this, "Peringatan: Harga jual lebih rendah dari harga beli!", Toast.LENGTH_LONG).show()
            // Anda bisa return di sini jika ingin melarang penyimpanan, atau biarkan lanjut.
        }

        // Simpan ke database
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nama_item", namaItem)
            put("kategori", kategori)
            put("stok", stokAwal)
            put("harga_beli", hargaBeli)
            put("harga_jual", hargaJual)
            // Catatan: Jika tabel sparepart Anda memiliki kolom untuk foto (misalnya 'foto_uri'),
            // Anda bisa menyimpannya di sini.
            // if (selectedImageUri != null) put("foto_uri", selectedImageUri.toString())
        }

        val result = db.insert("sparepart", null, values)

        if (result != -1L) {
            Toast.makeText(this, "Barang '$namaItem' berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            finish() // Tutup activity dan kembali ke daftar
        } else {
            Toast.makeText(this, "Gagal menyimpan barang ke database", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi utilitas untuk mengambil nama file asli dari URI gambar
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