package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import java.util.List;

public interface CommandLineGradleExecutionParameter<T> extends GradleExecutionParameter<T> {
    List<String> getAsArguments();
}
