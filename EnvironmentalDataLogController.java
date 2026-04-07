package com.cts.ecotrack.controller;

import com.cts.ecotrack.dto.DashboardStatsDTO;
import com.cts.ecotrack.dto.LogRequestDTO;
import com.cts.ecotrack.dto.LogResponseDTO;
import com.cts.ecotrack.service.EnvironmentalDataLogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/logs")
public class EnvironmentalDataLogController {

    @Autowired
    private EnvironmentalDataLogService logService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody LogRequestDTO dto) {
        return ResponseEntity.ok(logService.createLog(dto));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody LogRequestDTO dto) {
        return ResponseEntity.ok(logService.updateLog(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        logService.deleteLog(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/view")
    public ResponseEntity<List<LogResponseDTO>> viewLogs(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(logService.getFilteredLogs(id, location, type, status));
    }
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        // This will now correctly return the DTO object
        return ResponseEntity.ok(logService.getDashboardStats());
    }
}