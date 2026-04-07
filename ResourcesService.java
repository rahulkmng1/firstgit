package com.cts.ecotrack.service;

import com.cts.ecotrack.dao.PollutionSourceRepository;
import com.cts.ecotrack.dao.ResourcesRepository;
import com.cts.ecotrack.dao.UserRepository;
import com.cts.ecotrack.dto.ResourcesDTO;
import com.cts.ecotrack.entity.Resource;
import com.cts.ecotrack.entity.User;
import com.cts.ecotrack.exception.ResourcesNotFound;
import com.cts.ecotrack.util.EntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourcesService {

    private final ResourcesRepository repository;
    private final UserRepository userRepository;
    private final EntityMapper mapper;

    @Autowired
    public ResourcesService(ResourcesRepository repository, EntityMapper mapper, UserRepository userRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    public ResourcesDTO saveOrUpdateResource(ResourcesDTO dto) throws ResourcesNotFound {
        // Business Validation
        if (dto.getUsages().compareTo(dto.getCapacity()) > 0) {
            throw new ResourcesNotFound("Usage cannot exceed the total capacity.");
        }

        User officer = userRepository.getReferenceById(dto.getOfficerId());

        Resource entity = mapper.mapToResourceEntity(dto, officer);
        Resource saved = repository.save(entity);
        return mapper.mapToResourceDTO(saved);
    }

    public List<ResourcesDTO> getAllResources() {
        return repository.findAll().stream()
                .map(mapper::mapToResourceDTO)
                .collect(Collectors.toList());
    }

    public ResourcesDTO getResourceById(Integer id) throws ResourcesNotFound {
        Resource resource = repository.findById(id)
                .orElseThrow(() -> new ResourcesNotFound("Resource not found with ID: " + id));
        return mapper.mapToResourceDTO(resource);
    }

    public ResourcesDTO updateUsage(Integer id, BigDecimal newUsage) throws ResourcesNotFound {
        Resource resource = repository.findById(id)
                .orElseThrow(() -> new ResourcesNotFound("Resource not found with ID: " + id));

        // Logic check
        if (newUsage.compareTo(resource.getCapacity()) > 0) {
            throw new ResourcesNotFound("New usage exceeds capacity.");
        }

        resource.setUsages(newUsage);
        Resource updated = repository.save(resource);
        return mapper.mapToResourceDTO(updated);
    }

    public void deleteResource(Integer id) throws ResourcesNotFound {
        if (!repository.existsById(id)) {
            throw new ResourcesNotFound("Cannot delete: Resource not found.");
        }
        repository.deleteById(id);
    }
}