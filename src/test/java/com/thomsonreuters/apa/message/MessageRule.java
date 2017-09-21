package com.thomsonreuters.apa.message;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import quickfix.FieldNotFound;
import quickfix.Message;

import java.lang.annotation.*;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 */
public class MessageRule implements TestRule {
    private Map<Integer, String> expectedTags = new HashMap<>();
    public static Message result;

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ExpectedTags {
        ExpectedTag[] value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(ExpectedTags.class)
    public @interface ExpectedTag {
        int tag();

        String message() default "";
    }

    @Override
    public Statement apply(Statement base, Description description) {
        description.getAnnotations().forEach(a -> {
            if(a.annotationType() == ExpectedTag.class){
                expectedTags.put(((ExpectedTag)a).tag(), ((ExpectedTag)a).message());
            }
        });

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                base.evaluate();
                verify();
            }
        };
    }

    protected void before() {
    }

    protected void verify() {
        if(result == null){
            fail("Did not create anything");
        }
        for(Integer i : expectedTags.keySet()){
            String expected = expectedTags.get(i);
            try {
                assertEquals(expected, result.getString(i));
            }catch (final FieldNotFound fnf){
                fail(fnf.getMessage());
            }
        }

    }

}
