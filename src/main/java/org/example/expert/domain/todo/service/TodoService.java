package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.config.authuser.AuthUserDetails;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;

    @Transactional
    public TodoSaveResponse saveTodo(AuthUserDetails authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail())
        );
    }

    public Page<TodoResponse> getTodos(int page, int size, String weather, String startDate, String endDate) {
        Pageable pageable = PageRequest.of(page - 1, size);

//        Page<Todo> todos = todoRepository.findAllByOrderByModifiedAtDesc(pageable);

        //String타입의 날짜 형식을 LocalDateTime으로 변환
        LocalDateTime startDateTime = (startDate != null) ? LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE).atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE).atTime(23, 59, 59) : null;

        Page<Todo> todos = todoRepository.findByWeatherAndModifedAt(weather, startDateTime, endDateTime, pageable);

        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    public TodoResponse getTodo(long todoId) {
        Todo todo = todoRepository.findByIdWithUser(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));

        User user = todo.getUser();

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(user.getId(), user.getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        );
    }

    public Page<TodoSearchResponse> searchTodos(int page, int size, String keyword, String startDate, String endDate, String managerName) {

        Pageable pageable = PageRequest.of(page - 1, size);

        //String타입의 날짜 형식을 LocalDateTime으로 변환
        LocalDateTime startDateTime = (startDate != null) ? LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE).atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE).atTime(23, 59, 59) : null;

        return todoRepository.search(keyword, startDateTime, endDateTime, managerName, pageable);
    }
}
