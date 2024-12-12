package com.example.notesmart.data.dao

import java.sql.Connection
import java.sql.DriverManager

object MySqlConnection {
    fun getConnection(): Connection {
    println("conectando")
        return DriverManager.getConnection(
            "jdbc:mysql://192.168.0.178:3306/school",
            "clara",
            "LbCDy5QMpNek]-[5"
        )

    }
}