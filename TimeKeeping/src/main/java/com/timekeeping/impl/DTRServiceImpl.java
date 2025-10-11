package com.timekeeping.impl;

import com.timekeeping.dtos.DTRDTO;
import com.timekeeping.entitymodels.DTR;
import com.timekeeping.repositories.DTRRepository;
import com.timekeeping.services.DTRService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DTRServiceImpl implements DTRService {

    private static final Logger log = LoggerFactory.getLogger(DTRServiceImpl.class);

    private final DTRRepository dtrRepository;

    public DTRServiceImpl(DTRRepository dtrRepository) {
        this.dtrRepository = dtrRepository;
    }

    @Override
    public Boolean createEmployeeDTR(DTRDTO dtrdto) throws Exception {
        try {
            DTR dtr = new DTR(0L, dtrdto.getEmployeeId(), dtrdto.getDtrDate(), dtrdto.getWorkDate(), dtrdto.getTimeIn(),
                    dtrdto.getBreakOut(), dtrdto.getBreakIn(), dtrdto.getTimeOut(), dtrdto.getLateMin(), dtrdto.getUnderMin());
            dtrRepository.save(dtr);

            return true;
        } catch(Exception e) {
            log.error("Error createEmployeeDTR: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<DTRDTO> getEmployeeDTR(String employeeId, LocalDateTime fromDate, LocalDateTime toDate) {
        List<DTR> dtrList = dtrRepository.findByEmployeeIdAndDtrDateBetween(employeeId, fromDate, toDate).orElseThrow(() -> new RuntimeException("DTR not found"));;

        List<DTRDTO> dtrdtoList = new ArrayList<>();
        for(DTR dtr : dtrList) {
            DTRDTO dtrdto = new DTRDTO(dtr.getDtrId(), dtr.getEmployeeId(), dtr.getDtrDate(), dtr.getWorkDate(), dtr.getTimeIn(),
                    dtr.getBreakOut(), dtr.getBreakIn(), dtr.getTimeOut(), dtr.getLateMin(), dtr.getUnderMin());

            dtrdtoList.add(dtrdto);
        }

        return dtrdtoList;
    }
}