package com.cyberarmor.myapplication

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode

class VideoTranscodeActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private var inputPath: String? = null
    // Puedes definir una ruta de salida (en este ejemplo, se usa el almacenamiento externo)
    private val outputPath = "/sdcard/output_video.mp4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_transcode)
        videoView = findViewById(R.id.videoView)

        inputPath = intent.getStringExtra("video_path")
        if (inputPath == null) {
            Toast.makeText(this, "No se proporcionó una ruta de video", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        transcodeVideo()
    }

    private fun transcodeVideo() {
        // Comando FFmpeg: convierte el video de entrada a MP4 usando libx264 y aac
        val command = "-i $inputPath -c:v libx264 -c:a aac $outputPath"
        FFmpegKit.executeAsync(command) { session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                runOnUiThread {
                    Toast.makeText(this, "Transcodificación completada", Toast.LENGTH_SHORT).show()
                    playVideo()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Error en la transcodificación", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun playVideo() {
        videoView.setVideoURI(Uri.parse(outputPath))
        videoView.start()
    }
}
