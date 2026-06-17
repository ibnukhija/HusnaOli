package com.example.husnaoli

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.husnaoli.databinding.DialogStrukBinding
import com.example.husnaoli.databinding.FragmentTransaksiKasirBinding
import com.example.husnaoli.network.BarangResponse
import com.example.husnaoli.network.LoginResponse
import com.example.husnaoli.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class TransaksiKasirFragment : Fragment() {

    private var _binding: FragmentTransaksiKasirBinding? = null
    private val binding get() = _binding!!
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

        setupListeners()
        setupKategoriSpinner()
        loadBarang()
    }

    private fun loadBarang() {
        RetrofitClient.instance.getBarang().enqueue(object : Callback<BarangResponse> {
            override fun onResponse(call: Call<BarangResponse>, response: Response<BarangResponse>) {
                if (_binding == null || !isAdded) return
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res != null && res.status == "success") {
                        val listBarangTampil = mutableListOf<String>()
                        val dataBarangRaw = res.data

                        dataBarangRaw.forEach {
                            listBarangTampil.add("${it.namaItem}\nRp${formatRupiah(it.hargaJual)}\nStok: ${it.stok}")
                        }

                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listBarangTampil)
                        binding.gvBarangKasir.adapter = adapter

                        binding.gvBarangKasir.setOnItemClickListener { _, _, position, _ ->
                            val item = dataBarangRaw[position]
                            if (item.stok > 0) {
                                tambahKeKeranjang(item.itemId, item.namaItem, item.hargaJual)
                            } else {
                                Toast.makeText(requireContext(), "Stok habis!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            override fun onFailure(call: Call<BarangResponse>, t: Throwable) {
                if (_binding == null || !isAdded) return
                Toast.makeText(requireContext(), "Gagal memuat barang", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun tambahKeKeranjang(id: Int, nama: String, harga: Int) {
        // Jika ID = 0 (Jasa), buat antrean baris baru secara independen di list keranjang
        val index = if (id == 0) -1 else listKeranjang.indexOfFirst { it["id"] == id }

        if (index != -1) {
            val qty = (listKeranjang[index]["qty"] as Int) + 1
            listKeranjang[index]["qty"] = qty
            listKeranjang[index]["subtotal"] = qty * harga
        } else {
            listKeranjang.add(hashMapOf(
                "id" to id,
                "nama" to nama,
                "qty" to 1,
                "price" to harga,
                "subtotal" to harga
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

    private fun prosesTransaksi(uangBayar: Int) {
        val kembalian = uangBayar - totalBayar

        val itemsToSync = listKeranjang.map {
            mapOf(
                "id" to it["id"],
                "qty" to it["qty"],
                "price" to it["price"]
            )
        }
        val itemsJson = Gson().toJson(itemsToSync)

        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", 0)

        RetrofitClient.instance.simpanTransaksi(
            userId, totalBayar, uangBayar, kembalian, itemsJson
        ).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (_binding == null || !isAdded) return
                if (response.isSuccessful && response.body()?.status == "success") {
                    // Tampilkan dialog nota penagihan sebelum list dibersihkan
                    showStrukDialog(uangBayar)

                    listKeranjang.clear()
                    binding.etUangBayar.setText("")
                    refreshKeranjang()
                    loadBarang()
                } else {
                    val msg = response.body()?.message ?: "Gagal proses transaksi"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                if (_binding == null || !isAdded) return
                Toast.makeText(requireContext(), "Koneksi Gagal", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupListeners() {
        binding.btnTambahJasa.setOnClickListener {
            val namaJasa = binding.etNamaJasa.text.toString().trim()
            val biaya = binding.etBiayaJasa.text.toString().toIntOrNull() ?: 0

            if (namaJasa.isNotEmpty() && biaya > 0) {
                tambahKeKeranjang(0, "[JASA] $namaJasa", biaya)
                binding.etNamaJasa.text.clear()
                binding.etBiayaJasa.text.clear()
            } else {
                Toast.makeText(requireContext(), "Lengkapi data nama dan biaya jasa!", Toast.LENGTH_SHORT).show()
            }
        }

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
                prosesTransaksi(bayar)
            }
        }
    }

    private fun showStrukDialog(bayar: Int) {
        val dialogBinding = DialogStrukBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        dialogBinding.tvStrukTanggal.text = "Tanggal: " + sdf.format(Date())

        listKeranjang.forEach { item ->
            val tvItem = TextView(requireContext()).apply {
                text = "${item["nama"]} (x${item["qty"]}) \n-> Rp ${formatRupiah(item["subtotal"] as Int)}"
                textSize = 13f
                setPadding(0, 4, 0, 4)
                setTextColor(Color.BLACK)
            }
            dialogBinding.llStrukItems.addView(tvItem)
        }

        dialogBinding.tvStrukTotal.text = "Rp ${formatRupiah(totalBayar)}"
        dialogBinding.tvStrukBayar.text = "Rp ${formatRupiah(bayar)}"
        dialogBinding.tvStrukKembali.text = "Rp ${formatRupiah(bayar - totalBayar)}"

        dialogBinding.btnTutupStruk.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setupKategoriSpinner() {
        val categories = arrayOf("Semua Kategori", "Oli", "Ban", "Suku Cadang")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKategoriKasir.adapter = adapter
    }

    private fun formatRupiah(number: Int): String {
        return NumberFormat.getNumberInstance(Locale("id", "ID")).format(number)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}