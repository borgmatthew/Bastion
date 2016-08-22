package rocks.bastion.core.json;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.http.entity.ContentType;
import rocks.bastion.core.*;
import rocks.bastion.core.resource.ResourceLoader;
import rocks.bastion.core.resource.ResourceNotFoundException;
import rocks.bastion.core.resource.UnreadableResourceException;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A JSON request for use during Bastion tests. The JSON body is supplied either inline or from a JSON file on the
 * classpath. Automatically sets the correct content type to use. This request will verify that the provided JSON body
 * contains valid JSON.
 */
public class JsonRequest implements HttpRequest {

    /**
     * Construct an HTTP request containing a JSON body from the given JSON string. Initially, the request will have
     * the "application/json" HTTP header and no other additional headers and no query parameters. It will also have
     * a descriptive name which is generated by combining the HTTP method with the URL.
     *
     * @param method The HTTP method to use for this request
     * @param url    The URL to send this request on
     * @param json   The JSON text to send as body content for this request
     * @return An HTTP request containing the specified JSON body text
     * @throws InvalidJsonException Thrown if the specified JSON text is not valid JSON text
     */
    public static JsonRequest fromString(HttpMethod method, String url, String json) throws InvalidJsonException {
        return new JsonRequest(method, url, json);
    }

    /**
     * Construct an HTTP request, using the POST method, containing a JSON body from the given JSON string. Initially,
     * the request will have the "application/json" HTTP header and no other additional headers and no query parameters.
     * It will also have a descriptive name which is generated by combining the HTTP method with the URL.
     *
     * @param url  The URL to send this request on
     * @param json The JSON text to send as body content for this request
     * @return An HTTP request containing the specified JSON body text
     * @throws InvalidJsonException Thrown if the specified JSON text is not valid JSON text
     */
    public static JsonRequest postFromString(String url, String json) throws InvalidJsonException {
        return fromString(HttpMethod.POST, url, json);
    }

    /**
     * Construct an HTTP request, using the PUT method, containing a JSON body from the given JSON string. Initially,
     * the request will have the "application/json" HTTP header and no other additional headers and no query parameters.
     * It will also have a descriptive name which is generated by combining the HTTP method with the URL.
     *
     * @param url  The URL to send this request on
     * @param json The JSON text to send as body content for this request
     * @return An HTTP request containing the specified JSON body text
     * @throws InvalidJsonException Thrown if the specified JSON text is not valid JSON text
     */
    public static JsonRequest putFromString(String url, String json) throws InvalidJsonException {
        return fromString(HttpMethod.PUT, url, json);
    }

    /**
     * <p>
     * Construct an HTTP request containing a JSON body that is read from the given resource. The resource source is specified
     * as a resource URL as described in {@link ResourceLoader}. Valid resource URLs include (but
     * are not limited to):
     * </p>
     * <ul>
     * <li>{@code classpath:/rocks/bastion/json/Sushi.json}</li>
     * <li>{@code file:/home/user/Sushi.json}</li>
     * </ul>
     * <p>
     * For more information about which resource URLs are accepted see the documentation for {@link ResourceLoader}.
     * </p>
     * <p>
     * Initially, the request will have the "application/json" HTTP header and no other additional headers and no query
     * parameters. It will also have a descriptive name which is generated by combining the HTTP method with the URL.
     * </p>
     *
     * @param method     The HTTP method to use for this request
     * @param url        The URL to send this request on
     * @param jsonSource The source URL to load the JSON text from, for this request
     * @return An HTTP request containing the specified JSON body text
     * @throws InvalidJsonException        Thrown if the specified JSON text is not valid JSON text
     * @throws UnreadableResourceException Thrown if the specified resource exists but cannot be read (because it is a directory, for example)
     * @throws ResourceNotFoundException   Thrown if the specified resource does not exist
     */
    public static JsonRequest fromResource(HttpMethod method, String url, String jsonSource) throws InvalidJsonException, UnreadableResourceException, ResourceNotFoundException {
        Objects.requireNonNull(jsonSource);
        return new JsonRequest(method, url, new ResourceLoader(jsonSource).load());
    }

    /**
     * <p>
     * Construct an HTTP request, using the POST method, containing a JSON body that is read from the given resource. The resource source
     * is specified as a resource URL as described in {@link ResourceLoader}. Valid resource URLs include (but
     * are not limited to):
     * </p>
     * <ul>
     * <li>{@code classpath:/rocks/bastion/json/Sushi.json}</li>
     * <li>{@code file:/home/user/Sushi.json}</li>
     * </ul>
     * <p>
     * For more information about which resource URLs are accepted see the documentation for {@link ResourceLoader}.
     * </p>
     * <p>
     * Initially, the request will have the "application/json" HTTP header and no other additional headers and no query parameters.
     * It will also have a descriptive name which is generated by combining the HTTP method with the URL.
     * </p>
     *
     * @param url        The URL to send this request on
     * @param jsonSource The resource URL to load the JSON text from, for this request
     * @return An HTTP request containing the specified JSON body text
     * @throws InvalidJsonException        Thrown if the specified JSON text is not valid JSON text
     * @throws UnreadableResourceException Thrown if the specified resource exists but cannot be read (because it is a directory, for example)
     * @throws ResourceNotFoundException   Thrown if the specified resource does not exist
     */
    public static JsonRequest postFromResource(String url, String jsonSource) throws InvalidJsonException, UnreadableResourceException, ResourceNotFoundException {
        return fromResource(HttpMethod.POST, url, jsonSource);
    }

    /**
     * <p>
     * Construct an HTTP request, using the PUT method, containing a JSON body that is read from the given resource. The resource source
     * is specified as a resource URL as described in {@link ResourceLoader}. Valid resource URLs include (but
     * are not limited to):
     * </p>
     * <ul>
     * <li>{@code classpath:/rocks/bastion/json/Sushi.json}</li>
     * <li>{@code file:/home/user/Sushi.json}</li>
     * </ul>
     * <p>
     * For more information about which resource URLs are accepted see the documentation for {@link ResourceLoader}.
     * </p>
     * <p>
     * Initially,
     * the request will have the "application/json" HTTP header and no other additional headers and no query parameters.
     * It will also have a descriptive name which is generated by combining the HTTP method with the URL.
     *
     * @param url        The URL to send this request on
     * @param jsonSource The resource URL to load the JSON text from, for this request
     * @return An HTTP request containing the specified JSON body text
     * @throws InvalidJsonException        Thrown if the specified JSON text is not valid JSON text
     * @throws UnreadableResourceException Thrown if the specified resource exists but cannot be read (because it is a directory, for example)
     * @throws ResourceNotFoundException   Thrown if the specified resource does not exist
     */
    public static JsonRequest putFromResource(String url, String jsonSource) throws InvalidJsonException, UnreadableResourceException, ResourceNotFoundException {
        return fromResource(HttpMethod.PUT, url, jsonSource);
    }

    /**
     * <p>
     * Similar to the {@link #fromTemplate(HttpMethod, String, String, Map)} method but also supports template variable replacements. This allows you
     * to store a request template as a JSON file where the template is written using <a href="https://mustache.github.io/">Mustache</a>. Each variable's
     * value is supplied as the {@code variableAssignments} parameter in a map. If the template cannot resolve a variable, an exception is thrown.
     * </p>
     * <p>
     * The template file is specified as a resource URL as described in {@link ResourceLoader}. Valid resource URLs include (but
     * are not limited to):
     * </p>
     * <ul>
     * <li>{@code classpath:/rocks/bastion/json/Sushi.json}</li>
     * <li>{@code file:/home/user/Sushi.json}</li>
     * </ul>
     * <p>
     * For more information about which resource URLs are accepted see the documentation for {@link ResourceLoader}.
     * </p>
     * <p>
     * Initially,
     * the request will have the "application/json" HTTP header and no other additional headers and no query parameters.
     * It will also have a descriptive name which is generated by combining the HTTP method with the URL.
     * </p>
     *
     * @param method              The HTTP method to use for this request
     * @param url                 The URL to send this request on
     * @param jsonTemplateSource  The resource URL to load the template file from, for this request
     * @param variableAssignments The values used when resolving variables in the loaded template
     * @return An HTTP request containing the specified JSON body text
     * @throws InvalidJsonException         Thrown if the specified JSON text is not valid JSON text
     * @throws UnreadableResourceException  Thrown if the specified resource exists but cannot be read (because it is a directory, for example)
     * @throws ResourceNotFoundException    Thrown if the specified resource does not exist
     * @throws TemplateCompilationException Thrown if a variable in the loaded template does not have an assignment in the {@code variableAssignments} map
     */
    public static JsonRequest fromTemplate(HttpMethod method, String url, String jsonTemplateSource, Map<String, String> variableAssignments) {
        TemplateContentCompiler compiler = new TemplateContentCompiler(new ResourceLoader(jsonTemplateSource).load());
        compiler.addAllVariableAssignments(variableAssignments);
        return new JsonRequest(method, url, compiler.getContent());
    }

    public static JsonRequest postFromTemplate(String url, String jsonTemplateSource, Map<String, String> variableAssignments) {
        return fromTemplate(HttpMethod.POST, url, jsonTemplateSource, variableAssignments);
    }

    public static JsonRequest putFromTemplate(String url, String jsonTemplateSource, Map<String, String> variableAssignments) {
        return fromTemplate(HttpMethod.PUT, url, jsonTemplateSource, variableAssignments);
    }

    public static JsonRequest patchFromTemplate(String url, String jsonTemplateSource, Map<String, String> variableAssignments) {
        return fromTemplate(HttpMethod.PATCH, url, jsonTemplateSource, variableAssignments);
    }

    public static JsonRequest deleteFromTemplate(String url, String jsonTemplateSource, Map<String, String> variableAssignments) {
        return fromTemplate(HttpMethod.DELETE, url, jsonTemplateSource, variableAssignments);
    }

    private CommonRequestAttributes requestAttributes;

    protected JsonRequest(HttpMethod method, String url, String json) throws InvalidJsonException {
        Objects.requireNonNull(method);
        Objects.requireNonNull(url);
        Objects.requireNonNull(json);

        requestAttributes = new CommonRequestAttributes(method, url, json);
        requestAttributes.setContentType(ContentType.APPLICATION_JSON);

        validateJson();
    }

    /**
     * Override the content-type that will be used for this request. Initially, the content-type for a {@code JSONRequest}
     * is "application/json" but you can override what is sent using this method.
     *
     * @param contentType A content-type to use for this request. Can be {@literal null}.
     * @return This request (for method chaining)
     */
    public JsonRequest overrideContentType(ContentType contentType) {
        requestAttributes.setContentType(contentType);
        return this;
    }

    /**
     * Add a new HTTP header that will be sent with this request.
     *
     * @param name  A non-{@literal null} name for the new header
     * @param value A non-{@literal null} value for the new header
     * @return This request (for method chaining)
     */
    public JsonRequest addHeader(String name, String value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        requestAttributes.addHeader(name, value);
        return this;
    }

    /**
     * Add a new HTTP query parameter that will be sent with this request.
     *
     * @param name  A non-{@literal null} name for the new query parameter
     * @param value A non-{@literal null} value for the new query parameter
     * @return This request (for method chaining)
     */
    public JsonRequest addQueryParam(String name, String value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        requestAttributes.addQueryParam(name, value);
        return this;
    }

    /**
     * Add a new HTTP route parameter that will be sent with this request. Put a placeholder for the route parameter in
     * the request URL by surrounding a parameter's name using braces (eg. {@code http://sushi.test/{id}/ingredients}).
     * The URL in the previous example contains one route param which can be replaced with a numerical value using
     * {@code addRouteParam("id", "53")}, for example.
     *
     * @param name  A non-{@literal null} name for the new route parameter
     * @param value A non-{@literal null} value for the new route parameter
     * @return This request (for method chaining)
     */
    public JsonRequest addRouteParam(String name, String value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        requestAttributes.addRouteParam(name, value);
        return this;
    }

    @Override
    public String name() {
        return requestAttributes.name();
    }

    @Override
    public String url() {
        return requestAttributes.url();
    }

    @Override
    public HttpMethod method() {
        return requestAttributes.method();
    }

    @Override
    public Optional<ContentType> contentType() {
        return requestAttributes.contentType();
    }

    @Override
    public Collection<ApiHeader> headers() {
        return requestAttributes.headers();
    }

    @Override
    public Collection<ApiQueryParam> queryParams() {
        return requestAttributes.queryParams();
    }

    @Override
    public Collection<RouteParam> routeParams() {
        return requestAttributes.routeParams();
    }

    @Override
    public Object body() {
        return requestAttributes.body();
    }

    private void validateJson() throws InvalidJsonException {
        String jsonBody = requestAttributes.body().toString();
        try {
            new JsonParser().parse(jsonBody);
        } catch (JsonSyntaxException parseException) {
            throw new InvalidJsonException(parseException, jsonBody);
        }
    }
}
