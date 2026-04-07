package com.cts.ecotrack.service;

import com.cts.ecotrack.dao.UserRepository;
import com.cts.ecotrack.dto.*;
import com.cts.ecotrack.entity.Project;
import com.cts.ecotrack.entity.Resource;
import com.cts.ecotrack.entity.User;
import com.cts.ecotrack.dao.ProjectRepository;
import com.cts.ecotrack.dao.ResourceRepository;
import com.cts.ecotrack.dao.EnvironmentalDataLogRepository;
import com.cts.ecotrack.entity.EnvironmentalDataLog;
import com.cts.ecotrack.exception.ResourceNotFoundException;
import com.cts.ecotrack.exception.BadRequestException;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final EnvironmentalDataLogRepository environmentalDataLogRepository;

    public DashboardKpiDTO getDashboardKpis() {
        long total     = projectRepository.count();
        long active    = projectRepository.countByStatus("Active");
        long delayed   = projectRepository.countByStatus("Delayed");
        long completed = projectRepository.countByStatus("Completed");

        return DashboardKpiDTO.builder()
                .totalProjects(total)
                .activeProjects(active)
                .delayedProjects(delayed)
                .completedProjects(completed)
                .build();
    }

    public List<ProjectSummaryDTO> getAllProjectSummaries() {
        return projectRepository.findAll().stream()
                .map(p -> ProjectSummaryDTO.builder()
                        .projectId(p.getProjectId())
                        .title(p.getTitle())
                        .status(p.getStatus())
                        .startDate(p.getStartDate())
                        .endDate(p.getEndDate())
                        .budget(p.getBudget())
                        .build())
                .collect(Collectors.toList());
    }


        @Transactional
        public void createProject(ProjectRequestDTO dto) {
            // 1. Fetch the Project Manager (Required)
            User manager = userRepository.findById(dto.getProjectManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Project manager not found with id: " + dto.getProjectManagerId()));

            // 2. LOGIC CHECK: At least one must be present
            if (dto.getResourceId() == null && dto.getEntryId() == null) {
                throw new BadRequestException("Project must be associated with either a Resource or an Environmental Data Log.");
            }

            if (dto.getResourceId() != null && dto.getEntryId() != null) {
                throw new BadRequestException("Project must be associated with either a Resource or an Environmental Data Log Put Any ONe.");
            }

            Resource resource = null;
            if (dto.getResourceId() != null) {
                resource = resourceRepository.findById(dto.getResourceId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Resource not found with id: " + dto.getResourceId()));
            }

            EnvironmentalDataLog dataLog = null;
            if (dto.getEntryId() != null) {
                dataLog = environmentalDataLogRepository.findById(dto.getEntryId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Environmental Data Log not found with id: " + dto.getEntryId()));
            }


            LocalDate start = LocalDate.parse(dto.getStartDate());
            LocalDate end   = LocalDate.parse(dto.getEndDate());
            if (end.isBefore(start)) {
                throw new BadRequestException("End date cannot be before start date");
            }


            Project project = Project.builder()
                    .projectId(generateProjectId())
                    .projectManager(manager)
                    .resource(resource)
                    .environmentalDataLog(dataLog)
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .startDate(start)
                    .endDate(end)
                    .budget(new BigDecimal(dto.getBudget()))
                    .status(dto.getStatus())
                    .build();

            projectRepository.save(project);
        }

    public Project getProjectById(Integer projectId) {
        //  ResourceNotFoundException instead of IllegalArgumentException
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id: " + projectId));
    }

    public ProjectResponseDTO getProjectDtoById(Integer id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id: " + id));

        return ProjectResponseDTO.builder()
                .title(project.getTitle())
                .description(project.getDescription())
                .budget(project.getBudget())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .build();
    }

    @Transactional
    public void updateProject(Integer projectId, String newStatus) {
        Project project = getProjectById(projectId);
        project.setStatus(newStatus);
        projectRepository.save(project);
    }

    @Transactional
    public void updateProject(Integer projectId, ProjectRequestDTO dto) {
        Project project = getProjectById(projectId);

        if (dto.getTitle() != null)       project.setTitle(dto.getTitle());
        if (dto.getDescription() != null) project.setDescription(dto.getDescription());
        if (dto.getStartDate() != null)   project.setStartDate(LocalDate.parse(dto.getStartDate()));
        if (dto.getEndDate() != null)     project.setEndDate(LocalDate.parse(dto.getEndDate()));
        if (dto.getBudget() != null)      project.setBudget(new BigDecimal(dto.getBudget()));
        if (dto.getStatus() != null)      project.setStatus(dto.getStatus());


        if (project.getEndDate().isBefore(project.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        projectRepository.save(project);
    }


    @Transactional
    public void deleteProject(Integer projectId) {

        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        projectRepository.deleteById(projectId);
    }


    public List<ProjectStatusDTO> getProjectsByStatus(String status) {
        return projectRepository.findByStatus(status)
                .stream()
                .map(project -> new ProjectStatusDTO(
                        project.getProjectId(),
                        project.getTitle()
                ))
                .collect(Collectors.toList());
    }



    public List<ProposedProjectDTO> getUpcomingProjects() {
        List<ProposedProjectDTO> proposals = new ArrayList<>();

        // 1. Process Environmental Data Logs
        List<EnvironmentalDataLog> logs = environmentalDataLogRepository.findByStatus("Verified");
        for (EnvironmentalDataLog log : logs) {
            String priority = "Low";

            if (log.getValue() != null) {
                double rawVal = log.getValue().doubleValue();
                String type = log.getType() != null ? log.getType().toString().toLowerCase() : "";


                double normalizedVal;
                if (type.contains("soil")) {
                    normalizedVal = (rawVal / 14.0) * 100;
                } else if (type.contains("water")) {
                    normalizedVal = (rawVal / 1000.0) * 100;
                } else if (type.contains("air")) {
                    normalizedVal = (rawVal / 300.0) * 100;
                } else {
                    normalizedVal = rawVal;
                }

                if (normalizedVal > 75) priority = "High";
                else if (normalizedVal > 50) priority = "Medium";
            }

            String requesterInfo = "System";
            if (log.getOfficerEnvironmental() != null) {
                User u = log.getOfficerEnvironmental();
                requesterInfo = String.format("%s (%s)", u.getName(), u.getRole());
            }

            proposals.add(ProposedProjectDTO.builder()
                    .projectType(log.getType().toString())
                    .requestedBy(requesterInfo)
                    .priority(priority)
                    .build());
        }

        List<Resource> resources = resourceRepository.findAll();
        for (Resource res : resources) {
            String requesterInfo = "Resource Officer";
            if (res.getOfficerPollutionControl() != null) {
                User u = res.getOfficerPollutionControl();
                requesterInfo = String.format("%s (%s)", u.getName(), u.getRole());
            }

            proposals.add(ProposedProjectDTO.builder()
                    .projectType(res.getType().toString())
                    .requestedBy(requesterInfo)
                    .priority(res.getStatus())
                    .build());
        }

        return proposals;
    }


    private Integer generateProjectId() {
        return projectRepository.findMaxProjectId()
                .map(id -> id + 1)
                .orElse(1);
    }
}
