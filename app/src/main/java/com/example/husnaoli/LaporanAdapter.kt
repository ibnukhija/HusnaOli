package com.example.husnaoli

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.husnaoli.databinding.ItemLaporanBinding

class LaporanAdapter(
    private val list: List<Any>,
    private val jenis: Int
) : BaseAdapter() {

    override fun getCount(): Int = list.size
    override fun getItem(position: Int) = list[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding: ItemLaporanBinding
        val view: View

        if (convertView == null) {
            binding = ItemLaporanBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
            view = binding.root
            view.tag = binding
        } else {
            binding = convertView.tag as ItemLaporanBinding
            view = convertView
        }

        val item = list[position]

        if (jenis == 0 && item is LaporanMasuk) {
            binding.tvTanggal.text = item.tanggal
            binding.tvKasirSupplier.text = item.supplier
            binding.tvDetail.text = item.detail
            binding.tvTotal.text = "Rp ${String.format("%,.0f", item.totalModal.toDouble())}"
            binding.tvTotal.setTextColor(0xFFDC3545.toInt())
        } else if (jenis == 1 && item is LaporanKeluar) {
            binding.tvTanggal.text = item.tanggal
            binding.tvKasirSupplier.text = item.kasir
            binding.tvDetail.text = item.detail
            binding.tvTotal.text = "Rp ${String.format("%,.0f", item.totalHarga.toDouble())}"
            binding.tvTotal.setTextColor(0xFF28A745.toInt())
        }

        return view
    }
}