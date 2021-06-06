package com.fetty.studiooffice.rest.test;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> allAccess() {
        return ResponseEntity.ok("Public Content");
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('ROLE_BASIC_USER')")
    public String userAccess() {
        return "User Content.";
    }

    @GetMapping("/engineer")
    @PreAuthorize("hasRole('ROLE_SOUND_ENGINEER')")
    public String engineerAccess() {
        return "Engineer Board.";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String adminAccess() {
        return "Admin Board.";
    }
}