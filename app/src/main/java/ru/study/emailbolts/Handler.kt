package ru.study.emailbolts

interface Handler {
    fun onSuccess(userInfo: String)
    fun onError()
}

