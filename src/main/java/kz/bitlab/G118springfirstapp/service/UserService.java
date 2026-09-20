package kz.bitlab.G118springfirstapp.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import kz.bitlab.G118springfirstapp.model.City;
import kz.bitlab.G118springfirstapp.model.User;
import kz.bitlab.G118springfirstapp.repository.CityRepository;
import kz.bitlab.G118springfirstapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository users;
    private final CityRepository cities;

    public List<User> getUsers() { return users.findAllByOrderByIdAsc(); }
    public List<City> getCities() { return cities.findAllByOrderByIdAsc(); }
    public City getCityById(Long id) {
        return id == null ? null : cities.findById(id).orElse(null);
    }
    public User getUserById(Long id) { return users.findById(id).orElse(null); }

    @Transactional
    public User addUser(User user) {
        user.setId(null);
        return users.save(user);
    }

    @Transactional
    public void editUser(Long id, String email, String fullName, Long cityId) {
        User user = requireUser(id);
        City city = getCityById(cityId);
        if (cityId != null && city == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Город не найден");
        }
        user.setEmail(email);
        user.setFullName(fullName);
        user.setCity(city);
    }

    @Transactional
    public void deleteUserById(Long id) { users.delete(requireUser(id)); }

    public List<User> findUsers(String search) {
        return users.findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrderByFullNameDescIdDesc(
                search, search);
    }
    public Page<User> getUserPage(String search, int page) {
        var request = PageRequest.of(Math.max(0, page), 10, Sort.by("id").descending());
        Page<User> result = users.findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
                search, search, request);
        // A deleted last row or an old bookmark can point beyond the last page.
        if (result.getNumber() > 0 && result.getNumber() >= result.getTotalPages()) {
            request = PageRequest.of(Math.max(0, result.getTotalPages() - 1), 10, request.getSort());
            result = users.findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(search, search, request);
        }
        return result;
    }

    private User requireUser(Long id) {
        return users.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }
}
