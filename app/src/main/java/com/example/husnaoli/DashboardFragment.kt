package com.example.husnaoli

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.husnaoli.databinding.FragmentDashboardBinding
import com.example.husnaoli.databinding.ItemLaporanRowBinding
import com.example.husnaoli.network.DashboardResponse
import com.example.husnaoli.network.RetrofitClient
import com.example.husnaoli.network.TransaksiTerakhir
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinner()
        loadStats()
    }

    private fun setupSpinner() {
        val options = arrayOf("Harian", "Mingguan", "Bulanan")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = adapter
    }

    private fun loadStats() {
        RetrofitClient.instance.getDashboardStats().enqueue(object : Callback<DashboardResponse> {
            override fun onResponse(call: Call<DashboardResponse>, response: Response<DashboardResponse>) {
                if (_binding == null || !isAdded) return
                if (response.isSuccessful) {
                    val stats = response.body()
                    if (stats != null && stats.status == "success") {
                        binding.tvTotalBarang.text = stats.totalBarang.toString()
                        binding.tvStokTipis.text = stats.stokTipis.toString()
                        binding.tvOmset.text = formatRupiah(stats.omset)

                        // Update Transaksi Terakhir dengan Kolom Terpisah
                        val listLast = stats.transaksiTerakhir
                        if (listLast.isNullOrEmpty()) {
                            binding.lvTransaksiTerakhir.visibility = View.GONE
                            binding.tvEmptyTransaksi.visibility = View.VISIBLE
                        } else {
                            binding.lvTransaksiTerakhir.visibility = View.VISIBLE
                            binding.tvEmptyTransaksi.visibility = View.GONE

                            // Menggunakan Custom Adapter (TransaksiAdapter)
                            val listAdapter = TransaksiAdapter(listLast)
                            binding.lvTransaksiTerakhir.adapter = listAdapter
                        }
                    }
                }
            }

            override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
                if (_binding == null || !isAdded) return
                Log.e("API_ERROR", t.message ?: "Unknown Error")
                context?.let {
                    Toast.makeText(it, "Gagal memuat statistik", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun formatRupiah(number: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(number).replace("Rp", "Rp ")
    }

    // --- Custom Adapter untuk memisahkan kolom ---
    inner class TransaksiAdapter(private val list: List<TransaksiTerakhir>) : BaseAdapter() {
        override fun getCount(): Int = list.size
        override fun getItem(position: Int): Any = list[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val bindingItem: ItemLaporanRowBinding
            val view: View

            if (convertView == null) {
                bindingItem = ItemLaporanRowBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
                view = bindingItem.root
                view.tag = bindingItem
            } else {
                bindingItem = convertView.tag as ItemLaporanRowBinding
                view = convertView
            }

            val item = list[position]
            bindingItem.apply {
                tvCol1.text = item.tanggal      // Kolom 1: Tanggal
                tvCol2.text = item.kasir ?: "-" // Kolom 2: Kasir
                tvCol3.text = item.detail ?: "-" // Kolom 3: Barang Yang Terjual
                tvCol4.text = formatRupiah(item.total) // Kolom 4: Total
            }
            return view
        }
    }

    override fun onResume() {
        super.onResume()
        loadStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}