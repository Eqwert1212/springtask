package kz.bitlab.G118springfirstapp.controller;

import java.util.ArrayList;
import java.util.List;
import kz.bitlab.G118springfirstapp.service.UserService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import kz.bitlab.G118springfirstapp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class HomeControllerTests {
    @Autowired private MockMvc mvc;
    @Autowired private UserService userService;
    private List<User> original;

    @BeforeEach
    void saveUsers() {
        original = new ArrayList<>(userService.getUsers());
    }

    @Test
    void invalidAdditionShowsErrorsAndPreservesInputWithoutSaving() throws Exception {
        mvc.perform(post("/add-user").with(csrf()).param("email", "not-email").param("fullName", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeHasFieldErrors("userForm", "email", "fullName"))
                .andExpect(content().string(containsString("Введите имя")))
                .andExpect(content().string(containsString("value=\"not-email\"")));
        assertEquals(original, userService.getUsers());
    }

    @Test
    void validAdditionTrimsInputAndIgnoresUneditableFields() throws Exception {
        mvc.perform(post("/add-user").with(csrf()).param("email", " new@example.test ")
                        .param("fullName", " New User ").param("cityId", "1")
                        .param("id", "1").param("programmingLanguage", "Injected"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
        User added = userService.getUsers().get(original.size());
        assertEquals("new@example.test", added.getEmail());
        assertEquals("New User", added.getFullName());
        assertEquals(1L, added.getCity().getId());
        assertNull(added.getProgrammingLanguage());
        assertTrue(original.stream().noneMatch(user -> user.getId().equals(added.getId())));
    }

    @Test
    void invalidCityIsRejectedIncludingMalformedNumbers() throws Exception {
        for (String city : List.of("999999", "abc")) {
            mvc.perform(post("/add-user").with(csrf()).param("email", "new@example.test")
                            .param("fullName", "New User").param("cityId", city))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeHasFieldErrors("userForm", "cityId"));
        }
        assertEquals(original, userService.getUsers());
    }

    @Test
    void invalidEditDoesNotMutateStoredUserAndShowsAttemptedValues() throws Exception {
        User existing = original.get(0);
        String email = existing.getEmail();
        String name = existing.getFullName();
        mvc.perform(post("/user-edit/" + existing.getId()).with(csrf()).param("email", "broken")
                        .param("fullName", "Attempted Name").param("cityId", "1"))
                .andExpect(status().isOk()).andExpect(view().name("userDetails"))
                .andExpect(model().attributeHasFieldErrors("userForm", "email"))
                .andExpect(content().string(containsString("value=\"Attempted Name\"")));
        assertEquals(email, existing.getEmail());
        assertEquals(name, existing.getFullName());
    }

    @Test
    void missingUsersReturn404() throws Exception {
        mvc.perform(get("/user-details").param("userId", "999999"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/user-edit/999999").with(csrf()).param("email", "new@example.test")
                        .param("fullName", "New User"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/user-delete/999999").with(csrf())).andExpect(status().isNotFound());
    }

    @Test
    void homeAndSearchRenderCreationForm() throws Exception {
        for (String path : List.of("/", "/search", "/search-alt")) {
            mvc.perform(get(path)).andExpect(status().isOk())
                    .andExpect(model().attributeExists("userForm", "cities", "users"))
                    .andExpect(content().string(containsString("name=\"fullName\"")));
        }
    }

    @Test
    void longInputsAreRejected() throws Exception {
        mvc.perform(post("/add-user").with(csrf()).param("email", "a".repeat(250) + "@example.test")
                        .param("fullName", "a".repeat(101)))
                .andExpect(model().attributeHasFieldErrors("userForm", "email", "fullName"));
        assertEquals(original, userService.getUsers());
    }
    @Test
    void paginationKeepsSearchAndHandlesOutOfRangePages() throws Exception {
        for (int i = 0; i < 23; i++) {
            userService.addUser(new User(null, "paging" + i + "@example.test",
                    "Paging Fixture " + i, null, null));
        }
        var first = userService.getUserPage("Paging Fixture", 0);
        var second = userService.getUserPage("Paging Fixture", 1);
        assertEquals(23, first.getTotalElements());
        assertEquals(10, first.getContent().size());
        assertEquals(10, second.getContent().size());
        assertTrue(first.getContent().stream().noneMatch(second.getContent()::contains));
        assertTrue(first.getContent().get(0).getId() > second.getContent().get(0).getId());
        assertEquals(3, userService.getUserPage("Paging Fixture", 99).getContent().size());
        assertEquals(0, userService.getUserPage("Paging Fixture", -1).getNumber());
        assertTrue(userService.getUserPage("no-such-paging-fixture", 99).isEmpty());
        mvc.perform(get("/").param("search", "Paging Fixture").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Страница 2 из 3")))
                .andExpect(content().string(containsString("search=Paging%20Fixture")))
                .andExpect(content().string(containsString("page=2")));
        mvc.perform(get("/").param("search", "no-such-paging-fixture"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Пользователи не найдены")));
    }
    @Test
    void modifyingRequestsRequireValidCsrfTokens() throws Exception {
        for (String path : List.of("/add-user", "/user-edit/1", "/user-delete/1")) {
            mvc.perform(post(path).param("email", "csrf@example.test").param("fullName", "Blocked"))
                    .andExpect(status().isForbidden());
            mvc.perform(post(path).with(csrf().useInvalidToken())
                            .param("email", "csrf@example.test").param("fullName", "Blocked"))
                    .andExpect(status().isForbidden());
        }
        assertEquals(original, userService.getUsers());
    }

    @Test
    void formsContainTokensAndValidTokensAllowEditingAndDeleting() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        var user = userService.addUser(new User(null, "csrf-test@example.test", "CSRF Test", null, null));
        mvc.perform(get("/user-details").param("userId", user.getId().toString()))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(post("/user-edit/" + user.getId()).with(csrf())
                        .param("email", "csrf-test@example.test").param("fullName", "Edited"))
                .andExpect(status().is3xxRedirection());
        assertEquals("Edited", userService.getUserById(user.getId()).getFullName());
        mvc.perform(post("/user-delete/" + user.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertNull(userService.getUserById(user.getId()));
    }
}
