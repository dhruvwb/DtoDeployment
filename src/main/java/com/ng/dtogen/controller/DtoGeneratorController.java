package com.ng.dtogen.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ng.dtogen.model.CompareRequest;
import com.ng.dtogen.service.DtoGeneratorServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/dto")
public class DtoGeneratorController {

    private final DtoGeneratorServiceImpl dtoGeneratorService;

    public DtoGeneratorController(DtoGeneratorServiceImpl dtoGeneratorService) {
        this.dtoGeneratorService = dtoGeneratorService;
    }

    @PostMapping(value = "/xml/generate" , consumes = "application/xml")
    public String generateXmlDto(
            @RequestBody String xml,
            @RequestParam String rootClassName,
            @RequestParam String prefix,
            @RequestParam(defaultValue = "false") boolean includeAnnotations
    ) throws Exception {

        return dtoGeneratorService.generateXmlDto(
                xml, rootClassName, prefix, includeAnnotations);
    }

    @PostMapping(value="/json/generate", consumes = "application/json")
    public String generateJsonDto(
            @RequestBody Object json,
            @RequestParam String rootClassName,
            @RequestParam String prefix,
            @RequestParam(defaultValue = "false") boolean includeAnnotations
    ) throws Exception {

        return dtoGeneratorService.generateJsonDto(
                json, rootClassName, prefix, includeAnnotations);
    }

    @PostMapping(value = "/soap/generate", consumes = "application/xml")
    public String generateSoapDto(
            @RequestBody String xml,
            @RequestParam String rootClassName,
            @RequestParam String prefix,
            @RequestParam(defaultValue = "false") boolean includeAnnotations
    ) throws Exception {

        return dtoGeneratorService.generateSoapDto(
                xml, rootClassName, prefix, includeAnnotations);
    }

    @PostMapping(value = "/json/compare",consumes = "application/json")
    public String compareJson(@RequestBody CompareRequest request) throws Exception {
        return dtoGeneratorService.compareJson(request);
    }
    
   
}
