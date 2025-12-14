package com.ng.dtogen.service;

import com.ng.dtogen.detect.FormatDetector;
import com.ng.dtogen.model.*;
import com.ng.dtogen.persistence.StoredPayload;
import com.ng.dtogen.persistence.StoredPayloadRepository;
import com.ng.dtogen.templating.MustacheTemplateEngine;
import com.ng.dtogen.templating.FieldModel;
import com.ng.dtogen.util.CaseUtil;
import com.ng.dtogen.util.XmlUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class GeneratorService {

	private final List<FormatDetector> detectors;
	private final MustacheTemplateEngine template;
	private final StoredPayloadRepository repo;

	private final Map<String, String> classCache = new HashMap<>();

	public GenerationResponse process(GenerationRequest req) {
		FormatDetector detector = detectors.stream().filter(d -> d.canHandle(req.getPayload())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unknown format"));

		SupportedFormat format = detector.getFormat();
		String payload = req.getPayload();
		String generated;

		switch (format) {
		case XML:
			generated = handleXml(req.getRootClassName(), payload, format);
			break;
		case SOAP:
			generated = handleXml(req.getRootClassName(), payload, format); // Reuse XML logic
			break;
		case JSON:
			generated = handleJson(req.getRootClassName(), payload, format, detector);
			break;
		default:
			throw new UnsupportedOperationException("Unsupported format: " + format);
		}

		repo.save(StoredPayload.of(format, payload));
		return GenerationResponse.builder().detectedFormat(format).generatedSource(generated).build();
	}

	private String handleXml(String rootClassName, String payload, SupportedFormat format) {
		Element root = XmlUtil.parseRoot(payload);
		Map<String, Object> tree = XmlUtil.elementToMap(root);
		String rootClass = capitalize(rootClassName);
		GenerationArtifacts artifacts = buildDtosRecursively(rootClass, tree, format);
		StringBuilder sb = new StringBuilder();
		artifacts.classSources.forEach(sb::append);
		return sb.toString();
	}

	private String handleJson(String rootClassName, String payload, SupportedFormat format, FormatDetector detector) {
		Map<String, Object> jsonTree;

		try {
			String trimmed = payload.trim();
			String actualJson;

			// If payload is escaped string → unwrap it
			if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
				com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
				actualJson = mapper.readValue(trimmed, String.class);
			} else {
				actualJson = trimmed;
			}

			// Instead of calling detector.extractJsonTree, use Jackson directly
			com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
			@SuppressWarnings("unchecked")
			Map<String, Object> tree = mapper.readValue(actualJson, Map.class);
			jsonTree = tree;

		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid JSON payload: " + e.getMessage(), e);
		}

		String rootClass = capitalize(rootClassName);
		GenerationArtifacts artifacts = buildDtosRecursively(rootClass, jsonTree, format);
		StringBuilder sb = new StringBuilder();
		artifacts.classSources.forEach(sb::append);
		return sb.toString();
	}

	private GenerationArtifacts buildDtosRecursively(String className, Map<String, Object> structure,
			SupportedFormat format) {
		List<FieldModel> fields = new ArrayList<>();
		List<String> classSources = new ArrayList<>();

		int index = 0;
		int total = structure.size();

		for (Map.Entry<String, Object> entry : structure.entrySet()) {
			String orig = entry.getKey();
			String javaName = CaseUtil.toCamel(orig);
			Object val = entry.getValue();
			boolean isLast = (++index == total);

			if (val instanceof Map<?, ?> mapVal) {
// Check if map is primitive (all values are primitive types)
				boolean allPrimitive = mapVal.values().stream()
						.allMatch(v -> v == null || v instanceof String || v instanceof Number || v instanceof Boolean);
				if (allPrimitive) {
					Object firstVal = mapVal.values().stream()
	                        .filter(Objects::nonNull)
	                        .findFirst()
	                        .orElse(null); // use null instead of ""

					String valueType = inferJavaType(firstVal);
					fields.add(new FieldModel(orig, javaName, "Map<String," + valueType + ">", !orig.equals(javaName),
							!isLast));
				} else {
// Nested object
					String nestedClassName = capitalize(className) + capitalize(javaName);
					if (!classCache.containsKey(nestedClassName)) {
						@SuppressWarnings("unchecked")
						Map<String, Object> nestedMap = (Map<String, Object>) val;
						GenerationArtifacts nested = buildDtosRecursively(nestedClassName, nestedMap, format);
						classCache.put(nestedClassName, nested.getClassSource());
						classSources.addAll(nested.classSources);
					}
					fields.add(new FieldModel(orig, javaName, nestedClassName, !orig.equals(javaName), !isLast));
				}

			} else if (val instanceof List<?> list && !list.isEmpty()) {
				Object first = list.get(0);

				if (first instanceof Map<?, ?>) {
// Merge all keys in the list of maps
					String nestedClassName = capitalize(className) + capitalize(javaName);

					if (!classCache.containsKey(nestedClassName)) {
						Map<String, Object> mergedMap = new LinkedHashMap<>();
						for (Object obj : list) {
							@SuppressWarnings("unchecked")
							Map<String, Object> mapItem = (Map<String, Object>) obj;
							for (Map.Entry<String, Object> e : mapItem.entrySet()) {
								mergedMap.putIfAbsent(e.getKey(), e.getValue());
							}
						}
						GenerationArtifacts nested = buildDtosRecursively(nestedClassName, mergedMap, format);
						classCache.put(nestedClassName, nested.getClassSource());
						classSources.addAll(nested.classSources);
					}

					fields.add(new FieldModel(orig, javaName, "List<" + nestedClassName + ">", !orig.equals(javaName),
							!isLast));

				} else {
// List of primitives
					String javaType = inferJavaType(first);
					fields.add(
							new FieldModel(orig, javaName, "List<" + javaType + ">", !orig.equals(javaName), !isLast));
				}

			} else {
// Single primitive value
				String javaType = inferJavaType(val);
				fields.add(new FieldModel(orig, javaName, javaType, !orig.equals(javaName), !isLast));
			}
		}

		String dtoSource = template.renderDto(className, fields, format);
		classSources.add(dtoSource);
		return new GenerationArtifacts(fields, dtoSource, classSources);
	}

	private String inferJavaType(Object value) {
		if (value == null)
			return "String";
		if (value instanceof Integer || value instanceof Long)
			return "int";
		if (value instanceof Double || value instanceof Float)
			return "double";
		if (value instanceof Boolean)
			return "boolean";
		if (value instanceof String strVal) {
			strVal = strVal.trim();
			if (strVal.matches("^-?\\d+$"))
				return "Integer";
			if (strVal.matches("^-?\\d+\\.\\d+$"))
				return "Double";
			if (strVal.equalsIgnoreCase("true") || strVal.equalsIgnoreCase("false"))
				return "Boolean";
			if (strVal.matches("^\\d{4}-\\d{2}-\\d{2}.*"))
				return "LocalDate";
			return "String";
		}
		return "String";
	}

	private String inferJavaTypeForJson(Object value) {
		if (value == null)
			return "String";

		if (value instanceof Integer || value instanceof Long)
			return "int";
		if (value instanceof Double || value instanceof Float)
			return "double";
		if (value instanceof Boolean)
			return "boolean";

		if (value instanceof String strVal) {
			strVal = strVal.trim();
			if (strVal.matches("^-?\\d+$"))
				return "int";
			if (strVal.matches("^-?\\d+\\.\\d+$"))
				return "double";
			if (strVal.equalsIgnoreCase("true") || strVal.equalsIgnoreCase("false"))
				return "boolean";
			if (strVal.matches("^\\d{4}-\\d{2}-\\d{2}.*"))
				return "LocalDate";
			return "String";
		}

		return "String";
	}

	private String capitalize(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	private static class GenerationArtifacts {
		final List<FieldModel> topFields;
		final String classSource;
		final List<String> classSources;

		GenerationArtifacts(List<FieldModel> topFields, String classSource, List<String> classSources) {
			this.topFields = topFields;
			this.classSource = classSource;
			this.classSources = classSources;
		}

		String getClassSource() {
			return classSource;
		}
	}
}
