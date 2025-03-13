package com.cyberarmor.myapplication

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.File

class VideoTranscodeActivity : AppCompatActivity() {

    private lateinit var libVLC: LibVLC
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var videoLayout: VLCVideoLayout
    private var inputPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_transcode)
        videoLayout = findViewById(R.id.video_layout)

        // Recibir la ruta del video enviado desde MainActivity
        inputPath = intent.getStringExtra("video_path")
        Log.d("VideoTranscode", "Received video path: $inputPath")
        if (inputPath == null) {
            Toast.makeText(this, "No se proporcionó una ruta de video", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Opciones para LibVLC, incluyendo la rotación
        val options = arrayListOf(
            "--video-filter=transform",
            "--transform-type=90" // 90, 180 o 270 según lo necesites
        )
        libVLC = LibVLC(this, options)
        mediaPlayer = MediaPlayer(libVLC)
        mediaPlayer.attachViews(videoLayout, null, false, false)

        // Convertir la ruta en una URI válida con esquema "file://"
        val fileUri = Uri.fromFile(File(inputPath))
        Log.d("VideoTranscode", "Using file URI: $fileUri")

        // Configurar el Media y reproducir
        val media = Media(libVLC, fileUri)
        mediaPlayer.media = media
        media.release() // Libera el objeto media ya que se transfiere al MediaPlayer

        mediaPlayer.play()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.pause()
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.detachViews()
        libVLC.release()
    }
}
