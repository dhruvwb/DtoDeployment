	package com.ng.dtogen.model;
	
	import lombok.Builder;
	import lombok.Data;
	
	@Data
	@Builder
	public class GenerationResponse {
	    private SupportedFormat detectedFormat; // JSON / XML / SOAP
	    private String generatedSource;         // DTO class text
	}
