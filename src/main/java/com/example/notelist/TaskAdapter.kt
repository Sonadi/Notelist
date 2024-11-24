package com.example.notelist

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView

class TaskAdapter(context: Context, tasks: MutableList<String>) : ArrayAdapter<String>(context, 0, tasks) {

    private val stopwatchMap = mutableMapOf<Int, Stopwatch>()
    private val handler = Handler(Looper.getMainLooper())

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.activity_taskactivity, parent, false)

        val taskNameTextView: TextView = view.findViewById(R.id.taskItemName)
        val stopwatchDisplay: TextView = view.findViewById(R.id.stopwatchDisplay)
        val startButton: Button = view.findViewById(R.id.startStopwatchButton)
        val stopButton: Button = view.findViewById(R.id.stopStopwatchButton)

        val task = getItem(position)
        taskNameTextView.text = task

        val stopwatch = stopwatchMap[position] ?: Stopwatch().also { stopwatchMap[position] = it }

        startButton.setOnClickListener {
            stopwatch.start()
            updateStopwatchDisplay(stopwatch, stopwatchDisplay)
        }

        stopButton.setOnClickListener {
            stopwatch.stop()
            updateStopwatchDisplay(stopwatch, stopwatchDisplay)
        }

        handler.postDelayed(object : Runnable {
            override fun run() {
                updateStopwatchDisplay(stopwatch, stopwatchDisplay)
                handler.postDelayed(this, 1000) // Update every second
            }
        }, 1000)

        return view
    }

    private fun updateStopwatchDisplay(stopwatch: Stopwatch, display: TextView) {
        val elapsedTime = stopwatch.getElapsedTime() / 1000
        val hours = elapsedTime / 3600
        val minutes = (elapsedTime % 3600) / 60
        val seconds = elapsedTime % 60
        display.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
