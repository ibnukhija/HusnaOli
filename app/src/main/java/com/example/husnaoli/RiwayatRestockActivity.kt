package com.example.husnaoli

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.husnaoli.databinding.ActivityRiwayatRestockBinding
import com.example.husnaoli.databinding.ItemRiwayatRestockBinding

class RiwayatRestockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiwayatRestockBinding
    private lateinit var dbHelper: DBHusnaOli
    private lateinit var adapter: RiwayatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiwayatRestockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHusnaOli(this)
        setupRecyclerView()
        setupListeners()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = RiwayatAdapter(mutableListOf()) { id ->
            hapusRiwayat(id)
        }
        binding.rvRiwayatRestock.layoutManager = LinearLayoutManager(this)
        binding.rvRiwayatRestock.adapter = adapter
    }

    private fun loadData() {
        val list = mutableListOf<Riwayat>()
        val db = dbHelper.readableDatabase
        
        // Menggunakan nama tabel yang sesuai dengan DBHusnaOli.kt
        val sql = """
            SELECT r.restock_id, r.tanggal_masuk, r.nama_toko, 
            (SELECT GROUP_CONCAT(i.nama_item || ' (x' || d.jumlah || ')') 
             FROM detail_restock_items d 
             JOIN items i ON d.item_id = i.item_id 
             WHERE d.restock_id = r.restock_id) as detail,
            r.keterangan 
            FROM restock_items r 
            ORDER BY r.restock_id DESC
        """.trimIndent()

        val cursor = db.rawQuery(sql, null)

        if (cursor.moveToFirst()) {
            do {
                list.add(Riwayat(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3) ?: "-",
                    cursor.getString(4)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        adapter.updateData(list)
    }

    private fun hapusRiwayat(id: Int) {
        val db = dbHelper.writableDatabase
        val result = db.delete("restock_items", "restock_id=?", arrayOf(id.toString()))
        if (result > 0) {
            Toast.makeText(this, "Riwayat dihapus", Toast.LENGTH_SHORT).show()
            loadData()
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        
        binding.btnInputStok.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("TARGET_FRAGMENT", "INPUT_STOK")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    data class Riwayat(
        val id: Int,
        val tanggal: String,
        val supplier: String,
        val detail: String,
        val keterangan: String?
    )

    class RiwayatAdapter(
        private var list: List<Riwayat>,
        private val onDelete: (Int) -> Unit
    ) : RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemRiwayatRestockBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemRiwayatRestockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.binding.apply {
                tvTanggal.text = item.tanggal
                tvSupplier.text = item.supplier
                tvDetailBarang.text = item.detail
                tvKeterangan.text = item.keterangan ?: "-"
                btnHapusRiwayat.setOnClickListener { onDelete(item.id) }
            }
        }

        override fun getItemCount(): Int = list.size

        fun updateData(newList: List<Riwayat>) {
            list = newList
            notifyDataSetChanged()
        }
    }
}
