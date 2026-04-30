package com.greenlight.spring_boot_security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.greenlight.spring_boot_security.models.User;
import com.greenlight.spring_boot_security.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void addUser(User user) {
        // Хэшируем пароль перед сохранением
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public Optional<User> findUserWithRolesByFirstName(String username) {
//        return userRepository.findUserWithRolesByFirstName(username);
//    }

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
        return userRepository.findUserById(id);
    }

    @Override
    @Transactional
    public void updateUser(User user) {
        Optional<User> existingUser = findUserById(user.getId());
        if (existingUser.isPresent()) {
            User dbUser = existingUser.get();
            // Хэшируем пароль только если он был изменён
            if (user.getPassword() != null && !user.getPassword().isEmpty() && !user.getPassword().equals(dbUser.getPassword())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                // Сохраняем старый пароль
                user.setPassword(dbUser.getPassword());
            }
            userRepository.save(user);
        }
    }

    @Override
    @Transactional
    public void deleteUserById(int id) {
        userRepository.deleteById(id);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public boolean isNameUnique(String name, Integer userId) {
//        Optional<User> existingUser = userRepository.findUserByFirstName(name);
//        if (existingUser.isEmpty()) {
//            return true; // Имя свободно
//        }
//        // Если userId не null, проверяем, что это тот же пользователь (редактирование)
//        if (userId != null && existingUser.get().getId() == userId) {
//            return true; // Это тот же пользователь — уникальность сохраняется
//        }
//        return false; // Найден другой пользователь с таким именем
//    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailUnique(String email, Integer userId) {
        Optional<User> existingUser = userRepository.findUserByEmail(email);
        if (existingUser.isEmpty()) {
            return true; // Email свободно
        }
        // Если userId не null, проверяем, что это тот же пользователь (редактирование)
        if (userId != null && existingUser.get().getId() == userId) {
            return true; // Это тот же пользователь — уникальность сохраняется
        }
        return false; // Найден другой пользователь с таким email
    }

}
