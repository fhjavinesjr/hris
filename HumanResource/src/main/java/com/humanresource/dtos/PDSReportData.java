package com.humanresource.dtos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provider-neutral data prepared for the four PDS report pages and their
 * subreports.
 *
 * Jasper field names are kept in the maps because the existing CSC templates
 * already define a large, stable field contract. Database access and
 * provider-specific type conversion stay outside the templates.
 */
public final class PDSReportData {

    private final Map<String, Object> pageOneRow;
    private final Map<String, Object> pageFourRow;
    private final List<Map<String, ?>> childrenRows;
    private final List<Map<String, ?>> civilServiceRows;
    private final List<Map<String, ?>> workExperienceRows;
    private final List<Map<String, ?>> voluntaryWorkRows;
    private final List<Map<String, ?>> learningAndDevelopmentRows;
    private final List<Map<String, ?>> otherInformationRows;
    private final List<Map<String, ?>> referenceRows;

    public PDSReportData(
            Map<String, Object> pageOneRow,
            Map<String, Object> pageFourRow,
            List<? extends Map<String, ?>> childrenRows,
            List<? extends Map<String, ?>> civilServiceRows,
            List<? extends Map<String, ?>> workExperienceRows,
            List<? extends Map<String, ?>> voluntaryWorkRows,
            List<? extends Map<String, ?>> learningAndDevelopmentRows,
            List<? extends Map<String, ?>> otherInformationRows,
            List<? extends Map<String, ?>> referenceRows) {
        this.pageOneRow = immutableRow(pageOneRow);
        this.pageFourRow = immutableRow(pageFourRow);
        this.childrenRows = immutableRows(childrenRows);
        this.civilServiceRows = immutableRows(civilServiceRows);
        this.workExperienceRows = immutableRows(workExperienceRows);
        this.voluntaryWorkRows = immutableRows(voluntaryWorkRows);
        this.learningAndDevelopmentRows = immutableRows(learningAndDevelopmentRows);
        this.otherInformationRows = immutableRows(otherInformationRows);
        this.referenceRows = immutableRows(referenceRows);
    }

    public Map<String, Object> getPageOneRow() {
        return pageOneRow;
    }

    public Map<String, Object> getPageFourRow() {
        return pageFourRow;
    }

    public List<Map<String, ?>> getChildrenRows() {
        return childrenRows;
    }

    public List<Map<String, ?>> getCivilServiceRows() {
        return civilServiceRows;
    }

    public List<Map<String, ?>> getWorkExperienceRows() {
        return workExperienceRows;
    }

    public List<Map<String, ?>> getVoluntaryWorkRows() {
        return voluntaryWorkRows;
    }

    public List<Map<String, ?>> getLearningAndDevelopmentRows() {
        return learningAndDevelopmentRows;
    }

    public List<Map<String, ?>> getOtherInformationRows() {
        return otherInformationRows;
    }

    public List<Map<String, ?>> getReferenceRows() {
        return referenceRows;
    }

    private static Map<String, Object> immutableRow(Map<String, Object> row) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(row));
    }

    private static List<Map<String, ?>> immutableRows(List<? extends Map<String, ?>> rows) {
        List<Map<String, ?>> copies = new ArrayList<>(rows.size());
        for (Map<String, ?> row : rows) {
            copies.add(Collections.unmodifiableMap(new LinkedHashMap<>(row)));
        }
        return Collections.unmodifiableList(copies);
    }
}
