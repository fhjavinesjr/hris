package com.payroll.reports;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.payroll.dtos.AnnualizationTaxIdentityDTO;
import com.payroll.dtos.Bir1700ReviewDTO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Prefills only verified HRIS facts on the unmodified BIR January 2018 Form 1700. */
@Component
public class Bir1700OfficialFormWriter {
    private static final String RESOURCE = "reports/annualization/bir_1700_official_2018.pdf";
    private static final float HEIGHT = 936f;

    public void write(Bir1700ReviewDTO review, AnnualizationTaxIdentityDTO identity,
                      OutputStream output) throws IOException, DocumentException {
        if (review.employers().size() > 4) {
            throw new IllegalArgumentException("Form 1700 needs an additional employer sheet; the draft supports four rows.");
        }
        try (InputStream input = new ClassPathResource(RESOURCE).getInputStream()) {
            PdfReader reader = new PdfReader(input);
            try {
                if (reader.getNumberOfPages() != 2 || !pageSizeMatches(reader, 1)
                        || !pageSizeMatches(reader, 2)) {
                    throw new IllegalStateException("Unexpected official BIR Form 1700 template version.");
                }
                PdfStamper stamper = new PdfStamper(reader, output);
                try {
                    BaseFont font = BaseFont.createFont(BaseFont.HELVETICA,
                            BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                    fillFirstPage(stamper.getOverContent(1), font, review, identity);
                    fillSecondPage(stamper.getOverContent(2), font, review, identity);
                } finally {
                    stamper.close();
                }
            } finally {
                reader.close();
            }
        }
    }

    private boolean pageSizeMatches(PdfReader reader, int page) {
        return Math.abs(reader.getPageSize(page).getWidth() - 612f) < 0.1f
                && Math.abs(reader.getPageSize(page).getHeight() - HEIGHT) < 0.1f;
    }

    private void fillFirstPage(PdfContentByte canvas, BaseFont font, Bir1700ReviewDTO d,
                               AnnualizationTaxIdentityDTO identity) {
        text(canvas, font, Integer.toString(d.taxYear()), 126, 108, 54, 18, 9, true, false);
        tin(canvas, font, identity.employeeTin(), 40, 151, 58);
        text(canvas, font, identity.employeeRdoCode(), 326, 151, 44, 17, 8, true, false);
        text(canvas, font, "X", 435, 153, 11, 12, 9, true, false);
        text(canvas, font, legalName(identity, d.employeeName()), 13, 184, 570, 16, 8, false, false);
        text(canvas, font, identity.employeeAddress(), 13, 212, 460, 18, 7, false, false);
        text(canvas, font, identity.employeeZipCode(), 536, 230, 46, 16, 8, true, false);
        text(canvas, font, identity.employeeBirthDate() == null ? null
                : identity.employeeBirthDate().format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                15, 251, 110, 16, 8, false, false);
        text(canvas, font, identity.employeeNationality(), 14, 279, 210, 17, 8, false, false);
        text(canvas, font, identity.employeeContactNo(), 14, 309, 180, 18, 8, false, false);
        // Parts III-IV, spouse, foreign income and signatures require the individual's review.
        watermark(canvas, font);
    }

    private void fillSecondPage(PdfContentByte canvas, BaseFont font, Bir1700ReviewDTO d,
                                AnnualizationTaxIdentityDTO identity) {
        tin(canvas, font, identity.employeeTin(), 13, 104, 46);
        text(canvas, font, identity.employeeLastName(), 214, 104, 372, 14, 8, false, false);
        List<Bir1700ReviewDTO.EmployerLine> employers = d.employers();
        BigDecimal displayedGross = employers.stream()
                .map(line -> wholePesos(line.grossCompensation()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal displayedNonTaxable = wholePesos(d.nonTaxableCompensation());
        BigDecimal displayedWithheld = employers.stream()
                .map(line -> wholePesos(line.taxWithheld()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        amount(canvas, font, displayedGross, 355, 151, 122, 16);
        amount(canvas, font, displayedNonTaxable, 355, 168, 122, 16);
        amount(canvas, font, displayedGross.subtract(displayedNonTaxable), 355, 185, 122, 16);
        amount(canvas, font, displayedWithheld, 355, 389, 122, 16);

        float[] names = {563, 605, 646, 687};
        float[] tins = {582, 623, 664, 705};
        float[] income = {728, 745, 762, 779};
        for (int i = 0; i < employers.size(); i++) {
            Bir1700ReviewDTO.EmployerLine employer = employers.get(i);
            text(canvas, font, "X", 28, names[i], 10, 13, 8, true, false);
            text(canvas, font, employer.employerName(), 88, names[i], 217, 16, 7, false, false);
            text(canvas, font, employer.employerTin(), 385, tins[i], 190, 15, 7, false, false);
            amount(canvas, font, employer.grossCompensation(), 259, income[i], 112, 16);
            amount(canvas, font, employer.taxWithheld(), 515, income[i], 79, 16);
        }
        amount(canvas, font, displayedGross, 259, 795, 112, 16);
        amount(canvas, font, displayedWithheld, 515, 795, 79, 16);
        // Items 45-47, 55-59 and the taxpayer's payment must not be inferred from employer payroll.
        watermark(canvas, font);
        canvas.saveState();
        canvas.setColorFill(Color.RED);
        text(canvas, font, "HRIS prefill: " + d.payrollPeriods()
                + " payroll period(s); personal income, credits and filing need unreviewed.",
                12, 917, 580, 12, 6, false, false);
        canvas.restoreState();
    }

    private void tin(PdfContentByte canvas, BaseFont font, String tin,
                     float x, float top, float groupSpacing) {
        String digits = digits(tin);
        if (digits.length() < 9) return;
        for (int group = 0; group < 3; group++) {
            text(canvas, font, digits.substring(group * 3, group * 3 + 3),
                    x + group * groupSpacing, top, 36, 17, 8, false, false);
        }
    }

    private String digits(String value) { return value == null ? "" : value.replaceAll("\\D", ""); }

    private String legalName(AnnualizationTaxIdentityDTO identity, String fallback) {
        if (identity.employeeLastName() == null || identity.employeeFirstName() == null) return fallback;
        return identity.employeeLastName() + ", " + identity.employeeFirstName()
                + (identity.employeeMiddleName() == null ? "" : " " + identity.employeeMiddleName());
    }

    private void amount(PdfContentByte canvas, BaseFont font, BigDecimal amount,
                        float x, float top, float width, float height) {
        if (amount == null) return;
        String value = wholePesos(amount).toPlainString();
        text(canvas, font, value, x, top, width, height, 8, false, true);
    }

    private BigDecimal wholePesos(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount.setScale(0, RoundingMode.HALF_UP);
    }

    private void watermark(PdfContentByte canvas, BaseFont font) {
        canvas.saveState();
        PdfGState transparency = new PdfGState();
        transparency.setFillOpacity(0.15f);
        canvas.setGState(transparency);
        canvas.setColorFill(Color.RED);
        canvas.beginText();
        canvas.setFontAndSize(font, 51);
        canvas.showTextAligned(PdfContentByte.ALIGN_CENTER, "DRAFT - NOT FOR FILING", 306, 468, 35);
        canvas.endText();
        canvas.restoreState();
    }

    private void text(PdfContentByte canvas, BaseFont font, String value,
                      float x, float top, float width, float height, float size,
                      boolean center, boolean right) {
        if (value == null || value.isBlank()) return;
        String content = value.trim().replaceAll("\\s+", " ");
        float fitted = size;
        while (font.getWidthPoint(content, fitted) > width - 4 && fitted > 4.5f) fitted -= 0.25f;
        if (font.getWidthPoint(content, fitted) > width - 4) {
            throw new IllegalArgumentException("A Form 1700 field exceeds the official form space.");
        }
        float measured = font.getWidthPoint(content, fitted);
        float left = right ? x + width - measured - 2 : center ? x + (width - measured) / 2 : x + 2;
        float baseline = HEIGHT - top - (height + fitted) / 2 + 1;
        canvas.beginText();
        canvas.setFontAndSize(font, fitted);
        canvas.setTextMatrix(left, baseline);
        canvas.showText(content);
        canvas.endText();
    }
}
