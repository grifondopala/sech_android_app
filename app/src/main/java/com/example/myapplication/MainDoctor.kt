package com.example.myapplication

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import androidx.core.view.setPadding
import com.example.myapplication.interfaces.PatientDto


class MainDoctor : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View =  inflater.inflate(R.layout.fragment_main_doctor, container, false)

        val patients: Array<PatientDto> = getPatients();

        drawPatients(patients, view)

        return view
    }


    private fun getPatients(): Array<PatientDto>{
        return arrayOf(
            PatientDto(1, "Иван", "Иванович", "Иванов"),
            PatientDto(2, "Александр", "Александрович", "Александров"),
            PatientDto(3, "Кирилл", "Кириллович", "Кириллов"),
            PatientDto(1, "Иван", "Иванович", "Иванов"),
            PatientDto(2, "Александр", "Александрович", "Александров"),
            PatientDto(3, "Кирилл", "Кириллович", "Кириллов"),
            PatientDto(1, "Иван", "Иванович", "Иванов"),
            PatientDto(2, "Александр", "Александрович", "Александров"),
            PatientDto(3, "Кирилл", "Кириллович", "Кириллов"),
        )
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun drawPatients(patients: Array<PatientDto>, view: View){
        val patientsLayout: LinearLayout = view.findViewById(R.id.patients_view)
        for(patient in patients){
            val patientContainer: LinearLayout = LinearLayout(this.context).apply {
                setPadding(20)
                setBackgroundColor(ContextCompat.getColor(this.context, R.color.grey))
            }
            val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )

            patientContainer.setBackgroundDrawable(resources.getDrawable(R.drawable.patient_border))

            layoutParams.setMargins(0, 50, 0, 0)

            layoutParams.height = 200

            val patientFIO: TextView = TextView(this.context).apply {
                text = "${patient.name} ${patient.middleName} ${patient.surname}"
                setTextColor(ContextCompat.getColor(this.context, R.color.black))
            }

            patientContainer.addView(patientFIO)
            patientsLayout.addView(patientContainer, layoutParams)
        }
    }

}