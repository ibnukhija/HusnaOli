package com.example.husnaoli

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.husnaoli.databinding.ActivityRiwayatRestockBinding
import com.example.husnaoli.databinding.ItemRiwayatRestockBinding
import com.example.husnaoli.network.LoginResponse
import com.example.husnaoli.network.RetrofitClient
import com.example.husnaoli.network.RiwayatResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RiwayatRestockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiwayatRestockBinding
    private lateinit var adapter: RiwayatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiwayatRestockBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        RetrofitClient.instance.getRiwayatRestock().enqueue(object : Callback<RiwayatResponse> {
            override fun onResponse(call: Call<RiwayatResponse>, response: Response<RiwayatResponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res != null && res.status == "success") {
                        val list = res.data.map {
                            Riwayat(it.restockId, it.tanggalMasuk, it.namaToko, it.detail, it.keterangan)
                        }
                        adapter.updateData(list)
                    }
                }
            }

            override fun onFailure(call: Call<RiwayatResponse>, t: Throwable) {
                Toast.makeText(this@RiwayatRestockActivity, "Gagal muat data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun hapusRiwayat(id: Int) {
        RetrofitClient.instance.hapusRiwayatRestock(id).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@RiwayatRestockActivity, "Riwayat dihapus", Toast.LENGTH_SHORT).show()
                    loadData()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@RiwayatRestockActivity, "Gagal hapus", Toast.LENGTH_SHORT).show()
            }
        })
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
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    data class Riwayat(
        val id: Int, val tanggal: String, val supplier: String,
        val detail: String, val keterangan: String?
    )

    class RiwayatAdapter(private var list: List<Riwayat>, private val onDelete: (Int) -> Unit) :
        RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

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
