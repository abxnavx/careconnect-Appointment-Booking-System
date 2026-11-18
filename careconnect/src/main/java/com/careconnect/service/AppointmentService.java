package com.careconnect.service;

import com.careconnect.dto.AppointmentRequest;
import com.careconnect.dto.AppointmentResponse;
import com.careconnect.entity.Appointment;
import com.careconnect.entity.AppointmentStatus;
import com.careconnect.entity.Doctor;
import com.careconnect.entity.User;
import com.careconnect.repository.AppointmentRepository;
import com.careconnect.repository.DoctorRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppointmentService {

        private final AppointmentRepository appointmentRepository;

        private final DoctorRepository doctorRepository;

        public AppointmentService(
                        AppointmentRepository appointmentRepository,
                        DoctorRepository doctorRepository) {

                this.appointmentRepository = appointmentRepository;

                this.doctorRepository = doctorRepository;
        }

        public List<Appointment> getAllAppointments() {

                return appointmentRepository.findAll();
        }

        public List<Appointment> getAppointmentsByDate(
                        LocalDate date) {

                return appointmentRepository
                                .findByAppointmentDate(date);
        }

        public Appointment getAppointmentById(Long id) {
                return appointmentRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Appointment not found with ID: " + id));
        }

        public List<Appointment> getAppointmentsByPatient(User patient) {

                return appointmentRepository
                                .findByPatientOrderByAppointmentDateAscAppointmentTimeAsc(patient);
        }

        @Transactional
        public AppointmentResponse createAppointment(
                        AppointmentRequest request,
                        User patient) {

                Doctor doctor = doctorRepository
                                .findByIdAndIsActiveTrue(
                                                request.getDoctorId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Doctor not found."));

                boolean alreadyBooked = appointmentRepository
                                .existsByDoctorAndAppointmentDateAndAppointmentTimeAndStatus(
                                                doctor,
                                                request.getAppointmentDate(),
                                                request.getAppointmentTime(),
                                                AppointmentStatus.BOOKED);

                if (alreadyBooked) {

                        throw new IllegalArgumentException(
                                        "The selected time slot is not available.");
                }

                Appointment appointment = new Appointment();

                appointment.setPatient(patient);

                appointment.setDoctor(doctor);

                appointment.setAppointmentDate(
                                request.getAppointmentDate());

                appointment.setAppointmentTime(
                                request.getAppointmentTime());

                appointment.setStatus(AppointmentStatus.BOOKED);

                Appointment saved = appointmentRepository.save(
                                appointment);

                return toResponse(saved);
        }

        @Transactional
        public void cancelAppointment(
                        Long id,
                        User patient) {

                Appointment appointment = appointmentRepository
                                .findByIdAndPatient(
                                                id,
                                                patient)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Appointment not found."));

                if (!"Booked".equalsIgnoreCase(appointment.getStatus().name())) {

                        throw new IllegalArgumentException(
                                        "This appointment cannot be cancelled.");
                }

                appointment.setStatus(AppointmentStatus.CANCELLED);

                appointmentRepository.save(
                                appointment);
        }

        private AppointmentResponse toResponse(
                        Appointment appointment) {

                return new AppointmentResponse(
                                appointment.getId(),
                                appointment.getDoctor().getId(),
                                appointment.getDoctor().getName(),
                                appointment.getAppointmentDate(),
                                appointment.getAppointmentTime(),
                                appointment.getStatus() != null ? appointment.getStatus().name() : null);
        }
}