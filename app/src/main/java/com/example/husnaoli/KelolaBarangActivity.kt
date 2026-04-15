package com.example.husnaoli

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.husnaoli.databinding.ActivityKelolaBarangBinding
import com.example.husnaoli.databinding.ItemSparepartBinding

data class Sparepart(
    val id: Int,
    val namaItem: String,
    val kategori: String,
    val hargaBeli: Int,
    val hargaJual: Int,
    val stok: Int
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
        sparepartAdapter = SparepartAdapter(mutableListOf(),
            { s -> deleteBarang(s) },
            { s -> editBarang(s) }
        )
        binding.lvSparepart.adapter = sparepartAdapter

        binding.btnBack.setOnClickListener { finish() }

        binding.btnTambahBarang.setOnClickListener {
            startActivity(Intent(this, TambahBarangActivity::class.java))
        }

        loadData()
    }

    private fun loadData() {
        val list = mutableListOf<Sparepart>()
        val db = dbHelper.readableDatabase
        val query = """
            SELECT i.item_id, i.nama_item, k.nama_kategori, i.harga_beli, i.harga_jual, i.stok 
            FROM items i
            LEFT JOIN kategori k ON i.kategori_id = k.kategori_id
        """
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(Sparepart(
                    cursor.getInt(0), cursor.getString(1),
                    cursor.getString(2) ?: "Tanpa Kategori",
                    cursor.getInt(3), cursor.getInt(4), cursor.getInt(5)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        sparepartAdapter.updateData(list)
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
            val b = ItemSparepartBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
            val s = list[position]
            b.tvNamaItem.text = s.namaItem
            b.tvStok.text = "Stok: ${s.stok}"
            b.tvHargaJual.text = "Rp ${s.hargaJual}"

            b.btnHapus.setOnClickListener { onDelete(s) }
            b.btnEdit.setOnClickListener { onEdit(s) }
            return b.root
        }

        fun updateData(newList: List<Sparepart>) {
            list.clear()
            list.addAll(newList)
            notifyDataSetChanged()
        }
    }
}