package com.freddieapp.loginfrontend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class LoginViewController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/api/service-info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "login-frontend-service");
        info.put("port", 8088);
        info.put("database", "PostgreSQL Database 1 (freddie_customer)");
        info.put("status", "UP");
        info.put("authGateway", "http://localhost:8080/api/v1/auth");
        return ResponseEntity.ok(info);
    }
}
