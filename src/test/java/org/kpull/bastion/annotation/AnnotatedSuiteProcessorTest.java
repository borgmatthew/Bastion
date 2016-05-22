package org.kpull.bastion.annotation;

import org.junit.Before;
import org.junit.Test;
import org.kpull.bastion.core.ApiCall;
import org.kpull.bastion.core.ApiQueryParam;
import org.kpull.bastion.core.ApiSuite;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.data.MapEntry.entry;

/**
 * @author <a href="mailto:mail@kylepullicino.com">Kyle</a>
 */
public class AnnotatedSuiteProcessorTest {

    @Test
    public void processAnnotatedClass() throws Exception {
        System.setProperty("APPID", "testAppId");
        ApiSuite suite = new AnnotatedSuiteProcessor(AnnotatedSuite.class).processAnnotatedClass();
        assertThat(suite.getName()).isEqualTo("Open Weather API v2.5");
        assertThat(suite.getEnvironment()).contains(entry("APPID", "testAppId"));
        List<ApiCall> apiCalls = suite.getApiCalls();
        assertThat(apiCalls).hasSize(1);
        assertThat(apiCalls.get(0).getName()).isEqualTo("Test Request Name");
        assertThat(apiCalls.get(0).getDescription()).isEqualTo("Request One");
        assertThat(apiCalls.get(0).getRequest().getMethod()).isEqualTo("POST");
        assertThat(apiCalls.get(0).getRequest().getUrl()).isEqualTo("http://api.openweathermap.org/data/2.5/weather");
        assertThat(apiCalls.get(0).getRequest().getQueryParams()).containsOnly(new ApiQueryParam("APPID", "{{APPID}}"));
        assertThat(apiCalls.get(0).getRequest().getBody()).isEqualTo("{\"name\":\"john doe\",\"count\":6}");
    }

}