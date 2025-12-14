package com.ng.dtogen.controller;

import com.ng.dtogen.model.CompareRequest;
import com.ng.dtogen.service.DtoGeneratorService;
import com.ng.dtogen.service.DtoGeneratorServiceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/dto")
@CrossOrigin(
	    origins = {
	        "http://localhost:3000",
	        "https://dtodeployment.onrender.com",
	        "https://dto-bifkn8qe7-dhruv-pals-projects-e2909998.vercel.app"
	    }
	)
public class DtoGeneratorController {

    private final DtoGeneratorServiceImpl dtoGeneratorService;

    public DtoGeneratorController(DtoGeneratorServiceImpl dtoGeneratorService) {
        this.dtoGeneratorService = dtoGeneratorService;
    }

    @PostMapping("/xml/generate")
    public String generateXmlDto(
            @RequestBody String xml,
            @RequestParam String rootClassName,
            @RequestParam String prefix,
            @RequestParam(defaultValue = "false") boolean includeAnnotations
    ) throws Exception {

        return dtoGeneratorService.generateXmlDto(
                xml, rootClassName, prefix, includeAnnotations);
    }

    @PostMapping("/json/generate")
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

    @PostMapping("/json/compare")
    public String compareJson(@RequestBody CompareRequest request) throws Exception {
        return dtoGeneratorService.compareJson(request);
    }
}
