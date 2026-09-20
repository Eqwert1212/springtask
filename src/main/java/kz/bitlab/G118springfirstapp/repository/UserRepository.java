package kz.bitlab.G118springfirstapp.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import kz.bitlab.G118springfirstapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
            String email, String fullName, Pageable pageable);

    List<User> findAllByOrderByIdAsc();
    List<User> findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrderByFullNameDescIdDesc(
            String email, String fullName);
}
