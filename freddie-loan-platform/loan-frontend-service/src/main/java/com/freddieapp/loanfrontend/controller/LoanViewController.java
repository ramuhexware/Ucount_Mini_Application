package com.freddieapp.loanfrontend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class LoanViewController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/api/service-info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "loan-frontend-service");
        info.put("port", 8089);
        info.put("database", "PostgreSQL Database 2 (freddie_loans)");
        info.put("status", "UP");
        info.put("originationGateway", "http://localhost:8080/api/v1/loans");
        info.put("underwritingGateway", "http://localhost:8080/api/v1/underwriting");
        return ResponseEntity.ok(info);
    }
}
