package com.feedback.feedbacksystem.service.impl;

import com.feedback.feedbacksystem.dto.AdminSummaryAnalyticsDto;
import com.feedback.feedbacksystem.dto.CourseAnalyticsDto;
import com.feedback.feedbacksystem.dto.FacultyAnalyticsDto;
import com.feedback.feedbacksystem.dto.FormAnalyticsSummaryDto;
import com.feedback.feedbacksystem.service.AnalyticsService;
import com.feedback.feedbacksystem.service.ReportExportService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportExportServiceImpl implements ReportExportService {

    private final AnalyticsService analyticsService;

    @Override
    public byte[] generatePdfReport(Long formId, Long courseId, Long facultyId) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Paragraph title = new Paragraph("Feedback Collection System - Summary Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            if (formId != null) {
                FormAnalyticsSummaryDto formSummary = analyticsService.getFormAnalytics(formId);
                addFormSummaryToPdf(document, formSummary);
            } else if (courseId != null) {
                CourseAnalyticsDto courseSummary = analyticsService.getCourseAnalytics(courseId);
                addCourseSummaryToPdf(document, courseSummary);
            } else if (facultyId != null) {
                FacultyAnalyticsDto facultySummary = analyticsService.getFacultyAnalytics(facultyId);
                addFacultySummaryToPdf(document, facultySummary);
            } else {
                AdminSummaryAnalyticsDto adminSummary = analyticsService.getAdminSummaryAnalytics();
                addAdminSummaryToPdf(document, adminSummary);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF summary report", e);
            throw new RuntimeException("Failed to generate PDF report: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] generateExcelReport(Long formId, Long courseId, Long facultyId) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Feedback Metrics Summary");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Parameter / Metric");
            headerRow.createCell(1).setCellValue("Details / Value");
            for (int i = 0; i < 2; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            if (formId != null) {
                FormAnalyticsSummaryDto form = analyticsService.getFormAnalytics(formId);
                sheet.createRow(rowIndex++).createCell(0).setCellValue("Form Title");
                sheet.getRow(rowIndex - 1).createCell(1).setCellValue(form.getFormTitle());

                sheet.createRow(rowIndex++).createCell(0).setCellValue("Category");
                sheet.getRow(rowIndex - 1).createCell(1).setCellValue(form.getCategory());

                sheet.createRow(rowIndex++).createCell(0).setCellValue("Total Responses");
                sheet.getRow(rowIndex - 1).createCell(1).setCellValue(form.getTotalResponses());

                sheet.createRow(rowIndex++).createCell(0).setCellValue("Completion Rate (%)");
                sheet.getRow(rowIndex - 1).createCell(1).setCellValue(form.getCompletionRate() != null ? form.getCompletionRate() : 0.0);

                sheet.createRow(rowIndex++).createCell(0).setCellValue("Overall Average Rating");
                sheet.getRow(rowIndex - 1).createCell(1).setCellValue(form.getOverallAverageRating() != null ? form.getOverallAverageRating() : 0.0);
            } else if (courseId != null) {
                CourseAnalyticsDto course = analyticsService.getCourseAnalytics(courseId);
                sheet.createRow(rowIndex++).createCell(0).setCellValue("Course Name");
                sheet.getRow(rowIndex - 1).createCell(1).setCellValue(course.getCourseName());

                sheet.createRow(rowIndex++).createCell(0).setCellValue("Course Code");
                sheet.getRow(rowIndex - 1).createCell(1).setCellValue(course.getCourseCode());

                sheet.createRow(rowIndex++).createCell(0).setCellValue("Total Forms Assigned");
                sheet.getRow(rowIndex - 1).createCell(1).setCellValue(course.getTotalFormsAssigned());

                sheet.createRow(rowIndex++).createCell(0).setCellValue("Total Responses");
                sheet.getRow(rowIndex - 1).createCell(1).setCellValue(course.getTotalResponses());

                sheet.createRow(rowIndex++).createCell(0).setCellValue("Average Course Rating");
                sheet.getRow(rowIndex - 1).createCell(1).setCellValue(course.getAverageCourseRating() != null ? course.getAverageCourseRating() : 0.0);
            } else {
                AdminSummaryAnalyticsDto admin = analyticsService.getAdminSummaryAnalytics();
                sheet.createRow(rowIndex++).createCell(0).setCellValue("Total Forms");
                sheet.getRow(rowIndex - 1).createCell(1).setCellValue(admin.getTotalForms());

                sheet.createRow(rowIndex++).createCell(0).setCellValue("Total Active Forms");
                sheet.getRow(rowIndex - 1).createCell(1).setCellValue(admin.getTotalActiveForms());

                sheet.createRow(rowIndex++).createCell(0).setCellValue("Total Responses");
                sheet.getRow(rowIndex - 1).createCell(1).setCellValue(admin.getTotalResponses());

                sheet.createRow(rowIndex++).createCell(0).setCellValue("Overall Completion Rate (%)");
                sheet.getRow(rowIndex - 1).createCell(1).setCellValue(admin.getOverallCompletionRate() != null ? admin.getOverallCompletionRate() : 0.0);
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generating Excel report", e);
            throw new RuntimeException("Failed to generate Excel report: " + e.getMessage(), e);
        }
    }

    private void addFormSummaryToPdf(Document document, FormAnalyticsSummaryDto form) throws DocumentException {
        document.add(new Paragraph("Form Title: " + form.getFormTitle(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        document.add(new Paragraph("Category: " + form.getCategory()));
        document.add(new Paragraph("Total Responses: " + form.getTotalResponses()));
        document.add(new Paragraph("Completion Rate: " + (form.getCompletionRate() != null ? form.getCompletionRate() + "%" : "N/A")));
        document.add(new Paragraph("Overall Average Rating: " + (form.getOverallAverageRating() != null ? form.getOverallAverageRating() : "N/A")));
        document.add(new Paragraph(" "));

        if (form.getQuestionRatings() != null && !form.getQuestionRatings().isEmpty()) {
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.addCell(new PdfPCell(new Phrase("Question ID", FontFactory.getFont(FontFactory.HELVETICA_BOLD))));
            table.addCell(new PdfPCell(new Phrase("Question Text", FontFactory.getFont(FontFactory.HELVETICA_BOLD))));
            table.addCell(new PdfPCell(new Phrase("Average Rating", FontFactory.getFont(FontFactory.HELVETICA_BOLD))));

            form.getQuestionRatings().forEach(q -> {
                table.addCell(String.valueOf(q.getQuestionId()));
                table.addCell(q.getQuestionText());
                table.addCell(q.getAverageRating() != null ? String.valueOf(q.getAverageRating()) : "N/A");
            });
            document.add(table);
        }
    }

    private void addCourseSummaryToPdf(Document document, CourseAnalyticsDto course) throws DocumentException {
        document.add(new Paragraph("Course Name: " + course.getCourseName(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        document.add(new Paragraph("Course Code: " + course.getCourseCode()));
        document.add(new Paragraph("Total Forms Assigned: " + course.getTotalFormsAssigned()));
        document.add(new Paragraph("Total Responses: " + course.getTotalResponses()));
        document.add(new Paragraph("Average Rating: " + (course.getAverageCourseRating() != null ? course.getAverageCourseRating() : "N/A")));
    }

    private void addFacultySummaryToPdf(Document document, FacultyAnalyticsDto faculty) throws DocumentException {
        document.add(new Paragraph("Faculty Name: " + faculty.getFacultyName(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        document.add(new Paragraph("Employee ID: " + (faculty.getEmployeeId() != null ? faculty.getEmployeeId() : "N/A")));
        document.add(new Paragraph("Total Courses Taught: " + faculty.getTotalCoursesTaught()));
        document.add(new Paragraph("Total Responses Received: " + faculty.getTotalResponsesReceived()));
        document.add(new Paragraph("Overall Average Rating: " + (faculty.getOverallAverageRating() != null ? faculty.getOverallAverageRating() : "N/A")));
    }

    private void addAdminSummaryToPdf(Document document, AdminSummaryAnalyticsDto admin) throws DocumentException {
        document.add(new Paragraph("College-Wide Overview", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        document.add(new Paragraph("Total Forms: " + admin.getTotalForms()));
        document.add(new Paragraph("Active Forms: " + admin.getTotalActiveForms()));
        document.add(new Paragraph("Total Student Submissions: " + admin.getTotalResponses()));
        document.add(new Paragraph("Overall Completion Rate: " + (admin.getOverallCompletionRate() != null ? admin.getOverallCompletionRate() + "%" : "N/A")));
    }
}
