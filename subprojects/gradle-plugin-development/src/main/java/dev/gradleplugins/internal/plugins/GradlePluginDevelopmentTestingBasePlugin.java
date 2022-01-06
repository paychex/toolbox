package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.internal.GradlePluginDevelopmentTestSuiteInternal;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.inject.Inject;

abstract class GradlePluginDevelopmentTestingBasePlugin implements Plugin<Project> {
    @Inject
    public GradlePluginDevelopmentTestingBasePlugin() {}

    @Override
    public void apply(Project project) {
        project.getPluginManager().withPlugin("java-gradle-plugin", new RegisterTestingExtensionOnGradleDevelExtensionRule(project));
        project.getComponents().withType(GradlePluginDevelopmentTestSuiteInternal.class).configureEach(testSuite -> {
            project.afterEvaluate(proj -> {
                testSuite.finalizeComponent();
            });
        });
    }
}
