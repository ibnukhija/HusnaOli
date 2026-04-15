package com.example.husnaoli

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.husnaoli.databinding.ActivityLaporanBinding
import java.util.*

class LaporanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaporanBinding
    private lateinit var dbHelper: DBHusnaOli

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHusnaOli(this)

        setupDatePicker()
        setupSpinner()
        setupButtons()
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()

        binding.etStartDate.setOnClickListener {
            DatePickerDialog(this, android.app.DatePickerDialog.THEME_HOLO_DARK, { _, year, month, day ->
                binding.etStartDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.etEndDate.setOnClickListener {
            DatePickerDialog(this, android.app.DatePickerDialog.THEME_HOLO_DARK, { _, year, month, day ->
                binding.etEndDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupSpinner() {
        val jenisLaporan = arrayOf("Barang Masuk (Restock)", "Penjualan (Keluar)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, jenisLaporan)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerJenis.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnTampilkan.setOnClickListener {
            val start = binding.etStartDate.text.toString()
            val end = binding.etEndDate.text.toString()
            val jenis = binding.spinnerJenis.selectedItemPosition

            if (start.isEmpty() || end.isEmpty()) {
                Toast.makeText(this, "Silakan pilih tanggal mulai dan sampai", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = if (jenis == 0) {
                dbHelper.getLaporanMasuk(start, end)
            } else {
                dbHelper.getLaporanKeluar(start, end)
            }

            val adapter = LaporanAdapter(data, jenis)
            binding.lvLaporan.adapter = adapter

            // Hitung Grand Total
            val total = if (jenis == 0) {
                data.sumOf { (it as LaporanMasuk).totalModal }
            } else {
                data.sumOf { (it as LaporanKeluar).totalHarga }
            }
            binding.tvGrandTotal.text = "Rp ${String.format("%,.0f", total)}"
        }

        binding.btnCetak.setOnClickListener {
            Toast.makeText(this, "Fitur Cetak PDF akan ditambahkan nanti", Toast.LENGTH_LONG).show()
        }
    }
}