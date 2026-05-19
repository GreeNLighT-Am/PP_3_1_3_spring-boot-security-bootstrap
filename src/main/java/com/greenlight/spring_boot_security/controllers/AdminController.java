package com.greenlight.spring_boot_security.controllers;

import com.greenlight.spring_boot_security.validation.OnCreate;
import com.greenlight.spring_boot_security.validation.OnUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.greenlight.spring_boot_security.models.Role;
import com.greenlight.spring_boot_security.models.User;
import com.greenlight.spring_boot_security.repositories.RoleRepository;
import com.greenlight.spring_boot_security.service.UserService;

import javax.persistence.EntityNotFoundException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    private ResponseEntity<Map<String, String>> isUserExist(User user) {
        if (!userService.existsById(user.getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", String.format("Пользователь с ID %d не найден. Возможно, он был удален другим администратором.", user.getId())
                    ));
        }
        return null;
    }

    private void processEmail(User newUser, BindingResult bindingResult) {
        if (!userService.isEmailUnique(newUser.getEmail(), newUser.getId())) {
            bindingResult.rejectValue("email", "error.email", "Пользователь с таким email уже существует");
        }
    }

    private static ResponseEntity<Map<String, String>> processValidation(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }
        return null;
    }

    private void processRoles(User newUser, List<Integer> roleIds) {
        if (roleIds != null && !roleIds.isEmpty()) {
            List<Role> selectedRoles = roleRepository.findByIdIn(roleIds);
            newUser.setRoles(selectedRoles);
        } else {
            newUser.setRoles(Collections.emptyList());
        }
    }

    @GetMapping()
    public String adminPanel(Principal principal, Model model) {
        List<User> allUsers = userService.showAllUsers();
        model.addAttribute("allUsers", allUsers);

        Optional<User> authorisedUser = allUsers.stream()
                .filter(u -> u.getEmail().equals(principal.getName()))
                .findFirst();
        if (authorisedUser.isPresent()) {
            model.addAttribute("authorisedUser", authorisedUser.get());
        }

        model.addAttribute("allRoles", roleRepository.findAll());
        return "admin_panel";
    }

    @PostMapping("/add")
    public ResponseEntity<?> addUser(@ModelAttribute("newUser") @Validated(OnCreate.class) User newUser,
                                     BindingResult bindingResult,
                                     @RequestParam(value = "roles", required = false) List<Integer> roleIds) {

        processEmail(newUser, bindingResult);

        ResponseEntity<Map<String, String>> errors = processValidation(bindingResult);
        if (errors != null) return errors;

        processRoles(newUser, roleIds);

        userService.addUser(newUser);

        Map<String, String> response = new HashMap<>();
        response.put("message", String.format("Пользователь c email %s успешно создан.", newUser.getEmail()));
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateUser(
            @ModelAttribute("user") @Validated(OnUpdate.class) User user,
            BindingResult bindingResult,
            @RequestParam(value = "roles", required = false) List<Integer> roleIds) {

        ResponseEntity<Map<String, String>> userNotExist = isUserExist(user);
        if (userNotExist != null) return userNotExist;

        processEmail(user, bindingResult);

        ResponseEntity<Map<String, String>> errors = processValidation(bindingResult);
        if (errors != null) return errors;

        processRoles(user, roleIds);

        try {
            userService.updateUser(user);

            Map<String, String> response = new HashMap<>();
            response.put("message", String.format("Пользователь с email %s успешно отредактирован.", user.getEmail()));
            return ResponseEntity.ok().body(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", String.format("%s Возможно, он уже был удален другим администратором.", e.getMessage(
                            ))));
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteUser(@RequestParam(value = "id", required = false) Integer id) {

        Optional<User> existingUser = userService.findUserById(id);

        if (existingUser.isPresent()) {
            try {
                userService.deleteUserById(id);
                return ResponseEntity.ok(Map.of("message", String.format("Пользователь с email %s успешно удалён.", existingUser.get().getEmail())));
            } catch (EntityNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", String.format("%s Возможно, он уже был удален другим администратором.", e.getMessage())));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", String.format("Пользователь с %d не найден. Возможно, он уже был удален другим администратором.", id)));
        }

    }
}
