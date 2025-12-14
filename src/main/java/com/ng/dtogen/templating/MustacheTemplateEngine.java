package com.ng.dtogen.templating;

import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.ng.dtogen.model.SupportedFormat;

@Service
public class MustacheTemplateEngine {

    private final Mustache dtoTpl;

    public MustacheTemplateEngine() {
        MustacheFactory mf = new DefaultMustacheFactory();
        dtoTpl = mf.compile("templates/dto.mustache");
    }
    

    private final MustacheFactory mustacheFactory = new DefaultMustacheFactory();

    public String renderDto(String className, List<FieldModel> fields) {
        Map<String, Object> context = new HashMap<>();
        context.put("className", className);
        context.put("fields", fields);

        StringWriter writer = new StringWriter();
        dtoTpl.execute(writer, context);
        return writer.toString();
    }

    
    public String renderDto(String className, List<FieldModel> fields, SupportedFormat format) {
        Map<String, Object> context = new HashMap<>();
        context.put("className", className);
        context.put("fields", fields);

        // Include format-specific logic if needed
        switch (format) {
            case JSON:
                context.put("json", true);
                break;
            case XML:
                context.put("xml", true);
                break;
            case SOAP:
                context.put("soap", true);
                break;
            default:
                break;
        }

        Mustache mustache = mustacheFactory.compile("templates/dto.mustache");
        StringWriter writer = new StringWriter();
        mustache.execute(writer, context);
        return writer.toString();
    }
    
    public String renderDto(String className, Map<String, String> fields, SupportedFormat format) {
        List<String> keys = new ArrayList<>(fields.keySet());
        List<FieldModel> fieldModels = new ArrayList<>();

        for (int i = 0; i < keys.size(); i++) {
            String orig = keys.get(i);
            String java = fields.get(orig);
            boolean needsAnnotation = !orig.equals(java);
            boolean comma = (i < keys.size() - 1);
            String javaType = "String"; // Default type, or modify this based on logic
            fieldModels.add(new FieldModel(orig, java, javaType, needsAnnotation, comma));

        }

        Map<String, Object> ctx = Map.of(
            "className", className,
            "isJson", format == SupportedFormat.JSON,
            "isXml", format == SupportedFormat.XML || format == SupportedFormat.SOAP,
            "fields", fieldModels
        );

        StringWriter w = new StringWriter();
        dtoTpl.execute(w, ctx);
        return w.toString();
    }
}
