package com.example.notesmart.data.model

import java.io.Serializable

data class StudentModel(
    val id: Int,
    val name: String,
    val surname: String,
    val email: String,
    val password: String,
    val phone: String,
    var grade: Int,
    val enrollmentDate: String,
    var subjectName: String = "",
) : Serializable
