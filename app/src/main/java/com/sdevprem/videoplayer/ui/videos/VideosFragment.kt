package com.sdevprem.videoplayer.ui.videos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sdevprem.videoplayer.MainNavDirections
import com.sdevprem.videoplayer.databinding.FragmentVideosBinding
import com.sdevprem.videoplayer.utils.isStoragePermissionGranted
import com.sdevprem.videoplayer.utils.launchInLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class VideosFragment : Fragment() {
    lateinit var binding: FragmentVideosBinding
    private val viewModel: VideosViewModel by viewModels()
    private val adapter = VideoListAdapter {
        viewModel.setCurrentVideoListToActive()
        findNavController().navigate(
            MainNavDirections.actionGlobalNavigationToPlayer(absoluteAdapterPosition)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentVideosBinding.inflate(
            inflater,
            container,
            false
        ).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        videoList.layoutManager = LinearLayoutManager(requireContext())
        videoList.adapter = adapter

        if (requireContext().isStoragePermissionGranted()) launchInLifecycle {
            viewModel.videoList.collectLatest {
                adapter.videoList = it
            }
        }

        return@with
    }
}