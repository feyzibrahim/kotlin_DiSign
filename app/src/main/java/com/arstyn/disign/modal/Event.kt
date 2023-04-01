package com.arstyn.disign.modal

class Event(
    val eventId: String,
    val eventLimit: String,
    val eventName: String,
    val eventOrganizer: String,
    val eventPlace: String,
    val eventStatus: String,
    val eventDate: String,
    val eventTime: String,
    val createdBy: String
) {
    constructor(): this(
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "")
}