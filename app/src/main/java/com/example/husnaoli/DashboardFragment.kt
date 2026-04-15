package com.example.husnaoli

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.husnaoli.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBHusnaOli

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBHusnaOli(requireContext())

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
        val db = dbHelper.readableDatabase
        
        // Total Jenis Barang
        val cursorBarang = db.rawQuery("SELECT COUNT(*) FROM items", null)
        if (cursorBarang.moveToFirst()) {
            binding.tvTotalBarang.text = cursorBarang.getInt(0).toString()
        }
        cursorBarang.close()

        // Stok Tipis (< 5)
        val cursorStok = db.rawQuery("SELECT COUNT(*) FROM items WHERE stok < 5", null)
        if (cursorStok.moveToFirst()) {
            binding.tvStokTipis.text = cursorStok.getInt(0).toString()
        }
        cursorStok.close()

        // Total Transaksi
        val cursorPendapatan = db.rawQuery("SELECT SUM(total_harga) FROM transaksi", null)
        if (cursorPendapatan.moveToFirst()) {
            binding.tvOmset.text = "Rp ${cursorPendapatan.getDouble(0)}"
        }
        cursorPendapatan.close()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
