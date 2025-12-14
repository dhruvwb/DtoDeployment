package com.ng.dtogen.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ng.dtogen.model.GenerationRequest;
import com.ng.dtogen.model.GenerationResponse;
import com.ng.dtogen.service.DtoGeneratorService;
import com.ng.dtogen.service.GeneratorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GeneratorController {

    private final GeneratorService generatorService;

    private final DtoGeneratorService dtoGenSer;
    
//    @PostMapping("/generate")
    @PostMapping(
    	    value = "/generate",
    	    consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE },
    	    produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    	)
    public ResponseEntity<GenerationResponse> generate(@Validated @RequestBody GenerationRequest req) {
        GenerationResponse response = generatorService.process(req);
//        if (response.getResponseCode() == 200) {
//			response.setBody(new ObjectMapper().readValue(response.getBody().toString(), Object.class));
//		}
        return ResponseEntity.ok(response);
    }
    
     
    
    
}
