package com.administrative.services;

import com.administrative.dtos.AnnouncementDTO;

import java.util.List;

public interface AnnouncementService {

    List<AnnouncementDTO> getAll() throws Exception;

    AnnouncementDTO create(AnnouncementDTO dto) throws Exception;

    AnnouncementDTO update(Long announcementId, AnnouncementDTO dto) throws Exception;

    Boolean delete(Long announcementId) throws Exception;
}
