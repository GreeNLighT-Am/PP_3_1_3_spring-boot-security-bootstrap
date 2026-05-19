package com.greenlight.spring_boot_security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.greenlight.spring_boot_security.models.User;
import com.greenlight.spring_boot_security.repositories.UserRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private void processPassword(User updatedUser, User currentUser) {

        String newPassword = updatedUser.getPassword();
        String currentPassword = currentUser.getPassword();

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (!passwordEncoder.matches(newPassword, currentPassword)) {
                updatedUser.setPassword(passwordEncoder.encode(newPassword));
            } else {
                updatedUser.setPassword(currentPassword);
            }
        } else {
            updatedUser.setPassword(currentPassword);
        }
    }

    @Override
    @Transactional
    public void addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserWithRolesByEmail(String email) {
        return userRepository.findUserWithRolesByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> showAllUsers() {
        return userRepository.showAllUsers();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserById(int id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public void updateUser(User updatedUser) {
        Optional<User> existingUser = findUserById(updatedUser.getId());

        if (existingUser.isEmpty()) {
            throw new EntityNotFoundException("Пользователь с ID " + updatedUser.getId() + " не найден.");
        }

        User currentUser = existingUser.get();

        processPassword(updatedUser, currentUser);

        userRepository.save(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUserById(int id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("Пользователь с ID " + id + " не найден.");
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailUnique(String email, Integer userId) {
        Optional<User> existingUser = userRepository.findUserByEmail(email);
        if (existingUser.isEmpty()) {
            return true;
        }
        if (userId != null && existingUser.get().getId() == userId) {
            return true;
        }
        return false;
    }

    @Override
    public boolean existsById(int id) {
        return userRepository.existsById(id);
    }

}
