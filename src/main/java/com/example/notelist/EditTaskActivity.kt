package com.example.notelist

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class EditTaskActivity : AppCompatActivity() {

    private lateinit var editTaskName: EditText
    private lateinit var editTaskDesc: EditText
    private lateinit var editTaskDate: TextView
    private lateinit var editTaskTime: TextView
    private lateinit var selectDateButton: Button
    private lateinit var selectTimeButton: Button

    private var selectedDate: String = ""
    private var selectedTime: String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        editTaskName = findViewById(R.id.editTaskName)
        editTaskDesc = findViewById(R.id.editTaskDesc)
        editTaskDate = findViewById(R.id.editTaskDate)
        editTaskTime = findViewById(R.id.editTaskTime)
        selectDateButton = findViewById(R.id.selectDateButton)
        selectTimeButton = findViewById(R.id.selectTimeButton)
        val saveButton: Button = findViewById(R.id.saveButton)

        val taskData = intent.getStringExtra("TASK_DATA") ?: ""
        val taskParts = taskData.split(", ")
        if (taskParts.size >= 4) {
            val nameAndDesc = taskParts[0].split(": ")
            if (nameAndDesc.size == 2) {
                editTaskName.setText(nameAndDesc[0])
                editTaskDesc.setText(nameAndDesc[1])
            }
            selectedDate = taskParts[1].substringAfter("Date: ")
            selectedTime = taskParts[2].substringAfter("Time: ")

            editTaskDate.text = "Date: $selectedDate"
            editTaskTime.text = "Time: $selectedTime"
        }

        selectDateButton.setOnClickListener {
            showDatePickerDialog { date ->
                selectedDate = date
                editTaskDate.text = "Date: $selectedDate"
            }
        }

        selectTimeButton.setOnClickListener {
            showTimePickerDialog { time ->
                selectedTime = time
                editTaskTime.text = "Time: $selectedTime"
            }
        }

        saveButton.setOnClickListener {
            val updatedName = editTaskName.text.toString()
            val updatedDesc = editTaskDesc.text.toString()
            val updatedTask = "$updatedName: $updatedDesc, Date: $selectedDate, Time: $selectedTime"

            val returnIntent = Intent()
            returnIntent.putExtra("UPDATED_TASK", updatedTask)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
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
}
