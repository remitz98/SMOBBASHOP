package org.smobba.store

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

object AppContextHolder {
    var context: Context? = null
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContextHolder.context = applicationContext
        setContent {
            App()
        }
    }
}
