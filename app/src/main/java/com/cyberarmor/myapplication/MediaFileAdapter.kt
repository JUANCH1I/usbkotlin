package com.cyberarmor.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class MediaFileAdapter(context: Context, mediaFiles: List<MediaFile>) :
    ArrayAdapter<MediaFile>(context, 0, mediaFiles) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val mediaFile = getItem(position)
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        val text1 = view.findViewById<TextView>(android.R.id.text1)
        val text2 = view.findViewById<TextView>(android.R.id.text2)
        text1.text = mediaFile?.name
        text2.text = mediaFile?.type
        return view
    }
}
