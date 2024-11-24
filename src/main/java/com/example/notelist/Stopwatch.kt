package com.example.notelist

class Stopwatch {
    private var startTime: Long = 0
    private var isRunning: Boolean = false

    fun start() {
        startTime = System.currentTimeMillis()
        isRunning = true
    }

    fun stop(): Long {
        isRunning = false
        return getElapsedTime()
    }

    fun getElapsedTime(): Long {
        return if (isRunning) {
            System.currentTimeMillis() - startTime
        } else 0
    }

    fun reset() {
        startTime = 0
        isRunning = false
    }
}
