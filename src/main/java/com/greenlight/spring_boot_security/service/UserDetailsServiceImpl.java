package com.greenlight.spring_boot_security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.greenlight.spring_boot_security.models.User;
import com.greenlight.spring_boot_security.repositories.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findUserWithRolesByEmail(email);

        if (user.isEmpty())
            throw new UsernameNotFoundException("Пользователь с email " + email + " не найден");

        User foundUser = user.get();
        return new org.springframework.security.core.userdetails.User(
                foundUser.getUsername(),           // ← теперь email — это "username"
                foundUser.getPassword(),
                foundUser.getAuthorities()
        );
    }
}
