package ru.study.emailbolts

data class UserInfo(
    var email: String,
    val firstName: String,
    val lastName: String,
    val birthDate: String,
    val notes: String
)
