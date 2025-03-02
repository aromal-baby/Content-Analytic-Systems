package com.contentanalytics.content_analytic_system.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "roles")  // Roles DB
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Primary key
    private Long id;

    @Enumerated(EnumType.STRING)    //String 'cause better readability
    @Column(length = 20)
    private ERole name;

    // Constructor helps create a role with a specific role name
    public Role(ERole name) {
        this.name = name;
    }


    // Enumeration defining available role types in the system
    public enum ERole {
        ROLE_USER,
        ROLE_ANALYST,
        ROLE_ADMIN
    }
}
