package com.jsonTest.jsonTest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class TestCaseDto {

  private String url;
  private List<String> prerequisite;
  private String testName;
  private String method;
  private Map<String, String> postProcessor;
  private VerifyDto verify;
  private RequestDto request;

  public static class VerifyDto {
    private int httpStatus;
    private String responseResourceType;
    private Map<String, String> assertions;
    private Map<String, List<String>> dbAssertions;

    /**
     * parameterized constructor.
     */
    @JsonCreator
    public VerifyDto(@JsonProperty("responseResourceType") String responseResourceType,
        @JsonProperty("assertions") Map<String, String> assertions,
        @JsonProperty("httpStatus") int httpStatus,
        @JsonProperty("dbAssertions") Map<String, List<String>> dbAssertions) {
      this.responseResourceType = responseResourceType;
      this.assertions = assertions;
      this.httpStatus = httpStatus;
      this.dbAssertions = dbAssertions;
    }

    public String getResponseResourceType() {
      return responseResourceType;
    }

    public Map<String, String> getAssertions() {
      return assertions;
    }

    public int getHttpStatus() {
      return httpStatus;
    }

    public Map<String, List<String>> getDbAssertions() {
      return dbAssertions;
    }
  }

  public static class RequestDto {
    private Map<String, String> requestModificationBody;
    private String requestResource;

    /**
     * parameterized constructor.
     */
    @JsonCreator
    public RequestDto(
        @JsonProperty("requestModificationBody") Map<String, String> requestModificationBody,
        @JsonProperty("requestResource") String requestResource) {
      this.requestModificationBody = requestModificationBody;
      this.requestResource = requestResource;
    }

    public Map<String, String> getRequestModificationBody() {
      return requestModificationBody;
    }

    public String getRequestResource() {
      return requestResource;
    }
  }

  /**
   * parameterized constructor.
   */
  @JsonCreator
  public TestCaseDto(@JsonProperty("url") String url,
      @JsonProperty("prerequisite") List<String> prerequisite,
      @JsonProperty("testName") String testName, @JsonProperty("method") String method,
      @JsonProperty("postProcessor") Map<String, String> postProcessor,
      @JsonProperty("verify") VerifyDto verify, @JsonProperty("request") RequestDto request) {
    this.url = url;
    this.prerequisite = prerequisite;
    this.testName = testName;
    this.method = method;
    this.postProcessor = postProcessor;
    this.verify = verify;
    this.request = request;
  }

  public String getUrl() {
    return url;
  }

  public List<String> getPrerequisite() {
    return prerequisite;
  }

  public String getTestName() {
    return testName;
  }

  public String getMethod() {
    return method;
  }

  public Map<String, String> getPostProcessor() {
    return postProcessor;
  }

  public VerifyDto getVerify() {
    return verify;
  }

  public RequestDto getRequest() {
    return request;
  }

}
