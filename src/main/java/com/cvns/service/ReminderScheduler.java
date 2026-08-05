package com.cvns.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cvns.entities.AppEnums.AppointmentStatus;
import com.cvns.entities.AppEnums.NotificationType;
import com.cvns.entities.AppEnums.VaccinationStatus;
import com.cvns.repository.AppointmentRepository;
import com.cvns.repository.ChildRepository;
import com.cvns.repository.VaccinationRecordRepository;
import com.cvns.repository.VaccineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReminderScheduler {
    private final ChildRepository children;
    private final VaccineRepository vaccines;
    private final VaccinationRecordRepository records;
    private final AppointmentRepository appointments;
    private final NotificationService notifications;
    private final EmailClientService email;

    @Value("${app.reminder.zone:Asia/Kolkata}")
    private String reminderZone;

    @Scheduled(
            cron = "${app.reminder.cron:0 0 8 * * *}",
            zone = "${app.reminder.zone:Asia/Kolkata}")
    @Transactional
    public void sendDailyReminders() {
        LocalDate tomorrow = LocalDate.now(ZoneId.of(reminderZone)).plusDays(1);
        sendVaccinationReminders(tomorrow);
        sendAppointmentReminders(tomorrow);
    }

    private void sendVaccinationReminders(LocalDate reminderDate) {
        children.findAll().forEach(child ->
                vaccines.findAllByOrderByDueAgeMonthsAscDoseNumberAsc().forEach(vaccine -> {
                    boolean completed = records.findByChildIdAndVaccineId(child.getId(), vaccine.getId())
                            .map(record -> record.getStatus() == VaccinationStatus.COMPLETED)
                            .orElse(false);
                    LocalDate dueDate = child.getDateOfBirth().plusMonths(vaccine.getDueAgeMonths());

                    if (!completed && dueDate.equals(reminderDate)) {
                        String message = vaccine.getName() + " dose " + vaccine.getDoseNumber()
                                + " is due tomorrow for " + child.getName() + ".";
                        notifications.create(child.getParent(), "Vaccination reminder", message,
                                NotificationType.VACCINATION_REMINDER);
                        email.send(child.getParent().getEmail(), "Vaccination due tomorrow", message,
                                "VACCINATION_REMINDER");
                    }
                }));
    }

    private void sendAppointmentReminders(LocalDate reminderDate) {
        appointments.findByAppointmentDateAndStatusIn(reminderDate,
                        EnumSet.of(AppointmentStatus.BOOKED, AppointmentStatus.ACCEPTED))
                .forEach(appointment -> {
                    String message = "Appointment tomorrow for " + appointment.getChild().getName()
                            + " at " + appointment.getClinic().getName() + " "
                            + appointment.getAppointmentTime() + ".";
                    notifications.create(appointment.getBookedBy(), "Appointment reminder", message,
                            NotificationType.APPOINTMENT);
                    email.send(appointment.getBookedBy().getEmail(), "Appointment reminder", message,
                            "APPOINTMENT_REMINDER");
                });
    }
}
