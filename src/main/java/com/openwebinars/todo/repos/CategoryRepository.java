package com.openwebinars.todo.repos;

import com.openwebinars.todo.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
