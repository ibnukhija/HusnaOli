package com.example.husnaoli

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.husnaoli.databinding.FragmentDashboardKasirBinding
import com.example.husnaoli.network.DashboardKasirResponse
import com.example.husnaoli.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*

class DashboardKasirFragment : Fragment() {

    private var _binding: FragmentDashboardKasirBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardKasirBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSummary()
    }

    private fun loadSummary() {
        RetrofitClient.instance.getDashboardKasir().enqueue(object : Callback<DashboardKasirResponse> {
            override fun onResponse(call: Call<DashboardKasirResponse>, response: Response<DashboardKasirResponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res != null && res.status == "success") {
                        binding.tvTotalPendapatan.text = "Rp ${formatRupiah(res.totalPendapatan)}"
                        binding.tvJumlahTransaksi.text = "${res.jumlahTransaksi} Transaksi Terproses"

                        val listRiwayat = res.riwayat.map {
                            "Barang: ${it.detail ?: "-"}\nTotal: Rp ${formatRupiah(it.total)} (${it.tanggal})"
                        }

                        if (listRiwayat.isEmpty()) {
                            binding.lvRiwayatSingkat.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listOf("Belum ada transaksi hari ini."))
                        } else {
                            binding.lvRiwayatSingkat.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listRiwayat)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<DashboardKasirResponse>, t: Throwable) {
                Log.e("API_ERROR", t.message ?: "")
                Toast.makeText(requireContext(), "Gagal memuat ringkasan", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatRupiah(number: Int): String {
        return NumberFormat.getNumberInstance(Locale("in", "ID")).format(number)
    }

    override fun onResume() {
        super.onResume()
        loadSummary()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
