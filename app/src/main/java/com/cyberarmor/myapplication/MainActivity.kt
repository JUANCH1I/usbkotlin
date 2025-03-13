package com.cyberarmor.myapplication

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 100
    private lateinit var listView: ListView
    private lateinit var btnPlayPlaylist: Button
    // Lista con todos los archivos encontrados (videos e imágenes)
    private val mediaFiles = ArrayList<MediaFile>()
    // Lista filtrada (solo videos MP4)
    private var filteredMediaFiles = listOf<MediaFile>()
    private lateinit var adapter: ArrayAdapter<MediaFile>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView = findViewById(R.id.listView)
        btnPlayPlaylist = findViewById(R.id.btnPlayPlaylist)

        // Aseguramos que el ListView use selección múltiple
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
        } else {
            loadMediaFiles()
        }

        btnPlayPlaylist.setOnClickListener {
            // Recolectamos los archivos seleccionados (MediaFile)
            val selectedMediaFiles = ArrayList<MediaFile>()
            val checkedItems = listView.checkedItemPositions
            for (i in 0 until checkedItems.size()) {
                val position = checkedItems.keyAt(i)
                if (checkedItems.get(position)) {
                    selectedMediaFiles.add(filteredMediaFiles[position])
                }
            }
            if (selectedMediaFiles.isEmpty()) {
                Toast.makeText(this, "Seleccione al menos un video", Toast.LENGTH_SHORT).show()
            } else {
                // Inicia PlaylistActivity pasando la lista de MediaFile
                val intent = Intent(this, PlaylistActivity::class.java)
                Log.d("SelectedPaths", "Total selected paths: ${selectedMediaFiles.map { it.path }}")
                intent.putParcelableArrayListExtra("playlist", selectedMediaFiles)
                startActivity(intent)
            }
        }

    }

    private fun loadMediaFiles() {
        val resolver: ContentResolver = contentResolver

        // Consulta de videos
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
                // Filtrar solo archivos MP4
                if (path.lowercase().endsWith(".mp4")) {
                    val mediaFile = MediaFile(name, path, "video")
                    mediaFiles.add(mediaFile)
                    Log.d("MediaFile", "Video - Name: $name, Path: $path")
                }
            }
        }

        // Consulta de imágenes (opcional)
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
                // Filtrar imágenes comunes
                if (path.lowercase().endsWith(".jpg") ||
                    path.lowercase().endsWith(".jpeg") ||
                    path.lowercase().endsWith(".png")
                ) {
                    val mediaFile = MediaFile(name, path, "image")
                    mediaFiles.add(mediaFile)
                    Log.d("MediaFile", "Image - Name: $name, Path: $path")
                }
            }
        }

        // Filtrar solo videos MP4
        filteredMediaFiles = mediaFiles.filter { it.type == "video" && it.path.lowercase().endsWith(".mp4") || it.type == "image" }
        Log.d("FilteredFiles", "Total MP4 videos found: ${filteredMediaFiles.size}")
        filteredMediaFiles.forEach {
            Log.d("FilteredFiles", "Archivo -> Name: ${it.name}, Path: ${it.path}")
        }

        // Usamos simple_list_item_multiple_choice para mostrar checkboxes
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, filteredMediaFiles)
        listView.adapter = adapter
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
