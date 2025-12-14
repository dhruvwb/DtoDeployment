package com.ng.dtogen.controller;

import com.ng.dtogen.service.CompareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/compare/upload")
@RequiredArgsConstructor
public class CompareController {

    private final CompareService compareService;

    @PostMapping("/base")
    public ResponseEntity<String> uploadBase(@RequestBody String basePayload) {
        compareService.setBasePayload(basePayload);
        return ResponseEntity.ok("✅ Base payload stored successfully.");
    }

    @PostMapping("/compare")
    public ResponseEntity<String> uploadCompare(@RequestBody String comparePayload) {
        String result = compareService.compareWith(comparePayload);
        return ResponseEntity.ok(result);
    }
}
