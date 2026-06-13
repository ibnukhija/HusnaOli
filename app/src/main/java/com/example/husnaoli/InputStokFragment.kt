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
import com.example.husnaoli.databinding.FragmentInputStokBinding
import com.example.husnaoli.network.BarangResponse
import com.example.husnaoli.network.LoginResponse
import com.example.husnaoli.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class InputStokFragment : Fragment() {

    private var _binding: FragmentInputStokBinding? = null
    private val binding get() = _binding!!
    private var listBarang = mutableListOf<HashMap<String, Any>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputStokBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinnerBarang()
        setupListeners()
    }

    private fun setupSpinnerBarang() {
        RetrofitClient.instance.getBarang().enqueue(object : Callback<BarangResponse> {
            override fun onResponse(call: Call<BarangResponse>, response: Response<BarangResponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res != null && res.status == "success") {
                        listBarang.clear()
                        val names = mutableListOf<String>()
                        names.add("-- Pilih Barang --")

                        res.data.forEach {
                            val map = HashMap<String, Any>()
                            map["id"] = it.itemId
                            map["nama"] = it.namaItem
                            map["harga_beli"] = it.hargaBeli
                            listBarang.add(map)
                            names.add(it.namaItem)
                        }

                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerNamaBarang.adapter = adapter
                    }
                }
            }

            override fun onFailure(call: Call<BarangResponse>, t: Throwable) {
                Log.e("API_ERROR", t.message ?: "Unknown Error")
                Toast.makeText(requireContext(), "Gagal mengambil data barang", Toast.LENGTH_SHORT).show()
            }
        })

        // Tampilkan harga beli otomatis saat barang dipilih
        binding.spinnerNamaBarang.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val selectedItem = listBarang[position - 1]
                    val hargaBeli = selectedItem["harga_beli"] as Int
                    binding.etHargaBeli.setText(hargaBeli.toString())
                } else {
                    binding.etHargaBeli.text.clear()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupListeners() {
        binding.btnBackInput.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.etTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                binding.etTanggal.setText(formattedDate)
            }, year, month, day)

            datePickerDialog.show()
        }

        binding.btnSimpanStok.setOnClickListener {
            simpanStokMasuk()
        }
    }

    private fun simpanStokMasuk() {
        val supplier = binding.etSupplier.text.toString().trim()
        val tanggal = binding.etTanggal.text.toString().trim()
        val keterangan = binding.etKeterangan.text.toString().trim()
        val hargaBeliStr = binding.etHargaBeli.text.toString().trim()

        val selectedPos = binding.spinnerNamaBarang.selectedItemPosition
        val jumlahMasukStr = binding.etJumlahMasuk.text.toString().trim()

        if (supplier.isEmpty() || tanggal.isEmpty() || selectedPos == 0 || jumlahMasukStr.isEmpty()) {
            Toast.makeText(requireContext(), "Harap lengkapi semua data!", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedItem = listBarang[selectedPos - 1]
        val itemId = selectedItem["id"] as Int
        val jumlahMasuk = jumlahMasukStr.toIntOrNull() ?: 0
        val hargaBeli = hargaBeliStr.toIntOrNull() ?: (selectedItem["harga_beli"] as Int)

        RetrofitClient.instance.simpanRestock(
            tanggal,
            supplier,
            keterangan,
            itemId,
            jumlahMasuk,
            hargaBeli
        ).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res != null && res.status == "success") {
                        Toast.makeText(requireContext(), res.message, Toast.LENGTH_SHORT).show()
                        kosongkanForm()
                    } else {
                        Toast.makeText(requireContext(), res?.message ?: "Gagal simpan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Error Server: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Koneksi Gagal: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun kosongkanForm() {
        binding.etSupplier.text.clear()
        binding.etTanggal.text.clear()
        binding.etKeterangan.text.clear()
        binding.spinnerNamaBarang.setSelection(0)
        binding.etHargaBeli.text.clear()
        binding.etJumlahMasuk.text.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
