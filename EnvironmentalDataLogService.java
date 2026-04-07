package com.cts.ecotrack.service;

import com.cts.ecotrack.dto.*;
import com.cts.ecotrack.entity.*;
import com.cts.ecotrack.enums.EnvironmentalDataLogType;
import com.cts.ecotrack.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnvironmentalDataLogService {

    @Autowired private EnvironmentalDataLogRepository logRepo;
    @Autowired private DocumentUploadRepository docRepo;
    @Autowired private UserRepository userRepo;

    public EnvironmentalDataLog createLog(LogRequestDTO dto) {
        validateRange(dto.getType(), dto.getValue());
        return saveMapping(new EnvironmentalDataLog(), dto);
    }

    public EnvironmentalDataLog updateLog(Integer id, LogRequestDTO dto) {
        EnvironmentalDataLog existing = logRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Log not found: " + id));
        validateRange(dto.getType(), dto.getValue());
        return saveMapping(existing, dto);
    }


    public void deleteLog(Integer id) {
        logRepo.deleteById(id);
    }


    public DashboardStatsDTO getDashboardStats() {
        List<EnvironmentalDataLog> all = logRepo.findAll();
        long verified = all.stream().filter(l -> "Verified".equalsIgnoreCase(l.getStatus())).count();
        long pending = all.size() - verified;

        return DashboardStatsDTO.builder()
                .totalEntries(all.size())
                .verifiedCount(verified)
                .pendingCount(pending)
                .rejectedCount(0L)
                .build();
    }

    @Transactional
    public void verifyLogStatus(Integer entryId) {
        logRepo.findById(entryId).ifPresent(log -> {
            long docCount = docRepo.countByEnvironmentalDataLog_EntryId(entryId);
            if (docCount > 0) {
                log.setStatus("Verified");
                logRepo.saveAndFlush(log); // This updates the DB column
            }
        });
    }

    @Transactional
    private EnvironmentalDataLog saveMapping(EnvironmentalDataLog log, LogRequestDTO dto) {
        log.setType(EnvironmentalDataLogType.valueOf(dto.getType().toUpperCase()));
        log.setLocation(dto.getLocation());
        log.setValue(BigDecimal.valueOf(dto.getValue()));
        if (log.getDate() == null) log.setDate(LocalDateTime.now());

        userRepo.findById(dto.getCitizenId()).ifPresent(log::setCitizen);
        if (dto.getOfficerId() != null) {
            userRepo.findById(dto.getOfficerId()).ifPresent(log::setOfficerEnvironmental);
        }

        EnvironmentalDataLog savedLog = logRepo.save(log);
        long docCount = docRepo.countByEnvironmentalDataLog_EntryId(savedLog.getEntryId());
        savedLog.setStatus(docCount > 0 ? "Verified" : "Pending");
        return logRepo.save(savedLog);
    }

    public List<LogResponseDTO> getFilteredLogs(Integer id, String loc, String type, String status) {
        return logRepo.findAll().stream()
                .map(this::mapToDTO)
                .filter(l -> id == null || l.getEntryId().equals(id))
                .filter(l -> loc == null || l.getLocation().toLowerCase().contains(loc.toLowerCase()))
                .filter(l -> type == null || l.getType().equalsIgnoreCase(type))
                .filter(l -> status == null || l.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    private LogResponseDTO mapToDTO(EnvironmentalDataLog log) {
        long documentcount = docRepo.countByEnvironmentalDataLog_EntryId(log.getEntryId());
        return LogResponseDTO.builder()
                .entryId(log.getEntryId())
                .date(log.getDate())
                .location(log.getLocation())
                .type(log.getType().name())
                .value(log.getValue().toString())
                .status(log.getStatus() != null ? log.getStatus() : "Pending")
                .documentCount(documentcount)
                .officerId(log.getOfficerEnvironmental() != null ? log.getOfficerEnvironmental().getUserId() : null)
                .officerName(log.getOfficerEnvironmental() != null ? log.getOfficerEnvironmental().getName() : "N/A")
                .build();
    }

    private void validateRange(String type, Double val) {
        String t = type.toUpperCase();
        if (t.contains("AIR") && (val < 0 || val > 300)) throw new IllegalArgumentException("Air: 0-300 AQI");
        if (t.contains("WATER") && (val < 0 || val > 1000)) throw new IllegalArgumentException("Water: 0-1000 ppm");
        if (t.contains("SOIL") && (val < 0 || val > 14)) throw new IllegalArgumentException("Soil: 0-14 pH");
    }
}