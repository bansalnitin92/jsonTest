package com.jsonTest.jsonTest;



import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ApiResourceRestClient extends RelativeRestClient {

  public ApiResourceRestClient(HttpClient httpClient, String baseUrl) {
    super(httpClient, baseUrl);
  }

  /**
   * method to call get resource.
   * 
   * @param basePath base path.
   * @param queryParams map of query parameter.
   */
  public <T> T getResourceRestClient(String basePath, Map<String, Object> queryParams,
      Map<String, String> pathParams, Class<T> resourceClass, String auth) {
    final String updatedBasePath = getUpdatedBasePath(basePath, pathParams, queryParams);
    if (auth != null) {
      setHeaders(Arrays.asList(getAuthToken(auth)));
    }

    return get(getHttpRequest(updatedBasePath), resourceClass);
  }

  /**
   * method call for post request.
   * 
   * @param basePath base path.
   * @param queryParams query params.
   * @param pathParams path params.
   */
  public <T> T postResourceRestClient(String basePath, Map<String, Object> queryParams,
      Map<String, String> pathParams, Object obj, Class<T> resourceClass) {
    final String updatedBasePath = getUpdatedBasePath(basePath, pathParams, queryParams);
    return post(getHttpRequest(updatedBasePath), obj, resourceClass);
  }

  /**
   * method call for post request.
   * 
   * @param basePath base path.
   * @param queryParams query params.
   * @param pathParams path params.
   *
   */
  public <T> T postResourceRestClient(String basePath, Map<String, Object> queryParams,
      Map<String, String> pathParams, Object obj, Class<T> resourceClass, String auth) {
    final String updatedBasePath = getUpdatedBasePath(basePath, pathParams, queryParams);
    if (auth != null) {
      setHeaders(Arrays.asList(getAuthToken(auth)));
    }
    return post(getHttpRequest(updatedBasePath), obj, resourceClass);
  }

  /**
   * method call for put request.
   * 
   * @param basePath base path.
   * @param queryParams query params
   * @param pathParams path params
   * @param resource resource parameter.
   */
  public <T> T putResourceRestClient(String basePath, Map<String, Object> queryParams,
      Map<String, String> pathParams, Object resource, String auth) {
    final String updatedBasePath = getUpdatedBasePath(basePath, pathParams, queryParams);
    if (auth != null) {
      setHeaders(Arrays.asList(getAuthToken(auth)));
    }
    return put(getHttpRequest(updatedBasePath), resource);
  }

  /**
   * method call for delete post request.
   * 
   * @param basePath base path.
   * @param queryParams query params.
   * @param pathParams path params.
   */
  public void deleteResourceRestClient(String basePath, Map<String, Object> queryParams,
      Map<String, String> pathParams, String auth) {
    final String updatedBasePath = getUpdatedBasePath(basePath, pathParams, queryParams);
    if (auth != null) {
      setHeaders(Arrays.asList(getAuthToken(auth)));
    }
    delete(getHttpRequest(updatedBasePath));
  }

  private String getUpdatedBasePath(String basePath, Map<String, String> pathParams,
      Map<String, Object> queryParams) {
    String updatedBasePath = "";
    if (queryParams != null) {
      updatedBasePath = buildBasePath(queryParams);
    }
    updatedBasePath =
        StringUtils.isNotBlank(updatedBasePath) ? basePath + "?" + updatedBasePath : basePath;
    if (pathParams != null) {
      updatedBasePath = addPathParams(updatedBasePath, pathParams);
    }
    return updatedBasePath;
  }

  /**
   * method call for patch request.
   * 
   * @param basePath base path.
   * @param queryParams query params.
   * @param pathParams path params.
   */
  public <T> T patchResourceRestClient(String basePath, Map<String, Object> queryParams,
      Map<String, String> pathParams, Object obj, Class<T> resourceClass, String auth) {
    final String updatedBasePath = getUpdatedBasePath(basePath, pathParams, queryParams);
    if (auth != null) {
      setHeaders(Arrays.asList(getAuthToken(auth)));
    }
    return patch(getHttpRequest(updatedBasePath), obj, resourceClass);
  }

  /**
   * method to add path parameters to the base path.
   * 
   * @param path base path
   * @param pathParams path parameters
   * @return updated base path.
   */
  private String addPathParams(String path, Map<String, String> pathParams) {
    String updatedBasePath = path;
    for (final Map.Entry<String, String> pathParam : pathParams.entrySet()) {
      final String paramName = pathParam.getKey();
      updatedBasePath = updatedBasePath.replace("{" + paramName + "}", pathParam.getValue());
    }
    return updatedBasePath;
  }

  /**
   * method for building the base path if query parameters are provided.
   * 
   * @param queryParams map of query parameters
   * @return updated base path.
   */
  @SuppressWarnings("unchecked")
  private String buildBasePath(Map<String, Object> queryParams) {
    final StringBuilder pathBuilder = new StringBuilder();
    for (final Map.Entry<String, Object> queryParam : queryParams.entrySet()) {
      final String queryParamKey = queryParam.getKey();
      final Object queryParamValue = queryParam.getValue();
      if (queryParamValue instanceof List) {
        final List<String> queryParamList = (List<String>) queryParamValue;
        for (final String param : queryParamList) {
          pathBuilder.append(queryParamKey).append("=").append(param).append("&");
        }
      } else {
        pathBuilder.append(queryParamKey).append("=").append(queryParamValue).append("&");
      }
    }
    final int length = pathBuilder.length();
    if (pathBuilder.lastIndexOf("&") == length - 1) {
      pathBuilder.replace(length - 1, length, "");
    }
    return pathBuilder.toString();
  }

  /**
   * method to return jwt token to set Headers.
   * 
   * @param auth Authorization value.
   * @return jwt token
   */
  private NameValuePair getAuthToken(String auth) {
    return new BasicNameValuePair("Authorization", auth);
  }

}
