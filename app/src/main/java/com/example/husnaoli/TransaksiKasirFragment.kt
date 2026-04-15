package com.example.husnaoli

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.husnaoli.databinding.FragmentTransaksiKasirBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransaksiKasirFragment : Fragment() {

    private var _binding: FragmentTransaksiKasirBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBHusnaOli
    private val listKeranjang = mutableListOf<HashMap<String, Any>>()
    private var totalBayar: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransaksiKasirBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBHusnaOli(requireContext())

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

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listBarangTampil)
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
                Toast.makeText(requireContext(), "Stok habis!", Toast.LENGTH_SHORT).show()
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

        binding.lvKeranjang.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, displayList)
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
                Toast.makeText(requireContext(), "Keranjang masih kosong!", Toast.LENGTH_SHORT).show()
            } else if (bayar < totalBayar) {
                Toast.makeText(requireContext(), "Uang pembayaran kurang!", Toast.LENGTH_SHORT).show()
            } else {
                if (simpanTransaksiKeDatabase(bayar)) {
                    Toast.makeText(requireContext(), "Transaksi Berhasil!", Toast.LENGTH_LONG).show()
                    listKeranjang.clear()
                    binding.etUangBayar.setText("")
                    refreshKeranjang()
                    loadBarang()
                }
            }
        }
    }

    private fun setupKategoriSpinner() {
        val categories = arrayOf("Semua Kategori", "Oli", "Ban", "Suku Cadang")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKategoriKasir.adapter = adapter
    }

    private fun formatRupiah(number: Int): String {
        return NumberFormat.getNumberInstance(Locale("in", "ID")).format(number)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
