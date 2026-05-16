package com.openwebinars.todo.dto;

import com.openwebinars.todo.model.Priority;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// DTO de entrada para crear o editar una tarea, separa los datos del cliente de la entidad JPA
public record EditTaskCommand(
    String title,
    String description,
    LocalDateTime deadline,
    boolean completed,
    Priority priority,
    boolean importante,
    @Schema(example = "1") Long categoryId
){
}
