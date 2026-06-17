package com.example.husnaoli.network

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("login.php")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @GET("get_barang.php")
    fun getBarang(): Call<BarangResponse>

    @GET("get_barang_detail.php")
    fun getBarangDetail(
        @Query("item_id") itemId: Int
    ): Call<BarangResponse>

    @FormUrlEncoded
    @POST("delete_barang.php")
    fun deleteBarang(
        @Field("item_id") itemId: Int
    ): Call<LoginResponse>

    @GET("get_kategori.php")
    fun getKategori(): Call<KategoriResponse>

    @FormUrlEncoded
    @POST("tambah_barang.php")
    fun tambahBarang(
        @Field("nama_item") nama: String,
        @Field("kategori_id") kategoriId: Int,
        @Field("harga_beli") hargaBeli: Int,
        @Field("harga_jual") hargaJual: Int,
        @Field("stok") stok: Int,
        @Field("foto") foto: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("edit_barang.php")
    fun editBarang(
        @Field("item_id") itemId: Int,
        @Field("nama_item") nama: String,
        @Field("kategori_id") kategoriId: Int,
        @Field("harga_beli") hargaBeli: Int,
        @Field("harga_jual") hargaJual: Int,
        @Field("stok") stok: Int,
        @Field("foto") foto: String
    ): Call<LoginResponse>

    @GET("get_dashboard_stats.php")
    fun getDashboardStats(@Query("filter") filter: String): Call<DashboardResponse>

    @GET("get_riwayat_restock.php")
    fun getRiwayatRestock(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Call<RiwayatResponse>

    @FormUrlEncoded
    @POST("simpan_restock.php")
    fun simpanRestock(
        @Field("tanggal_masuk") tanggal: String,
        @Field("nama_toko") supplier: String,
        @Field("keterangan") keterangan: String,
        @Field("item_id") itemId: Int,
        @Field("jumlah") jumlah: Int,
        @Field("harga_beli_saat_itu") hargaBeli: Int
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("hapus_riwayat_restock.php")
    fun hapusRiwayatRestock(
        @Field("restock_id") restockId: Int
    ): Call<LoginResponse>

    @GET("get_user.php")
    fun getUsers(): Call<UserResponse>

    @FormUrlEncoded
    @POST("tambah_user.php")
    fun tambahUser(
        @Field("nama") nama: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("role") role: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("delete_user.php")
    fun deleteUser(
        @Field("user_id") userId: Int
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("simpan_transaksi.php")
    fun simpanTransaksi(
        @Field("user_id") userId: Int,
        @Field("total_harga") total: Int,
        @Field("pembayaran") bayar: Int,
        @Field("kembalian") kembali: Int,
        @Field("items") itemsJson: String
    ): Call<LoginResponse>

    @GET("get_dashboard_kasir.php")
    fun getDashboardKasir(): Call<DashboardKasirResponse>

    @GET("get_laporan_transaksi.php")
    fun getLaporanTransaksi(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Call<LaporanResponse>

    @GET("get_detail_transaksi.php")
    fun getDetailTransaksi(
        @Query("transaksi_id") id: Int
    ): Call<DetailTransaksiResponse>
}
