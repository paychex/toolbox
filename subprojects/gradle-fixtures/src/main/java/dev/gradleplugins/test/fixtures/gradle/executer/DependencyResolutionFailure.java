/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins.test.fixtures.gradle.executer;

import org.hamcrest.Matcher;

import java.util.regex.Pattern;

import static org.hamcrest.Matchers.matchesPattern;

public class DependencyResolutionFailure {
    private final ExecutionFailure failure;

    public DependencyResolutionFailure(ExecutionFailure failure, String configuration) {
        this.failure = failure;
        assertFailedConfiguration(configuration);
    }

    public DependencyResolutionFailure assertFailedConfiguration(String configuration) {
        failure.assertThatCause(matchesPattern("Could not resolve all (dependencies|artifacts|files) for configuration '" + Pattern.quote(configuration) + "'."));
        return this;
    }

    public DependencyResolutionFailure assertFailedDependencyRequiredBy(String dependency) {
        failure.assertThatCause(matchesPattern("(?ms).*Required by:\\s+" + dependency + ".*"));
        return this;
    }

    public DependencyResolutionFailure assertHasCause(String cause) {
        failure.assertHasCause(cause);
        return this;
    }

    public DependencyResolutionFailure assertThatCause(Matcher<String> matcher) {
        failure.assertThatCause(matcher);
        return this;
    }
}
