package com.humanresource.impl;

import com.humanresource.dtos.PDSReportData;
import com.humanresource.entitymodels.PersonalData;
import com.humanresource.repositories.ChildrenRepository;
import com.humanresource.repositories.CivilServiceEligibilityRepository;
import com.humanresource.repositories.EducationalBackgroundRepository;
import com.humanresource.repositories.LearningAndDevelopmentRepository;
import com.humanresource.repositories.PersonalDataRepository;
import com.humanresource.repositories.ReferencesRepository;
import com.humanresource.repositories.VoluntaryWorkRepository;
import com.humanresource.repositories.WorkExperienceRepository;
import com.humanresource.services.PDSReportService;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates all four PDS sheets (C1-C4) using provider-neutral repository data
 * and pre-compiled Jasper templates.
 *
 * The templates contain no SQL. This keeps SQL Server/PostgreSQL differences
 * in the JPA provider instead of report queries and also gives image, Boolean,
 * numeric, and date fields the same Java types on every database.
 */
@Service
public class PDSReportServiceImpl implements PDSReportService {

    private final PersonalDataRepository personalDataRepository;
    private final EducationalBackgroundRepository educationalBackgroundRepository;
    private final ChildrenRepository childrenRepository;
    private final CivilServiceEligibilityRepository civilServiceEligibilityRepository;
    private final WorkExperienceRepository workExperienceRepository;
    private final VoluntaryWorkRepository voluntaryWorkRepository;
    private final LearningAndDevelopmentRepository learningAndDevelopmentRepository;
    private final ReferencesRepository referencesRepository;
    private final PDSReportDataMapper reportDataMapper;

    public PDSReportServiceImpl(
            PersonalDataRepository personalDataRepository,
            EducationalBackgroundRepository educationalBackgroundRepository,
            ChildrenRepository childrenRepository,
            CivilServiceEligibilityRepository civilServiceEligibilityRepository,
            WorkExperienceRepository workExperienceRepository,
            VoluntaryWorkRepository voluntaryWorkRepository,
            LearningAndDevelopmentRepository learningAndDevelopmentRepository,
            ReferencesRepository referencesRepository,
            PDSReportDataMapper reportDataMapper) {
        this.personalDataRepository = personalDataRepository;
        this.educationalBackgroundRepository = educationalBackgroundRepository;
        this.childrenRepository = childrenRepository;
        this.civilServiceEligibilityRepository = civilServiceEligibilityRepository;
        this.workExperienceRepository = workExperienceRepository;
        this.voluntaryWorkRepository = voluntaryWorkRepository;
        this.learningAndDevelopmentRepository = learningAndDevelopmentRepository;
        this.referencesRepository = referencesRepository;
        this.reportDataMapper = reportDataMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public void generatePDS(Long employeeId, OutputStream out) throws Exception {
        PersonalData personalData = personalDataRepository.findByEmployeeId(employeeId);
        if (personalData == null) {
            throw new IllegalArgumentException(
                    "Personal Data Sheet record not found for employee ID " + employeeId
            );
        }

        Long personalDataId = personalData.getPersonalDataId();
        PDSReportData reportData = reportDataMapper.map(
                personalData,
                educationalBackgroundRepository.findByPersonalDataId(personalDataId),
                childrenRepository.findByPersonalDataId(personalDataId),
                civilServiceEligibilityRepository.findByPersonalDataId(personalDataId),
                workExperienceRepository.findByPersonalDataId(personalDataId),
                voluntaryWorkRepository.findByPersonalDataId(personalDataId),
                learningAndDevelopmentRepository.findByPersonalDataId(personalDataId),
                referencesRepository.findByPersonalDataId(personalDataId),
                LocalDate.now()
        );

        Map<String, Object> params = reportParameters(employeeId, reportData);

        JasperReport c1 = loadCompiledReport("reports/pds_c1.jasper");
        JasperReport c2 = loadCompiledReport("reports/pds_c2.jasper");
        JasperReport c3 = loadCompiledReport("reports/pds_c3.jasper");
        JasperReport c4 = loadCompiledReport("reports/pds_c4.jasper");

        JasperPrint p1 = JasperFillManager.fillReport(
                c1,
                params,
                singleRowDataSource(reportData.getPageOneRow())
        );
        JasperPrint p2 = JasperFillManager.fillReport(c2, params, new JREmptyDataSource(1));
        JasperPrint p3 = JasperFillManager.fillReport(c3, params, new JREmptyDataSource(1));
        JasperPrint p4 = JasperFillManager.fillReport(
                c4,
                params,
                singleRowDataSource(reportData.getPageFourRow())
        );

        JasperPrint merged = mergeReports(List.of(p1, p2, p3, p4));
        JasperExportManager.exportReportToPdfStream(merged, out);
    }

    private Map<String, Object> reportParameters(
            Long employeeId,
            PDSReportData reportData) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("EMPLOYEE_ID", employeeId);

        params.put(
                "PDS_C1_CHILDREN_SUBREPORT",
                loadCompiledReport("reports/pds_c1_children_sub.jasper")
        );
        params.put(
                "PDS_C1_CHILDREN_DATA_SOURCE",
                new JRMapCollectionDataSource(reportData.getChildrenRows())
        );

        params.put(
                "PDS_C2_CIVILSERVICE_SUBREPORT",
                loadCompiledReport("reports/pds_c2_civilservice_sub.jasper")
        );
        params.put(
                "PDS_C2_CIVILSERVICE_DATA_SOURCE",
                new JRMapCollectionDataSource(reportData.getCivilServiceRows())
        );
        params.put(
                "PDS_C2_WORKEXPERIENCE_SUBREPORT",
                loadCompiledReport("reports/pds_c2_workexperience_sub.jasper")
        );
        params.put(
                "PDS_C2_WORKEXPERIENCE_DATA_SOURCE",
                new JRMapCollectionDataSource(reportData.getWorkExperienceRows())
        );

        params.put(
                "PDS_C3_VOLUNTARYWORK_SUBREPORT",
                loadCompiledReport("reports/pds_c3_voluntarywork_sub.jasper")
        );
        params.put(
                "PDS_C3_VOLUNTARYWORK_DATA_SOURCE",
                new JRMapCollectionDataSource(reportData.getVoluntaryWorkRows())
        );
        params.put(
                "PDS_C3_LND_SUBREPORT",
                loadCompiledReport("reports/pds_c3_lnd_sub.jasper")
        );
        params.put(
                "PDS_C3_LND_DATA_SOURCE",
                new JRMapCollectionDataSource(reportData.getLearningAndDevelopmentRows())
        );
        params.put(
                "PDS_C3_OTHERINFORMATION_SUBREPORT",
                loadCompiledReport("reports/pds_c3_otherinformation_sub.jasper")
        );
        params.put(
                "PDS_C3_OTHERINFORMATION_DATA_SOURCE",
                new JRMapCollectionDataSource(reportData.getOtherInformationRows())
        );

        params.put(
                "PDS_C4_REFERENCES_SUBREPORT",
                loadCompiledReport("reports/pds_c4_references_sub.jasper")
        );
        params.put(
                "PDS_C4_REFERENCES_DATA_SOURCE",
                new JRMapCollectionDataSource(reportData.getReferenceRows())
        );

        return params;
    }

    private JRMapCollectionDataSource singleRowDataSource(Map<String, Object> row) {
        return new JRMapCollectionDataSource(
                Collections.<Map<String, ?>>singletonList(row)
        );
    }

    private JasperReport loadCompiledReport(String classpathPath) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathPath);
        if (!resource.exists()) {
            throw new IllegalStateException("PDS compiled Jasper file not found in classpath: " + classpathPath);
        }
        try (InputStream is = resource.getInputStream()) {
            Object report = JRLoader.loadObject(is);
            if (!(report instanceof JasperReport)) {
                throw new IllegalStateException("Classpath file is not a JasperReport: " + classpathPath);
            }
            return (JasperReport) report;
        } catch (JRException e) {
            throw new JRException("Unable to load compiled PDS Jasper file: " + classpathPath, e);
        }
    }

    private JasperPrint mergeReports(List<JasperPrint> prints) {
        JasperPrint master = prints.get(0);
        for (int i = 1; i < prints.size(); i++) {
            for (JRPrintPage page : prints.get(i).getPages()) {
                master.addPage(page);
            }
        }
        return master;
    }
}
