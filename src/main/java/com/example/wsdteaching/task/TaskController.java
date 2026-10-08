package com.example.wsdteaching.task;

import java.net.URI;
import java.util.List;

import com.example.wsdteaching.common.ApiResponse;
import com.example.wsdteaching.common.FailureSimulator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final FailureSimulator failureSimulator;

    public TaskController(TaskService taskService, FailureSimulator failureSimulator) {
        this.taskService = taskService;
        this.failureSimulator = failureSimulator;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Task>> create(@Valid @RequestBody TaskRequest request) {
        Task task = taskService.create(request);
        return ResponseEntity.created(URI.create("/api/tasks/" + task.id()))
                .body(ApiResponse.success(task));
    }

    @GetMapping
    public ApiResponse<List<Task>> findAll(@RequestParam(defaultValue = "none") String simulate) {
        failureSimulator.check(simulate);
        return ApiResponse.success(taskService.findAll());
    }

    @PutMapping("/{id}")
    public ApiResponse<Task> update(
            @PathVariable @Positive(message = "id는 양의 정수여야 합니다.") long id,
            @Valid @RequestBody TaskRequest request) {
        return ApiResponse.success(taskService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Task> delete(@PathVariable @Positive(message = "id는 양의 정수여야 합니다.") long id) {
        return ApiResponse.success(taskService.delete(id));
    }
}
