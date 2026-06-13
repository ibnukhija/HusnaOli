package com.example.husnaoli

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.husnaoli.databinding.FragmentLaporanBinding
import com.example.husnaoli.databinding.ItemLaporanRowBinding
import com.example.husnaoli.network.LaporanResponse
import com.example.husnaoli.network.RetrofitClient
import com.example.husnaoli.network.RiwayatResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class LaporanFragment : Fragment() {

    private var _binding: FragmentLaporanBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: LaporanReportAdapter
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLaporanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        loadLaporanMasuk() // Default load
    }

    private fun setupUI() {
        // Setup Spinner
        val options = arrayOf("Barang Masuk (Restock)", "Barang Keluar (Penjualan)")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = spinnerAdapter

        binding.spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateTableHeaders(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup Date Pickers
        binding.etStartDate.setOnClickListener { showDatePicker { date -> binding.etStartDate.setText(date) } }
        binding.etEndDate.setOnClickListener { showDatePicker { date -> binding.etEndDate.setText(date) } }

        // Filter Button
        binding.btnFilter.setOnClickListener {
            if (binding.spinnerType.selectedItemPosition == 0) {
                loadLaporanMasuk()
            } else {
                loadLaporanKeluar()
            }
        }
    }

    private fun updateTableHeaders(position: Int) {
        if (position == 0) {
            // Barang Masuk
            binding.tvReportLabel.text = "Laporan Restock (Barang Masuk)"
            binding.tvReportLabel.setTextColor(0xFF198754.toInt()) // Hijau
            binding.layoutHeaderMasuk.visibility = View.VISIBLE
            binding.layoutHeaderKeluar.visibility = View.GONE
            binding.tvTotalLabel.text = "TOTAL PENGELUARAN MODAL"
            binding.tvTotalValue.setTextColor(0xFFDC3545.toInt()) // Merah
            loadLaporanMasuk()
        } else {
            // Barang Keluar
            binding.tvReportLabel.text = "Laporan Penjualan (Barang Keluar)"
            binding.tvReportLabel.setTextColor(0xFF0D6EFD.toInt()) // Biru
            binding.layoutHeaderMasuk.visibility = View.GONE
            binding.layoutHeaderKeluar.visibility = View.VISIBLE
            binding.tvTotalLabel.text = "TOTAL OMSET PENJUALAN"
            binding.tvTotalValue.setTextColor(0xFF198754.toInt()) // Hijau
            loadLaporanKeluar()
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val date = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                onDateSelected(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupRecyclerView() {
        adapter = LaporanReportAdapter()
        binding.rvLaporan.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLaporan.adapter = adapter
    }

    private fun loadLaporanMasuk() {
        RetrofitClient.instance.getRiwayatRestock().enqueue(object : Callback<RiwayatResponse> {
            override fun onResponse(call: Call<RiwayatResponse>, response: Response<RiwayatResponse>) {
                if (_binding == null || !isAdded) return
                if (response.isSuccessful && response.body()?.status == "success") {
                    val data = response.body()?.data ?: listOf()
                    val rows = data.map {
                        val rowTotal = if (it.total != null && it.total > 0) it.total 
                                       else (it.hargaBeli ?: 0) * (it.jumlah ?: 0)
                        
                        ReportRow(it.tanggalMasuk, it.namaToko, it.detail, rowTotal)
                    }
                    adapter.updateData(rows)
                    calculateTotal(rows)
                }
            }
            override fun onFailure(call: Call<RiwayatResponse>, t: Throwable) {
                if (_binding == null || !isAdded) return
                Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadLaporanKeluar() {
        RetrofitClient.instance.getLaporanTransaksi().enqueue(object : Callback<LaporanResponse> {
            override fun onResponse(call: Call<LaporanResponse>, response: Response<LaporanResponse>) {
                if (_binding == null || !isAdded) return
                if (response.isSuccessful && response.body()?.status == "success") {
                    val data = response.body()?.data ?: listOf()
                    val rows = data.map {
                        ReportRow(
                            it.tanggal ?: "-",
                            it.kasir ?: "Anonim",
                            it.detail ?: "-",
                            it.total ?: 0
                        )
                    }
                    adapter.updateData(rows)
                    calculateTotal(rows)
                }
            }
            override fun onFailure(call: Call<LaporanResponse>, t: Throwable) {
                if (_binding == null || !isAdded) return
                // Log error ke Logcat untuk memudahkan pengecekan
                Log.e("LaporanFragment", "Error: ${t.message}")
                Toast.makeText(requireContext(), "Gagal memuat data: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun calculateTotal(rows: List<ReportRow>) {
        val total = rows.sumOf { it.value }
        binding.tvTotalValue.text = formatRupiah(total)
    }

    private fun formatRupiah(number: Int): String {
        return "Rp " + NumberFormat.getNumberInstance(Locale("in", "ID")).format(number)
    }

    data class ReportRow(
        val col1: String,
        val col2: String,
        val col3: String,
        val value: Int
    )

    class LaporanReportAdapter : RecyclerView.Adapter<LaporanReportAdapter.ViewHolder>() {
        private var list = mutableListOf<ReportRow>()

        class ViewHolder(val binding: ItemLaporanRowBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemLaporanRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.binding.apply {
                tvCol1.text = item.col1
                tvCol2.text = item.col2
                tvCol3.text = item.col3
                tvCol4.text = "Rp " + NumberFormat.getNumberInstance(Locale("in", "ID")).format(item.value)
            }
        }

        override fun getItemCount(): Int = list.size

        fun updateData(newList: List<ReportRow>) {
            list.clear()
            list.addAll(newList)
            notifyDataSetChanged()
        }

        fun clear() {
            list.clear()
            notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
