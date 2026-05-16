package com.openwebinars.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

// DTO para asignar varios tags a una tarea de una sola vez
public record AddTagsCommand(
    @Schema(example = "[1, 2]") List<Long> tagIds
) {
}
