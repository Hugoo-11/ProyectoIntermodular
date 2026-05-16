package com.openwebinars.todo.dto;

import com.openwebinars.todo.model.Category;

public record GetCategoryDto(Long id, String title) {

    public static GetCategoryDto of(Category c) {
        return new GetCategoryDto(c.getId(), c.getTitle());
    }
}
