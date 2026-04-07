package com.cts.ecotrack.controller;

import com.cts.ecotrack.dto.ResourcesDTO;
import com.cts.ecotrack.exception.ResourcesNotFound;
import com.cts.ecotrack.service.ResourcesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourcesController {

    private final ResourcesService service;

    @Autowired
    public ResourcesController(ResourcesService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ResourcesDTO> createResource(@RequestBody ResourcesDTO dto) throws ResourcesNotFound {
        // Pass the DTO directly to the service
        ResourcesDTO savedDto = service.saveOrUpdateResource(dto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ResourcesDTO>> getAll() {
        // No stream needed here if service returns List<ResourcesDTO>
        return ResponseEntity.ok(service.getAllResources());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourcesDTO> getById(@PathVariable Integer id) throws ResourcesNotFound {
        return ResponseEntity.ok(service.getResourceById(id));
    }

    @PatchMapping("/{id}/usage")
    public ResponseEntity<ResourcesDTO> updateUsage(@PathVariable Integer id, @RequestParam BigDecimal amount) throws ResourcesNotFound {
        return ResponseEntity.ok(service.updateUsage(id, amount));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) throws ResourcesNotFound {
        service.deleteResource(id);
        return ResponseEntity.ok("Resource deleted.");
    }
}