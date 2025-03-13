package com.cyberarmor.myapplication

import android.os.Parcel
import android.os.Parcelable

data class MediaFile(val name: String, val path: String, val type: String) : Parcelable {
    override fun toString(): String {
        return name // Se mostrará únicamente el nombre en el ListView
    }

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(path)
        parcel.writeString(type)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<MediaFile> {
        override fun createFromParcel(parcel: Parcel): MediaFile = MediaFile(parcel)
        override fun newArray(size: Int): Array<MediaFile?> = arrayOfNulls(size)
    }
}
