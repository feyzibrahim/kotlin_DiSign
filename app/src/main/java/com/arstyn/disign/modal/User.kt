package com.arstyn.disign.modal

class User (val name: String, val phone: String, val email: String, val dpUri: String, val uid: String) {
    constructor(): this("", "", "", "", "")
}