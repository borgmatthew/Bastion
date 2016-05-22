package org.kpull.bastion.annotations;

import org.junit.runner.RunWith;
import org.kpull.bastion.annotation.QueryParam;
import org.kpull.bastion.annotation.Request;
import org.kpull.bastion.annotation.Variable;
import org.kpull.bastion.junit.AnnotatedSuiteRunner;
import org.kpull.bastion.junit.BastionSuite;

/**
 * @author <a href="mailto:mail@kylepullicino.com">Kyle</a>
 */
@RunWith(AnnotatedSuiteRunner.class)
@BastionSuite(environment = {
        @Variable(name = "APPID", value = "a53fae0705d848512de95c6c59543e84")
})
public class AnnotatedSuite {

    @Request(name = "Test Request Name", method = "POST", url = "http://api.openweathermap.org/data/2.5/weather", type = "application/json", queryParams = {
            @QueryParam(name = "APPID", value = "{{APPID}}")
    })
    public Object requestOne() {
        return new Object();
    }

}
