package org.kpull.bastion.junit;

import org.junit.runner.Description;
import org.kpull.bastion.core.ApiCall;
import org.kpull.bastion.core.ApiSuite;

import java.util.Objects;

/**
 * @author <a href="mailto:mail@kylepullicino.com">Kyle</a>
 */
public final class ApiSuiteDescription {

    private ApiSuiteDescription() {
    }

    public static Description describe(ApiSuite apiSuite) {
        Objects.requireNonNull(apiSuite);
        String suiteName = apiSuite.getName();
        Description suiteDescription = Description.createSuiteDescription(suiteName);
        apiSuite.getApiCalls().forEach(apiCall -> suiteDescription.addChild(describeApiCall(suiteName, apiCall)));
        return suiteDescription;
    }

    public static Description describeApiCall(String suiteName, ApiCall apiCall) {
        return Description.createTestDescription(suiteName, apiCall.getName());
    }
}
