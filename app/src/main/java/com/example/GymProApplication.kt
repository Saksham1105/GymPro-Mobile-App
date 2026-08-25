package com.example

import android.app.Application
import com.example.di.Graph

class GymProApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.provide(this)
    }
}
