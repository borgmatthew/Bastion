package org.kpull.bastion.junit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.kpull.bastion.annotation.AnnotatedSuiteProcessor;
import org.kpull.bastion.core.ApiCall;
import org.kpull.bastion.core.ApiEnvironment;
import org.kpull.bastion.core.ApiSuite;
import org.kpull.bastion.runner.ApiCallExecutor;

import java.util.List;

/**
 * @author <a href="mailto:mail@kylepullicino.com">Kyle</a>
 */
public class AnnotatedSuiteRunner extends ParentRunner<ApiCall> {

    private ApiSuite suiteToRun;

    /**
     * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
     *
     * @param testClass
     */
    public AnnotatedSuiteRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        suiteToRun = new AnnotatedSuiteProcessor(testClass).processAnnotatedClass();
    }

    @Override
    protected List<ApiCall> getChildren() {
        return suiteToRun.getApiCalls();
    }

    @Override
    protected Description describeChild(ApiCall child) {
        return ApiSuiteDescription.describeApiCall(suiteToRun.getName(), child);
    }

    @Override
    protected void runChild(ApiCall apiCallToRun, RunNotifier notifier) {
        executeApiCall(suiteToRun, apiCallToRun, notifier);
    }

    private void executeApiCall(ApiSuite child, ApiCall apiCall, RunNotifier notifier) {
        Description apiCallDescription = describeChild(apiCall);
        notifier.fireTestStarted(apiCallDescription);
        try {
            ApiCallExecutor executor = new ApiCallExecutor(suiteToRun.getEnvironment(), apiCall, new ObjectMapper());
            executor.execute();
        } catch (AssumptionViolatedException e) {
            notifier.fireTestAssumptionFailed(new Failure(apiCallDescription, e));
        } catch (Throwable e) {
            notifier.fireTestFailure(new Failure(apiCallDescription, e));
        } finally {
            notifier.fireTestFinished(apiCallDescription);
        }
    }
}
