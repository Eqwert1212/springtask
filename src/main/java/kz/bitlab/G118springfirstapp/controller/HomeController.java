package kz.bitlab.G118springfirstapp.controller;

import java.util.List;
import kz.bitlab.G118springfirstapp.db.DbManager;
import kz.bitlab.G118springfirstapp.model.City;
import kz.bitlab.G118springfirstapp.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping("/") //@WebServlet("/") + doGet()
    public String homePage(Model model) {
        List<User> users = DbManager.getUsers();
        model.addAttribute("users", users); // req.setAttribute("users", users)
        return "home";
    }

    /**
     * String email = req.getParameter("email"); String fullName = req.getParameter("fullName"); User
     * user = new User(); user.setEmail(email); user.setFullName(fullName);.
     *
     * @param user новый пользователь
     * @return главная страница.
     */
    @PostMapping("/add-user")
    public String addUser(User user) {
        DbManager.addUser(user);
        return "redirect:/"; //resp.sendRedirect("/")
    }

    @GetMapping("/user-details")
    public String getUser(@RequestParam(name = "userId") Long id, Model model) {
        User user = DbManager.getUserById(id);
        List<City> cities = DbManager.getCities();
        model.addAttribute("cities", cities);
        model.addAttribute("user", user);
        return "userDetails";
    }

    @PostMapping("/user-edit/{id}")
    public String editUser(@RequestParam String email,
                           @RequestParam(name = "fullname") String fullName,
                           @PathVariable Long id,
                           @RequestParam(name = "city_id") Long cityId) {
        if (fullName == null || fullName.isEmpty() || email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Parameters can not be null");
        }
        DbManager.editUser(id, email, fullName, cityId);
        return "redirect:/";
    }

    @PostMapping("/user-delete/{id}")
    public String deleteUser(@PathVariable(name = "id") Long userId) {
        DbManager.deleteUserById(userId);
        return "redirect:/";
    }

    @GetMapping("/search")
    public String search(@RequestParam String search, Model model) {
        List<User> users = DbManager.findUsers(search);
        model.addAttribute("users", users);
        return "home";
    }

    @GetMapping("/search-alt")
    public String searchAlt(@RequestParam String search, Model model) {
        String users = DbManager.findUsersAlt(search);
        model.addAttribute("usersAlt", users);
        return "home";
    }
}
