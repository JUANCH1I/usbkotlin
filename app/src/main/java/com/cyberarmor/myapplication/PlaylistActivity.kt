package com.cyberarmor.myapplication

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.File

class PlaylistActivity : AppCompatActivity() {

    private lateinit var libVLC: LibVLC
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var videoLayout: VLCVideoLayout
    private lateinit var imageView: ImageView
    private var playlist: ArrayList<MediaFile> = arrayListOf()
    private var currentIndex = 0
    private val imageDisplayDuration: Long = 20000 // milisegundos
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)

        videoLayout = findViewById(R.id.video_layout)
        imageView = findViewById(R.id.image_view)

        // Recupera la lista de medios desde el Intent
        playlist = intent.getParcelableArrayListExtra("playlist") ?: arrayListOf()
        if (playlist.isEmpty()) {
            finish()
            return
        }

        // Inicializa libVLC con opciones (por ejemplo, rotación de 90°)
        val options = arrayListOf("--video-filter=transform", "--transform-type=90")
        libVLC = LibVLC(this, options)
        mediaPlayer = MediaPlayer(libVLC)
        mediaPlayer.attachViews(videoLayout, null, false, false)

        // Registrar listener de eventos para detectar el fin del video
        mediaPlayer.setEventListener { event ->
            if (event.type == MediaPlayer.Event.EndReached) {
                runOnUiThread { playNext() }
            }
        }

        currentIndex = 0
        playMediaAtIndex(currentIndex)
    }

    private fun playMediaAtIndex(index: Int) {
        if (index >= playlist.size) {
            currentIndex = 0
        }
        val mediaFile = playlist[currentIndex]
        Log.d("Playlist", "Reproduciendo: ${mediaFile.name} (${mediaFile.path})")

        if (mediaFile.type.lowercase() == "video") {
            // Reproducir video: muestra VLCVideoLayout y oculta ImageView
            videoLayout.visibility = View.VISIBLE
            imageView.visibility = View.GONE

            val fileUri = Uri.fromFile(File(mediaFile.path))
            val media = Media(libVLC, fileUri)
            mediaPlayer.media = media
            media.release()
            mediaPlayer.play()
        } else if (mediaFile.type.lowercase() == "image") {
            // Mostrar imagen: oculta VLCVideoLayout y muestra ImageView
            videoLayout.visibility = View.GONE
            imageView.visibility = View.VISIBLE

            // Cargar imagen con Glide
            Glide.with(this)
                .load(File(mediaFile.path))
                .into(imageView)

            // Detener cualquier reproducción de video
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            // Después de imageDisplayDuration milisegundos, pasar al siguiente medio
            handler.postDelayed({ playNext() }, imageDisplayDuration)
        }
    }

    private fun playNext() {
        // Si se estaba reproduciendo un video, detenerlo
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        currentIndex++
        if (currentIndex >= playlist.size) {
            currentIndex = 0
        }
        playMediaAtIndex(currentIndex)
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
        handler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        // Si el elemento actual es video y el reproductor no está en reproducción, reanudar
        if (!mediaPlayer.isPlaying && playlist[currentIndex].type.lowercase() == "video") {
            mediaPlayer.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.detachViews()
        libVLC.release()
    }
}
