package com.openwebinars.todo.model;

import com.openwebinars.todo.users.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // cada tag pertenece a un usuario
    @ManyToOne
    private User author;
}
