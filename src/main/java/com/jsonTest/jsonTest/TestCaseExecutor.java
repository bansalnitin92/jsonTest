package com.jsonTest.jsonTest;


import com.paysafe.op.commons.test.verify.VerifyUtil;
import com.paysafe.op.errorhandling.exceptions.InternalErrorException;
import com.paysafe.ss.ledger.component.dto.TestCaseDto;
import com.paysafe.ss.ledger.component.dto.TestCaseDto.VerifyDto;
import com.paysafe.ss.ledger.component.restClient.ApiResourceRestClient;
import com.paysafe.ss.ledger.component.verify.VerifyAnnotation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Parse Test Cases from JSON file.
 * 
 * @author nitinkumar
 */

@SuppressWarnings("rawtypes")
public class TestCaseExecutor {

  private ApiResourceRestClient apiRestClient;
  private Map<String, Class<?>> classMap;
  private Map<String, TestCaseDto> testCaseMap;
  private DbQueries dbQueries;
  private String authToken;

  /**
   * Initialize the parser through component test.
   * 
   * @param apiRestClient apiRestClient
   * @param classMap classMap
   */

  public TestCaseExecutor(ApiResourceRestClient apiRestClient, Map<String, Class<?>> classMap, DbQueries dbQueries) {
    this.apiRestClient = apiRestClient;
    this.classMap = classMap;
    this.dbQueries = dbQueries;
  }

  /**
   * function to parse test case file.
   * 
   * @param fileName fileName.
   */

  public void parseTestCase(String fileName) {
    List<TestCaseDto> testCases =
        Arrays.asList(readFile(TestDataConstants.TESTCASE_FILE_PATH + fileName, TestCaseDto[].class));
    testCaseMap = new HashMap<>();
    for (TestCaseDto testCase : testCases) {
      testCaseMap.put(testCase.getTestName(), testCase);
    }
  }

  /**
   * function to run test case.
   * 
   * @param testName testName.
   */

  public <T> void runTestCase(String testName) {
    TestCaseDto testCase = testCaseMap.get(testName);
    if (Objects.nonNull(testCase)) {
      runPrerequisiteSteps(testCase.getPrerequisite());
      Object requestResource = getRequestObject(testCase.getRequest());
      String method = testCase.getMethod();
      VerifyDto verify = testCase.getVerify();
      Map<String, Object> queryParams = null;
      Map<String, String> pathParams = null;
      T responseObj = execute(method, testCase.getUrl(), requestResource, getResponseResourceType(verify), queryParams,
          pathParams, authToken);
      if (Objects.nonNull(verify)) {
        runDefaultAssertions(verify.getDefaultAssertions(), responseObj);
        verifyObject(verify.getResponseAssertions(), flatMap("$", responseObj));
        runDbAssertions(verify.getDbAssertions(), responseObj);
      }
      runPostProcessor(testCase.getPostProcessor(), responseObj);
    } else {
      throw InternalErrorException.builder().internalError().detail("Test case is not present in the file").build();
    }
  }

  /**
   * function to read file
   * 
   * @param fileUrl fileUrl.
   * @param type type.
   */

  public <T> T readFile(String fileUrl, Class<T> type) {
    try {
      String jsonString = readjsonFile(fileUrl);
      jsonString = RegExParser.regexParser(jsonString);
      return TestDataConstants.MAPPER.readValue(jsonString, type);
    } catch (IOException e) {
      throw InternalErrorException.builder().internalError().cause(e).detail("Exception occurred while reading file")
          .build();
    }
  }

  /**
   * function to set Auth Headers
   * 
   * @param authToken authToken.
   */

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  private <T> T execute(String method, String url, Object requestResource, Class<T> responseResourceType,
      Map<String, Object> queryParams, Map<String, String> pathParams, String auth) {

    switch (method) {
    case "GET":
      return apiRestClient.getResourceRestClient(url, queryParams, pathParams, responseResourceType, auth);
    case "POST":
      return apiRestClient.postResourceRestClient(url, queryParams, pathParams, requestResource, responseResourceType,
          auth);
    case "PUT":
      return apiRestClient.putResourceRestClient(url, queryParams, pathParams, requestResource, auth);
    case "DELETE":
      apiRestClient.deleteResourceRestClient(url, queryParams, pathParams, auth);
      return null;
    case "PATCH":
      return apiRestClient.patchResourceRestClient(url, queryParams, pathParams, requestResource, responseResourceType,
          auth);
    default:
      throw new RuntimeException("invalid method");
    }
  }

  private <T> T getResponseResourceType(TestCaseDto.VerifyDto verify) {
    return (T) classMap.get(verify.getResponseResourceType());
  }

  private Object getRequestObject(TestCaseDto.RequestDto request) {

    Map<String, String> requestBodyMap = request.getRequestModificationBody();
    String requestResource = request.getRequestResource();
    Object requestObj =
        readFile(TestDataConstants.REQUEST_RESOURCE_PATH + requestResource, classMap.get(requestResource));
    if (requestBodyMap == null) {
      return requestObj;
    }

    for (Map.Entry<String, String> entry : requestBodyMap.entrySet()) {
      requestObj = JsonPath.parse(convertObjectToJsonString(requestObj)).set(entry.getKey(), entry.getValue()).json();
    }
    return requestObj;
  }

  private Map<String, String> flatMap(String parentKey, Object valueToConvert) {
    Map<String, Object> objectMap =
        TestDataConstants.MAPPER.convertValue(valueToConvert, TestDataConstants.MAP_TYPE_REFERENCE);
    String prefixKey = parentKey != null ? parentKey + "." : "";
    HashMap<String, String> flatmap = new HashMap<>();
    for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
      if (entry.getValue() instanceof String) {
        flatmap.put((prefixKey + entry.getKey()), (String) entry.getValue());
      }
      if (entry.getValue() instanceof Map) {
        flatmap.putAll(flatMap(prefixKey + entry.getKey(), (Map<String, Object>) entry.getValue()));
      }
    }
    return flatmap;
  }

  private void verifyObject(Map<String, String> expected, Map<String, String> actual) {
    for (final Map.Entry<String, String> entry : expected.entrySet()) {
      String expectedResponse = entry.getValue();
      String actualResponse = actual.get(entry.getKey());
      VerifyUtil.verifyEquals(entry.getKey(), expectedResponse, actualResponse);

    }
  }

  private <T> void runPrerequisiteSteps(List<String> steps) {
    if (Objects.isNull(steps)) {
      return;
    }
    for (String fileName : steps) {
      TestCaseDto testCase = readFile(TestDataConstants.TESTCASE_FILE_PATH + fileName, TestCaseDto.class);
      String url = testCase.getUrl();
      Object requestResource = (testCase.getRequest() != null) ? getRequestObject(testCase.getRequest()) : null;
      String method = testCase.getMethod();
      TestCaseDto.VerifyDto verify = testCase.getVerify();
      Map<String, Object> queryParams = null;
      Map<String, String> pathParams = null;
      T responseObj =
          execute(method, url, requestResource, getResponseResourceType(verify), queryParams, pathParams, authToken);
      runPostProcessor(testCase.getPostProcessor(), responseObj);
    }
  }

  private void runPostProcessor(Map<String, String> postProcessor, Object response) {
    if (Objects.isNull(postProcessor)) {
      return;
    }
    for (Map.Entry<String, String> entry : postProcessor.entrySet()) {
      if (entry.getValue().contains("$")) {
        String responseValue = flatMap("$", response).get(entry.getValue());
        RegExParser.assignValue(entry.getKey(), responseValue);
      } else {
        RegExParser.assignValue(entry.getKey(), entry.getValue());
      }
    }
  }

  private void runDbAssertions(Map<String, List<String>> dbAssertions, Object responseObj) {
    Map<String, Map<String, String>> map;
    try {
      String jsonString = readjsonFile(TestDataConstants.DB_VALIDATION_PATH);
      map = TestDataConstants.MAPPER.readValue(jsonString, new TypeReference<Map<String, Map<String, String>>>() {
      });
    } catch (IOException e) {
      throw InternalErrorException.builder().internalError().cause(e).detail("Exception occurred while reading file")
          .build();
    }
    for (Map.Entry<String, List<String>> entry : dbAssertions.entrySet()) {
      List<String> expectedValue = entry.getValue();
      int i = 0;
      for (Map.Entry<String, String> dbAssertion : map.get(entry.getKey()).entrySet()) {
        String query = RegExParser.setQueryParameterswithResponseValues(dbAssertion.getKey(),
            convertObjectToJsonString(responseObj));
        Object dbValue = dbQueries.executeSelectQuery(query);
        VerifyUtil.verifyEquals(TestDataConstants.VERIFICATION_FAILED, expectedValue.get(i++), dbValue);
      }
    }
  }

  private void runDefaultAssertions(String jsonFile, Object responseObj) {
    if (Objects.isNull(jsonFile)) {
      return;
    }
    Map<String, String> map;
    String jsonString = readjsonFile(TestDataConstants.RESPONSE_RESOURCE_PATH + jsonFile);
    try {
      map = TestDataConstants.MAPPER.readValue(jsonString, new TypeReference<Map<String, String>>() {
      });
      for (Map.Entry<String, String> entry : map.entrySet()) {
        Object responseValue = JsonPath.parse(convertObjectToJsonString(responseObj)).read(entry.getKey());
        if (entry.getValue().contains("@")) {
          verifyValidationAnnotation(responseValue, entry.getValue());
        } else {
          VerifyUtil.verifyEquals(entry.getKey(), entry.getValue(), responseValue);
        }
      }
    } catch (IOException e) {
      throw InternalErrorException.builder().internalError().cause(e)
          .detail("Exception occurred while converting into map").build();
    }
  }

  private String convertObjectToJsonString(Object object) {
    try {
      return TestDataConstants.MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw InternalErrorException.builder().internalError().cause(e)
          .detail("Exception occurred while converting object to json").build();
    }
  }

  private String readjsonFile(String jsonFile) {
    try {
      return new String(Files.readAllBytes(Paths.get(jsonFile)), Charset.forName("UTF-8"));
    } catch (IOException e) {
      throw InternalErrorException.builder().internalError().cause(e).detail("Exception occurred while reading file")
          .build();
    }
  }

  private void verifyValidationAnnotation(Object responseValue, String annotation) {

    switch (annotation) {
    case "@NotNull":
      VerifyUtil.verifyNotNull(TestDataConstants.VERIFICATION_FAILED, responseValue);
      break;
    case "@uuid":
      VerifyAnnotation.verifyUuid(responseValue.toString());
      break;
    default:
      throw new RuntimeException("Annotation not valid");
    }
  }

}
