package com.example.husnaoli

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.husnaoli.databinding.FragmentUserBinding
import com.example.husnaoli.databinding.ItemUserBinding

class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBHusnaOli
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
        dbHelper = DBHusnaOli(requireContext())

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
            deleteUser(user)
        }
        binding.rvUser.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUser.adapter = adapter
    }

    private fun loadData() {
        val list = mutableListOf<User>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT user_id, nama, username, role FROM user", null)

        if (cursor.moveToFirst()) {
            do {
                list.add(User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        adapter.updateData(list)
    }

    private fun deleteUser(user: User) {
        val db = dbHelper.writableDatabase
        val result = db.delete("user", "user_id=?", arrayOf(user.id.toString()))
        if (result > 0) {
            Toast.makeText(requireContext(), "User ${user.nama} berhasil dihapus", Toast.LENGTH_SHORT).show()
            loadData()
        }
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

                if (u.role.lowercase() == "admin") {
                    tvRole.setBackgroundColor(ContextCompat.getColor(root.context, R.color.role_admin_bg))
                    tvRole.setTextColor(ContextCompat.getColor(root.context, R.color.role_admin_text))
                } else {
                    tvRole.setBackgroundColor(ContextCompat.getColor(root.context, R.color.role_kasir_bg))
                    tvRole.setTextColor(ContextCompat.getColor(root.context, R.color.role_kasir_text))
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
