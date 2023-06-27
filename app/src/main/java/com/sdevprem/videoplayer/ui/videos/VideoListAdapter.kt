package com.sdevprem.videoplayer.ui.videos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sdevprem.videoplayer.data.model.Video
import com.sdevprem.videoplayer.databinding.VideoItemBinding

class VideoListAdapter(
    videoList: List<Video> = emptyList(),
    private val onItemClickListener: ViewHolder.(Video) -> Unit
) : Adapter<VideoListAdapter.VideoViewHolder>() {

    var videoList: List<Video> = videoList
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class VideoViewHolder(
        val binding: VideoItemBinding
    ) : ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onItemClickListener(videoList[absoluteAdapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        return VideoViewHolder(
            VideoItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = videoList.size

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) = with(holder.binding) {
        video = videoList[position]
    }
}