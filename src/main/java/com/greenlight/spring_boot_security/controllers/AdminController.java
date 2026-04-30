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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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

    private boolean isAjaxRequest() {
        String header = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(header);
    }


    @GetMapping()
    public String adminPanel(Principal principal, Model model, @ModelAttribute("newUser") User newUser) {
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
        return "admin/admin_page";
    }


    @PostMapping("/add")
    public String addUser(@ModelAttribute("newUser") @Validated(OnCreate.class) User newUser,
                          BindingResult bindingResult,
                          @RequestParam(value = "roles", required = false) List<Integer> roleIds,
                          RedirectAttributes redirectAttributes,
                          Model model, Principal principal) {

        List<User> allUsers = userService.showAllUsers();
        model.addAttribute("allUsers", allUsers);

        // Ищем авторизованного пользователя в уже загруженном списке
        Optional<User> authorisedUser = allUsers.stream()
                .filter(u -> u.getEmail().equals(principal.getName()))
                .findFirst();
        if (authorisedUser.isPresent()) {
            model.addAttribute("authorisedUser", authorisedUser.get());
        }


        // Проверка уникальности имени пользователя
        //        if (!userService.isNameUnique(user.getFirstName(), user.getId())) {
        //            bindingResult.rejectValue("firstName", "error.user", "Пользователь с таким именем уже существует");
        //        }

        // Проверка уникальности email пользователя
        if (!userService.isEmailUnique(newUser.getEmail(), newUser.getId())) {
            bindingResult.rejectValue("email", "error.email", "Пользователь с таким email уже существует");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("allUsers", userService.showAllUsers()); // ← важно!
            model.addAttribute("allRoles", roleRepository.findAll());
            model.addAttribute("openNewUserTab", true); // ← ключевой флаг!
            return "admin/admin_page";
        }

        // Обработка выбора ролей
        if (roleIds != null && !roleIds.isEmpty()) {
            List<Role> selectedRoles = roleRepository.findByIdIn(roleIds);
            newUser.setRoles(selectedRoles);
        } else {
            newUser.setRoles(Collections.emptyList());
        }

        userService.addUser(newUser);
        redirectAttributes.addFlashAttribute("successMessage",
                String.format("Пользователь %s успешно создан.", newUser.getFirstName()));
        return "redirect:/admin";
    }


    @PostMapping("/update")
    public ResponseEntity<?> updateUser(
            @ModelAttribute("user") @Validated(OnUpdate.class) User user,
            BindingResult bindingResult,
            @RequestParam(value = "roles", required = false) List<Integer> roleIds,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Проверка уникальности имени пользователя
        //        if (!userService.isNameUnique(user.getFirstName(), user.getId())) {
        //            bindingResult.rejectValue("firstName", "error.user", "Пользователь с таким именем уже существует");
        //        }

        // Проверка уникальности email пользователя
        if (!userService.isEmailUnique(user.getEmail(), user.getId())) {
            bindingResult.rejectValue("email", "error.email", "Пользователь с таким email уже существует");
        }

        if (bindingResult.hasErrors()) {
            // Если это AJAX-запрос — возвращаем JSON с ошибками
            if (isAjaxRequest()) {
                Map<String, String> errors = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );
                return ResponseEntity.badRequest().body(errors);
            }

            // Иначе — возвращаем HTML (как раньше)
            model.addAttribute("allRoles", roleRepository.findAll());
            return ResponseEntity.status(200).body("users/edit");
        }

        // Обработка ролей
        if (roleIds != null && !roleIds.isEmpty()) {
            List<Role> selectedRoles = roleRepository.findByIdIn(roleIds);
            user.setRoles(selectedRoles);
        } else {
            user.setRoles(Collections.emptyList());
        }

        userService.updateUser(user);

        if (isAjaxRequest()) {
            return ResponseEntity.ok().build(); // Успешно
        }

        redirectAttributes.addFlashAttribute("successMessage", "Пользователь успешно обновлён.");
        return ResponseEntity.ok().body("redirect:/admin");
    }


    @PostMapping("/delete")
    public String deleteUser(@RequestParam(value = "id", required = false) Integer id, RedirectAttributes redirectAttributes) {
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Некорректный id пользователя.");
            return "redirect:/admin";
        }

        Optional<User> userOpt = userService.findUserById(id);
        if (userOpt.isPresent()) {
            String userName = userOpt.get().getFirstName();
            userService.deleteUserById(id);
            redirectAttributes.addFlashAttribute("successMessage", String.format("Пользователь %s успешно удалён.", userName));
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", String.format("Ошибка при удалении пользователя: пользователь с ID %d не найден.", id));
        }
        return "redirect:/admin";
    }


//    @GetMapping("/user")
//    public String showUser(@RequestParam(value = "id", required = false) Integer id, Model model) {
//        if (id == null || id <= 0) {
//            model.addAttribute("errorMessage", "Ошибка отображения пользователя: значение id не может быть пустым, 0 или отрицательным.");
//            return "users/users";
//        }
//
//        Optional<User> userOpt = userService.showUserById(id);
//        if (userOpt.isPresent()) {
//            model.addAttribute("user", userOpt.get());
//            return "users/user";
//        } else {
//            model.addAttribute("errorMessage", String.format("Ошибка отображения пользователя: пользователь с ID %d не найден.", id));
//            return "users/users";
//        }
//    }

//    @GetMapping("/new")
//    public String newUser(@ModelAttribute("newUser") User user, Model model) {
//        model.addAttribute("allRoles", roleRepository.findAll());
//        return "users/new";
//    }


//    @GetMapping("/edit")
//    public String editUser(@RequestParam(value = "id", required = false) Integer id, Model model) {
//        if (id == null || id <= 0) {
//            model.addAttribute("errorMessage", "Ошибка редактирования пользователя: значение id не может быть пустым, 0 или отрицательным.");
//            return "users/users";
//        }
//
//        Optional<User> userOpt = userService.showUserById(id);
//        if (userOpt.isPresent()) {
//            User user = userOpt.get();
//            model.addAttribute("user", user);
//            model.addAttribute("allRoles", roleRepository.findAll());
//            return "users/edit";
//        } else {
//            model.addAttribute("errorMessage", String.format("Ошибка редактирования пользователя: пользователь с ID %d не найден.", id));
//            return "users/users";
//        }
//    }


}
