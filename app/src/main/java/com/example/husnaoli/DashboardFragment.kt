package com.example.husnaoli

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.husnaoli.databinding.FragmentDashboardBinding
import com.example.husnaoli.databinding.ItemLaporanRowBinding
import com.example.husnaoli.network.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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

        // Initial Chart UI Setup (Showing loading state)
        binding.lineChartPenjualan.setNoDataText("Memuat data grafik...")
        binding.barChartTerlaris.setNoDataText("Memuat data grafik...")

        setupSpinner()
    }

    private fun setupSpinner() {
        val options = arrayOf("Harian", "Mingguan", "Bulanan")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = adapter

        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedFilter = options[position].lowercase(Locale.ROOT)
                loadStats(selectedFilter)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadStats(filter: String) {
        Log.d("DashboardFragment", "Loading stats for filter: $filter")
        RetrofitClient.instance.getDashboardStats(filter).enqueue(object : Callback<DashboardResponse> {
            override fun onResponse(call: Call<DashboardResponse>, response: Response<DashboardResponse>) {
                if (_binding == null || !isAdded) return
                
                if (response.isSuccessful) {
                    val stats = response.body()
                    Log.d("DashboardFragment", "API Response: $stats")
                    
                    if (stats != null && stats.status.equals("success", ignoreCase = true)) {
                        binding.tvTotalBarang.text = stats.totalBarang.toString()
                        binding.tvStokTipis.text = stats.stokTipis.toString()
                        binding.tvOmset.text = formatRupiah(stats.omset)

                        updateTransaksiTerakhir(stats.transaksiTerakhir)
                        setupLineChart(stats.chartPenjualan)
                        setupBarChart(stats.topItems)
                    } else {
                        Log.e("DashboardFragment", "Stats status not success: ${stats?.status}")
                        setChartsNoData("Data tidak tersedia")
                    }
                } else {
                    Log.e("DashboardFragment", "Response not successful: ${response.code()}")
                    setChartsNoData("Gagal memuat data")
                }
            }

            override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
                if (_binding == null || !isAdded) return
                Log.e("API_ERROR", t.message ?: "Unknown Error")
                Toast.makeText(requireContext(), "Gagal memuat statistik", Toast.LENGTH_SHORT).show()
                setChartsNoData("Kesalahan koneksi")
            }
        })
    }

    private fun setChartsNoData(message: String) {
        binding.lineChartPenjualan.setNoDataText(message)
        binding.lineChartPenjualan.clear()
        binding.barChartTerlaris.setNoDataText(message)
        binding.barChartTerlaris.clear()
    }

    private fun updateTransaksiTerakhir(listLast: List<TransaksiTerakhir>?) {
        if (listLast.isNullOrEmpty()) {
            binding.lvTransaksiTerakhir.visibility = View.GONE
            binding.tvEmptyTransaksi.visibility = View.VISIBLE
        } else {
            binding.lvTransaksiTerakhir.visibility = View.VISIBLE
            binding.tvEmptyTransaksi.visibility = View.GONE
            binding.lvTransaksiTerakhir.adapter = TransaksiAdapter(listLast)
        }
    }

    private fun setupLineChart(chartData: com.example.husnaoli.network.ChartData?) {
        val chart = binding.lineChartPenjualan
        
        if (chartData == null || chartData.data.isEmpty()) {
            Log.d("DashboardFragment", "Line chart data is empty or null")
            chart.setNoDataText("Belum ada data penjualan untuk periode ini")
            chart.clear()
            return
        }

        val entries = chartData.data.mapIndexed { index, value ->
            Entry(index.toFloat(), value.toFloat())
        }

        val dataSet = LineDataSet(entries, "Penjualan (Rp)").apply {
            color = Color.parseColor("#1E40AF")
            setCircleColor(Color.parseColor("#1E40AF"))
            valueTextColor = Color.BLACK
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawCircleHole(false)
            setDrawFilled(true)
            fillColor = Color.parseColor("#1E40AF")
            fillAlpha = 40
            mode = LineDataSet.Mode.CUBIC_BEZIER
            valueTextSize = 10f
        }

        chart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(chartData.labels)
                granularity = 1f
                isGranularityEnabled = true
                labelCount = chartData.labels.size
                setDrawGridLines(false)
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }
            
            axisRight.isEnabled = false
            animateX(1000)
            notifyDataSetChanged()
            invalidate()
        }
    }

    private fun setupBarChart(topItems: List<TopItem>?) {
        val chart = binding.barChartTerlaris
        
        if (topItems.isNullOrEmpty()) {
            Log.d("DashboardFragment", "Bar chart data is empty or null")
            chart.setNoDataText("Belum ada data barang terlaris")
            chart.clear()
            return
        }

        val entries = topItems.mapIndexed { index, item ->
            BarEntry(index.toFloat(), item.totalSold.toFloat())
        }

        val dataSet = BarDataSet(entries, "Jumlah Terjual").apply {
            color = Color.parseColor("#FF5E00")
            valueTextColor = Color.BLACK
            valueTextSize = 10f
        }

        chart.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            setFitBars(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(topItems.map { it.namaItem })
                granularity = 1f
                isGranularityEnabled = true
                labelCount = topItems.size
                labelRotationAngle = -45f
                setDrawGridLines(false)
            }

            axisLeft.apply {
                axisMinimum = 0f
                granularity = 1f
                isGranularityEnabled = true
            }
            
            axisRight.isEnabled = false
            setExtraOffsets(0f, 0f, 0f, 20f)
            animateY(1000)
            notifyDataSetChanged()
            invalidate()
        }
    }

    private fun formatRupiah(number: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(number).replace("Rp", "Rp ")
    }

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
                tvCol1.text = item.tanggal
                tvCol2.text = item.kasir ?: "-"
                tvCol3.text = item.detail ?: "-"
                tvCol4.text = formatRupiah(item.total)
            }
            return view
        }
    }

    override fun onResume() {
        super.onResume()
        // Data loading is triggered by the spinner's default selection or manual change.
        // We don't need a redundant loadStats call here if setupSpinner is called in onViewCreated.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
