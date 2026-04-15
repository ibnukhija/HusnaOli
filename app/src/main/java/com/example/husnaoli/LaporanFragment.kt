package com.example.husnaoli

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.husnaoli.databinding.FragmentLaporanBinding
import java.util.*

class LaporanFragment : Fragment() {

    private var _binding: FragmentLaporanBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBHusnaOli

<<<<<<< Updated upstream
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLaporanBinding.inflate(inflater, container, false)
        dbHelper = DBHusnaOli(requireContext())

        setupDatePicker()
        setupSpinner()
        setupButtons()

        return binding.root
=======
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
>>>>>>> Stashed changes
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
<<<<<<< Updated upstream

        binding.etStartDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, day ->
                binding.etStartDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.etEndDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, day ->
                binding.etEndDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
=======
        binding.etStartDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                binding.etStartDate.setText(String.format("%04d-%02d-%02d", y, m + 1, d))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        binding.etEndDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                binding.etEndDate.setText(String.format("%04d-%02d-%02d", y, m + 1, d))
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
                Toast.makeText(requireContext(), "Pilih tanggal dulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = if (jenis == 0) {
                dbHelper.getLaporanMasuk(start, end)
            } else {
                dbHelper.getLaporanKeluar(start, end)
            }

            val adapter = LaporanAdapter(data, jenis)
            binding.lvLaporan.adapter = adapter

            val total = if (jenis == 0) {
                data.sumOf { (it as LaporanMasuk).totalModal }
            } else {
                data.sumOf { (it as LaporanKeluar).totalHarga }
            }
            binding.tvGrandTotal.text = "Rp ${String.format("%,.0f", total)}"
        }

        binding.btnCetak.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur cetak akan ditambahkan nanti", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
=======
                Toast.makeText(requireContext(), "Pilih tanggal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = if (jenis == 0) dbHelper.getLaporanMasuk(start, end) else dbHelper.getLaporanKeluar(start, end)
            binding.lvLaporan.adapter = LaporanAdapter(data, jenis)

            val total = if (jenis == 0) {
                data.filterIsInstance<LaporanMasuk>().sumOf { it.totalModal }
            } else {
                data.filterIsInstance<LaporanKeluar>().sumOf { it.totalHarga }
            }
            binding.tvGrandTotal.text = "Rp ${String.format("%,.0f", total.toDouble())}"
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
>>>>>>> Stashed changes
}