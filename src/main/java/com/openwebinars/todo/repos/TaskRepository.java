package com.openwebinars.todo.repos;

import com.openwebinars.todo.model.Priority;
import com.openwebinars.todo.model.Task;
import com.openwebinars.todo.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAuthor(User author);

    @Query("SELECT DISTINCT t FROM Task t WHERE t.author = :author " +
           "AND (:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "AND (:completed IS NULL OR t.completed = :completed) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:importante IS NULL OR t.importante = :importante)")
    List<Task> search(@Param("author") User author,
                      @Param("title") String title,
                      @Param("completed") Boolean completed,
                      @Param("categoryId") Long categoryId,
                      @Param("priority") Priority priority,
                      @Param("importante") Boolean importante);

    @Query("SELECT DISTINCT t FROM Task t JOIN t.tags tag WHERE tag.id IN :tagIds AND t.author = :author")
    List<Task> findByAuthorAndTagsIn(@Param("author") User author, @Param("tagIds") List<Long> tagIds);
}
