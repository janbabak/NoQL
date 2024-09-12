package com.janbabak.noqlbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.janbabak.noqlbackend.model.Role;
import com.janbabak.noqlbackend.model.user.RegisterRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

@Entity
@Table(name = "users") // to avoid conflict with SQL reserved word "user"
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue
    private UUID id;

    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Database> databases;

    public User(RegisterRequest request, PasswordEncoder passwordEncoder, Role role) {
        firstName = request.getFirstName();
        lastName = request.getLastName();
        email = request.getEmail();
        password = passwordEncoder.encode(request.getPassword());
        this.role = role;
        databases = new ArrayList<>();
    }

    @SuppressWarnings("unused")
    public void addDatabase(Database database) {
        for (Database db : databases) {
            if (db.getId().equals(database.getId())) {
                return; // database already in the list
            }
        }
        databases.add(database);
    }

    /**
     * Get list of granted authorities (roles).
     * @return list of authorities.
     */
    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return email;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }
}
