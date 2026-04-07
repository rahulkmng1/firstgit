package com.cts.ecotrack.util;

import com.cts.ecotrack.dto.PollutionSourceDTO;
import com.cts.ecotrack.dto.ResourcesDTO;
import com.cts.ecotrack.entity.PollutionSource;
import com.cts.ecotrack.entity.Resource;
import com.cts.ecotrack.entity.User;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    public PollutionSourceDTO mapToPollutionDTO(PollutionSource source) {
        return PollutionSourceDTO.builder()
                .sourceId(source.getSourceId())
                // Even if the User object is Lazy-Loaded or Ignored,
                // the ID should be accessible if the relationship is set.
                .officerId(source.getOfficerPollutionControl() != null ?
                        source.getOfficerPollutionControl().getUserId() : null)
                .type(source.getType())
                .location(source.getLocation())
                .status(source.getStatus())
                .build();
    }

    public ResourcesDTO mapToResourceDTO(Resource resource) {
        return ResourcesDTO.builder()
                .resourceId(resource.getResourceId())
                .officerId(resource.getOfficerPollutionControl() != null ? resource.getOfficerPollutionControl().getUserId() : null)
                .type(resource.getType())
                .capacity(resource.getCapacity())
                .usages(resource.getUsages())
                .status(resource.getStatus())
                .build();
    }

    public PollutionSource mapToPollutionEntity(PollutionSourceDTO dto, User officer) {
        return PollutionSource.builder()
                .sourceId(dto.getSourceId())
                .officerPollutionControl(officer) // This sets the FK!
                .type(dto.getType())
                .location(dto.getLocation())
                .status(dto.getStatus())
                .build();
    }

    public Resource mapToResourceEntity(ResourcesDTO dto, User officer) {
        return Resource.builder()
                .resourceId(dto.getResourceId())
                .officerPollutionControl(officer)
                .type(dto.getType())
                .capacity(dto.getCapacity())
                .usages(dto.getUsages())
                .status(dto.getStatus())
                .build();
    }
}