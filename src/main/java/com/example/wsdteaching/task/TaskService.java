package com.example.wsdteaching.task;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.example.wsdteaching.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final ConcurrentHashMap<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public Task create(TaskRequest request) {
        long id = sequence.incrementAndGet();
        Task task = new Task(id, request.title().strip(), request.completed());
        tasks.put(id, task);
        return task;
    }

    public List<Task> findAll() {
        return tasks.values().stream().sorted(Comparator.comparingLong(Task::id)).toList();
    }

    public Task update(long id, TaskRequest request) {
        return tasks.compute(id, (key, existing) -> {
            if (existing == null) {
                throw notFound();
            }
            return new Task(key, request.title().strip(), request.completed());
        });
    }

    public Task delete(long id) {
        Task deleted = tasks.remove(id);
        if (deleted == null) {
            throw notFound();
        }
        return deleted;
    }

    private ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "해당 할 일을 찾을 수 없습니다.");
    }
}
