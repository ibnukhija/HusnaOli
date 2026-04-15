package com.example.husnaoli

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.husnaoli.databinding.FragmentBarangBinding
import com.example.husnaoli.databinding.ItemSparepartBinding
import java.text.NumberFormat
import java.util.Locale

class BarangFragment : Fragment() {

    private var _binding: FragmentBarangBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBHusnaOli
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
        dbHelper = DBHusnaOli(requireContext())

        setupListView()
        
        binding.btnTambahBarang.setOnClickListener {
            startActivity(Intent(requireContext(), TambahBarangActivity::class.java))
        }

        binding.btnRiwayatRestock.setOnClickListener {
            // Berubah: Sekarang buka RiwayatRestockActivity dulu
            startActivity(Intent(requireContext(), RiwayatRestockActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupListView() {
        adapter = BarangAdapter(mutableListOf()) { barang ->
            deleteBarang(barang)
        }
        binding.lvSparepart.adapter = adapter
    }

    private fun loadData() {
        val list = mutableListOf<Barang>()
        val db = dbHelper.readableDatabase
        val sql = """
            SELECT i.item_id, i.nama_item, k.nama_kategori, i.harga_beli, i.harga_jual, i.stok, i.foto 
            FROM items i 
            JOIN kategori k ON i.kategori_id = k.kategori_id
        """.trimIndent()
        
        val cursor = db.rawQuery(sql, null)

        if (cursor.moveToFirst()) {
            do {
                list.add(Barang(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getInt(4),
                    cursor.getInt(5),
                    cursor.getString(6)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        adapter.updateData(list)
    }

    private fun deleteBarang(barang: Barang) {
        val db = dbHelper.writableDatabase
        val result = db.delete("items", "item_id=?", arrayOf(barang.id.toString()))
        if (result > 0) {
            Toast.makeText(requireContext(), "Barang ${barang.nama} berhasil dihapus", Toast.LENGTH_SHORT).show()
            loadData()
        }
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

                // Menampilkan Foto dengan Glide
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
