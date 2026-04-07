package com.cts.ecotrack.service;

import com.cts.ecotrack.dao.UserRepository;
import com.cts.ecotrack.entity.Impact;
import com.cts.ecotrack.entity.Project;
import com.cts.ecotrack.entity.User;
import com.cts.ecotrack.dao.ProjectRepository;
import com.cts.ecotrack.dto.ImpactRequestDTO;
import com.cts.ecotrack.dto.ImpactSummaryDTO;
import com.cts.ecotrack.dto.GlobalImpactTotalsDTO;
import com.cts.ecotrack.dto.ProjectImpactDTO;
import com.cts.ecotrack.enums.Status;
import com.cts.ecotrack.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImpactService {

    private final com.cts.ecotrack.dao.ImpactRepository impactRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // ─── Global Impact Totals ────────────────────────────────────────────────
    // Powers: Impact page → 4 top KPI cards
    public GlobalImpactTotalsDTO getGlobalImpactTotals() {
        List<Impact> allImpacts = impactRepository.findAll();

        double totalCo2    = allImpacts.stream().mapToDouble(i -> extractMetric(i, "co2Reduced")).sum();
        double totalTrees  = allImpacts.stream().mapToDouble(i -> extractMetric(i, "treesPlanted")).sum();
        double totalEnergy = allImpacts.stream().mapToDouble(i -> extractMetric(i, "energySaved")).sum();
        double totalPeople = allImpacts.stream().mapToDouble(i -> extractMetric(i, "peopleImpacted")).sum();
        double totalWater  = allImpacts.stream().mapToDouble(i -> extractMetric(i, "waterConserved")).sum();

        return GlobalImpactTotalsDTO.builder()
                .co2ReducedTons(totalCo2)
                .treesPlanted((long) totalTrees)
                .energySavedKwh(totalEnergy)
                .peopleImpacted((long) totalPeople)
                .waterConservedLitres(totalWater)
                .build();
    }

    // ─── Project-wise Impact Analysis ────────────────────────────────────────
    // Powers: Impact page → "Project-wise Impact Analysis" table
    public List<ProjectImpactDTO> getProjectWiseImpact() {
        return projectRepository.findAll().stream()
                .map(project -> {
                    List<Impact> impacts = impactRepository.findByProject_ProjectId(project.getProjectId());

                    double co2    = impacts.stream().mapToDouble(i -> extractMetric(i, "co2Reduced")).sum();
                    double trees  = impacts.stream().mapToDouble(i -> extractMetric(i, "treesPlanted")).sum();
                    double energy = impacts.stream().mapToDouble(i -> extractMetric(i, "energySaved")).sum();
                    double water  = impacts.stream().mapToDouble(i -> extractMetric(i, "waterConserved")).sum();
                    double people = impacts.stream().mapToDouble(i -> extractMetric(i, "peopleImpacted")).sum();

                    return ProjectImpactDTO.builder()
                            .projectId(project.getProjectId())
                            .projectName(project.getTitle())
                            .co2ReducedTons(co2)
                            .treesPlanted((long) trees)
                            .energySavedKwh(energy)
                            .waterConservedLitres(water)
                            .peopleImpacted((long) people)
                            .build();
                })
                .filter(dto -> dto.getCo2ReducedTons() > 0
                        || dto.getTreesPlanted() > 0
                        || dto.getEnergyKwh() > 0
                        || dto.getPeopleImpacted() > 0)
                .collect(Collectors.toList());
    }

    // ─── Carbon Footprint Reduction Bar Chart ────────────────────────────────
    // Powers: Impact page → "Carbon Footprint Reduction" section
    public List<ProjectImpactDTO> getCarbonFootprintByProject() {
        List<ProjectImpactDTO> projectImpacts = getProjectWiseImpact();
        double total = projectImpacts.stream().mapToDouble(ProjectImpactDTO::getCo2ReducedTons).sum();

        projectImpacts.forEach(dto ->
                dto.setCo2Percentage(total > 0 ? (dto.getCo2ReducedTons() / total) * 100 : 0));

        return projectImpacts;
    }

    // ─── Community Reach Bar Chart ───────────────────────────────────────────
    // Powers: Impact page → "Community Reach" section
    public List<ProjectImpactDTO> getCommunityReachByProject() {
        List<ProjectImpactDTO> projectImpacts = getProjectWiseImpact();
        double total = projectImpacts.stream().mapToDouble(p -> p.getPeopleImpacted()).sum();

        projectImpacts.forEach(dto ->
                dto.setPeoplePercentage(total > 0 ? ((double) dto.getPeopleImpacted() / total) * 100 : 0));

        return projectImpacts;
    }

    // ─── Get Impact for a Single Project ────────────────────────────────────
    // Powers: ProjectDetail page → Impact section / tab
    public List<ImpactSummaryDTO> getImpactsByProject(Integer projectId) {
        //  Check project exists first
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }

        return impactRepository.findByProject_ProjectId(projectId).stream()
                .map(i -> ImpactSummaryDTO.builder()
                        .impactId(i.getImpactId())
                        .projectId(projectId)
                        .metricsJson(i.getMetricsJson())
                        .date(i.getDate())
                        .status(String.valueOf(i.getStatus()))
                        .build())
                .collect(Collectors.toList());

    }

    // ─── Record Impact Entry ─────────────────────────────────────────────────
    @Transactional
    public Impact recordImpact(ImpactRequestDTO dto) {
        // ResourceNotFoundException instead of IllegalArgumentException
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id: " + dto.getProjectId()));

        User manager = project.getProjectManager();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("co2Reduced",     dto.getCo2ReducedTons());
        metrics.put("treesPlanted",   dto.getTreesPlanted());
        metrics.put("energySaved",    dto.getEnergySavedKwh());
        metrics.put("waterConserved", dto.getWaterConservedLitres());
        metrics.put("peopleImpacted", dto.getPeopleImpacted());

        Impact impact = Impact.builder()
                .impactId(generateImpactId())
                .project(project)
                .projectManager(manager)
                .metricsJson(metrics)
                .date(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .status(String.valueOf(Status.valueOf(dto.getStatus() != null ? dto.getStatus() : "ACTIVE")))
                .build();

        return impactRepository.save(impact);
    }

    // ─── Update Impact Entry ─────────────────────────────────────────────────
    @Transactional
    public Impact updateImpact(Integer impactId, ImpactRequestDTO dto) {
        //  ResourceNotFoundException instead of IllegalArgumentException
        Impact impact = impactRepository.findById(impactId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Impact not found with id: " + impactId));

        Map<String, Object> metrics = impact.getMetricsJson() != null
                ? new HashMap<>(impact.getMetricsJson())
                : new HashMap<>();

        if (dto.getCo2ReducedTons() != null)      metrics.put("co2Reduced",     dto.getCo2ReducedTons());
        if (dto.getTreesPlanted() != null)         metrics.put("treesPlanted",   dto.getTreesPlanted());
        if (dto.getEnergySavedKwh() != null)       metrics.put("energySaved",    dto.getEnergySavedKwh());
        if (dto.getWaterConservedLitres() != null) metrics.put("waterConserved", dto.getWaterConservedLitres());
        if (dto.getPeopleImpacted() != null)       metrics.put("peopleImpacted", dto.getPeopleImpacted());

        impact.setMetricsJson(metrics);
        if (dto.getDate() != null)   impact.setDate(dto.getDate());
        if (dto.getStatus() != null) impact.setStatus(dto.getStatus());

        return impactRepository.save(impact);
    }

    // ─── Delete Impact Entry ─────────────────────────────────────────────────
    @Transactional
    public void deleteImpact(Integer impactId) {
        //  Check existence before deleting
        if (!impactRepository.existsById(impactId)) {
            throw new ResourceNotFoundException("Impact not found with id: " + impactId);
        }
        impactRepository.deleteById(impactId);
    }

    // ─── Helper: Extract Metric from JSON ────────────────────────────────────
    private double extractMetric(Impact impact, String key) {
        if (impact.getMetricsJson() == null) return 0;
        Object value = impact.getMetricsJson().get(key);
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try { return Double.parseDouble(value.toString()); }
        catch (NumberFormatException e) { return 0; }
    }

    // ─── Helper: Generate Impact ID ──────────────────────────────────────────
    private Integer generateImpactId() {
        return impactRepository.findMaxImpactId()
                .map(id -> id + 1)
                .orElse(1);
    }
}
