package com.arstyn.disign.modal

import com.google.firebase.Timestamp

class Participant(
    val eventId: String,
    val scannedTime: Timestamp?,
    val scannedDate: String,
    val name: String,
    val dpUrl: String,
    val phoneNumber: String
) {
    constructor(): this("",null, "","","","")
}