package com.payroll.services;

import com.payroll.dtos.AnnualizationEmployeeDTO;
import com.payroll.dtos.Bir2316ReportDTO;
import java.io.OutputStream;

public interface Bir2316ReportService {
    Bir2316ReportDTO prepareReportData(AnnualizationEmployeeDTO annualization);

    void generatePdf(AnnualizationEmployeeDTO annualization, String performedBy, OutputStream output)
            throws Exception;
}
