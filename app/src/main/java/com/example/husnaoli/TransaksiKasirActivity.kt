package com.example.husnaoli

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.husnaoli.databinding.ActivityTransaksiKasirBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransaksiKasirActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransaksiKasirBinding
    private lateinit var dbHelper: DBHusnaOli
    private val listKeranjang = mutableListOf<HashMap<String, Any>>()
    private var totalBayar: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransaksiKasirBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHusnaOli(this)

        // Ambil data nama dari Intent Login agar tetap tampil
        val namaKasir = intent.getStringExtra("USER_NAMA") ?: "Kasir"
        binding.tvWelcome.text = "Halo, $namaKasir"

        setupBottomNav()
        setupListeners()
        setupKategoriSpinner()
        loadBarang()
    }

    private fun loadBarang() {
        val listBarangTampil = mutableListOf<String>()
        val dataBarangRaw = mutableListOf<HashMap<String, Any>>()

        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT item_id, nama_item, harga_jual, stok FROM items", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val nama = cursor.getString(1)
                val harga = cursor.getInt(2)
                val stok = cursor.getInt(3)

                dataBarangRaw.add(hashMapOf("id" to id, "nama" to nama, "harga" to harga, "stok" to stok))
                listBarangTampil.add("$nama\nRp${formatRupiah(harga)}\nStok: $stok")
            } while (cursor.moveToNext())
        }
        cursor.close()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listBarangTampil)
        binding.gvBarangKasir.adapter = adapter

        binding.gvBarangKasir.setOnItemClickListener { _, _, position, _ ->
            val item = dataBarangRaw[position]
            val stokSisa = item["stok"] as Int

            if (stokSisa > 0) {
                tambahKeKeranjang(
                    item["id"] as Int,
                    item["nama"] as String,
                    item["harga"] as Int
                )
            } else {
                Toast.makeText(this, "Stok habis!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun tambahKeKeranjang(id: Int, nama: String, harga: Int) {
        val index = listKeranjang.indexOfFirst { it["id"] == id }
        if (index != -1) {
            val qty = (listKeranjang[index]["qty"] as Int) + 1
            listKeranjang[index]["qty"] = qty
            listKeranjang[index]["subtotal"] = qty * harga
        } else {
            listKeranjang.add(hashMapOf(
                "id" to id, "nama" to nama, "qty" to 1, "harga" to harga, "subtotal" to harga
            ))
        }
        refreshKeranjang()
    }

    private fun refreshKeranjang() {
        totalBayar = 0
        val displayList = listKeranjang.map {
            val sub = it["subtotal"] as Int
            totalBayar += sub
            "${it["nama"]} (x${it["qty"]}) - Rp${formatRupiah(sub)}"
        }

        binding.lvKeranjang.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList)
        binding.tvTotalBayar.text = "Rp ${formatRupiah(totalBayar)}"
        hitungKembalian()
    }

    private fun hitungKembalian() {
        val bayar = binding.etUangBayar.text.toString().toIntOrNull() ?: 0
        val kembali = if (bayar >= totalBayar) bayar - totalBayar else 0
        binding.tvUangKembali.text = "Rp ${formatRupiah(kembali)}"
    }

    private fun simpanTransaksiKeDatabase(uangBayar: Int): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()

        return try {
            val tanggal = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val kembalian = uangBayar - totalBayar

            val vTrx = ContentValues().apply {
                put("user_id", 1)
                put("tanggal_transaksi", tanggal)
                put("total_harga", totalBayar.toDouble())
                put("pembayaran", uangBayar.toDouble())
                put("kembalian", kembalian.toDouble())
            }
            val trxId = db.insert("transaksi", null, vTrx)

            for (item in listKeranjang) {
                val id = item["id"] as Int
                val qty = item["qty"] as Int
                val harga = item["harga"] as Int

                val vDet = ContentValues().apply {
                    put("transaksi_id", trxId)
                    put("item_id", id)
                    put("jumlah", qty)
                    put("harga_jual_saat_itu", harga.toDouble())
                }
                db.insert("detail_transaksi", null, vDet)
                db.execSQL("UPDATE items SET stok = stok - $qty WHERE item_id = $id")
            }

            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }

    private fun setupListeners() {
        binding.etUangBayar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                hitungKembalian()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnBersihkanCart.setOnClickListener {
            listKeranjang.clear()
            refreshKeranjang()
        }

        binding.btnProsesTransaksi.setOnClickListener {
            val bayar = binding.etUangBayar.text.toString().toIntOrNull() ?: 0

            if (listKeranjang.isEmpty()) {
                Toast.makeText(this, "Keranjang masih kosong!", Toast.LENGTH_SHORT).show()
            } else if (bayar < totalBayar) {
                Toast.makeText(this, "Uang pembayaran kurang!", Toast.LENGTH_SHORT).show()
            } else {
                if (simpanTransaksiKeDatabase(bayar)) {
                    Toast.makeText(this, "Transaksi Berhasil!", Toast.LENGTH_LONG).show()
                    listKeranjang.clear()
                    binding.etUangBayar.setText("")
                    refreshKeranjang()
                    loadBarang()
                }
            }
        }

        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupKategoriSpinner() {
        val categories = arrayOf("Semua Kategori", "Oli", "Ban", "Suku Cadang")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKategoriKasir.adapter = adapter
    }

    private fun setupBottomNav() {
        binding.bottomNavigationKasir.selectedItemId = R.id.nav_kasir
        binding.bottomNavigationKasir.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard_kasir -> {
                    // PINDAH KE DASHBOARD
                    val intent = Intent(this, DashboardKasirActivity::class.java)
                    // Kirim ulang nama kasir agar di dashboard juga muncul namanya
                    intent.putExtra("USER_NAMA", binding.tvWelcome.text.toString().replace("Halo, ", ""))
                    startActivity(intent)
                    overridePendingTransition(0, 0) // Menghilangkan animasi kedip saat pindah menu
                    true
                }
                R.id.nav_kasir -> true
                else -> false
            }
        }
    }

    private fun formatRupiah(number: Int): String {
        return NumberFormat.getNumberInstance(Locale("in", "ID")).format(number)
    }
}