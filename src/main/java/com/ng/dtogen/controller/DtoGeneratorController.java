package com.ng.dtogen.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ng.dtogen.model.CompareRequest;
import com.ng.dtogen.model.JsonDtoRequest;
import com.ng.dtogen.util.JsonComparatorJackson;
import com.ng.dtogen.util.JsonDtoGenerator;
import com.ng.dtogen.util.SoapDtoGenerator;
import com.ng.dtogen.util.XmlDtoGenerator;

@RestController
@RequestMapping("/dto")
public class DtoGeneratorController {

	private final XmlDtoGenerator dtoGenerator;
	private final JsonDtoGenerator jsonDtoGenerator;
	private final SoapDtoGenerator soapDtoGenerator;
	private final JsonComparatorJackson jsonComparatorJackson;
	private final ObjectMapper objectMapper;
	// ✅ Constructor Injection
	public DtoGeneratorController(XmlDtoGenerator dtoGenerator, JsonDtoGenerator jsonDtoGenerator,
			SoapDtoGenerator soapDtoGenerator,JsonComparatorJackson jsonComparatorJackson,ObjectMapper objectMapper) {
		this.dtoGenerator = dtoGenerator;
		this.jsonDtoGenerator = jsonDtoGenerator;
		this.soapDtoGenerator = soapDtoGenerator;
		this.jsonComparatorJackson = jsonComparatorJackson;
		this.objectMapper = objectMapper;
	}

	/**
	 * POST endpoint to generate DTO from XML
	 *
	 * URL: POST /dto/Xml/generate Body: { "xml": "<root>...</root>", "rootPrefix":
	 * "My" }
	 */
	@PostMapping("/xml/generate")
	public String generateDto(@RequestBody String xml, @RequestParam String rootClassName, @RequestParam String prefix,
			@RequestParam(defaultValue = "false") boolean includeAnnotations) throws Exception {
		return dtoGenerator.generateDtoFromXml(xml, rootClassName, prefix, includeAnnotations);
	}

	@PostMapping("/json/generate")
	public String generateJsonDto(@RequestBody JsonDtoRequest request) throws Exception {
		String jsonString = new ObjectMapper().writeValueAsString(request.getJson());
		return jsonDtoGenerator.generateDtoFromJson(jsonString, request.getRootPrefix(), request.getRootClass());
	}

	@PostMapping(value = "/soap/generate", consumes = "application/xml")
	public String generateSoapDto(@RequestBody String xml, @RequestParam String rootPrefix,
			@RequestParam String rootClass) throws Exception {
		return soapDtoGenerator.generateDtoFromSoap(xml, rootPrefix, rootClass);
	}

    @PostMapping("/json/compare")
    public String compareJson(@RequestBody CompareRequest request) throws Exception {
    	 JsonNode currentJson = objectMapper.readTree(
    			 objectMapper.writeValueAsString(request.getCurrentJson())
         );

         JsonNode expectedJson = objectMapper.readTree(
        		 objectMapper.writeValueAsString(request.getExpectedJson())
         );

         return jsonComparatorJackson.compareJson(currentJson, expectedJson);
    }
}
