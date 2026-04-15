package com.example.husnaoli

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.husnaoli.databinding.ItemLaporanBinding

class LaporanAdapter(
<<<<<<< Updated upstream
    private var list: List<Any>,
    private val jenis: Int   // 0 = Masuk, 1 = Keluar
=======
    private val list: List<Any>,
    private val jenis: Int
>>>>>>> Stashed changes
) : BaseAdapter() {

    override fun getCount(): Int = list.size
    override fun getItem(position: Int) = list[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
<<<<<<< Updated upstream
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
=======
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
>>>>>>> Stashed changes
    }
}