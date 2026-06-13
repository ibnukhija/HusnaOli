package com.example.husnaoli.network

import com.google.gson.annotations.SerializedName

data class KategoriResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<KategoriData>
)

data class KategoriData(
    @SerializedName("kategori_id") val kategoriId: Int,
    @SerializedName("nama_kategori") val namaKategori: String
)
