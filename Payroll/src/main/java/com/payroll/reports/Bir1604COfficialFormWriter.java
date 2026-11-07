package com.payroll.reports;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.payroll.dtos.TaxRemittance1601CMonthDTO;
import com.payroll.dtos.Bir1604COfficerReviewDTO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** A clearly marked preview on the unaltered BIR January 2018 Form 1604-C. */
@Component
public class Bir1604COfficialFormWriter {
    private static final String RESOURCE = "reports/annualization/bir_1604c_official_2018.pdf";
    private static final float HEIGHT = 936f;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public record Data(int taxYear, String tin, String rdo, String name, String address,
                       String zipCode, List<TaxRemittance1601CMonthDTO> months,
                       Bir1604COfficerReviewDTO officerReview,
                       String previewReason) {}

    public void write(Data data, OutputStream output) throws IOException, DocumentException {
        if (data.months() == null || data.months().size() != 12) {
            throw new IllegalArgumentException("The 1604-C preview requires twelve month slots.");
        }
        try (InputStream input = new ClassPathResource(RESOURCE).getInputStream()) {
            PdfReader reader = new PdfReader(input);
            try {
                if (reader.getNumberOfPages() != 1
                        || Math.abs(reader.getPageSize(1).getWidth() - 612f) > 0.1f
                        || Math.abs(reader.getPageSize(1).getHeight() - HEIGHT) > 0.1f) {
                    throw new IllegalStateException("Unexpected official BIR 1604-C template version.");
                }
                PdfStamper stamper = new PdfStamper(reader, output);
                try {
                    PdfContentByte canvas = stamper.getOverContent(1);
                    BaseFont font = BaseFont.createFont(BaseFont.HELVETICA,
                            BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                    fill(data, canvas, font);
                } finally {
                    stamper.close();
                }
            } finally {
                reader.close();
            }
        }
    }

    private void fill(Data d, PdfContentByte canvas, BaseFont font) {
        String year = Integer.toString(d.taxYear());
        if (!year.matches("20[0-9]{2}")) throw new IllegalArgumentException("Invalid 1604-C tax year.");
        text(canvas, font, year.substring(2), 169, 121, 37, 16, 9, true);
        String tin = d.tin() == null ? "" : d.tin().replaceAll("\\D", "");
        if (tin.length() < 9 || tin.length() > 14) {
            throw new IllegalArgumentException("Employer TIN and branch must contain 9 to 14 digits.");
        }
        text(canvas, font, tin.substring(0, 3), 239, 161, 40, 12, 8, true);
        text(canvas, font, tin.substring(3, 6), 292, 161, 39, 12, 8, true);
        text(canvas, font, tin.substring(6, 9), 345, 161, 39, 12, 8, true);
        if (tin.length() > 9) text(canvas, font, tin.substring(9), 398, 161, 74, 12, 8, true);
        text(canvas, font, d.rdo(), 554, 161, 37, 12, 8, true);
        text(canvas, font, d.name(), 24, 190, 550, 15, 8, false);
        text(canvas, font, d.address(), 24, 223, 438, 14, 8, false);
        text(canvas, font, d.zipCode(), 535, 240, 50, 14, 8, true);
        text(canvas, font, "X", 242, 261, 12, 12, 9, true); // Government withholding agent.

        Bir1604COfficerReviewDTO review = d.officerReview();
        if (review != null) {
            text(canvas, font, review.sheetsAttached() == null ? null : review.sheetsAttached().toString(),
                    554, 121, 36, 15, 8, true);
            text(canvas, font, review.contactNumber(), 26, 294, 153, 12, 8, false);
            text(canvas, font, review.emailAddress(), 185, 294, 405, 12, 8, false);
            if ("YES".equals(review.refundsReleased())) {
                text(canvas, font, "X", 215, 314, 12, 12, 9, true);
                text(canvas, font, date(review.refundDate()), 480, 314, 106, 12, 8, true);
            } else if ("NO".equals(review.refundsReleased())) {
                text(canvas, font, "X", 261, 314, 12, 12, 9, true);
            }
            amount(canvas, font, review.overremittanceAmount(), 243, 336, 145, 13);
            if (review.firstCreditingMonth() != null)
                text(canvas, font, String.format("%02d", review.firstCreditingMonth()),
                        551, 336, 36, 13, 8, true);
            text(canvas, font, review.signatoryName(), 314, 833, 272, 11, 8, true);
            text(canvas, font, review.signatoryTitle() + " / TIN " + review.signatoryTin(),
                    314, 843, 272, 9, 6, true);
        }

        BigDecimal taxTotal = BigDecimal.ZERO;
        BigDecimal adjustmentTotal = BigDecimal.ZERO;
        BigDecimal penaltiesTotal = BigDecimal.ZERO;
        BigDecimal remittedTotal = BigDecimal.ZERO;
        for (int index = 0; index < 12; index++) {
            TaxRemittance1601CMonthDTO month = d.months().get(index);
            if (month.taxMonth() != index + 1 || !"CONFIRMED".equals(month.status())) continue;
            float firstTop = 387.5f + index * 13.35f;
            float secondTop = 591.5f + index * 13.35f;
            text(canvas, font, date(month.remittanceDate()), 99, firstTop, 124, 12, 7, true);
            text(canvas, font, month.remittanceChannel(), 227, firstTop, 120, 12, 7, false);
            text(canvas, font, month.referenceNo(), 354, firstTop, 116, 12, 7, false);
            amount(canvas, font, month.taxesWithheld(), 477, firstTop, 113, 12);
            amount(canvas, font, month.adjustment(), 99, secondTop, 160, 12);
            amount(canvas, font, month.penalties(), 267, secondTop, 168, 12);
            amount(canvas, font, month.amountRemitted(), 441, secondTop, 149, 12);
            taxTotal = taxTotal.add(zero(month.taxesWithheld()));
            adjustmentTotal = adjustmentTotal.add(zero(month.adjustment()));
            penaltiesTotal = penaltiesTotal.add(zero(month.penalties()));
            remittedTotal = remittedTotal.add(zero(month.amountRemitted()));
        }
        amount(canvas, font, taxTotal, 477, 550, 113, 12);
        amount(canvas, font, adjustmentTotal, 99, 752, 160, 12);
        amount(canvas, font, penaltiesTotal, 267, 752, 168, 12);
        amount(canvas, font, remittedTotal, 441, 752, 149, 12);
        // Officer-confirmed answers never imply a filed return or authorized signature.
        String warning = "DRAFT PREVIEW - NOT FOR FILING";
        text(canvas, font, warning, 345, 5, 225, 14, 10, true);
        text(canvas, font, d.previewReason(), 24, 913, 560, 15, 6, false);
    }

    private String date(LocalDate value) { return value == null ? null : DATE.format(value); }
    private BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

    private void amount(PdfContentByte canvas, BaseFont font, BigDecimal value,
                        float x, float top, float width, float height) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        text(canvas, font, format.format(zero(value)), x, top, width, height, 7, false, true);
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
        float fitted = size;
        while (font.getWidthPoint(content, fitted) > width - 4 && fitted > 4.5f) fitted -= 0.25f;
        if (font.getWidthPoint(content, fitted) > width - 4) {
            throw new IllegalArgumentException("A BIR 1604-C field exceeds the official form space.");
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
