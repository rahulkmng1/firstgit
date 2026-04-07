package com.cts.ecotrack.service;

import com.cts.ecotrack.dao.UserRepository;
import com.cts.ecotrack.entity.Milestone;
import com.cts.ecotrack.entity.Project;
import com.cts.ecotrack.entity.User;
import com.cts.ecotrack.dao.MilestoneRepository;
import com.cts.ecotrack.dao.ProjectRepository;
import com.cts.ecotrack.dto.MilestoneRequestDTO;
import com.cts.ecotrack.dto.MilestoneListDTO;
import com.cts.ecotrack.exception.ResourceNotFoundException;
import com.cts.ecotrack.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // ─── Get All Milestones ──────────────────────────────────────────────────
    public List<MilestoneListDTO> getAllMilestones() {
        return milestoneRepository.findAllWithProject().stream()
                .map(m -> MilestoneListDTO.builder()
                        .milestoneId(m.getMilestoneId())
                        .title(m.getTitle())
                        .projectId(m.getProject() != null ? m.getProject().getProjectId() : null)
                        .projectName(m.getProject() != null ? m.getProject().getTitle() : "—")
                        .dueDate(m.getDate())
                        .status(m.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    // ─── Get Milestones by Project ───────────────────────────────────────────
    public List<MilestoneListDTO> getMilestonesByProject(Integer projectId) {
        //  Check project exists first
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        return milestoneRepository.findByProject_ProjectId(projectId).stream()
                .map(m -> MilestoneListDTO.builder()
                        .milestoneId(m.getMilestoneId())
                        .title(m.getTitle())
                        .projectId(projectId)
                        .projectName(m.getProject().getTitle())
                        .dueDate(m.getDate())
                        .status(m.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    // ─── Add New Milestone ───────────────────────────────────────────────────
    @Transactional
    public Milestone createMilestone(MilestoneRequestDTO dto) {
        //  ResourceNotFoundException instead of IllegalArgumentException
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id: " + dto.getProjectId()));

        //  BadRequestException for past due date
        LocalDate dueDate = LocalDate.parse(dto.getDueDate());
        if (dueDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Milestone due date cannot be in the past");
        }

        User manager = project.getProjectManager();

        Milestone milestone = Milestone.builder()
                .milestoneId(generateMilestoneId())
                .project(project)
                .projectManager(manager)
                .title(dto.getTitle())
                .date(dueDate)
                .status(dto.getStatus())
                .build();

        return milestoneRepository.save(milestone);
    }

    // ─── Update Milestone Status ─────────────────────────────────────────────
    @Transactional
    public Milestone updateMilestoneStatus(Integer milestoneId, String newStatus) {
        Milestone milestone = getMilestoneById(milestoneId);
        milestone.setStatus(newStatus);
        return milestoneRepository.save(milestone);
    }

    // ─── Update Milestone ────────────────────────────────────────────────────
    @Transactional
    public MilestoneListDTO updateMilestone(Integer milestoneId, MilestoneRequestDTO dto) {
        Milestone milestone = getMilestoneById(milestoneId);

        // Apply changes
        if (dto.getTitle() != null)   milestone.setTitle(dto.getTitle());
        if (dto.getDueDate() != null) milestone.setDate(LocalDate.parse(dto.getDueDate()));
        if (dto.getStatus() != null)  milestone.setStatus(dto.getStatus());

        if (dto.getProjectId() != null) {
            Project project = projectRepository.findById(dto.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
            milestone.setProject(project);
            milestone.setProjectManager(project.getProjectManager());
        }

        Milestone updated = milestoneRepository.save(milestone);

        // Transform and return
        return MilestoneListDTO.builder()
                .milestoneId(updated.getMilestoneId())
                .title(updated.getTitle())
                .projectId(updated.getProject().getProjectId())
                .projectName(updated.getProject().getTitle())
                .dueDate(updated.getDate()) // Mapping entity 'date' to DTO 'dueDate'
                .status(updated.getStatus())
                .build();
    }

    // ─── Delete Milestone ────────────────────────────────────────────────────
    @Transactional
    public void deleteMilestone(Integer milestoneId) {
        // Check existence before deleting
        if (!milestoneRepository.existsById(milestoneId)) {
            throw new ResourceNotFoundException("Milestone not found with id: " + milestoneId);
        }
        milestoneRepository.deleteById(milestoneId);
    }

    // ─── Get Milestone by ID ─────────────────────────────────────────────────
    public Milestone getMilestoneById(Integer milestoneId) {
        //  ResourceNotFoundException instead of IllegalArgumentException
        return milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Milestone not found with id: " + milestoneId));
    }

    // ─── Helper: Generate Milestone ID ───────────────────────────────────────
    private Integer generateMilestoneId() {
        return milestoneRepository.findMaxMilestoneId()
                .map(id -> id + 1)
                .orElse(1);
    }

}
