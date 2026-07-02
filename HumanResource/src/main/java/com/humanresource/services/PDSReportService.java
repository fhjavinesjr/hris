package com.humanresource.services;

import java.io.OutputStream;

public interface PDSReportService {

    /**
     * Generate all four PDS sheets (C1–C4) as a single merged PDF
     * and write them to the provided output stream.
     *
     * @param employeeId the employee whose PDS is being generated
     * @param out        the output stream to write the PDF bytes into
     */
    void generatePDS(Long employeeId, OutputStream out) throws Exception;
}
