package com.example.husnaoli

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHusnaOli(context: Context) :
    SQLiteOpenHelper(context, DB_Name, null, DB_Ver){
    override fun onCreate(db: SQLiteDatabase?) {
        val tuser = "CREATE TABLE user (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nama TEXT NOT NULL, "  +
                "username TEXT NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT NOT NULL)"

        val tkategori = "CREATE TABLE kategori (" +
                "kategori_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nama_kategori TEXT NOT NULL)"

        val titems = "CREATE TABLE items (" +
                "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nama_item TEXT NOT NULL, " +
                "kategori_id INTEGER NOT NULL, " +
                "harga_beli INTEGER DEFAULT 0, " +
                "harga_jual INTEGER DEFAULT 0, " +
                "stok INTEGER DEFAULT 0, " +
                "foto TEXT, " +
                "FOREIGN KEY(kategori_id) REFERENCES kategori(kategori_id) ON DELETE CASCADE)"

        val ttransaksi = "CREATE TABLE transaksi (" +
                "transaksi_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "tanggal_transaksi TEXT NOT NULL, " +
                "total_harga REAL NOT NULL, " +
                "pembayaran REAL NOT NULL, " +
                "kembalian REAL NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES user(user_id) ON DELETE CASCADE)"

        val tdetail_transaksi = "CREATE TABLE detail_transaksi (" +
                "detail_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "transaksi_id INTEGER NOT NULL, " +
                "item_id INTEGER NOT NULL, " +
                "jumlah INTEGER NOT NULL DEFAULT 0, " +
                "harga_jual_saat_itu REAL NOT NULL DEFAULT 0, " +
                "FOREIGN KEY(transaksi_id) REFERENCES transaksi(transaksi_id) ON DELETE CASCADE, " +
                "FOREIGN KEY(item_id) REFERENCES items(item_id) ON DELETE CASCADE)"

        val trestock_items = "CREATE TABLE restock_items (" +
                "restock_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tanggal_masuk TEXT NOT NULL, " +
                "nama_toko TEXT NOT NULL, " +
                "keterangan TEXT)"

        val tdetail_restock_items = "CREATE TABLE detail_restock_items (" +
                "detail_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "restock_id INTEGER NOT NULL, " +
                "item_id INTEGER NOT NULL, " +
                "jumlah INTEGER NOT NULL DEFAULT 0, " +
                "harga_beli_saat_itu REAL NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (restock_id) REFERENCES restock_items (restock_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (item_id) REFERENCES items (item_id) ON DELETE CASCADE)"

        val insuser = "INSERT INTO user(nama, username, password, role) VALUES" +
                "('Husna', 'husna', 'husna123', 'admin')"

        val inskategori = "INSERT INTO kategori(nama_kategori) VALUES" +
                "('Oli & Pelumas')," +
                "('Ban & Velg')," +
                "('Sistem Pengereman')," +
                "('Mesin & Transmisi')," +
                "('Kelistrikan')," +
                "('Aksesoris')"


        db?.execSQL(tuser)
        db?.execSQL(tkategori)
        db?.execSQL(titems)
        db?.execSQL(ttransaksi)
        db?.execSQL(tdetail_transaksi)
        db?.execSQL(trestock_items)
        db?.execSQL(tdetail_restock_items)
        db?.execSQL(insuser)
        db?.execSQL(inskategori)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Jika versi database naik, Anda bisa menghapus tabel lama dan membuatnya kembali (untuk tahap development)
        db?.execSQL("DROP TABLE IF EXISTS detail_restock_items")
        db?.execSQL("DROP TABLE IF EXISTS restock_items")
        db?.execSQL("DROP TABLE IF EXISTS detail_transaksi")
        db?.execSQL("DROP TABLE IF EXISTS transaksi")
        db?.execSQL("DROP TABLE IF EXISTS items")
        db?.execSQL("DROP TABLE IF EXISTS kategori")
        db?.execSQL("DROP TABLE IF EXISTS user")
        onCreate(db)
    }

    companion object{
        val DB_Name = "HusnaOli"
        val DB_Ver = 3
    }
}