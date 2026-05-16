package com.openwebinars.todo.dto;

import com.openwebinars.todo.model.Priority;
import com.openwebinars.todo.model.Task;
import com.openwebinars.todo.users.NewUserResponse;

import java.time.LocalDateTime;
import java.util.List;

// DTO de respuesta para tareas: evita exponer la entidad JPA y oculta datos internos
public record GetTaskDto(
        Long id,
        String title,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deadline,
        boolean completed,
        Priority priority,
        boolean importante,
        NewUserResponse author,
        GetCategoryDto category,
        List<GetTagDto> tags
) {
    public static GetTaskDto of(Task t) {
        return new GetTaskDto(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getCreatedAt(),
                t.getUpdatedAt(),
                t.getDeadline(),
                t.isCompleted(),
                t.getPriority(),
                t.isImportante(),
                NewUserResponse.of(t.getAuthor()),
                t.getCategory() != null ? GetCategoryDto.of(t.getCategory()) : null,
                t.getTags().stream().map(GetTagDto::of).toList()
        );
    }
}
