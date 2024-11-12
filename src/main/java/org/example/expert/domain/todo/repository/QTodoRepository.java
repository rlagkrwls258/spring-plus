package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

public interface QTodoRepository {

    Optional<Todo> findByIdWithUser(Long todoId);

    Page<TodoSearchResponse> search(
            String keyword,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String managerName,
            Pageable pageable
    );

}
