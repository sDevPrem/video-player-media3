package com.sdevprem.videoplayer.utils

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sdevprem.videoplayer.R

@BindingAdapter("videoUri")
fun ImageView.loadImageFromVideoUri(uri: Uri?) {
    if (uri == null)
        return
    Glide.with(this)
        .asBitmap()
        .load(uri)
        .apply(RequestOptions().placeholder(R.drawable.ic_videos))
        .into(this)
}