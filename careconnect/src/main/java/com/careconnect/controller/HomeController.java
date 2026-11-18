
package com.careconnect.controller;

import com.careconnect.repository.AppointmentRepository;
import com.careconnect.repository.DoctorRepository;
import com.careconnect.repository.UserRepository;
import com.careconnect.entity.AppointmentStatus;
import com.careconnect.entity.Role;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class HomeController {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    public HomeController(
            AppointmentRepository appointmentRepository,
            DoctorRepository doctorRepository,
            UserRepository userRepository) {

        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String dashboard(Model model) {

        LocalDate today = LocalDate.now();
        // Today's appointment details
        model.addAttribute(
                "appointments",
                appointmentRepository.findByAppointmentDateOrderByAppointmentTimeAsc(today));

        // Today's appointments
        model.addAttribute(
                "todayAppointments",
                appointmentRepository.countByAppointmentDate(today));

        // Total doctors
        model.addAttribute(
                "totalDoctors",
                doctorRepository.count());

        // Registered patients
        model.addAttribute(
                "totalPatients",
                userRepository.countByRole(Role.PATIENT));

        // Pending appointments (BOOKED status)
        model.addAttribute(
                "pendingAppointments",
                appointmentRepository.countByStatus(AppointmentStatus.BOOKED));

        return "Home";
    }
}
