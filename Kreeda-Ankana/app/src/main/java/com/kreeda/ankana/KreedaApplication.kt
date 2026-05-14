package com.kreeda.ankana

import android.app.Application

/**
 * Application class for Kreeda-Ankana.
 * Initialize Firebase and other global services here.
 */
class KreedaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // TODO: Initialize Firebase when google-services.json is added
        // FirebaseApp.initializeApp(this)
    }
}
