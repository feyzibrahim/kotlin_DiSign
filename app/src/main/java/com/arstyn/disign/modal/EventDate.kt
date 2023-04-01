package com.arstyn.disign.modal

import com.google.firebase.Timestamp

class EventDate(
    val eventDate: String,
    val eventId: String,
    val timestamp: Timestamp?
) {
    constructor(): this("", "", null)
}