package com.testing.traningproject.config;

import com.testing.traningproject.model.entity.Role;
import com.testing.traningproject.model.entity.User;
import com.testing.traningproject.model.enums.AccountStatus;
import com.testing.traningproject.model.enums.RoleName;
import com.testing.traningproject.repository.RoleRepository;
import com.testing.traningproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Data Initializer
 * Runs once when application starts to insert initial data
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initializeRoles();
        initializeAdminUser();
    }

    /**
     * Initialize roles if they don't exist
     */
    private void initializeRoles() {
        // Check if roles already exist
        if (roleRepository.count() > 0) {
            log.info("Roles already exist. Skipping initialization.");
            return;
        }

        // Create roles
        for (RoleName roleName : RoleName.values()) {
            Role role = Role.builder()
                    .name(roleName)
                    .build();
            roleRepository.save(role);
        }
        log.info("Roles initialization completed successfully!");
    }

    /**
     * Initialize admin user if it doesn't exist
     */
    private void initializeAdminUser() {
        log.info("Checking if admin user needs to be initialized...");

        String adminEmail = "admin@trainingproject.com";

        // Check if admin already exists
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user already exists. Skipping initialization.");
            return;
        }

        // Get ADMIN role
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN role not found. Roles must be initialized first."));

        // Create roles set
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);

        // Create admin user
        User adminUser = User.builder()
                .firstName("Super")
                .lastName("Admin")
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode("Admin@12345"))
                .accountStatus(AccountStatus.ACTIVE)
                .roles(roles)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(adminUser);

        log.info("Admin user created successfully!");
    }
}

