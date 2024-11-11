package org.example.expert.domain.todo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;


import java.util.Optional;

@RequiredArgsConstructor
public class QTodoRepositoryImpl implements QTodoRepository{
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId){
        QTodo todo = QTodo.todo;
        Todo result = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();
        return Optional.ofNullable(result);
    }
}
