package com.feedback.feedbacksystem.service;

public interface ReportExportService {

    byte[] generatePdfReport(Long formId, Long courseId, Long facultyId);

    byte[] generateExcelReport(Long formId, Long courseId, Long facultyId);
}
