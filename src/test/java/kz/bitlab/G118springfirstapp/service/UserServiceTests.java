package kz.bitlab.G118springfirstapp.service;

import jakarta.persistence.EntityManager;
import kz.bitlab.G118springfirstapp.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTests {
    @Autowired UserService service;
    @Autowired EntityManager entityManager;

    @Test
    void createsEditsSearchesAndDeletesPersistedUserWithoutAffectingSeedUsers() {
        int initialCount = service.getUsers().size();
        User first = service.addUser(new User(null, "first@example.test", "First", null, null));
        User second = service.addUser(new User(null, "second@example.test", "Second", null, null));
        Long id = first.getId();
        assertNotEquals(id, second.getId());
        assertTrue(id > 6);
        entityManager.flush();
        entityManager.clear();
        assertEquals("First", service.getUserById(id).getFullName());
        service.editUser(id, "updated@example.test", "Updated Name", 1L);
        entityManager.flush();
        entityManager.clear();
        assertEquals(1L, service.getUserById(id).getCity().getId());
        assertTrue(service.findUsers("UPDATED").stream().anyMatch(u -> u.getId().equals(id)));
        service.deleteUserById(id);
        service.deleteUserById(second.getId());
        entityManager.flush();
        entityManager.clear();
        assertNull(service.getUserById(id));
        assertEquals(initialCount, service.getUsers().size());
    }
}
