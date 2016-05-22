package org.kpull.bastion.annotation;

import org.junit.runner.RunWith;
import org.kpull.bastion.annotation.QueryParam;
import org.kpull.bastion.annotation.Request;
import org.kpull.bastion.annotation.EnvVar;
import org.kpull.bastion.junit.AnnotatedSuiteRunner;
import org.kpull.bastion.junit.BastionSuite;

/**
 * @author <a href="mailto:mail@kylepullicino.com">Kyle</a>
 */
@RunWith(AnnotatedSuiteRunner.class)
@BastionSuite(name = "Open Weather API v2.5")
public class AnnotatedSuite {

    @EnvVar(name = "APPID")
    public String appId() {
        return System.getProperty("APPID");
    }

    @Request(name = "Test Request Name", description = "Request One", method = "POST", url = "http://api.openweathermap.org/data/2.5/weather", type = "application/json", queryParams = {
            @QueryParam(name = "APPID", value = "{{APPID}}")
    })
    public ExampleRequestModel requestOne() {
        return new ExampleRequestModel("john doe", 6L);
    }

}
