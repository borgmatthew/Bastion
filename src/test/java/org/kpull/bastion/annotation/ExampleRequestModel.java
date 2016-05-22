package org.kpull.bastion.annotation;

/**
 * @author <a href="mailto:mail@kylepullicino.com">Kyle</a>
 */
public class ExampleRequestModel {

    private String name;

    private long count;

    public ExampleRequestModel(String name, long count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public long getCount() {
        return count;
    }
}
