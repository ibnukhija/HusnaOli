package com.example.husnaoli

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.husnaoli.databinding.ActivityTambahBarangBinding
import com.example.husnaoli.network.KategoriResponse
import com.example.husnaoli.network.LoginResponse
import com.example.husnaoli.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream

class TambahBarangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahBarangBinding
    private var selectedImageUri: Uri? = null

    private val kategoriIds = mutableListOf<Int>()

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.tvFileName.text = getFileName(uri) ?: "Gambar dipilih"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupKategoriSpinner()
        setupListeners()
    }

    private fun setupKategoriSpinner() {
        RetrofitClient.instance.getKategori().enqueue(object : Callback<KategoriResponse> {
            override fun onResponse(call: Call<KategoriResponse>, response: Response<KategoriResponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res != null && res.status == "success") {
                        val categories = mutableListOf<String>()
                        kategoriIds.clear()
                        res.data.forEach {
                            categories.add(it.namaKategori)
                            kategoriIds.add(it.kategoriId)
                        }
                        val adapter = ArrayAdapter(this@TambahBarangActivity, android.R.layout.simple_spinner_item, categories)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerKategori.adapter = adapter
                    }
                }
            }
            override fun onFailure(call: Call<KategoriResponse>, t: Throwable) {
                Log.e("API_ERROR", t.message ?: "")
            }
        })
    }

    private fun setupListeners() {
        binding.btnBackHeader.setOnClickListener { finish() }
        binding.btnChooseFile.setOnClickListener { getContent.launch("image/*") }
        binding.btnSimpan.setOnClickListener { simpanBarang() }
        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun simpanBarang() {
        val namaItem = binding.etNamaItem.text.toString().trim()
        val stokAwal = binding.etStokAwal.text.toString().toIntOrNull() ?: 0
        val hargaBeli = binding.etHargaBeli.text.toString().toIntOrNull() ?: 0
        val hargaJual = binding.etHargaJual.text.toString().toIntOrNull() ?: 0

        val selectedIndex = binding.spinnerKategori.selectedItemPosition
        if (selectedIndex == -1 || kategoriIds.isEmpty()) {
            Toast.makeText(this, "Pilih kategori!", Toast.LENGTH_SHORT).show()
            return
        }
        val kategoriId = kategoriIds[selectedIndex]

        if (namaItem.isEmpty()) {
            Toast.makeText(this, "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        // Konversi Gambar ke Base64
        val base64Image = selectedImageUri?.let { uriToBase64(it) } ?: ""

        RetrofitClient.instance.tambahBarang(
            namaItem, kategoriId, hargaBeli, hargaJual, stokAwal, base64Image
        ).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@TambahBarangActivity, "Berhasil!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@TambahBarangActivity, "Gagal simpan", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@TambahBarangActivity, "Koneksi Gagal", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fungsi sakti untuk mengubah gambar dari Galeri menjadi Teks (Base64)
    private fun uriToBase64(uri: Uri): String {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            
            // Kompres gambar agar tidak terlalu berat saat dikirim ke server (Quality: 70%)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("IMAGE_ERROR", e.message ?: "")
            ""
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = it.getString(index)
                }
            }
        }
        return result ?: uri.path?.substringAfterLast('/')
    }
}
