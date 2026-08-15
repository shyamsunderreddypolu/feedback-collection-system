package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.service.ReportExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports/export")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('FACULTY')")
public class ReportExportController {

    private final ReportExportService reportExportService;

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdfReport(
            @RequestParam(required = false) Long formId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long facultyId) {

        byte[] pdfBytes = reportExportService.generatePdfReport(formId, courseId, facultyId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "feedback_summary_report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportExcelReport(
            @RequestParam(required = false) Long formId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long facultyId) {

        byte[] excelBytes = reportExportService.generateExcelReport(formId, courseId, facultyId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "feedback_metrics_summary.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }
}
