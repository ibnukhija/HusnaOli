package com.example.husnaoli

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.husnaoli.databinding.ItemLaporanBinding

class LaporanAdapter(
    private var list: List<Any>,
    private val jenis: Int   // 0 = Masuk, 1 = Keluar
) : BaseAdapter() {

    override fun getCount(): Int = list.size
    override fun getItem(position: Int) = list[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding = ItemLaporanBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
        val item = list[position]

        if (jenis == 0) { // Barang Masuk
            val data = item as LaporanMasuk
            binding.tvTanggal.text = data.tanggal
            binding.tvKasirSupplier.text = data.supplier
            binding.tvDetail.text = data.detail
            binding.tvTotal.text = "Rp ${String.format("%,.0f", data.totalModal)}"
            binding.tvTotal.setTextColor(0xFFDC3545.toInt()) // Merah
        } else { // Penjualan
            val data = item as LaporanKeluar
            binding.tvTanggal.text = data.tanggal
            binding.tvKasirSupplier.text = data.kasir
            binding.tvDetail.text = data.detail
            binding.tvTotal.text = "Rp ${String.format("%,.0f", data.totalHarga)}"
            binding.tvTotal.setTextColor(0xFF28A745.toInt()) // Hijau
        }

        return binding.root
    }
}