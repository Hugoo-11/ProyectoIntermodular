package com.openwebinars.todo.controller;

import com.openwebinars.todo.dto.GetCategoryDto;
import com.openwebinars.todo.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// endpoint para que cualquier usuario autenticado pueda ver las categorias
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Listar categorias", description = "Cualquier usuario puede ver las categorias disponibles")
    @GetMapping
    public List<GetCategoryDto> getAll() {
        return categoryService.findAll().stream()
                .map(GetCategoryDto::of)
                .toList();
    }
}
