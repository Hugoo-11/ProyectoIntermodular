package com.openwebinars.todo.repos;

import com.openwebinars.todo.model.Tag;
import com.openwebinars.todo.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByAuthor(User author);
}
