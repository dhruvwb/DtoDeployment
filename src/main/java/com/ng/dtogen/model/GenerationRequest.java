package com.ng.dtogen.model;

import jakarta.validation.constraints.NotBlank;   // <-- add this line
import lombok.Data;

@Data
public class GenerationRequest {

    /** Raw payload (JSON / XML / SOAP envelope) */
    @NotBlank	
    private String payload;

    /** Base class name you’d like, e.g. "KycAudit" */
    @NotBlank
    private String rootClassName;
}
