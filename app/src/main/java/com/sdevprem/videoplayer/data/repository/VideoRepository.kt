package com.sdevprem.videoplayer.data.repository

import android.content.Context
import android.provider.MediaStore
import androidx.core.net.toUri
import com.sdevprem.videoplayer.data.model.Folder
import com.sdevprem.videoplayer.data.model.Video
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepository @Inject constructor(
    @ApplicationContext
    private val appContext: Context
) {

    var activeVideoList = emptyList<Video>()

    fun getVideos(folderId: String?) = flow<List<Video>> {
        val videoList = ArrayList<Video>()

        val selection = folderId?.let { MediaStore.Video.Media.BUCKET_ID + " = ?" }
        val selectionArgs = folderId?.let { arrayOf(folderId) }
        val projection = arrayOf(
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.BUCKET_ID
        )

        val cursor = appContext.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            MediaStore.Video.Media.DATE_ADDED + " DESC"
        )
        if (cursor != null && cursor.moveToNext()) {
            val titleIndex = cursor.getColumnIndex(MediaStore.Video.Media.TITLE)
            val idIndex = cursor.getColumnIndex(MediaStore.Video.Media._ID)
            val folderIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
            val pathIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
            val durationIndex = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)

            do try {
                val file = File(cursor.getString(pathIndex))
                if (file.exists())
                    videoList += Video(
                        title = cursor.getString(titleIndex),
                        id = cursor.getString(idIndex),
                        duration = cursor.getString(durationIndex).toLong(),
                        folderName = cursor.getString(folderIndex),
                        size = cursor.getString(sizeIndex),
                        path = cursor.getString(pathIndex),
                        videoUri = file.toUri()
                    )
            } catch (_: Exception) {
                continue
            } while (cursor.moveToNext())

            cursor.close()
            emit(videoList)
        }
    }.flowOn(Dispatchers.IO)

    fun getAllVideoFolders() = flow<List<Folder>> {
        val folderList = ArrayList<Folder>()
        val folderSet = HashSet<String>()

        val projection = arrayOf(
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
        )
        val cursor = appContext.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )
        if (cursor != null && cursor.moveToNext()) {
            val folderNameIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            val folderIdIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID)

            do {
                val folderId = cursor.getString(folderIdIndex)
                if (folderSet.add(folderId)) {
                    folderList += Folder(
                        id = folderId,
                        name = cursor.getString(folderNameIndex)
                    )
                }
            } while (cursor.moveToNext())

            cursor.close()
            emit(folderList)
        }
    }
}