package com.sdevprem.videoplayer.ui.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sdevprem.videoplayer.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FoldersViewModel @Inject constructor(
    videoRepository: VideoRepository
) : ViewModel() {
    val folderList = videoRepository.getAllVideoFolders()
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )
}