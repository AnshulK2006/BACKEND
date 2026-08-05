package com.cvns.config;

import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.cvns.entities.*;
import com.cvns.entities.AppEnums.UserRole;
import com.cvns.repository.*;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seed(UserRepository users, ClinicRepository clinics,
            VaccineRepository vaccines, PasswordEncoder encoder) {
        return args -> {
            create(users, encoder, "System", "Admin", "admin@cvns.com", "9999999999",
                    "Admin@123", UserRole.ROLE_ADMIN, 18.5204, 73.8567);
            create(users, encoder, "Demo", "Parent", "parent@cvns.com", "9888888888",
                    "Parent@123", UserRole.ROLE_PARENT, 18.5204, 73.8567);
            User clinicUser = create(users, encoder, "Demo", "Clinic", "clinic@cvns.com", "9777777777",
                    "Clinic@123", UserRole.ROLE_CLINIC, 18.5308, 73.8475);

            if (clinics.findByOwnerId(clinicUser.getId()).isEmpty()) {
                Clinic c = new Clinic();
                c.setName("Sunrise Child Care Clinic");
                c.setEmail(clinicUser.getEmail());
                c.setPhone(clinicUser.getPhone());
                c.setAddress("Shivaji Nagar");
                c.setCity("Pune");
                c.setLatitude(18.5308);
                c.setLongitude(73.8475);
                c.setVerified(true);
                c.setOwner(clinicUser);
                clinics.save(c);
            }

            vaccine(vaccines, "BCG", "Tuberculosis protection", 0, 1);
            vaccine(vaccines, "Hepatitis B", "Birth dose", 0, 1);
            vaccine(vaccines, "OPV", "Oral polio vaccine", 0, 1);
            vaccine(vaccines, "Pentavalent", "DPT, Hep B and Hib", 2, 1);
            vaccine(vaccines, "Rotavirus", "Rotavirus protection", 2, 1);
            vaccine(vaccines, "PCV", "Pneumococcal vaccine", 2, 1);
            vaccine(vaccines, "Pentavalent", "Second dose", 3, 2);
            vaccine(vaccines, "Rotavirus", "Second dose", 3, 2);
            vaccine(vaccines, "Pentavalent", "Third dose", 4, 3);
            vaccine(vaccines, "MR", "Measles and rubella", 9, 1);
            vaccine(vaccines, "DPT Booster", "First booster", 18, 1);
            vaccine(vaccines, "MR", "Second dose", 18, 2);
        };
    }

    private User create(UserRepository repo, PasswordEncoder encoder, String firstName, String lastName,
            String email, String phone, String password, UserRole role, double latitude, double longitude) {
        User u = repo.findByEmail(email).orElseGet(() -> {
            User created = new User();
            created.setFirstName(firstName);
            created.setLastName(lastName);
            created.setEmail(email);
            created.setPhone(phone);
            created.setPassword(encoder.encode(password));
            created.setUserRole(role);
            created.setDob(LocalDate.of(1995, 1, 1));
            created.setAddress("Demo address");
            created.setCity("Pune");
            return repo.save(created);
        });
        if (!u.isEmailVerified()) {
            u.setEmailVerified(true);
            repo.save(u);
        }
        if (u.getLatitude() == null || u.getLongitude() == null) {
            u.setLatitude(latitude);
            u.setLongitude(longitude);
            repo.save(u);
        }
        return u;
    }

    private void vaccine(VaccineRepository repo, String name, String description, int months, int dose) {
        if (!repo.existsByNameAndDoseNumber(name, dose)) {
            Vaccine v = new Vaccine();
            v.setName(name);
            v.setDescription(description);
            v.setDueAgeMonths(months);
            v.setDoseNumber(dose);
            repo.save(v);
        }
    }
}
