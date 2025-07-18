package com.janbabak.noqlbackend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.janbabak.noqlbackend.model.Role;
import com.janbabak.noqlbackend.model.user.RegisterRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @Size(min = 1, max = 32)
    private String firstName;

    @Size(min = 1, max = 32)
    private String lastName;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @Size(min = 8, max = 64)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    private Integer queryLimit = 0;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Database> databases;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomModel> models;

    public User(RegisterRequest request, PasswordEncoder passwordEncoder, Role role, Integer queryLimit) {
        firstName = request.firstName();
        lastName = request.lastName();
        email = request.email();
        password = passwordEncoder.encode(request.password());
        this.queryLimit = queryLimit;
        this.role = role;
        databases = new ArrayList<>();
        models = new ArrayList<>();
    }

    @SuppressWarnings("unused")
    public void addDatabase(Database database) {
        if (databases == null) {
            databases = new ArrayList<>();
        }
        for (Database db : databases) {
            if (db.getId().equals(database.getId())) {
                return; // database already in the list
            }
        }
        databases.add(database);
    }

    @SuppressWarnings("unused")
    public void addCustomModel(CustomModel model) {
        if (models == null) {
            models = new ArrayList<>();
        }
        for (CustomModel m : models) {
            if (m.getId().equals(model.getId())) {
                return; // model already in the list
            }
        }
        models.add(model);
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

    @JsonIgnore
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
