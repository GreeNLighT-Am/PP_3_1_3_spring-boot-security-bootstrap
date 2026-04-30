package com.greenlight.spring_boot_security.models;

import com.greenlight.spring_boot_security.validation.OnCreate;
import com.greenlight.spring_boot_security.validation.OnUpdate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "first_name")
    @NotEmpty(message = "firstName не должно быть пустым", groups = {OnCreate.class, OnUpdate.class})
    @Size(min = 2, max = 30, message = "Длина firstName должна быть от 2-х до 30-ти знаков", groups = {OnCreate.class, OnUpdate.class})
    private String firstName;

    @Column(name = "last_name")
    @NotEmpty(message = "lastName не должно быть пустым", groups = {OnCreate.class, OnUpdate.class})
    @Size(min = 2, max = 30, message = "Длина lastName должна быть от 2-х до 30-ти знаков", groups = {OnCreate.class, OnUpdate.class})
    private String lastName;

    @Column(name = "age")
    @NotNull(message = "Возраст не должен быть пустым", groups = {OnCreate.class, OnUpdate.class})
    @Min(value = 1, message = "Возраст должен быть больше 0", groups = {OnCreate.class, OnUpdate.class})
    private Integer age;

    @Column(name = "email")
    @NotEmpty(message = "Email не должен быть пустым", groups = {OnCreate.class, OnUpdate.class})
    @Email(message = "Email должен быть валидным", groups = {OnCreate.class, OnUpdate.class})
    private String email;

    @Column
    @NotEmpty(message = "Пароль не должен быть пустым", groups = OnCreate.class)
    @Size(min = 4, message = "Длина пароля должна быть от 4-х знаков", groups = {OnCreate.class, OnUpdate.class})
    private String password;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(r -> new SimpleGrantedAuthority(r.getRole()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}