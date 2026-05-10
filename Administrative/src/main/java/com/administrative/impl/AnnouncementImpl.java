package com.administrative.impl;

import com.administrative.dtos.AnnouncementDTO;
import com.administrative.entitymodels.Announcement;
import com.administrative.repositories.AnnouncementRepository;
import com.administrative.services.AnnouncementService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnnouncementImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementImpl(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    private AnnouncementDTO toDTO(Announcement entity) {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setAnnouncementId(entity.getAnnouncementId());
        dto.setEffectivityDate(entity.getEffectivityDate());
        dto.setEffectiveUntil(entity.getEffectiveUntil());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        return dto;
    }

    @Override
    public List<AnnouncementDTO> getAll() throws Exception {
        return announcementRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public AnnouncementDTO create(AnnouncementDTO dto) throws Exception {
        Announcement entity = new Announcement(
                null,
                dto.getEffectivityDate(),
                dto.getEffectiveUntil(),
                dto.getTitle(),
                dto.getContent()
        );
        return toDTO(announcementRepository.save(entity));
    }

    @Transactional
    @Override
    public AnnouncementDTO update(Long announcementId, AnnouncementDTO dto) throws Exception {
        Optional<Announcement> existing = announcementRepository.findById(announcementId);
        if (existing.isEmpty()) {
            throw new Exception("Announcement not found with id: " + announcementId);
        }
        Announcement entity = existing.get();
        entity.setEffectivityDate(dto.getEffectivityDate());
        entity.setEffectiveUntil(dto.getEffectiveUntil());
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        return toDTO(announcementRepository.save(entity));
    }

    @Transactional
    @Override
    public Boolean delete(Long announcementId) throws Exception {
        if (!announcementRepository.existsById(announcementId)) {
            return false;
        }
        announcementRepository.deleteById(announcementId);
        return true;
    }
}
