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
    public void updateUser(User updatedUser) {
        Optional<User> existingUser = findUserById(updatedUser.getId());
        if (existingUser.isPresent()) {
            User currentUser = existingUser.get();

            // Обработка пароля
            String newPassword = updatedUser.getPassword();
            String currentPassword = currentUser.getPassword();
            // Проверяем что пароль передан
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                // Проверяем, изменился ли пароль
                if (!passwordEncoder.matches(newPassword, currentPassword)) {
                    // Пароль действительно новый - хешируем
                    updatedUser.setPassword(passwordEncoder.encode(newPassword));
                } else {
                    // Пароль не изменился - оставляем существующий хеш
                    updatedUser.setPassword(currentPassword);
                }
            } else {
                // Пароль не был передан (пустой или null)
                // Сохраняем текущий пароль пользователя
                updatedUser.setPassword(currentPassword);
            }

            userRepository.save(updatedUser);
        }
    }

    @Override
    @Transactional
    public void deleteUserById(int id) {
        userRepository.deleteById(id);
    }

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
