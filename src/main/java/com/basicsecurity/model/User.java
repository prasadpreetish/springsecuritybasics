package com.basicsecurity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users") // Define table name for clarity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incrementing ID
    private long id;

    @Column(unique = true,nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true,nullable = false)
    private String email;

    // Many-to-Many relationship with Role entity
    // Users can have multiple roles, and roles can be assigned to multiple users
    @ManyToMany(fetch = FetchType.EAGER) // Fetch roles eagerly when user is loaded
    @JoinTable(
            name="users_roles",// name of the join table
            joinColumns = @JoinColumn(name = "user_id"), // Column for User ID in join table
            inverseJoinColumns = @JoinColumn(name = "role_id") // Column for Role ID in join table
    )
    private Set<Role> roles = new HashSet<>();
}
