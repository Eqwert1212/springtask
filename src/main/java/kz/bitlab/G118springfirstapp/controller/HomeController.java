package kz.bitlab.G118springfirstapp.controller;

import jakarta.validation.Valid;
import kz.bitlab.G118springfirstapp.service.UserService;
import lombok.RequiredArgsConstructor;
import kz.bitlab.G118springfirstapp.form.UserForm;
import kz.bitlab.G118springfirstapp.model.User;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final UserService userService;

    @InitBinder("userForm")
    public void initFormBinder(WebDataBinder binder) {
        binder.setAllowedFields("email", "fullName", "cityId");
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @ModelAttribute("userForm")
    public UserForm userForm() {
        return new UserForm();
    }

    @GetMapping({"/", "/search", "/search-alt"})
    public String homePage(@RequestParam(defaultValue = "") String search,
                           @RequestParam(defaultValue = "0") int page, Model model) {
        populateHome(model, search.strip(), page);
        return "home";
    }

    @PostMapping("/add-user")
    public String addUser(@Valid @ModelAttribute("userForm") UserForm form,
                          BindingResult errors, Model model, RedirectAttributes redirect) {
        validateCity(form, errors);
        if (errors.hasErrors()) {
            populateHome(model, "", 0);
            return "home";
        }
        User user = new User();
        user.setEmail(form.getEmail());
        user.setFullName(form.getFullName());
        user.setCity(userService.getCityById(form.getCityId()));
        userService.addUser(user);
        redirect.addFlashAttribute("success", "Пользователь добавлен");
        return "redirect:/";
    }

    @GetMapping("/user-details")
    public String getUser(@RequestParam(name = "userId") Long id, Model model) {
        User user = requireUser(id);
        UserForm form = new UserForm();
        form.setEmail(user.getEmail());
        form.setFullName(user.getFullName());
        form.setCityId(user.getCity() == null ? null : user.getCity().getId());
        model.addAttribute("userForm", form);
        populateDetails(model, id);
        return "userDetails";
    }

    @PostMapping("/user-edit/{id}")
    public String editUser(@PathVariable("id") Long id,
                           @Valid @ModelAttribute("userForm") UserForm form,
                           BindingResult errors, Model model, RedirectAttributes redirect) {
        requireUser(id);
        validateCity(form, errors);
        if (errors.hasErrors()) {
            populateDetails(model, id);
            return "userDetails";
        }
        userService.editUser(id, form.getEmail(), form.getFullName(), form.getCityId());
        redirect.addFlashAttribute("success", "Изменения сохранены");
        return "redirect:/";
    }

    @PostMapping("/user-delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirect) {
        requireUser(id);
        userService.deleteUserById(id);
        redirect.addFlashAttribute("success", "Пользователь удалён");
        return "redirect:/";
    }

    private void validateCity(UserForm form, BindingResult errors) {
        if (!errors.hasFieldErrors("cityId") && form.getCityId() != null
                && userService.getCityById(form.getCityId()) == null) {
            errors.rejectValue("cityId", "invalidCity", "Выберите город из списка");
        }
    }

    private User requireUser(Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }
        return user;
    }

    private void populateHome(Model model, String search, int page) {
        var userPage = userService.getUserPage(search, page);
        model.addAttribute("userPage", userPage);
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("search", search);
        model.addAttribute("cities", userService.getCities());
    }

    private void populateDetails(Model model, Long id) {
        model.addAttribute("userId", id);
        model.addAttribute("cities", userService.getCities());
    }
}
