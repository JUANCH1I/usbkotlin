package com.cyberarmor.myapplication

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 100
    private lateinit var listView: ListView
    private val mediaFiles = ArrayList<MediaFile>()
    private lateinit var adapter: MediaFileAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView = findViewById(R.id.listView)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
            loadMediaFiles()
        }
    }

    private fun loadMediaFiles() {
        val resolver: ContentResolver = contentResolver

        // Consultar videos
        val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA
        )
        val videoCursor: Cursor? = resolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            videoProjection, null, null, null
        )
        videoCursor?.use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex)
                val path = cursor.getString(pathIndex)
                mediaFiles.add(MediaFile(name, path, "video"))
            }
        }

        // Consultar imágenes
        val imageProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA
        )
        val imageCursor: Cursor? = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            imageProjection, null, null, null
        )
        imageCursor?.use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex)
                val path = cursor.getString(pathIndex)
                mediaFiles.add(MediaFile(name, path, "image"))
            }
        }

        adapter = MediaFileAdapter(this, mediaFiles)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selected = mediaFiles[position]
            if (selected.type == "video") {
                // Lanzamos la actividad de reproducción/transcodificación pasando la ruta del video
                val intent = Intent(this, VideoTranscodeActivity::class.java)
                intent.putExtra("video_path", selected.path)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Visualización de imágenes no implementada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadMediaFiles()
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
