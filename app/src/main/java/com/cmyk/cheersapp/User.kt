package com.cmyk.cheersapp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(
    var id: String = "",
    var userName: String = "",
    var email: String = "",
    var password: String = ""
) : Parcelable {
}