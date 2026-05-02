package com.greenlight.spring_boot_security.controllers;

import com.greenlight.spring_boot_security.validation.OnCreate;
import com.greenlight.spring_boot_security.validation.OnUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.greenlight.spring_boot_security.models.Role;
import com.greenlight.spring_boot_security.models.User;
import com.greenlight.spring_boot_security.repositories.RoleRepository;
import com.greenlight.spring_boot_security.service.UserService;

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

    @GetMapping()
    public String adminPanel(Principal principal, Model model) {
        List<User> allUsers = userService.showAllUsers();
        model.addAttribute("allUsers", allUsers);

        // Ищем авторизованного пользователя в уже загруженном списке
        Optional<User> authorisedUser = allUsers.stream()
                .filter(u -> u.getEmail().equals(principal.getName()))
                .findFirst();
        if (authorisedUser.isPresent()) {
            model.addAttribute("authorisedUser", authorisedUser.get());
        }

        model.addAttribute("allRoles", roleRepository.findAll());
        return "admin_page";
    }


    @PostMapping("/add")
    public ResponseEntity<?> addUser(@ModelAttribute("newUser") @Validated(OnCreate.class) User newUser,
                                     BindingResult bindingResult,
                                     @RequestParam(value = "roles", required = false) List<Integer> roleIds) {

        // Проверка уникальности email пользователя
        if (!userService.isEmailUnique(newUser.getEmail(), newUser.getId())) {
            bindingResult.rejectValue("email", "error.email", "Пользователь с таким email уже существует");
        }

        if (bindingResult.hasErrors()) {
            // Для AJAX — возвращаем JSON с ошибками валидации
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        // Обработка ролей
        if (roleIds != null && !roleIds.isEmpty()) {
            List<Role> selectedRoles = roleRepository.findByIdIn(roleIds);
            newUser.setRoles(selectedRoles);
        } else {
            newUser.setRoles(Collections.emptyList());
        }

        userService.addUser(newUser);

        // Для AJAX — возвращаем успех в JSON
        Map<String, String> response = new HashMap<>();
        response.put("message", String.format("Пользователь c email %s успешно создан.", newUser.getEmail()));
        return ResponseEntity.ok().body(response);
    }


    @PostMapping("/update")
    public ResponseEntity<?> updateUser(
            @ModelAttribute("user") @Validated(OnUpdate.class) User user,
            BindingResult bindingResult,
            @RequestParam(value = "roles", required = false) List<Integer> roleIds) {

        // Проверка уникальности email пользователя
        if (!userService.isEmailUnique(user.getEmail(), user.getId())) {
            bindingResult.rejectValue("email", "error.email", "Пользователь с таким email уже существует");
        }

        if (bindingResult.hasErrors()) {
            // Для AJAX — возвращаем JSON с ошибками валидации
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        // Обработка ролей
        if (roleIds != null && !roleIds.isEmpty()) {
            List<Role> selectedRoles = roleRepository.findByIdIn(roleIds);
            user.setRoles(selectedRoles);
        } else {
            user.setRoles(Collections.emptyList());
        }

        userService.updateUser(user);

        // Для AJAX — возвращаем успех в JSON
        Map<String, String> response = new HashMap<>();
        response.put("message", String.format("Пользователь с email %s успешно отредактирован.", user.getEmail()));
        return ResponseEntity.ok().body(response);
    }


    @PostMapping("/delete")
    public String deleteUser(@RequestParam(value = "id", required = false) Integer id, RedirectAttributes redirectAttributes) {
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Некорректный id пользователя.");
            return "redirect:/admin";
        }

        Optional<User> userOpt = userService.findUserById(id);
        if (userOpt.isPresent()) {
            String userEmail = userOpt.get().getEmail();
            userService.deleteUserById(id);
            redirectAttributes.addFlashAttribute("successMessage", String.format("Пользователь %s успешно удалён.", userEmail));
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", String.format("Ошибка при удалении пользователя: пользователь с ID %d не найден.", id));
        }
        return "redirect:/admin";
    }

}
