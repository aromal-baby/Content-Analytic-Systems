package com.contentanalytics.content_analytic_system.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "users")  // DB for users
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Primary key
    private long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "last_login")
    private String lastLogin;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)    //Many-to-many relationship with the role entity
                                            //User can have multiple roles and the roles can be assigned to multiple users
    @JoinTable(
        name = "user_roles",    //Junction table (name)
        joinColumns = @JoinColumn(name = "user_id"),    // Foreign key - user
        inverseJoinColumns = @JoinColumn(name = "role_id")  //Foreign key column - role
    )
    private Set<Role> roles = new HashSet<>();  // Using set 'cause we don't need duplicate values


    /* A callback method that automatically sets creation and updates timestamps
    when a new user entity is persisted
     */
    @PrePersist //To execute before the expected implementation is done
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // Same operation but before an update occurs
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
