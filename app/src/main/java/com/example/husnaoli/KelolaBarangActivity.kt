package com.example.husnaoli

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.husnaoli.databinding.ActivityKelolaBarangBinding
import com.example.husnaoli.databinding.ItemSparepartBinding

// ================= DATA CLASS =================
data class Sparepart(
    val id: Int,
    val namaItem: String,
    val kategori: String, // Nanti kita join query untuk dapatkan nama kategori
    val hargaBeli: Int,
    val hargaJual: Int,
    val stok: Int
)

// ================= ACTIVITY =================
class KelolaBarangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKelolaBarangBinding
    private lateinit var dbHelper: DBHusnaOli
    private lateinit var adapter: SparepartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKelolaBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHusnaOli(this)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        setupRecyclerView()
        loadData()

        binding.btnTambahBarang.setOnClickListener {
            val intent = Intent(this, TambahBarangActivity::class.java)
            startActivity(intent)
        }

        binding.btnTambahStok.setOnClickListener {
            val intent = Intent(this, InputStokBaruActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = SparepartAdapter(mutableListOf(),
            onDeleteClick = { sparepart -> deleteBarang(sparepart) },
            onEditClick = { sparepart -> editBarang(sparepart) }
        )

        binding.rvSparepart.layoutManager = LinearLayoutManager(this)
        binding.rvSparepart.adapter = adapter
    }

    private fun loadData() {
        val list = mutableListOf<Sparepart>()
        val db = dbHelper.readableDatabase

        // Menggunakan JOIN agar kita bisa menampilkan 'nama_kategori', bukan sekadar angka 'kategori_id'
        val query = """
            SELECT i.item_id, i.nama_item, k.nama_kategori, i.harga_beli, i.harga_jual, i.stok 
            FROM items i
            LEFT JOIN kategori k ON i.kategori_id = k.kategori_id
        """

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                list.add(
                    Sparepart(
                        cursor.getInt(0), // item_id
                        cursor.getString(1), // nama_item
                        cursor.getString(2) ?: "Tanpa Kategori", // nama_kategori (bisa null jika belum diset)
                        cursor.getInt(3), // harga_beli
                        cursor.getInt(4), // harga_jual
                        cursor.getInt(5)  // stok
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        adapter.updateData(list)
    }

    private fun deleteBarang(sparepart: Sparepart) {
        val db = dbHelper.writableDatabase
        // Tabelnya bernama 'items', kolom ID bernama 'item_id'
        val result = db.delete(
            "items",
            "item_id=?",
            arrayOf(sparepart.id.toString())
        )

        if (result > 0) {
            Toast.makeText(this, "Barang ${sparepart.namaItem} dihapus", Toast.LENGTH_SHORT).show()
            loadData()
        }
    }

    private fun editBarang(sparepart: Sparepart) {
        val intent = Intent(this, EditBarangActivity::class.java)
        intent.putExtra("id", sparepart.id)
        startActivity(intent)
    }

    class SparepartAdapter(
        private var listSparepart: List<Sparepart>,
        private val onDeleteClick: (Sparepart) -> Unit,
        private val onEditClick: (Sparepart) -> Unit
    ) : RecyclerView.Adapter<SparepartAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemSparepartBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemSparepartBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val s = listSparepart[position]

            holder.binding.apply {
                tvNamaItem.text = s.namaItem
                tvKategori.text = s.kategori
                tvHargaBeli.text = "Rp ${s.hargaBeli}"
                tvHargaJual.text = "Rp ${s.hargaJual}"
                tvStok.text = s.stok.toString()

                btnHapus.setOnClickListener {
                    onDeleteClick(s)
                }

                btnEdit.setOnClickListener {
                    onEditClick(s)
                }
            }
        }

        override fun getItemCount(): Int = listSparepart.size

        fun updateData(newList: List<Sparepart>) {
            listSparepart = newList
            // Menggunakan notifyDataSetChanged agar aman
            notifyDataSetChanged()
        }
    }
}