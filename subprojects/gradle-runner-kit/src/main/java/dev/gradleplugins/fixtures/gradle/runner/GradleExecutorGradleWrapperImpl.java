package dev.gradleplugins.fixtures.gradle.runner;

import dev.gradleplugins.fixtures.gradle.runner.parameters.GradleExecutionCommandLineParameter;
import dev.gradleplugins.fixtures.gradle.runner.parameters.GradleExecutionParameter;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.WrapperGradleDistribution;
import dev.nokee.core.exec.*;
import lombok.val;
import org.apache.commons.lang3.SystemUtils;
import org.gradle.wrapper.GradleUserHomeLookup;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.gradleplugins.fixtures.file.FileSystemUtils.file;
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.inherit;
import static java.util.Arrays.asList;

final class GradleExecutorGradleWrapperImpl extends AbstractGradleExecutor {
    @Override
    protected GradleExecutionResult doRun(GradleExecutionContext parameters) {
        if (!(parameters.getDistribution().get() instanceof WrapperGradleDistribution)) {
            throw new InvalidRunnerConfigurationException("The Gradle wrapper executor doesn't support customizing the distribution");
        }

//        System.out.println("Starting with " + parameters.getAllArguments());

        val workingDirectory = parameters.getProjectDirectory().orElseGet(parameters.getWorkingDirectory()::get);
        val relativePath = parameters.getGradleUserHomeDirectory().get().toPath().relativize(GradleUserHomeLookup.gradleUserHome().toPath());
        val wrapperProperties = new Properties();
        try (val inStream = new FileInputStream(file(workingDirectory, "gradle/wrapper/gradle-wrapper.properties"))) {
            wrapperProperties.load(inStream);
            wrapperProperties.setProperty("distributionPath", relativePath.toString() + "/wrapper/dists");
            wrapperProperties.setProperty("zipStorePath", relativePath.toString() + "/wrapper/dists");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try (val outStream = new FileOutputStream(file(workingDirectory, "gradle/wrapper/gradle-wrapper.properties"))) {
            wrapperProperties.store(outStream, null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        CommandLine command = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            command = CommandLine.of(asList("cmd", "/c", "gradlew.bat"), allArguments(parameters));
        } else {
            command = CommandLine.of("./gradlew", allArguments(parameters));
        }
        val result = command.newInvocation()
                .withEnvironmentVariables(environmentVariables(parameters))
                .workingDirectory(parameters.getWorkingDirectory().get())
                .redirectStandardOutput(CommandLineToolInvocationStandardOutputRedirect.forwardTo(parameters.getStandardOutput().get()))
                .redirectErrorOutput(CommandLineToolInvocationErrorOutputRedirect.forwardTo(parameters.getStandardError().get()))
                .buildAndSubmit(new ProcessBuilderEngine())
                .waitFor();

        return new GradleExecutionResultNokeeExecImpl(result);
    }

    private static List<String> allArguments(GradleExecutionContext parameters) {
        val result = new ArrayList<String>();
        result.add("-Dorg.gradle.jvmargs=" + getImplicitBuildJvmArgs().stream().map(it -> "'" + it + "'").collect(Collectors.joining(" ")));
        parameters.getExecutionParameters().stream().filter(GradleExecutionCommandLineParameter.class::isInstance).flatMap(GradleExecutorGradleWrapperImpl::asArguments).forEach(result::add);
        return result;
    }

    private static Stream<String> asArguments(GradleExecutionParameter<?> parameter) {
        return ((GradleExecutionCommandLineParameter<?>) parameter).getAsArguments().stream();
    }

    private static CommandLineToolInvocationEnvironmentVariables environmentVariables(GradleExecutionContext parameters) {
        return parameters.getEnvironmentVariables().map(CommandLineToolInvocationEnvironmentVariables::from).orElse(inherit()).plus(CommandLineToolInvocationEnvironmentVariables.from(parameters.getJavaHome().getAsEnvironmentVariables()));
    }

//    public GradleHandle start(GradleExecutionParameters parameters) {
//
//    }
}
