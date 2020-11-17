package com.cmyk.cheersapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Room(
    var teller: String = "",
    var listener: String = "",
    var neededRole: String = "",
    var roomFull: Boolean = false,
    var roomId: Int = 0
) : Parcelable