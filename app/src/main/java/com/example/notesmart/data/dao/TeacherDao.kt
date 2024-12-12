package com.example.notesmart.data.dao

import com.example.notesmart.data.model.TeacherModel

object TeacherDao {

    fun listar(dato: String): List<TeacherModel> {

        val lista = mutableListOf<TeacherModel>()

        val ps = MySqlConnection.getConnection().prepareStatement(
            "SELECT id, name, surname FROM teacher WHERE email LIKE concat('%', ?, '%');"
        )

        ps.setString(1, dato)

        val rs = ps.executeQuery()

        while (rs.next()) {
            lista.add(
                TeacherModel(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("surname")
                )
            )
        }
        rs.close()
        ps.close()

        return lista
    }

    fun validateTeacher(email: String, password: String): Boolean {
        val ps = MySqlConnection.getConnection().prepareStatement(
            "SELECT id FROM teacher WHERE email = ? AND password = ?;"
        )

        ps.setString(1, email)
        ps.setString(2, password)

        val rs = ps.executeQuery()

        return rs.next()
    }

}