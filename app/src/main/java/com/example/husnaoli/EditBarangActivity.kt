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
import com.example.husnaoli.databinding.ActivityEditBarangBinding
import com.example.husnaoli.network.BarangResponse
import com.example.husnaoli.network.KategoriResponse
import com.example.husnaoli.network.LoginResponse
import com.example.husnaoli.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream

class EditBarangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBarangBinding
    private var sparepartId: Int = -1
    private val kategoriIds = mutableListOf<Int>()
    private var selectedImageUri: Uri? = null
    private var currentFotoUrl: String = ""

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.tvFileName.text = getFileName(uri) ?: "Gambar dipilih"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sparepartId = intent.getIntExtra("ITEM_ID", -1)

        if (sparepartId != -1) {
            setupKategoriSpinner()
        } else {
            Toast.makeText(this, "Data tidak ditemukan!", Toast.LENGTH_SHORT).show()
            finish()
        }
        setupListeners()
    }

    private fun setupKategoriSpinner() {
        RetrofitClient.instance.getKategori().enqueue(object : Callback<KategoriResponse> {
            override fun onResponse(call: Call<KategoriResponse>, response: Response<KategoriResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val categories = mutableListOf<String>()
                    kategoriIds.clear()
                    response.body()?.data?.forEach {
                        categories.add(it.namaKategori)
                        kategoriIds.add(it.kategoriId)
                    }
                    val adapter = ArrayAdapter(this@EditBarangActivity, android.R.layout.simple_spinner_item, categories)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerKategori.adapter = adapter
                    
                    loadDataLama(sparepartId)
                }
            }
            override fun onFailure(call: Call<KategoriResponse>, t: Throwable) {}
        })
    }

    private fun setupListeners() {
        binding.btnBackHeader.setOnClickListener { finish() }
        binding.btnBack.setOnClickListener { finish() }
        binding.btnChooseFile.setOnClickListener { getContent.launch("image/*") }
        binding.btnUpdate.setOnClickListener { updateDataBarang() }
        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadDataLama(id: Int) {
        RetrofitClient.instance.getBarangDetail(id).enqueue(object : Callback<BarangResponse> {
            override fun onResponse(call: Call<BarangResponse>, response: Response<BarangResponse>) {
                if (response.isSuccessful && response.body()?.data?.isNotEmpty() == true) {
                    val b = response.body()!!.data[0]
                    binding.apply {
                        etNamaItem.setText(b.namaItem)
                        etStokSaatIni.setText(b.stok.toString())
                        etHargaBeli.setText(b.hargaBeli.toString())
                        etHargaJual.setText(b.hargaJual.toString())
                        currentFotoUrl = b.foto ?: ""
                        tvFileName.text = if (currentFotoUrl.isNotEmpty()) "Sudah ada foto" else "No file chosen"

                        val pos = categoriesFindPosition(b.namaKategori)
                        if (pos != -1) spinnerKategori.setSelection(pos)
                    }
                }
            }
            override fun onFailure(call: Call<BarangResponse>, t: Throwable) {}
        })
    }

    private fun updateDataBarang() {
        val nama = binding.etNamaItem.text.toString().trim()
        val stok = binding.etStokSaatIni.text.toString().toIntOrNull() ?: 0
        val beli = binding.etHargaBeli.text.toString().toIntOrNull() ?: 0
        val jual = binding.etHargaJual.text.toString().toIntOrNull() ?: 0
        val kategoriId = if (binding.spinnerKategori.selectedItemPosition != -1) kategoriIds[binding.spinnerKategori.selectedItemPosition] else -1

        if (nama.isEmpty()) return

        // Jika user pilih foto baru, kirim Base64. Jika tidak, kirim URL lama.
        val fotoData = selectedImageUri?.let { uriToBase64(it) } ?: currentFotoUrl

        RetrofitClient.instance.editBarang(sparepartId, nama, kategoriId, beli, jual, stok, fotoData)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(this@EditBarangActivity, "Update Berhasil", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {}
            })
    }

    private fun uriToBase64(uri: Uri): String {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) { "" }
    }

    private fun categoriesFindPosition(nama: String): Int {
        for (i in 0 until binding.spinnerKategori.count) {
            if (binding.spinnerKategori.getItemAtPosition(i).toString() == nama) return i
        }
        return -1
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use { if (it.moveToFirst()) result = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)) }
        }
        return result ?: uri.path?.substringAfterLast('/')
    }
}
