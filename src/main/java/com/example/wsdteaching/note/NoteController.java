package com.example.wsdteaching.note;

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
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;
    private final FailureSimulator failureSimulator;

    public NoteController(NoteService noteService, FailureSimulator failureSimulator) {
        this.noteService = noteService;
        this.failureSimulator = failureSimulator;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Note>> create(@Valid @RequestBody NoteRequest request) {
        Note note = noteService.create(request);
        return ResponseEntity.created(URI.create("/api/notes/" + note.id()))
                .body(ApiResponse.success(note));
    }

    @GetMapping
    public ApiResponse<List<Note>> findAll(@RequestParam(defaultValue = "none") String simulate) {
        failureSimulator.check(simulate);
        return ApiResponse.success(noteService.findAll());
    }

    @PutMapping("/{id}")
    public ApiResponse<Note> update(
            @PathVariable @Positive(message = "id는 양의 정수여야 합니다.") long id,
            @Valid @RequestBody NoteRequest request) {
        return ApiResponse.success(noteService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Note> delete(@PathVariable @Positive(message = "id는 양의 정수여야 합니다.") long id) {
        return ApiResponse.success(noteService.delete(id));
    }
}
