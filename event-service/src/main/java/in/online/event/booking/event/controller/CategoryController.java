package in.online.event.booking.event.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.online.event.booking.event.entity.Category;
import in.online.event.booking.event.service.CategoryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/{realm}/category")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	@PreAuthorize("hasRole('SUPER_ADMIN')")
	@PostMapping
	public ResponseEntity<Category> createCategory(@PathVariable String realm, @RequestBody Category category) {
		Category response = categoryService.createCategory(category);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<Category>> getAllCategories(@PathVariable String realm) {
		List<Category> categories = categoryService.getAllCategories();
		return ResponseEntity.ok(categories);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Category> getCategoryById(@PathVariable String realm, @PathVariable String id) {
		Category category = categoryService.getCategoryById(id);
		return ResponseEntity.ok(category);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole(SUPER_ADMIN)")
	public ResponseEntity<Void> deleteCategory(@PathVariable String realm, @PathVariable String id) {
		categoryService.deleteCategory(id);
		return ResponseEntity.noContent().build();
	}
}
