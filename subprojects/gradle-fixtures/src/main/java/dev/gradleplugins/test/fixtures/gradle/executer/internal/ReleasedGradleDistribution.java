/*
 * Copyright 2011 the original author or authors.
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
package dev.gradleplugins.test.fixtures.gradle.executer.internal;

import org.gradle.util.DistributionLocator;

import java.io.File;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ReleasedGradleDistribution extends DownloadableGradleDistribution {

    public ReleasedGradleDistribution(String version, File versionDirectory) {
        super(version, versionDirectory);
    }

    @Override
    protected URL getDownloadURL() {
        try {
            return new DistributionLocator().getDistributionFor(getVersion()).toURL();
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }
}
