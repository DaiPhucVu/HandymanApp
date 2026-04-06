package com.example.handyman

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.util.Calendar

class JobRequestDoneFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_job_request_done, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val confirmMessage = view.findViewById<TextView>(R.id.tvSubtitle)
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        val currentDate = String.format("%02d/%02d/%04d", day, month, year)
        confirmMessage.text = "Booking confirmed on\n$currentDate"

        val backBttn = view.findViewById<TextView>(R.id.btnBack)
        backBttn.setOnClickListener{
            val action = JobRequestDoneFragmentDirections.actionJobRequestDoneFragmentToServiceCategoryFragment()
            findNavController().navigate(action)
        }
    }
}
