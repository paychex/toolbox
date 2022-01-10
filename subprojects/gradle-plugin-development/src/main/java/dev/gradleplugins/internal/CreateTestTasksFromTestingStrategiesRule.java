package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginTestingStrategy;
import dev.gradleplugins.GradleVersionCoverageTestingStrategy;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.util.GUtil;

import java.util.Set;

import static dev.gradleplugins.internal.util.TestingStrategyPropertyUtils.testingStrategy;
import static java.util.Collections.emptyList;

final class CreateTestTasksFromTestingStrategiesRule implements Action<GradlePluginDevelopmentTestSuiteInternal> {
    private final TaskContainer tasks;
    private final ObjectFactory objects;
    private final SetProperty<Test> testElements;

    public CreateTestTasksFromTestingStrategiesRule(TaskContainer tasks, ObjectFactory objects, SetProperty<Test> testElements) {
        this.tasks = tasks;
        this.objects = objects;
        this.testElements = testElements;
    }

    @Override
    public void execute(GradlePluginDevelopmentTestSuiteInternal testSuite) {
        testSuite.getTestingStrategies().disallowChanges();
        Set<GradlePluginTestingStrategy> strategies = testSuite.getTestingStrategies().get();
        if (strategies.isEmpty()) {
            TaskProvider<Test> testTask = createTestTask(testSuite);
            testTask.configure(applyTestActions(testSuite));
        } else if (strategies.size() == 1) {
            TaskProvider<Test> testTask = createTestTask(testSuite);
            testTask.configure(applyTestActions(testSuite));
            testTask.configure(configureTestingStrategy(testSuite, (GradlePluginTestingStrategyInternal) strategies.iterator().next()));
        } else {
            for (GradlePluginTestingStrategy strategy : strategies) {
                TaskProvider<Test> testTask = createTestTask(testSuite, ((GradlePluginTestingStrategyInternal)strategy).getName());
                testTask.configure(applyTestActions(testSuite));
                testTask.configure(configureTestingStrategy(testSuite, (GradlePluginTestingStrategyInternal) strategy));
            }
        }
    }

    private Action<Test> applyTestActions(GradlePluginDevelopmentTestSuiteInternal testSuite) {
        return task -> {
            for (Action<? super Test> action : testSuite.getTestTaskActions()) {
                action.execute(task);
            }
        };
    }

    private Action<Test> configureTestingStrategy(GradlePluginDevelopmentTestSuiteInternal testSuite, GradlePluginTestingStrategyInternal strategy) {
        return task -> {
            if (!(strategy instanceof GradleVersionCoverageTestingStrategy)) {
                throw new RuntimeException("Unknown testing strategy");
            }
            String version = ((GradleVersionCoverageTestingStrategy) strategy).getVersion();
            task.systemProperty("dev.gradleplugins.defaultGradleVersion", version);
            testingStrategy(task).set(strategy);
        };
    }

    private TaskProvider<Test> createTestTask(GradlePluginDevelopmentTestSuiteInternal testSuite) {
        return createTestTask(testSuite, "");
    }

    private TaskProvider<Test> createTestTask(GradlePluginDevelopmentTestSuiteInternal testSuite, String variant) {
        String taskName = testSuite.getName() + StringUtils.capitalize(variant);

        TaskProvider<Test> result = null;
        if (tasks.getNames().contains(taskName)) {
            result = tasks.named(taskName, Test.class);
        } else {
            result = tasks.register(taskName, Test.class);
        }

        result.configure(it -> {
            it.setDescription("Runs the " + GUtil.toWords(testSuite.getName()) + "s.");
            it.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);

            it.setTestClassesDirs(objects.fileCollection().from(testSuite.getSourceSet().map(t -> (Object) t.getOutput().getClassesDirs()).orElse(emptyList())));
            it.setClasspath(objects.fileCollection().from(testSuite.getSourceSet().map(t -> (Object) t.getRuntimeClasspath()).orElse(emptyList())));
        });

        // Register test task to TaskView
        testElements.add(result);

        return result;
    }
}