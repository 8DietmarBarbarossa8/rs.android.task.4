package com.bignerdranch.android.studentstorage.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bignerdranch.android.studentstorage.Callbacks
import com.bignerdranch.android.studentstorage.R
import com.bignerdranch.android.studentstorage.model.Student
import com.bignerdranch.android.studentstorage.viewmodel.StudentDetailViewModel

class StudentFragment : Fragment() {
    private lateinit var nameStudent: EditText
    private lateinit var ageStudent: EditText
    private lateinit var ratingStudent: EditText
    private lateinit var functionalButton: Button
    private val studentDetailViewModel: StudentDetailViewModel by lazy {
        ViewModelProviders.of(this).get(StudentDetailViewModel::class.java)
    }
    private var student: Student = Student()
    private var callbacks: Callbacks? = null
    private var mayEditStudentData = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as? Callbacks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this){
            if (mayEditStudentData)
                correctStudentData()
            snapBackToReality()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student, container, false)

        nameStudent = view.findViewById(R.id.name_student) as EditText
        ageStudent = view.findViewById(R.id.age_student) as EditText
        ratingStudent = view.findViewById(R.id.rating_student) as EditText
        functionalButton = view.findViewById(R.id.add_button) as Button

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val studentId = arguments?.getSerializable(ARG_STUDENT_ID) as Int?

        if (studentId == null) addStudent()
        else {
            mayEditStudentData = true
            functionalButton.text = resources.getString(R.string.delete)
            functionalButton.background = ResourcesCompat
                .getDrawable(resources, R.drawable.deleting_button_border, null)

            studentDetailViewModel.loadStudent(studentId)
            studentDetailViewModel.studentLiveData.observe(
                viewLifecycleOwner,
                { student ->
                    student?.let {
                        this.student = student
                        updateUI()
                    }
                }
            )

            functionalButton.setOnClickListener {
                studentDetailViewModel.deleteStudent(student)
                Toast.makeText(context, R.string.deleting_notification, Toast.LENGTH_SHORT).show()
                snapBackToReality()
            }
        }
    }

    private fun updateUI() {
        nameStudent.setText(student.name)
        ageStudent.setText(student.age.toString())
        ratingStudent.setText(student.rating.toString())
    }

    private fun addStudent(){
        functionalButton.setOnClickListener {
            val name = nameStudent.text.toString()
            val age = ageStudent.text.toString().toIntOrNull()
            val rating = ratingStudent.text.toString().toFloatOrNull()
            if (name.isNotEmpty() && age != null && rating != null){
                student = Student(name = name, age = age, rating = rating)
                studentDetailViewModel.addStudent(student)
                Toast.makeText(context, R.string.adding_notification, Toast.LENGTH_SHORT).show()
                snapBackToReality()
            } else Toast.makeText(context,
                R.string.input_error_notification, Toast.LENGTH_SHORT).show()
        }
    }

    private fun correctStudentData(){
        student.name = nameStudent.text.toString()
        student.age = try {
            ageStudent.text.toString().toInt()
        } catch (e: Exception) {student.age}
        student.rating = try {
            ratingStudent.text.toString().toFloat()
        } catch (e: Exception) {student.rating}
    }

    private fun snapBackToReality(){
        callbacks?.onMainScreen("name")
    }

    override fun onStop() {
        super.onStop()
        studentDetailViewModel.saveStudent(student)
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    companion object {
        private const val ARG_STUDENT_ID = "student_id"

        @JvmStatic
        fun newInstance(studentId: Int): StudentFragment {
            val args = Bundle().apply { putInt(ARG_STUDENT_ID, studentId) }
            return StudentFragment().apply { arguments = args }
        }

        @JvmStatic
        fun newInstance() = StudentFragment()
    }
}