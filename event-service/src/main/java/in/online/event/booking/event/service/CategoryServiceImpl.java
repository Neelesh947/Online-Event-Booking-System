package in.online.event.booking.event.service;

import java.util.List;

import org.springframework.stereotype.Service;

import in.online.event.booking.event.entity.Category;
import in.online.event.booking.event.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;

	public Category createCategory(Category category) {
		return categoryRepository.save(category);
	}

	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	public Category getCategoryById(String id) {
		return categoryRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + id));
	}

	public void deleteCategory(String id) {
		if (!categoryRepository.existsById(id)) {
			throw new EntityNotFoundException("Category not found with ID: " + id);
		}
		categoryRepository.deleteById(id);
	}
}
