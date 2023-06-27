package com.sdevprem.videoplayer.ui.player

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView.ControllerVisibilityListener
import androidx.navigation.navArgs
import com.sdevprem.videoplayer.PlayerNavArgs
import com.sdevprem.videoplayer.R
import com.sdevprem.videoplayer.data.repository.VideoRepository
import com.sdevprem.videoplayer.databinding.ActivityPlayerBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityPlayerBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var videoRepository: VideoRepository
    private var player: ExoPlayer? = null
    private val args by navArgs<PlayerNavArgs>()
    private var currentVideoPos = -1
    private var playbackPos = 0L
    private var mediaItemIndex = 0
    private var playWhenReady = true
    private var shouldRepeat = false
    private var isFullscreen = false
    private var isLocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        setContentView(binding.root)
        if (currentVideoPos < 0)
            currentVideoPos = args.videoPos
        initBinding()
    }

    @OptIn(UnstableApi::class)
    private fun initBinding() = with(binding) {
        backBtn.setOnClickListener { finish() }
        playPauseBtn.setOnClickListener {
            if (player?.isPlaying == true)
                pauseVideo()
            else playVideo()
        }
        nextBtn.setOnClickListener { playNext() }
        previousBtn.setOnClickListener { playPrevious() }
        repeatBtn.setOnClickListener {
            player?.let {
                shouldRepeat = !shouldRepeat
                setRepeatMode(it)
            }
        }
        fullscreenBtn.setOnClickListener {
            player?.let {
                isFullscreen = !isFullscreen
                setFullscreenMode(it)
            }
        }
        playerView.setControllerVisibilityListener(ControllerVisibilityListener {
            setControllerVisibility(it)
        })
        lockBtn.setOnClickListener {
            isLocked = !isLocked
            setLockMode()
        }
        playerView.controllerShowTimeoutMs = 2000
    }

    private fun setControllerVisibility(visibility: Int) = with(binding) {
        topController.visibility = visibility
        bottomController.visibility = visibility
        playPauseBtn.visibility = visibility
        if (!isLocked)
            lockBtn.visibility = visibility
    }

    @OptIn(UnstableApi::class)
    private fun setLockMode() = if (isLocked) {
        binding.playerView.useController = false
        binding.playerView.hideController()
        binding.lockBtn.setImageResource(R.drawable.ic_lock_open)
    } else {
        binding.playerView.useController = true
        binding.playerView.showController()
        binding.lockBtn.setImageResource(R.drawable.ic_lock)
    }

    private fun playVideo() {
        binding.playPauseBtn.setImageResource(R.drawable.ic_pause)
        player?.play()
    }

    private fun pauseVideo() {
        binding.playPauseBtn.setImageResource(R.drawable.ic_play)
        player?.pause()
    }

    private fun playNext() {
        currentVideoPos = ++currentVideoPos % videoRepository.activeVideoList.size
        initializePlayer()
    }

    private fun playPrevious() {
        val videoListSize = videoRepository.activeVideoList.size
        currentVideoPos = (--currentVideoPos + videoListSize) % videoListSize
        initializePlayer()
    }

    private fun setRepeatMode(player: ExoPlayer) = with(binding) {
        if (shouldRepeat) {
            player.repeatMode = Player.REPEAT_MODE_ONE
            repeatBtn.setImageResource(androidx.media3.ui.R.drawable.exo_legacy_controls_repeat_one)
        } else {
            player.repeatMode = Player.REPEAT_MODE_OFF
            repeatBtn.setImageResource(androidx.media3.ui.R.drawable.exo_legacy_controls_repeat_off)
        }
    }

    @OptIn(UnstableApi::class)
    private fun setFullscreenMode(player: ExoPlayer) = with(binding) {
        if (isFullscreen) {
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            fullscreenBtn.setImageResource(androidx.media3.ui.R.drawable.exo_ic_fullscreen_exit)
        } else {
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            fullscreenBtn.setImageResource(androidx.media3.ui.R.drawable.exo_ic_fullscreen_enter)
        }
    }

    private fun initializePlayer() {
        player?.release()
        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                binding.playerView.player = exoPlayer
                val videoUri = videoRepository.activeVideoList[currentVideoPos].videoUri
                val mediaItem = MediaItem.fromUri(videoUri)
                exoPlayer.setMediaItem(mediaItem, playbackPos)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.prepare()
                binding.videoTitle.text = videoUri.toFile().name
                binding.videoTitle.isSelected = true
                setRepeatMode(exoPlayer)
                setFullscreenMode(exoPlayer)
                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED)
                            playNext()
                    }
                })
            }
        if (playWhenReady)
            playVideo()
        else pauseVideo()

    }

    private fun releasePlayer() = player?.let {
        playbackPos = it.currentPosition
        mediaItemIndex = it.currentMediaItemIndex
        playWhenReady = it.playWhenReady
        it.release()
        player = null
    }

    public override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            initializePlayer()
        }
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M || player == null) {
            initializePlayer()
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            releasePlayer()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            releasePlayer()
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}