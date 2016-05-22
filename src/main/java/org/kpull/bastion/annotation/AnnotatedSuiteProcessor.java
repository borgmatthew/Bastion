package org.kpull.bastion.annotation;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.kpull.bastion.core.*;
import org.kpull.bastion.junit.BastionSuite;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * @author <a href="mailto:mail@kylepullicino.com">Kyle</a>
 */
public class AnnotatedSuiteProcessor {

    private Class<?> suiteClass;

    public AnnotatedSuiteProcessor(Class<?> suiteClass) {
        Objects.requireNonNull(suiteClass);
        this.suiteClass = suiteClass;
    }

    public ApiSuite processAnnotatedClass() {
        try {
            BastionSuite bastionSuiteInfo = suiteClass.getAnnotation(BastionSuite.class);
            if (bastionSuiteInfo == null) {
                throw new IllegalArgumentException(format("The specified class [%s] should have the BastionSuite annotation", suiteClass.getName()));
            }
            String suiteName = StringUtils.defaultIfBlank(bastionSuiteInfo.name(), suiteClass.getSimpleName());
            ApiEnvironment environment = new ApiEnvironment(Arrays.stream(bastionSuiteInfo.environment()).collect(Collectors.toMap(Variable::name, Variable::value)));
            Object suiteInstance = suiteClass.newInstance();
            List<ApiCall> calls = Arrays.stream(suiteClass.getMethods()).filter(method -> method.isAnnotationPresent(Request.class))
                    .map(method -> {
                        Request requestInfo = method.getAnnotation(Request.class);
                        String callName = StringUtils.defaultIfBlank(requestInfo.name(), method.getName());
                        String description = requestInfo.description();
                        List<ApiHeader> headers = Arrays.stream(requestInfo.headers()).map(new Function<Header, ApiHeader>() {
                            @Override
                            public ApiHeader apply(Header header) {
                                return new ApiHeader(header.name(), header.value());
                            }
                        }).collect(Collectors.toList());
                        List<ApiQueryParam> queryParams = Arrays.stream(requestInfo.queryParams()).map(new Function<QueryParam, ApiQueryParam>() {
                            @Override
                            public ApiQueryParam apply(QueryParam queryParam) {
                                return new ApiQueryParam(queryParam.name(), queryParam.value());
                            }
                        }).collect(Collectors.toList());
                        String body = null;
                        body = evaluateBodyFromMethodReturnValue(suiteInstance, method);
                        ApiRequest apiRequest = new ApiRequest(requestInfo.method(), requestInfo.url(), headers, requestInfo.type(), body, queryParams);
                        return new ApiCall(callName, description, apiRequest, new ApiResponse(Lists.newLinkedList(), 0, "", ""), null, null, Callback.NO_OPERATION_CALLBACK);
                    }).collect(Collectors.toList());
            return new ApiSuite(suiteName, environment, calls);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Error creating an instance of a Bastion suite. Make sure that the suite has a default constructor.", e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Error creating an instance of a Bastion suite.", e);
        }
    }

    private String evaluateBodyFromMethodReturnValue(Object suiteInstance, Method method) {
        try {
            if (method.getReturnType() != null) {
                return new Gson().toJson(method.invoke(suiteInstance));
            }
            return "";
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Error invoking Bastion suite method", e);
        }
    }

}
