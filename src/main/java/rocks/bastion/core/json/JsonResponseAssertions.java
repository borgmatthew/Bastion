package rocks.bastion.core.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import rocks.bastion.core.*;
import rocks.bastion.core.resource.ResourceLoader;
import rocks.bastion.core.resource.ResourceNotFoundException;
import rocks.bastion.core.resource.UnreadableResourceException;

import java.io.IOException;
import java.util.*;

import static java.lang.String.format;

/**
 * Performs assertions on a response by checking for status code and content-type. The user supplies a JSON string
 * that will act as the expected body of the response. Bastion will then compare the actual received JSON string
 * with the user-supplied expectation. The comparison will not be a straight-up string equality check though: Bastion
 * will interpret the JSON of both the expected body and the actual response body and perform a structural comparison. This
 * is important, because the JSON specification says that a JSON object is unordered and we do not want a different
 * order of properties to fail the assertion.
 *
 * @author <a href="mailto:mail@kylepullicino.com">Kyle</a>
 */
public class JsonResponseAssertions implements Assertions<Object> {

    /**
     * Creates a {@code JsonResponseAssertions} object which expects the specified response HTTP status code and the specified JSON.
     * The supplied JSON must be syntactically correct; otherwise, an exception is thrown to indicate that the expected JSON string
     * is invalid.
     *
     * @param expectedStatusCode The expected HTTP status code
     * @param expectedJson       The expected JSON object
     * @return An assertions object for use with the {@link rocks.bastion.core.builder.AssertionsBuilder#withAssertions(Assertions)} method
     * @throws InvalidJsonException Thrown if the given JSON text is not valid JSON text
     */
    public static JsonResponseAssertions fromString(int expectedStatusCode, String expectedJson) {
        return new JsonResponseAssertions(expectedStatusCode, expectedJson);
    }

    /**
     * Creates a {@code JsonResponseAssertions} object which expects the specified response HTTP status code and JSON, which
     * is loaded from the specified resource. The resource source is specified
     * as a resource URL as described in {@link ResourceLoader}. Valid resource URLs include (but
     * are not limited to):
     * <ul>
     * <li>{@code classpath:/rocks/bastion/json/Sushi.json}</li>
     * <li>{@code file:/home/user/Sushi.json}</li>
     * </ul>
     *
     * For more information about which resource URLs are accepted see the documentation for {@link ResourceLoader}.
     * <p>
     * The loaded JSON must be syntactically correct; otherwise, an exception is thrown to indicate that the expected JSON string
     * is invalid.
     *
     * @param expectedStatusCode The expected HTTP status code
     * @param expectedJsonSource The resource to load the expected JSON object from
     * @return An assertions object for use with the {@link rocks.bastion.core.builder.AssertionsBuilder#withAssertions(Assertions)} method
     * @throws InvalidJsonException        Thrown if the loaded JSON text is not valid JSON text
     * @throws UnreadableResourceException Thrown if the specified resource exists but cannot be read (because it is a directory, for example)
     * @throws ResourceNotFoundException   Thrown if the specified resource does not exist
     */
    public static JsonResponseAssertions fromResource(int expectedStatusCode, String expectedJsonSource) {
        Objects.requireNonNull(expectedJsonSource);
        return new JsonResponseAssertions(expectedStatusCode, new ResourceLoader(expectedJsonSource).load());
    }

    /**
     * Similar to the {@link #fromResource(int, String)} method but also supports template variable assignments.
     * This allows you to store an expected JSON object's template as a JSON file where the template
     * is written using <a href="https://mustache.github.io/">Mustache</a>. Each variable's value is supplied as the
     * {@code variableAssignments} parameter in a map. If the template cannot resolve a variable, an exception is thrown.
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
     * The loaded JSON, after variables are resolved, must be syntactically correct; otherwise, an exception is
     * thrown to indicate that the expected JSON string is invalid.
     * </p>
     *
     * @param expectedStatusCode  The expected HTTP status code
     * @param expectedJsonSource  The resource to load the expected JSON object template from
     * @param variableAssignments The values used when resolving variables in the loaded template
     * @return An assertions object for use with the {@link rocks.bastion.core.builder.AssertionsBuilder#withAssertions(Assertions)} method
     * @throws InvalidJsonException         Thrown if the loaded JSON text is not valid JSON text
     * @throws UnreadableResourceException  Thrown if the specified resource exists but cannot be read (because it is a directory, for example)
     * @throws ResourceNotFoundException    Thrown if the specified resource does not exist
     * @throws TemplateCompilationException Thrown if a variable in the loaded template does not have an assignment in the {@code variableAssignments} map
     */
    public static JsonResponseAssertions fromTemplate(int expectedStatusCode, String expectedJsonSource, Map<String, String> variableAssignments) {
        Objects.requireNonNull(expectedJsonSource);
        Objects.requireNonNull(variableAssignments);
        TemplateContentCompiler compiler = new TemplateContentCompiler(new ResourceLoader(expectedJsonSource).load());
        compiler.addAllVariableAssignments(variableAssignments);
        return new JsonResponseAssertions(expectedStatusCode, compiler.getContent());
    }

    private int expectedStatusCode;
    private ContentType contentType;
    private String expectedJson;
    private Collection<String> ignoredFieldsValue;

    protected JsonResponseAssertions(int expectedStatusCode, String expectedJson) {
        Objects.requireNonNull(expectedJson);

        this.expectedStatusCode = expectedStatusCode;
        contentType = ContentType.APPLICATION_JSON;
        this.expectedJson = expectedJson;
        ignoredFieldsValue = new HashSet<>();

        validateExpectedJson();
    }

    /**
     * Ignore particular fields' values in the actual JSON response. Notice that Bastion will still fail the assertion if a
     * JSON field is missing, is in the wrong place, or is extra. Ignoring fields' values using this method is useful for
     * randomly generated values in the response, such as IDs or timestamps.
     * <br><br>
     * Implementation wise, when performing the JSON patch diff between the expected and the actual responses, Bastion will ignore
     * any patch operations which have {@code op} {@code "replace"} and a field which is one of the ignored fields.
     *
     * @param fields The fields' names to ignore
     * @return This object (for method chaining)
     */
    public JsonResponseAssertions ignoreValuesForProperties(String... fields) {
        Objects.requireNonNull(fields);
        Arrays.stream(fields).forEach(this::ignoreValueForProperty);
        return this;
    }

    /**
     * The assertions object will initially check that the content-type header returned by the actual response is
     * "application/json". This can be overriden to check for a different content-type header using this method. Despite
     * this, this assertions object will still try to interpret the body as if it were JSON text.
     *
     * @param contentType The expected content-type header
     * @return This object (for method chaining)
     */
    public JsonResponseAssertions overrideContentType(ContentType contentType) {
        Objects.requireNonNull(contentType);
        this.contentType = contentType;
        return this;
    }

    @Override
    public void execute(int statusCode, ModelResponse<?> response, Object model) throws AssertionError {
        try {
            Assert.assertEquals("Response Status Code", expectedStatusCode, statusCode);
            assertContentTypeHeader(response);
            JsonNode jsonPatch = computeJsonPatch(response);
            assertJsonPatchIsEmpty(jsonPatch);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while parsing JSON text", e);
        }
    }

    private static void assertJsonPatchIsEmpty(JsonNode jsonPatch) {
        if (jsonPatch.size() != 0) {
            Assert.fail(format("Actual response body is not as expected. The following JSON Patch (as per RFC-6902) tells you what operations you need " +
                    "to perform to transform the actual response body into the expected response body:\n %s", jsonPatch.toString()));
        }
    }

    private void ignoreValueForProperty(String field) {
        Objects.requireNonNull(field);
        ignoredFieldsValue.add(sanitizePropertyName(field));
    }

    private String sanitizePropertyName(String field) {
        if (!field.startsWith("/")) {
            return '/' + field;
        }
        return field;
    }

    private void validateExpectedJson() throws InvalidJsonException {
        try {
            new JsonParser().parse(expectedJson);
        } catch (JsonParseException parseException) {
            throw new InvalidJsonException(parseException, expectedJson);
        }
    }

    private void assertContentTypeHeader(Response response) {
        Assert.assertTrue("Content-type exists in response", response.getContentType().isPresent());
        Assert.assertEquals("Content-type MIME type", contentType.getMimeType(), response.getContentType().get().getMimeType());
    }

    private JsonNode computeJsonPatch(Response response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();
        JsonNode actualJsonTree = factory.createParser(response.getBody()).readValueAsTree();
        JsonNode expectedJsonTree = factory.createParser(expectedJson).readValueAsTree();
        JsonNode jsonPatch = JsonDiff.asJson(actualJsonTree, expectedJsonTree);
        removeReplaceOpsForIgnoredFields(jsonPatch);
        return jsonPatch;
    }

    private void removeReplaceOpsForIgnoredFields(Iterable jsonPatch) {
        Iterator<JsonNode> patchIterator = jsonPatch.iterator();
        while (patchIterator.hasNext()) {
            JsonNode patchOperation = patchIterator.next();
            JsonNode operationType = patchOperation.get("op");
            JsonNode pathName = patchOperation.get("path");
            if (operationType.asText().equals("replace") && ignoredFieldsValue.contains(pathName.asText())) {
                patchIterator.remove();
            }
        }
    }

}
