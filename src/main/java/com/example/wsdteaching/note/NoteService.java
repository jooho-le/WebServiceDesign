package com.example.wsdteaching.note;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.example.wsdteaching.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class NoteService {

    private final ConcurrentHashMap<Long, Note> notes = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public Note create(NoteRequest request) {
        long id = sequence.incrementAndGet();
        Note note = new Note(id, request.title().strip(), request.content().strip());
        notes.put(id, note);
        return note;
    }

    public List<Note> findAll() {
        return notes.values().stream().sorted(Comparator.comparingLong(Note::id)).toList();
    }

    public Note update(long id, NoteRequest request) {
        return notes.compute(id, (key, existing) -> {
            if (existing == null) {
                throw notFound();
            }
            return new Note(key, request.title().strip(), request.content().strip());
        });
    }

    public Note delete(long id) {
        Note deleted = notes.remove(id);
        if (deleted == null) {
            throw notFound();
        }
        return deleted;
    }

    private ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "해당 메모를 찾을 수 없습니다.");
    }
}
