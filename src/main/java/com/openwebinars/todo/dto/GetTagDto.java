package com.openwebinars.todo.dto;

import com.openwebinars.todo.model.Tag;

public record GetTagDto(Long id, String name) {

    public static GetTagDto of(Tag t) {
        return new GetTagDto(t.getId(), t.getName());
    }
}
