package com.openwebinars.todo.service;

import com.openwebinars.todo.dto.CreateTagDto;
import com.openwebinars.todo.error.TagNotFoundException;
import com.openwebinars.todo.model.Tag;
import com.openwebinars.todo.repos.TagRepository;
import com.openwebinars.todo.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<Tag> findByAuthor(User author) {
        return tagRepository.findByAuthor(author);
    }

    public Tag findById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));
    }

    public Tag save(CreateTagDto dto, User author) {
        Tag tag = Tag.builder()
                .name(dto.name())
                .author(author)
                .build();
        return tagRepository.save(tag);
    }

    public Tag edit(Long id, CreateTagDto dto, User author) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));

        if (!tag.getAuthor().getId().equals(author.getId()))
            throw new AccessDeniedException("No puedes editar este tag");

        tag.setName(dto.name());
        return tagRepository.save(tag);
    }

    public void delete(Long id, User author) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));

        if (!tag.getAuthor().getId().equals(author.getId()))
            throw new AccessDeniedException("No puedes eliminar este tag");

        tagRepository.delete(tag);
    }
}
