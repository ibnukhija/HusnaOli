package com.example.husnaoli

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.husnaoli.databinding.FragmentDashboardKasirBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DashboardKasirFragment : Fragment() {

    private var _binding: FragmentDashboardKasirBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBHusnaOli

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardKasirBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBHusnaOli(requireContext())
        loadSummary()
    }

    private fun loadSummary() {
        val db = dbHelper.readableDatabase
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Hitung Pendapatan Hari Ini
        val cursor = db.rawQuery(
            "SELECT SUM(total_harga), COUNT(transaksi_id) " +
                    "FROM transaksi " +
                    "WHERE tanggal_transaksi LIKE ?",
            arrayOf("$today%")
        )

        if (cursor.moveToFirst()) {
            val total = cursor.getInt(0)
            val count = cursor.getInt(1)

            binding.tvTotalPendapatan.text = "Rp ${formatRupiah(total)}"
            binding.tvJumlahTransaksi.text = "$count Transaksi Terproses"
        }
        cursor.close()

        // Load 5 Transaksi Terakhir untuk Riwayat Singkat
        val listRiwayat = mutableListOf<String>()
        val cursorRiwayat = db.rawQuery(
            "SELECT transaksi_id, total_harga, tanggal_transaksi " +
                    "FROM transaksi " +
                    "ORDER BY transaksi_id DESC LIMIT 5",
            null
        )

        if (cursorRiwayat.moveToFirst()) {
            do {
                val id = cursorRiwayat.getInt(0)
                val total = cursorRiwayat.getInt(1)
                val tgl = cursorRiwayat.getString(2)
                listRiwayat.add("ID Transaksi: $id - Rp ${formatRupiah(total)}\n$tgl")
            } while (cursorRiwayat.moveToNext())
        } else {
            listRiwayat.add("Belum ada transaksi hari ini.")
        }
        cursorRiwayat.close()

        binding.lvRiwayatSingkat.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listRiwayat)
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
