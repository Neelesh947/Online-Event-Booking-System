package in.online.event.booking.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.online.event.booking.event.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String>{

	boolean existsByName(String name);
}
