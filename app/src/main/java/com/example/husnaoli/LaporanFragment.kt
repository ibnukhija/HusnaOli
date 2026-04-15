package com.example.husnaoli

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.husnaoli.databinding.FragmentLaporanBinding
import java.util.*
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient

class LaporanFragment : Fragment() {

    private var _binding: FragmentLaporanBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBHusnaOli

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLaporanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBHusnaOli(requireContext())
        setupDatePicker()
        setupSpinner()
        setupButtons()
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        binding.etStartDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                binding.etStartDate.setText(String.format("%04d-%02d-%02d", y, m + 1, d))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        binding.etEndDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                binding.etEndDate.setText(String.format("%04d-%02d-%02d", y, m + 1, d))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupSpinner() {
        val jenis = arrayOf("Barang Masuk (Restock)", "Penjualan (Keluar)")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, jenis)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerJenis.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnTampilkan.setOnClickListener {
            val start = binding.etStartDate.text.toString()
            val end = binding.etEndDate.text.toString()
            val jenis = binding.spinnerJenis.selectedItemPosition

            if (start.isEmpty() || end.isEmpty()) {
                Toast.makeText(requireContext(), "Harap pilih tanggal mulai dan akhir!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = if (jenis == 0) dbHelper.getLaporanMasuk(start, end) else dbHelper.getLaporanKeluar(start, end)

            // validasi data kosong
            if (data.isEmpty()) {
                binding.lvLaporan.adapter = null
                binding.tvGrandTotal.text = "Rp 0"

                // dialog peringatan
                AlertDialog.Builder(requireContext())
                    .setTitle("Peringatan")
                    .setMessage("Belum ada data laporan pada periode tersebut.")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            binding.lvLaporan.adapter = LaporanAdapter(data, jenis)

            val total = if (jenis == 0) {
                data.filterIsInstance<LaporanMasuk>().sumOf { it.totalModal }
            } else {
                data.filterIsInstance<LaporanKeluar>().sumOf { it.totalHarga }
            }
            binding.tvGrandTotal.text = "Rp ${String.format("%,.0f", total.toDouble())}"
        }

        binding.btnCetak.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur cetak sedang dikembangkan (PDF)", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}