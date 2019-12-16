package com.jsonTest.jsonTest;

import com.jayway.jsonpath.JsonPath;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExParser {

  private static HashMap<String, String> varMap = new HashMap<String, String>();
  private static String REGEX_METHOD = "method";
  private static String REGEX_VAR = "var";

  /**
   * function to prase regex.
   * 
   * @param json json
   */
  public static String regexParser(String json) {

    Pattern pattern = Pattern.compile(TestDataConstants.PATTERN);
    Matcher matcher = pattern.matcher(json);
    StringBuffer replacedJson = new StringBuffer();

    while (matcher.find()) {
      String methodName = matcher.group(REGEX_METHOD);
      String varName = matcher.group(REGEX_VAR);
      matcher = (methodName != null)
          ? matcher.appendReplacement(replacedJson, runHelperMethods(methodName))
          : matcher.appendReplacement(replacedJson, getValue(varName));
    }
    matcher.appendTail(replacedJson);
    return replacedJson.toString();
  }

  /**
   * function to replace query parameters with response values.
   * 
   * @param json json
   * @param responseObj responseObj
   */
  public static String setQueryParameterswithResponseValues(String json, String responseObj) {
    Pattern pattern = Pattern.compile(TestDataConstants.DB_PATTERN);
    Matcher matcher = pattern.matcher(json);
    StringBuffer replacedJson = new StringBuffer();
    while (matcher.find()) {
      String paramName = matcher.group(1);
      matcher =
          matcher.appendReplacement(replacedJson, replacewithResponseValue(paramName, responseObj));
    }
    matcher.appendTail(replacedJson);
    return replacedJson.toString();
  }

  /**
   * function to assign value to context.
   * 
   * @param var var
   * @param value value
   */
  public static void assignValue(String var, String value) {
    varMap.put(var, value);
    TestDataConstants.CONTEXT.set(varMap);
  }

  /**
   * function to get value from context.
   * 
   * @param var var
   */

  public static String getValue(String var) {
    return TestDataConstants.CONTEXT.get().get(var);
  }

  private static String runHelperMethods(String methodName) {
    try {
      return (String) HelperMethods.class.getDeclaredMethod(methodName, null)
          .invoke(new HelperMethods(), null);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | NoSuchMethodException | SecurityException e) {
      throw InternalErrorException.builder().internalError().cause(e)
          .detail("Exception occurred while executing helper methods").build();
    }
  }

  private static String replacewithResponseValue(String paramName, String responseObj) {
    return JsonPath.parse(responseObj).read(paramName);
  }
}
