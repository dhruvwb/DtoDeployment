package com.ng.dtogen.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ng.dtogen.model.CompareRequest;
import com.ng.dtogen.util.JsonComparatorJackson;
import com.ng.dtogen.util.JsonDtoGenerator;
import com.ng.dtogen.util.SoapDtoGenerator;
import com.ng.dtogen.util.XmlDtoGenerator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DtoGeneratorServiceImpl implements DtoGeneratorService {

    private final XmlDtoGenerator xmlDtoGenerator;
    private final JsonDtoGenerator jsonDtoGenerator;
    private final SoapDtoGenerator soapDtoGenerator;
    private final JsonComparatorJackson jsonComparator;
    private final ObjectMapper objectMapper;

    public DtoGeneratorServiceImpl(
            XmlDtoGenerator xmlDtoGenerator,
            JsonDtoGenerator jsonDtoGenerator,
            SoapDtoGenerator soapDtoGenerator,
            JsonComparatorJackson jsonComparator,
            ObjectMapper objectMapper
    ) {
        this.xmlDtoGenerator = xmlDtoGenerator;
        this.jsonDtoGenerator = jsonDtoGenerator;
        this.soapDtoGenerator = soapDtoGenerator;
        this.jsonComparator = jsonComparator;
        this.objectMapper = objectMapper;
    }

    @Override
    public String generateXmlDto(
            String xml,
            String rootClassName,
            String prefix,
            boolean includeAnnotations
    ) throws Exception {

        log.info("Generating XML DTO | rootClassName={}, prefix={}, includeAnnotations={}",
                rootClassName, prefix, includeAnnotations);

        String dto = xmlDtoGenerator.generateDtoFromXml(
                xml, rootClassName, prefix, includeAnnotations);

        log.info("XML DTO generation completed");
        return dto;
    }

    @Override
    public String generateJsonDto(
            Object json,
            String rootClassName,
            String prefix,
            boolean includeAnnotations
    ) throws Exception {

        log.info("Generating JSON DTO | rootClassName={}, prefix={}, includeAnnotations={}",
                rootClassName, prefix, includeAnnotations);

        String jsonString = objectMapper.writeValueAsString(json);

        String dto = jsonDtoGenerator.generateDtoFromJson(
                jsonString, rootClassName, prefix, includeAnnotations);

        log.info("JSON DTO generation completed");
        return dto;
    }

    @Override
    public String generateSoapDto(
            String xml,
            String rootClassName,
            String prefix,
            boolean includeAnnotations
    ) throws Exception {

        log.info("Generating SOAP DTO | rootClassName={}, prefix={}, includeAnnotations={}",
                rootClassName, prefix, includeAnnotations);

        String dto = soapDtoGenerator.generateDtoFromSoap(
                xml, rootClassName, prefix, includeAnnotations);

        log.info("SOAP DTO generation completed");
        return dto;
    }

    @Override
    public String compareJson(CompareRequest request) throws Exception {

        log.info("Comparing JSON payloads");

        JsonNode currentJson = objectMapper.readTree(
                objectMapper.writeValueAsString(request.getCurrentJson()));

        JsonNode expectedJson = objectMapper.readTree(
                objectMapper.writeValueAsString(request.getExpectedJson()));

        String result = jsonComparator.compareJson(currentJson, expectedJson);

        log.info("JSON comparison completed");
        return result;
    }
}
