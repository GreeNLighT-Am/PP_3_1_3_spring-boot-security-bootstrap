package com.greenlight.spring_boot_security.service;

import com.greenlight.spring_boot_security.models.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    void addUser(User user);

    Optional<User> findUserWithRolesByEmail(String email);

    List<User> showAllUsers();

    Optional<User> findUserById(int id);

    void updateUser(User user);

    void deleteUserById(int id);

    boolean isEmailUnique(String name, Integer userId);

}
