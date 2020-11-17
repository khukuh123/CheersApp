package com.cmyk.cheersapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Message(
    var text: String = "",
    var sender: String = "",
    var receiver: String = "",
    var timeStamp: String = ""
) : Parcelable {
    fun toMap(): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        result["text"] = text
        result["sender"] = sender
        result["receiver"] = receiver
        result["timeStamp"] = timeStamp
        return result
    }
}