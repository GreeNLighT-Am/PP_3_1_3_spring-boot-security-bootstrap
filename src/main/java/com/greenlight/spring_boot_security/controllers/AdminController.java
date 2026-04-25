package com.greenlight.spring_boot_security.controllers;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.greenlight.spring_boot_security.models.Role;
import com.greenlight.spring_boot_security.models.User;
import com.greenlight.spring_boot_security.repositories.RoleRepository;
import com.greenlight.spring_boot_security.service.UserService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    @GetMapping()
    public String showUsers(Model model) {

        model.addAttribute("users", userService.showAllUsers());

        return "users/users";
    }

    @GetMapping("/user")
    public String showUser(@RequestParam(value = "id", required = false) Integer id, Model model) {
        if (id == null || id <= 0) {
            model.addAttribute("errorMessage", "Ошибка отображения пользователя: значение id не может быть пустым, 0 или отрицательным.");
            return "users/users";
        }

        Optional<User> userOpt = userService.showUserById(id);
        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
            return "users/user";
        } else {
            model.addAttribute("errorMessage", String.format("Ошибка отображения пользователя: пользователь с ID %d не найден.", id));
            return "users/users";
        }
    }

    @GetMapping("/new")
    public String newUser(@ModelAttribute("user") User user, Model model) {
        model.addAttribute("allRoles", roleRepository.findAll());
        return "users/new";
    }

    @PostMapping("/add")
    public String addUser(@ModelAttribute("user") @Valid User user,
                          BindingResult userBindingResult,
                          @RequestParam(value = "roles", required = false) List<Integer> roleIds,
                          RedirectAttributes redirectAttributes,
                          Model model) {

        // Проверка уникальности имени пользователя
        if (!userService.isNameUnique(user.getName(), user.getId())) {
            userBindingResult.rejectValue(
                    "name",
                    "error.user",
                    "Пользователь с таким именем уже существует"
            );
        }

        // Проверка уникальности email пользователя
        if (!userService.isEmailUnique(user.getEmail(), user.getId())) {
            userBindingResult.rejectValue(
                    "email",
                    "error.email",
                    "Пользователь с таким email уже существует"
            );
        }

        if (userBindingResult.hasErrors()) {
            model.addAttribute("allRoles", roleRepository.findAll());
            return "users/new";
        }

        // Обработка выбора ролей
        if (roleIds != null && !roleIds.isEmpty()) {
            Collection<Role> selectedRoles = roleRepository.findByIdIn(roleIds);
            user.setRoles(selectedRoles);
        } else {
            user.setRoles(Collections.emptyList());
        }

        userService.addUser(user);
        redirectAttributes.addFlashAttribute("successMessage",
                String.format("Пользователь %s успешно создан.", user.getName()));
        return "redirect:/admin";
    }

    @GetMapping("/edit")
    public String editUser(@RequestParam(value = "id", required = false) Integer id, Model model) {
        if (id == null || id <= 0) {
            model.addAttribute("errorMessage", "Ошибка редактирования пользователя: значение id не может быть пустым, 0 или отрицательным.");
            return "users/users";
        }

        Optional<User> userOpt = userService.showUserById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);
            model.addAttribute("allRoles", roleRepository.findAll());
            return "users/edit";
        } else {
            model.addAttribute("errorMessage", String.format("Ошибка редактирования пользователя: пользователь с ID %d не найден.", id));
            return "users/users";
        }
    }

    @PostMapping("/update")
    public String updateUser(@ModelAttribute("user") @Valid User user,
                             BindingResult userBindingResult,
                             @RequestParam(value = "roles", required = false) List<Integer> roleIds,
                             RedirectAttributes redirectAttributes, Model model) {

        // Проверка уникальности имени пользователя
        if (!userService.isNameUnique(user.getName(), user.getId())) {
            userBindingResult.rejectValue(
                    "name",
                    "error.user",
                    "Пользователь с таким именем уже существует"
            );
        }

        // Проверка уникальности email пользователя
        if (!userService.isEmailUnique(user.getEmail(), user.getId())) {
            userBindingResult.rejectValue(
                    "email",
                    "error.email",
                    "Пользователь с таким email уже существует"
            );
        }

        if (userBindingResult.hasErrors()) {
            model.addAttribute("allRoles", roleRepository.findAll());
            return "users/edit";
        }

        // Обработка выбора ролей
        if (roleIds != null && !roleIds.isEmpty()) {
            Collection<Role> selectedRoles = roleRepository.findByIdIn(roleIds);
            user.setRoles(selectedRoles);
        } else {
            user.setRoles(Collections.emptyList());
        }

        userService.updateUser(user);
        redirectAttributes.addFlashAttribute("successMessage", "Пользователь успешно обновлён.");

        return "redirect:/admin/user?id=" + user.getId();
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam(value = "id", required = false) Integer id, RedirectAttributes redirectAttributes) {
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Некорректный id пользователя.");
            return "redirect:/admin";
        }

        Optional<User> userOpt = userService.showUserById(id);
        if (userOpt.isPresent()) {
            String userName = userOpt.get().getName();
            userService.deleteUserById(id);
            redirectAttributes.addFlashAttribute("successMessage", String.format("Пользователь %s успешно удалён.", userName));
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", String.format("Ошибка при удалении пользователя: пользователь с ID %d не найден.", id));
        }
        return "redirect:/admin";
    }

}
