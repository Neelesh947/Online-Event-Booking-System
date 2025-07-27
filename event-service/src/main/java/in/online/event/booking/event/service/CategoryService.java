package in.online.event.booking.event.service;

import java.util.List;

import in.online.event.booking.event.entity.Category;

public interface CategoryService {

	Category createCategory(Category category);

	List<Category> getAllCategories();

	Category getCategoryById(String id);

	void deleteCategory(String id);

}
