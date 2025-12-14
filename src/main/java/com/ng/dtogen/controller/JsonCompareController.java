// controller/JsonCompareController.java
package com.ng.dtogen.controller;

import com.ng.dtogen.model.JsonCompareRequest;
import com.ng.dtogen.model.JsonCompareResponse;
import com.ng.dtogen.service.JsonCompareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")  // Allow React frontend
public class JsonCompareController {

    @Autowired
    private JsonCompareService jsonCompareService;

    @PostMapping("/compareJson")
    public JsonCompareResponse compareJson(@RequestBody JsonCompareRequest request) {
        return jsonCompareService.compare(request.getLeft(), request.getRight());
    }
}
