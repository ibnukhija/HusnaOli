package com.example.husnaoli

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.husnaoli.databinding.ActivityKelolaBarangBinding
import com.example.husnaoli.databinding.ItemSparepartBinding

data class Sparepart(
    val id: Int,
    val namaItem: String,
    val kategori: String,
    val hargaBeli: Int,
    val hargaJual: Int,
    val stok: Int,
    val foto: String?
)

class KelolaBarangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKelolaBarangBinding
    private lateinit var dbHelper: DBHusnaOli
    private lateinit var sparepartAdapter: SparepartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKelolaBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHusnaOli(this)

        // Setup ListView
        sparepartAdapter = SparepartAdapter(
            mutableListOf(),
            { s -> deleteBarang(s) },
            { s -> editBarang(s) }
        )
        binding.lvSparepart.adapter = sparepartAdapter

        binding.btnBack.setOnClickListener { finish() }

        binding.btnTambahBarang.setOnClickListener {
            startActivity(Intent(this, TambahBarangActivity::class.java))
        }

        binding.bottomNavigation.selectedItemId = R.id.nav_barang
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.nav_user -> {
                    val intent = Intent(this, KelolaUserActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.nav_barang -> true
                R.id.nav_laporan -> {
                    Toast.makeText(this, "Membuka Laporan", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }
    }

    private fun loadData() {
        val list = mutableListOf<Sparepart>()
        val db = dbHelper.readableDatabase
        val query = "SELECT items.item_id, items.nama_item, kategori.nama_kategori, " +
                "items.harga_beli, items.harga_jual, items.stok, items.foto " +
                "FROM items " +
                "JOIN kategori ON items.kategori_id = kategori.kategori_id"

        val cursor = db.rawQuery(query, null)
        
        if (cursor.moveToFirst()) {
            val idIdx = cursor.getColumnIndex("item_id")
            val namaIdx = cursor.getColumnIndex("nama_item")
            val katIdx = cursor.getColumnIndex("nama_kategori")
            val beliIdx = cursor.getColumnIndex("harga_beli")
            val jualIdx = cursor.getColumnIndex("harga_jual")
            val stokIdx = cursor.getColumnIndex("stok")
            val fotoIdx = cursor.getColumnIndex("foto")

            do {
                list.add(
                    Sparepart(
                        cursor.getInt(idIdx),
                        cursor.getString(namaIdx),
                        cursor.getString(katIdx) ?: "Tanpa Kategori",
                        cursor.getInt(beliIdx),
                        cursor.getInt(jualIdx),
                        cursor.getInt(stokIdx),
                        cursor.getString(fotoIdx)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        
        // Masukkan list hasil query ke dalam adapter agar tampil di UI
        sparepartAdapter.updateData(list)
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun deleteBarang(s: Sparepart) {
        val db = dbHelper.writableDatabase
        db.delete("items", "item_id=?", arrayOf(s.id.toString()))
        loadData()
        Toast.makeText(this, "${s.namaItem} dihapus", Toast.LENGTH_SHORT).show()
    }

    private fun editBarang(s: Sparepart) {
        val intent = Intent(this, EditBarangActivity::class.java)
        intent.putExtra("id", s.id)
        startActivity(intent)
    }

    // ADAPTER LISTVIEW
    inner class SparepartAdapter(
        private var list: MutableList<Sparepart>,
        private val onDelete: (Sparepart) -> Unit,
        private val onEdit: (Sparepart) -> Unit
    ) : BaseAdapter() {

        override fun getCount(): Int = list.size
        override fun getItem(position: Int) = list[position]
        override fun getItemId(position: Int) = list[position].id.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val b =
                ItemSparepartBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
            val s = list[position]

            b.tvNamaItem.text = s.namaItem
            b.tvKategori.text = s.kategori
            b.tvStok.text = s.stok.toString()
            b.tvHargaBeli.text = "Rp ${s.hargaBeli}"
            b.tvHargaJual.text = "Rp ${s.hargaJual}"

            // Tampilkan foto menggunakan Glide jika URI ada
            if (!s.foto.isNullOrEmpty()) {
                Glide.with(parent!!.context)
                    .load(s.foto)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(b.imgSparepart)
            } else {
                b.imgSparepart.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            b.btnHapus.setOnClickListener { onDelete(s) }
            b.btnEdit.setOnClickListener { onEdit(s) }
            return b.root
        }

        fun updateData(newList: List<Sparepart>) {
            this.list.clear()
            this.list.addAll(newList)
            notifyDataSetChanged()
        }
    }
}
