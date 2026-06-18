package com.crane.dispatch.controller;

import com.crane.dispatch.dto.CollisionWarningDto;
import com.crane.dispatch.dto.CranePositionDto;
import com.crane.dispatch.dto.DispatchTaskRequest;
import com.crane.dispatch.dto.HistoryPointDto;
import com.crane.dispatch.entity.CraneTask;
import com.crane.dispatch.service.CraneDispatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CraneDispatchController {

    private final CraneDispatchService service;

    public CraneDispatchController(CraneDispatchService service) {
        this.service = service;
    }

    @GetMapping("/positions")
    public List<CranePositionDto> getPositions() {
        return service.getAllPositions();
    }

    @GetMapping("/collision-check")
    public CollisionWarningDto checkCollision() {
        return service.checkCollision();
    }

    @PostMapping("/dispatch")
    public ResponseEntity<?> dispatchTask(@RequestBody DispatchTaskRequest request) {
        try {
            CraneTask task = service.dispatchTask(request);
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/tasks")
    public List<CraneTask> getTasks(@RequestParam(required = false) String craneId) {
        return service.getTaskHistory(craneId);
    }

    @GetMapping("/v1/crane/history")
    public List<HistoryPointDto> getHistory(@RequestParam(required = false, defaultValue = "10") Integer minutes) {
        return service.getPositionHistory(minutes);
    }
}
