package com.groundandgrow.service;

import com.groundandgrow.dto.SessionTypeDTO;
import com.groundandgrow.model.SessionType;
import com.groundandgrow.repository.SessionTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing session types
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionTypeService {

    private final SessionTypeRepository sessionTypeRepository;

    /**
     * Get all active session types
     */
    public List<SessionTypeDTO> getAllActiveSessionTypes() {
        List<SessionType> sessionTypes = sessionTypeRepository.findByIsActive(true);
        return sessionTypes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get session types by modality
     */
    public List<SessionTypeDTO> getSessionTypesByModality(String modality) {
        SessionType.Modality modalityEnum = SessionType.Modality.valueOf(modality.toUpperCase());
        List<SessionType> sessionTypes = sessionTypeRepository.findByModalityAndIsActive(modalityEnum, true);
        return sessionTypes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get session type by ID
     */
    public SessionTypeDTO getSessionTypeById(String id) {
        SessionType sessionType = sessionTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session type not found"));
        return convertToDTO(sessionType);
    }

    /**
     * Convert SessionType to DTO
     */
    private SessionTypeDTO convertToDTO(SessionType sessionType) {
        SessionTypeDTO dto = new SessionTypeDTO();
        dto.setId(sessionType.getId());
        dto.setName(sessionType.getName());
        dto.setDescription(sessionType.getDescription());
        dto.setDurationMinutes(sessionType.getDurationMinutes());
        dto.setPrice(sessionType.getPrice().doubleValue());
        dto.setModality(sessionType.getModality().name());
        dto.setIsActive(sessionType.getIsActive());
        return dto;
    }
}
