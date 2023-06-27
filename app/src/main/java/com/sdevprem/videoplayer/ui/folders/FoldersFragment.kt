package com.sdevprem.videoplayer.ui.folders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sdevprem.videoplayer.databinding.FragmentFoldersBinding
import com.sdevprem.videoplayer.utils.isStoragePermissionGranted
import com.sdevprem.videoplayer.utils.launchInLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class FoldersFragment : Fragment() {
    lateinit var binding: FragmentFoldersBinding
    private val viewModel: FoldersViewModel by viewModels()
    private val adapter = FolderListAdapter {
        findNavController()
            .navigate(
                FoldersFragmentDirections.actionFoldersToFolderVideos(it.id, it.name)
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentFoldersBinding
            .inflate(
                inflater,
                container,
                false
            ).also {
                binding = it
            }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        folderList.layoutManager = LinearLayoutManager(requireContext())
        folderList.adapter = adapter

        if (requireContext().isStoragePermissionGranted())
            launchInLifecycle {
                viewModel.folderList.collectLatest {
                    adapter.folderList = it
                }
            }
        return@with
    }
}