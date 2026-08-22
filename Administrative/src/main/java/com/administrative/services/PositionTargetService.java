package com.administrative.services;

import com.administrative.dtos.PositionTargetPageResponse;
import com.administrative.dtos.PositionTargetResponse;
import com.administrative.dtos.PositionTargetType;

public interface PositionTargetService {
    PositionTargetPageResponse list(PositionTargetType type, String search, int page, int size);

    PositionTargetResponse get(PositionTargetType type, Long id);
}
