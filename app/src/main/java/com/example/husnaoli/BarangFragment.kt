package com.example.husnaoli

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.husnaoli.databinding.FragmentBarangBinding
import com.example.husnaoli.databinding.ItemSparepartBinding
import com.example.husnaoli.network.BarangResponse
import com.example.husnaoli.network.LoginResponse
import com.example.husnaoli.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class BarangFragment : Fragment() {

    private var _binding: FragmentBarangBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: BarangAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBarangBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListView()
        
        binding.btnTambahBarang.setOnClickListener {
            startActivity(Intent(requireContext(), TambahBarangActivity::class.java))
        }

        binding.btnRiwayatRestock.setOnClickListener {
            startActivity(Intent(requireContext(), RiwayatRestockActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupListView() {
        adapter = BarangAdapter(mutableListOf()) { barang ->
            showDeleteConfirmation(barang)
        }
        binding.lvSparepart.adapter = adapter
    }

    private fun loadData() {
        RetrofitClient.instance.getBarang().enqueue(object : Callback<BarangResponse> {
            override fun onResponse(call: Call<BarangResponse>, response: Response<BarangResponse>) {
                if (_binding == null || !isAdded) return
                
                if (response.isSuccessful) {
                    val barangResponse = response.body()
                    if (barangResponse != null && barangResponse.status == "success") {
                        val list = barangResponse.data.map {
                            Barang(
                                it.itemId,
                                it.namaItem,
                                it.namaKategori,
                                it.hargaBeli,
                                it.hargaJual,
                                it.stok,
                                it.foto
                            )
                        }
                        adapter.updateData(list)
                    } else {
                        context?.let { Toast.makeText(it, "Gagal memuat data", Toast.LENGTH_SHORT).show() }
                    }
                } else {
                    context?.let { Toast.makeText(it, "Error Server: ${response.code()}", Toast.LENGTH_SHORT).show() }
                }
            }

            override fun onFailure(call: Call<BarangResponse>, t: Throwable) {
                if (_binding == null || !isAdded) return
                Log.e("API_ERROR", t.message ?: "Unknown Error")
                context?.let { Toast.makeText(it, "Koneksi Gagal", Toast.LENGTH_LONG).show() }
            }
        })
    }

    private fun showDeleteConfirmation(barang: Barang) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Barang")
            .setMessage("Apakah Anda yakin ingin menghapus ${barang.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteBarang(barang)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteBarang(barang: Barang) {
        RetrofitClient.instance.deleteBarang(barang.id).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (_binding == null || !isAdded) return
                
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res != null && res.status == "success") {
                        Toast.makeText(requireContext(), res.message, Toast.LENGTH_SHORT).show()
                        loadData() // Refresh data setelah hapus
                    } else {
                        Toast.makeText(requireContext(), res?.message ?: "Gagal menghapus", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Error Server: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                if (_binding == null || !isAdded) return
                Toast.makeText(requireContext(), "Koneksi Gagal", Toast.LENGTH_SHORT).show()
            }
        })
    }

    data class Barang(
        val id: Int,
        val nama: String,
        val kategori: String,
        val hargaBeli: Int,
        val hargaJual: Int,
        val stok: Int,
        val foto: String?
    )

    inner class BarangAdapter(
        private var listBarang: List<Barang>,
        private val onDeleteClick: (Barang) -> Unit
    ) : BaseAdapter() {

        override fun getCount(): Int = listBarang.size
        override fun getItem(position: Int): Any = listBarang[position]
        override fun getItemId(position: Int): Long = listBarang[position].id.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val bindingItem: ItemSparepartBinding
            val view: View

            if (convertView == null) {
                bindingItem = ItemSparepartBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
                view = bindingItem.root
                view.tag = bindingItem
            } else {
                view = convertView
                bindingItem = view.tag as ItemSparepartBinding
            }

            val b = listBarang[position]
            bindingItem.apply {
                tvNamaItem.text = b.nama
                tvKategori.text = b.kategori
                tvHargaBeli.text = "Rp ${formatRupiah(b.hargaBeli)}"
                tvHargaJual.text = "Rp ${formatRupiah(b.hargaJual)}"
                tvStok.text = b.stok.toString()

                Glide.with(root.context)
                    .load(b.foto)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(imgSparepart)

                btnEdit.setOnClickListener {
                    val intent = Intent(requireContext(), EditBarangActivity::class.java)
                    intent.putExtra("ITEM_ID", b.id)
                    startActivity(intent)
                }
                btnHapus.setOnClickListener { onDeleteClick(b) }
            }

            return view
        }

        fun updateData(newList: List<Barang>) {
            listBarang = newList
            notifyDataSetChanged()
        }

        private fun formatRupiah(number: Int): String {
            return NumberFormat.getNumberInstance(Locale("in", "ID")).format(number)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
