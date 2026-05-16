package com.openwebinars.todo.service;

import com.openwebinars.todo.dto.CreateCategoryDto;
import com.openwebinars.todo.error.CategoryNotFoundException;
import com.openwebinars.todo.model.Category;
import com.openwebinars.todo.repos.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    public Category save(CreateCategoryDto dto) {
        Category c = Category.builder()
                .title(dto.title())
                .build();
        return categoryRepository.save(c);
    }

    public Category edit(Long id, CreateCategoryDto dto) {
        return categoryRepository.findById(id)
                .map(c -> {
                    c.setTitle(dto.title());
                    return categoryRepository.save(c);
                })
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    public void delete(Long id) {
        if (!categoryRepository.existsById(id))
            throw new CategoryNotFoundException(id);
        categoryRepository.deleteById(id);
    }
}
