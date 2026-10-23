package com.payroll.reports;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.payroll.dtos.Bir2316ReportDTO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/** Writes values over the unaltered BIR Form 2316, September 2021 (ENCS) PDF. */
@Component
public class Bir2316OfficialFormWriter {
    private static final String RESOURCE =
            "reports/annualization/bir_2316_official_2021.pdf";
    private static final float PAGE_HEIGHT = 936f;
    private static final float AMOUNT_X = 208f;
    private static final float AMOUNT_WIDTH = 101f;
    private static final float RIGHT_AMOUNT_X = 484f;
    private static final float RIGHT_AMOUNT_WIDTH = 101f;

    public void write(Bir2316ReportDTO data, OutputStream output)
            throws IOException, DocumentException {
        validateTotals(data);
        try (InputStream input = new ClassPathResource(RESOURCE).getInputStream()) {
            PdfReader reader = new PdfReader(input);
            try {
                if (reader.getNumberOfPages() != 1
                        || Math.abs(reader.getPageSize(1).getWidth() - 612f) > 0.1f
                        || Math.abs(reader.getPageSize(1).getHeight() - PAGE_HEIGHT) > 0.1f) {
                    throw new IllegalStateException("Unexpected BIR Form 2316 template version or page size.");
                }
                PdfStamper stamper = new PdfStamper(reader, output);
                try {
                    PdfContentByte canvas = stamper.getOverContent(1);
                    BaseFont font = BaseFont.createFont(
                            BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                    fill(data, canvas, font);
                } finally {
                    stamper.close();
                }
            } finally {
                reader.close();
            }
        }
    }

    private void validateTotals(Bir2316ReportDTO d) {
        requireSum("38", d.item38(), d.item29(), d.item30(), d.item31(),
                d.item32(), d.item33(), d.item34(), d.item35(), d.item36(), d.item37());
        requireSum("52", d.item52(), d.item39(), d.item40(), d.item41(),
                d.item42(), d.item43(), d.item44A(), d.item44B(), d.item45(),
                d.item46(), d.item47(), d.item48(), d.item49(), d.item50(),
                d.item51A(), d.item51B());
        requireSum("19", d.item19PresentGross(), d.item38(), d.item52());
        requireSum("20", d.item20PresentNonTaxable(), d.item38());
        requireSum("21", d.item21PresentTaxable(), d.item52());
        requireSum("23", d.item23GrossTaxable(), d.item21PresentTaxable(),
                d.item22PreviousTaxable());
        requireSum("26", d.item26TotalWithheldAdjusted(), d.item25APresentWithheld(),
                d.item25BPreviousWithheld());
        requireSum("28", d.item28TotalTaxesWithheld(), d.item26TotalWithheldAdjusted(),
                d.item27PeraTaxCredit());
    }

    private void requireSum(String item, BigDecimal expected, BigDecimal... parts) {
        BigDecimal actual = BigDecimal.ZERO;
        for (BigDecimal part : parts) actual = actual.add(part == null ? BigDecimal.ZERO : part);
        if ((expected == null ? BigDecimal.ZERO : expected).compareTo(actual) != 0) {
            throw new IllegalArgumentException("BIR Form 2316 item " + item
                    + " does not reconcile with its component items.");
        }
    }

    private void fill(Bir2316ReportDTO d, PdfContentByte canvas, BaseFont font) {
        text(canvas, font, d.taxYear(), 124, 94, 72, 17, 9, true);
        text(canvas, font, d.periodFrom(), 387, 94, 70, 17, 8, true);
        text(canvas, font, d.periodTo(), 513, 94, 70, 17, 8, true);

        tin(canvas, font, d.employeeTin(), 123);
        text(canvas, font, d.employeeName(), 43, 148, 210, 15, 8, false);
        text(canvas, font, d.employeeRdoCode(), 265, 148, 37, 15, 7, true);
        text(canvas, font, d.employeeRegisteredAddress(), 43, 177, 210, 15, 7, false);
        text(canvas, font, d.employeeZipCode(), 260, 177, 48, 15, 7, true);
        text(canvas, font, d.employeeLocalAddress(), 43, 202, 210, 15, 7, false);
        text(canvas, font, d.employeeLocalZipCode(), 260, 202, 48, 15, 7, true);
        text(canvas, font, d.employeeBirthDate(), 47, 254, 100, 16, 8, true);
        text(canvas, font, d.employeeContactNo(), 168, 254, 140, 16, 8, false);
        amount(canvas, font, d.statutoryDailyRate(), 210, 273, 97, 15);
        amount(canvas, font, d.statutoryMonthlyRate(), 210, 291, 97, 15);
        text(canvas, font, d.mweMark(), 48, 309, 14, 15, 9, true);

        tin(canvas, font, d.employerTin(), 335);
        text(canvas, font, d.employerName(), 43, 361, 264, 15, 8, false);
        text(canvas, font, d.employerAddress(), 43, 387, 210, 15, 7, false);
        text(canvas, font, d.employerZipCode(), 260, 387, 48, 15, 7, true);
        text(canvas, font, d.mainEmployerMark(), 116, 405, 14, 15, 9, true);

        tin(canvas, font, d.previousEmployerTin(), 432);
        text(canvas, font, d.previousEmployerName(), 43, 458, 264, 15, 8, false);
        text(canvas, font, d.previousEmployerAddress(), 43, 484, 210, 15, 7, false);
        text(canvas, font, d.previousEmployerZipCode(), 260, 484, 48, 15, 7, true);

        BigDecimal[] summary = {
                d.item19PresentGross(), d.item20PresentNonTaxable(),
                d.item21PresentTaxable(), d.item22PreviousTaxable(),
                d.item23GrossTaxable(), d.item24TaxDue(),
                d.item25APresentWithheld(), d.item25BPreviousWithheld(),
                d.item26TotalWithheldAdjusted(), d.item27PeraTaxCredit(),
                d.item28TotalTaxesWithheld()
        };
        float[] summaryTops = {513, 532, 552, 571, 590, 609, 628, 647, 667, 687, 708};
        for (int index = 0; index < summary.length; index++) {
            amount(canvas, font, summary[index], AMOUNT_X, summaryTops[index], AMOUNT_WIDTH, 15);
        }

        BigDecimal[] nonTaxable = {
                d.item29(), d.item30(), d.item31(), d.item32(), d.item33(),
                d.item34(), d.item35(), d.item36(), d.item37(), d.item38()
        };
        float[] nonTaxableTops = {142, 161, 181, 201, 221, 241, 260, 279, 299, 318};
        for (int index = 0; index < nonTaxable.length; index++) {
            amount(canvas, font, nonTaxable[index], RIGHT_AMOUNT_X,
                    nonTaxableTops[index], RIGHT_AMOUNT_WIDTH, 15);
        }

        BigDecimal[] taxable = {
                d.item39(), d.item40(), d.item41(), d.item42(), d.item43(),
                d.item44A(), d.item44B(), d.item45(), d.item46(), d.item47(),
                d.item48(), d.item49(), d.item50(), d.item51A(), d.item51B(), d.item52()
        };
        float[] taxableTops = {
                355, 374, 394, 413, 432, 459, 478, 503, 523, 542,
                561, 580, 599, 625, 645, 667
        };
        for (int index = 0; index < taxable.length; index++) {
            amount(canvas, font, taxable[index], RIGHT_AMOUNT_X,
                    taxableTops[index], RIGHT_AMOUNT_WIDTH, 15);
        }
        text(canvas, font, d.item44ADescription(), 343, 459, 131, 15, 6, false);
        text(canvas, font, d.item44BDescription(), 343, 478, 131, 15, 6, false);
        text(canvas, font, d.item51ADescription(), 343, 625, 131, 15, 6, false);
        text(canvas, font, d.item51BDescription(), 343, 645, 131, 15, 6, false);
        // The official employer/employee declarations and signature spaces remain unaltered.
    }

    private void tin(PdfContentByte canvas, BaseFont font, String value, float top) {
        String digits = value == null ? "" : value.replaceAll("\\D", "");
        if (digits.isEmpty()) return;
        if (digits.length() != 9 && digits.length() != 12 && digits.length() != 13) {
            throw new IllegalArgumentException("BIR Form 2316 TIN must have nine, twelve, or thirteen digits.");
        }
        text(canvas, font, digits.substring(0, 3), 87, top, 36, 15, 8, true);
        text(canvas, font, digits.substring(3, 6), 130, top, 44, 15, 8, true);
        text(canvas, font, digits.substring(6, 9), 180, top, 44, 15, 8, true);
        if (digits.length() > 9) {
            text(canvas, font, digits.substring(9), 230, top, 75, 15, 8, true);
        }
    }

    private void amount(PdfContentByte canvas, BaseFont font, BigDecimal value,
            float x, float top, float width, float height) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        text(canvas, font, formatter.format(value == null ? BigDecimal.ZERO : value),
                x, top, width, height, 7, false, true);
    }

    private void text(PdfContentByte canvas, BaseFont font, String value,
            float x, float top, float width, float height, float size, boolean center) {
        text(canvas, font, value, x, top, width, height, size, center, false);
    }

    private void text(PdfContentByte canvas, BaseFont font, String value,
            float x, float top, float width, float height, float size,
            boolean center, boolean right) {
        if (value == null || value.isBlank()) return;
        String content = value.trim().replaceAll("\\s+", " ");
        float fittedSize = size;
        while (font.getWidthPoint(content, fittedSize) > width - 4 && fittedSize > 4.5f) {
            fittedSize -= 0.25f;
        }
        if (font.getWidthPoint(content, fittedSize) > width - 4) {
            throw new IllegalArgumentException("BIR Form 2316 value does not fit its official field: " + content);
        }
        float textWidth = font.getWidthPoint(content, fittedSize);
        float position = right ? x + width - textWidth - 2
                : center ? x + (width - textWidth) / 2 : x + 2;
        float baseline = PAGE_HEIGHT - top - (height + fittedSize) / 2 + 1;
        canvas.beginText();
        canvas.setFontAndSize(font, fittedSize);
        canvas.setTextMatrix(position, baseline);
        canvas.showText(content);
        canvas.endText();
    }
}
