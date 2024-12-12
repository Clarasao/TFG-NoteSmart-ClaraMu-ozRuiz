package com.example.notesmart.data.model

import java.security.Signature

data class TeacherModel(
    var id : Int = 0,
    var name : String = "",
    var surname : String = "",
    var email : String = "",
    var password : String = "",
    var signature: String = "",
)