package com.distributor.ordermanagement.controller;

import com.distributor.ordermanagement.dto.ReportResponse;
import com.distributor.ordermanagement.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('DISTRIBUTOR')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ReportResponse getReports() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return reportService.generateReport(auth.getName(), auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DISTRIBUTOR")));
    }
}
