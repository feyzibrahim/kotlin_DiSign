package com.arstyn.disign.modal

import com.google.firebase.Timestamp

class HistoryEvent(
    val id: String,
    val uid: String,
    val name: String,
    val organizer: String,
    val timestamp: Timestamp?,
    val place: String
) {
    constructor(): this("","","","",null,"")
}