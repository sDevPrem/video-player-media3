package com.sdevprem.videoplayer.ui.folders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.sdevprem.videoplayer.data.model.Folder
import com.sdevprem.videoplayer.databinding.FolderItemBinding

class FolderListAdapter(
    folderList: List<Folder> = emptyList(),
    val onItemClick: (Folder) -> Unit
) : Adapter<FolderListAdapter.FolderViewHolder>() {
    var folderList = folderList
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class FolderViewHolder(
        val binding: FolderItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { onItemClick(folderList[absoluteAdapterPosition]) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        return FolderViewHolder(
            FolderItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = folderList.size

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) = with(holder.binding) {
        folderName.text = folderList[position].name
    }

}