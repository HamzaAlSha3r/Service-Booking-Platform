package com.testing.traningproject.model.entity;

import com.testing.traningproject.model.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

/**
 * Role Entity - يمثل الأدوار في النظام (CUSTOMER, SERVICE_PROVIDER, ADMIN)
 */
@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", unique = true, nullable = false, length = 50)
    private RoleName name;
}

