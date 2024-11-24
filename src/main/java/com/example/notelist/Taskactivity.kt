package com.example.notelist

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Notification
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class Taskactivity : AppCompatActivity() {

    private val filename = "tasks.txt"//name of the file used to store tasks
    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private val taskList = mutableListOf<String>()
    private val requestCodeEditTask = 1
    private var selectedTaskPosition: Int? = null
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1

    // Stopwatch Variables
    private var stopwatchRunning = false
    private var stopwatchStartTime = 0L
    private var stopwatchTimeBuffer = 0L
    private var stopwatchUpdateTime = 0L
    private lateinit var stopwatchHandler: Handler
    private lateinit var stopwatchTextView: TextView
    private lateinit var startStopButton: Button
    private lateinit var resetButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_taskactivity)

        com.example.notelist.Notification.createNotificationChannel(this)

        // Check for notification permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE)
        }





        val taskListView: ListView = findViewById(R.id.taskListView)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, taskList)
        taskListView.adapter = adapter
        loadTasks()

        taskListView.setOnItemClickListener { _, _, position, _ ->
            selectedTaskPosition = position
            val task = taskList[position]
            val intent = Intent(this, EditTaskActivity::class.java)
            intent.putExtra("TASK_DATA", task)
            startActivityForResult(intent, requestCodeEditTask)
        }

        taskListView.setOnItemLongClickListener { _, _, position, _ ->
            deleteTask(position)
            true
        }

        val addTaskButton: ExtendedFloatingActionButton = findViewById(R.id.newtask)
        addTaskButton.setOnClickListener {
            addTask()
            updateTaskList()
        }

        val selectDateButton: Button = findViewById(R.id.selectDateButton)
        val selectTimeButton: Button = findViewById(R.id.selectTimeButton)
        val taskDateTextView: TextView = findViewById(R.id.taskDate)
        val taskTimeTextView: TextView = findViewById(R.id.taskTime)

        selectDateButton.setOnClickListener {
            showDatePickerDialog { date ->
                selectedDate = date
                taskDateTextView.text = "Date: $selectedDate"
            }
        }

        selectTimeButton.setOnClickListener {
            showTimePickerDialog { time ->
                selectedTime = time
                taskTimeTextView.text = "Time: $selectedTime"
            }
        }

        // Stopwatch setup
        stopwatchTextView = findViewById(R.id.stopwatchDisplay)
        startStopButton = findViewById(R.id.startStopwatchButton)
        resetButton = findViewById(R.id.stopStopwatchButton)

        stopwatchHandler = Handler()
        startStopButton.setOnClickListener { startStopwatch() }
        resetButton.setOnClickListener { resetStopwatch() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCodeEditTask && resultCode == RESULT_OK) {
            val updatedTask = data?.getStringExtra("UPDATED_TASK")
            if (updatedTask != null && selectedTaskPosition != null) {
                taskList[selectedTaskPosition!!] = updatedTask
                saveTasks()
                updateTaskList()
            }
        }
    }

    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            onDateSelected(date)
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun showTimePickerDialog(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val time = "$selectedHour:$selectedMinute"
            onTimeSelected(time)
        }, hour, minute, true)
        timePickerDialog.show()
    }

    private fun addTask() {
        val taskNameEditText: EditText = findViewById(R.id.editTaskName)
        val taskDescEditText: EditText = findViewById(R.id.editTaskDesc)
        val taskName = taskNameEditText.text.toString()
        val taskDesc = taskDescEditText.text.toString()
        val currentStopwatchTime = stopwatchTextView.text.toString() // Capture stopwatch time

        if (taskName.isNotBlank() && taskDesc.isNotBlank() && selectedDate.isNotBlank() && selectedTime.isNotBlank()) {
            val task = "$taskName: $taskDesc, Date: $selectedDate, Time: $selectedTime, Stopwatch: $currentStopwatchTime"
            taskList.add(task)
            saveTasks()
            taskNameEditText.text.clear()
            taskDescEditText.text.clear()
            selectedDate = ""
            selectedTime = ""
            updateTaskList()

            // Create a TaskD object with all task details, including the stopwatch time
            val taskData = TaskD(
                title = taskName,
                description = taskDesc,
                date = selectedDate,
                time = selectedTime,
                stopwatchTime = currentStopwatchTime // Pass stopwatch time
            )

            // Show a notification when a task is added
            com.example.notelist.Notification.sendNotification(this, taskData)

        } else {
            Toast.makeText(this, "Please enter all task details", Toast.LENGTH_SHORT).show()
        }
    }



    private fun deleteTask(position: Int) {
        taskList.removeAt(position)
        saveTasks()
        updateTaskList()
    }

    private fun saveTasks() {
        try {
            openFileOutput(filename, Context.MODE_PRIVATE).use { outputStream ->
                taskList.forEach { task ->
                    outputStream.write((task + "\n").toByteArray())
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadTasks() {
        val file = File(filesDir, filename)
        if (file.exists()) {
            try {
                FileInputStream(file).use { inputStream ->
                    inputStream.bufferedReader().use { reader ->
                        val lines = reader.readLines()
                        taskList.clear()
                        taskList.addAll(lines)
                        updateTaskList()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateTaskList() {
        val taskListView: ListView = findViewById(R.id.taskListView)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, taskList.map { task ->
            // Add stopwatch time to the displayed string
            task
        })
        taskListView.adapter = adapter
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Stopwatch Functions

    private val updateStopwatchRunnable = object : Runnable {
        override fun run() {
            val currentTime = SystemClock.uptimeMillis() - stopwatchStartTime
            stopwatchUpdateTime = stopwatchTimeBuffer + currentTime
            val secs = (stopwatchUpdateTime / 1000).toInt()
            val mins = secs / 60
            val hours = mins / 60
            stopwatchTextView.text = String.format("%02d:%02d:%02d", hours, mins % 60, secs % 60)
            stopwatchHandler.postDelayed(this, 1000)
        }
    }

    private fun startStopwatch() {
        if (!stopwatchRunning) {
            stopwatchStartTime = SystemClock.uptimeMillis()
            stopwatchHandler.postDelayed(updateStopwatchRunnable, 0)
            startStopButton.text = "Pause"
            stopwatchRunning = true
        } else {
            stopwatchTimeBuffer += SystemClock.uptimeMillis() - stopwatchStartTime
            stopwatchHandler.removeCallbacks(updateStopwatchRunnable)
            startStopButton.text = "Start"
            stopwatchRunning = false
        }
    }

    private fun resetStopwatch() {
        stopwatchStartTime = 0L
        stopwatchTimeBuffer = 0L
        stopwatchUpdateTime = 0L
        stopwatchTextView.text = "00:00:00"
        stopwatchHandler.removeCallbacks(updateStopwatchRunnable)
        startStopButton.text = "Start"
        stopwatchRunning = false
    }


}
