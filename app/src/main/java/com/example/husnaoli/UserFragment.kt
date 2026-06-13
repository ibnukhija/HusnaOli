package com.example.husnaoli

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.husnaoli.databinding.FragmentUserBinding
import com.example.husnaoli.databinding.ItemUserBinding
import com.example.husnaoli.network.LoginResponse
import com.example.husnaoli.network.RetrofitClient
import com.example.husnaoli.network.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        
        binding.btnTambahUser.setOnClickListener {
            startActivity(Intent(requireContext(), TambahUserActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter(mutableListOf<User>()) { user ->
            showDeleteConfirmation(user)
        }
        binding.rvUser.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUser.adapter = adapter
    }

    private fun loadData() {
        RetrofitClient.instance.getUsers().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (_binding == null || !isAdded) return

                if (response.isSuccessful) {
                    val res = response.body()
                    if (res != null && res.status == "success") {
                        val list = res.data.map {
                            User(it.userId, it.nama, it.username, it.role)
                        }
                        adapter.updateData(list)
                    }
                } else {
                    Toast.makeText(requireContext(), "Error Server: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("API_ERROR", t.message ?: "Unknown Error")
                Toast.makeText(requireContext(), "Koneksi Gagal", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteConfirmation(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus User")
            .setMessage("Apakah Anda yakin ingin menghapus user ${user.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteUser(user)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteUser(user: User) {
        RetrofitClient.instance.deleteUser(user.id).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res != null && res.status == "success") {
                        Toast.makeText(requireContext(), res.message, Toast.LENGTH_SHORT).show()
                        loadData()
                    } else {
                        Toast.makeText(requireContext(), res?.message ?: "Gagal menghapus", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Koneksi Gagal", Toast.LENGTH_SHORT).show()
            }
        })
    }

    data class User(
        val id: Int,
        val nama: String,
        val username: String,
        val role: String
    )

    class UserAdapter(
        private var listUser: List<User>,
        private val onDeleteClick: (User) -> Unit
    ) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

        class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val b = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return UserViewHolder(b)
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            val u = listUser[position]
            holder.binding.apply {
                tvNo.text = (position + 1).toString()
                tvNama.text = u.nama
                tvUsername.text = u.username
                tvRole.text = u.role.uppercase()

                // Pastikan resource color role_admin_bg, dll sudah ada di colors.xml
                try {
                    if (u.role.lowercase() == "admin") {
                        tvRole.setBackgroundColor(ContextCompat.getColor(root.context, R.color.role_admin_bg))
                        tvRole.setTextColor(ContextCompat.getColor(root.context, R.color.role_admin_text))
                    } else {
                        tvRole.setBackgroundColor(ContextCompat.getColor(root.context, R.color.role_kasir_bg))
                        tvRole.setTextColor(ContextCompat.getColor(root.context, R.color.role_kasir_text))
                    }
                } catch (e: Exception) {
                    // Fallback jika color tidak ditemukan
                }

                btnDelete.setOnClickListener { onDeleteClick(u) }
            }
        }

        override fun getItemCount(): Int = listUser.size

        fun updateData(newList: List<User>) {
            listUser = newList
            notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
