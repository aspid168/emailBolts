package ru.study.emailbolts

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PreLoader: AppCompatActivity() {
    companion object {
        private const val SHARED_PREFERENCES = "SHARED_PREFERENCES"
        private const val DATA_JSON = "DATA_JSON"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)
            .getString(DATA_JSON, null)
        if (data == null) {
            MainActivity.startActivity(this)
        } else {
            UserActivity.startActivity(this)
        }
        finish()
    }
}
