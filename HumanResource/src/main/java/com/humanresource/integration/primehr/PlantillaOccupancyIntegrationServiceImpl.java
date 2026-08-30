package com.humanresource.integration.primehr;

import com.humanresource.entitymodels.EmployeeAppointment;
import com.humanresource.repositories.EmployeeAppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

@Service @Transactional(readOnly=true)
public class PlantillaOccupancyIntegrationServiceImpl implements PlantillaOccupancyIntegrationService {
    private final EmployeeAppointmentRepository appointments;
    public PlantillaOccupancyIntegrationServiceImpl(EmployeeAppointmentRepository a){appointments=a;}
    public PlantillaOccupancyResponse get(Long plantillaId){if(plantillaId==null||plantillaId<1)throw new IllegalArgumentException("plantillaId must be positive");
        EmployeeAppointment a=appointments.findTop1ByPlantillaIdAndActiveAppointmentTrueOrderByAssumptionToDutyDateDescEmployeeAppointmentIdDesc(plantillaId).orElse(null);Instant at=Instant.now();
        String raw=a==null?plantillaId+"|VACANT":String.join("|",plantillaId.toString(),a.getEmployeeAppointmentId().toString(),a.getEmployeeId().toString(),String.valueOf(a.getAssumptionToDutyDate()),String.valueOf(a.getActiveAppointment()));
        return new PlantillaOccupancyResponse(plantillaId,a!=null,a==null?null:a.getEmployeeAppointmentId(),a==null?null:a.getAssumptionToDutyDate(),sha(raw),at);}
    private static String sha(String s){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}
