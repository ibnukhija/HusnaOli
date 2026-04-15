package com.example.husnaoli

import android.app.DatePickerDialog
import android.content.ContentValues
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.husnaoli.databinding.FragmentInputStokBinding
import java.util.Calendar

class InputStokFragment : Fragment() {

    private var _binding: FragmentInputStokBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBHusnaOli
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
        dbHelper = DBHusnaOli(requireContext())

        setupSpinnerBarang()
        setupListeners()
    }

    private fun setupSpinnerBarang() {
        listBarang.clear()
        val names = mutableListOf<String>()
        names.add("-- Pilih Barang --")

        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT item_id, nama_item, harga_beli FROM items", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val nama = cursor.getString(1)
                val hargaBeli = cursor.getInt(2)

                val map = HashMap<String, Any>()
                map["id"] = id
                map["nama"] = nama
                map["harga_beli"] = hargaBeli
                listBarang.add(map)

                names.add(nama)
            } while (cursor.moveToNext())
        }
        cursor.close()

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerNamaBarang.adapter = adapter
    }

    private fun setupListeners() {
        binding.etTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // %02d agar bulan 4 jadi 04, dan hari 1 jadi 01
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
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

        val selectedPos = binding.spinnerNamaBarang.selectedItemPosition
        val jumlahMasukStr = binding.etJumlahMasuk.text.toString().trim()

        if (supplier.isEmpty() || tanggal.isEmpty() || selectedPos == 0 || jumlahMasukStr.isEmpty()) {
            Toast.makeText(requireContext(), "Harap lengkapi semua data!", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedItem = listBarang[selectedPos - 1]
        val itemId = selectedItem["id"] as Int
        // val namaBarang = selectedItem["nama"] as String
        val jumlahMasuk = jumlahMasukStr.toIntOrNull() ?: 0
        val hargaBeli = selectedItem["harga_beli"] as Int

        val db = dbHelper.writableDatabase
        db.beginTransaction()

        try {
            // Menggunakan tabel yang ada di DBHusnaOli: restock_items dan detail_restock_items
            val valuesRestock = ContentValues().apply {
                put("tanggal_masuk", tanggal)
                put("nama_toko", supplier)
                put("keterangan", keterangan)
            }
            val restockId = db.insert("restock_items", null, valuesRestock)

            if (restockId != -1L) {
                val valuesDetail = ContentValues().apply {
                    put("restock_id", restockId)
                    put("item_id", itemId)
                    put("jumlah", jumlahMasuk)
                    put("harga_beli_saat_itu", hargaBeli)
                }
                db.insert("detail_restock_items", null, valuesDetail)

                // Update stok di tabel items
                db.execSQL("UPDATE items SET stok = stok + $jumlahMasuk WHERE item_id = $itemId")

                db.setTransactionSuccessful()
                Toast.makeText(requireContext(), "Stok Berhasil Ditambahkan", Toast.LENGTH_SHORT).show()
                kosongkanForm()
            } else {
                Toast.makeText(requireContext(), "Gagal menyimpan data restock", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            db.endTransaction()
        }
    }

    private fun kosongkanForm() {
        binding.etSupplier.text.clear()
        binding.etTanggal.text.clear()
        binding.etKeterangan.text.clear()
        binding.spinnerNamaBarang.setSelection(0)
        binding.etJumlahMasuk.text.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
