package com.jsonTest.jsonTest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.HashMap;
import java.util.Map;

public class TestDataConstants {

  public static final String VERIFICATION_FAILED = "verification failed";
  public static final String RESOURCE_FILE_PATH = "src/componentTest/resources/";
  public static final String TESTCASE_FILE_PATH = "src/componentTest/resources/testCases/";
  public static final String REQUEST_RESOURCE_PATH = "src/componentTest/resources/requestResource/";
  public static final String DB_VALIDATION_PATH =
      "src/componentTest/resources/testCases/dbValidations.json";
  public static final String METHOD_PATTERN = "\\{\\{" + "(?<method>.+?)" + "\\}\\}";
  public static final String VARIABLE_PATTERN = "\\{[$]" + "(?<var>.+?)" + "\\}";
  public static final String PATTERN = METHOD_PATTERN + "|" + VARIABLE_PATTERN;
  public static final String DB_PATTERN = "\\$." + "([^']+)";

  public static final ThreadLocal<HashMap<String, String>> CONTEXT =
      new ThreadLocal<HashMap<String, String>>();

  public static final ObjectMapper MAPPER = getMapper();
  public static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE =
      new TypeReference<Map<String, Object>>() {
      };

  private static ObjectMapper getMapper() {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JodaModule());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, true);
    objectMapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    return objectMapper;
  }

}
