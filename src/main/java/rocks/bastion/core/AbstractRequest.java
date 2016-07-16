package rocks.bastion.core;

import org.apache.http.entity.ContentType;

import java.util.Collection;
import java.util.Objects;

/**
 * Created by ChiaraFSC on 16/07/2016.
 */
public abstract class AbstractRequest implements Request {

    protected String name;
    protected String url;
    protected HttpMethod method;
    protected ContentType contentType;
    protected Collection<ApiHeader> headers;
    protected Collection<ApiQueryParam> queryParams;
    protected String body;

    protected AbstractRequest(HttpMethod method, String url, String body) {
        Objects.requireNonNull(method);
        Objects.requireNonNull(url);

        this.method = method;
        this.url = url;
        this.body = body;
    }

    /**
     * Set the content-type that will be used for this request.
     *
     * @param contentType A non-{@literal null} content-type to use for this request
     * @return This request (for method chaining)
     */
    public AbstractRequest setContentType(ContentType contentType) {
        Objects.requireNonNull(contentType);
        this.contentType = contentType;
        return this;
    }

    /**
     * Add a new HTTP header that will be sent with this request.
     *
     * @param name  A non-{@literal null} name for the new header
     * @param value A non-{@literal null} value for the new header
     * @return This request (for method chaining)
     */
    public AbstractRequest addHeader(String name, String value) {
        this.headers.add(new ApiHeader(name, value));
        return this;
    }

    /**
     * Add a new HTTP query parameter that will be sent with this request.
     *
     * @param name  A non-{@literal null} name for the new query parameter
     * @param value A non-{@literal null} value for the new query parameter
     * @return This request (for method chaining)
     */
    public AbstractRequest addQueryParam(String name, String value) {
        this.queryParams.add(new ApiQueryParam(name, value));
        return this;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public HttpMethod method() {
        return method;
    }

    @Override
    public ContentType contentType() {
        return contentType;
    }

    @Override
    public Collection<ApiHeader> headers() {
        return headers;
    }

    @Override
    public Collection<ApiQueryParam> queryParams() {
        return queryParams;
    }

    @Override
    public Object body() {
        return body;
    }

}
