package com.cmyk.cheersapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(
    var id: String = "",
    var username: String = "",
    var email: String = "",
    var password: String = "",
    var status: String = "idle",
    var room: Room = Room(),
    var currentRole: String = ""
) : Parcelable