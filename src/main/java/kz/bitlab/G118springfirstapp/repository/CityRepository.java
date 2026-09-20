package kz.bitlab.G118springfirstapp.repository;

import java.util.List;
import kz.bitlab.G118springfirstapp.model.City;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findAllByOrderByIdAsc();
}
