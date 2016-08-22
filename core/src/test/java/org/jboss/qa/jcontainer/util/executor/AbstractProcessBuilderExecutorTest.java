/*
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.qa.jcontainer.util.executor;

import static org.apache.commons.io.FileUtils.getFile;

import org.apache.commons.lang3.SystemUtils;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbstractProcessBuilderExecutorTest {

	public static final String OUTPUT_TEXT = "output text";
	public static final String ERROR_TEXT = "error text";

	public static final String EXPECTED_RESULT = OUTPUT_TEXT + "\n" + ERROR_TEXT + "\n";

	protected static ProcessBuilder processBuilder;

	private TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Rule
	public TemporaryFolder getTemporaryFolder() {
		return temporaryFolder;
	}

	@BeforeClass
	public static void setUpClass() {
		final ArrayList<String> cmd = new ArrayList<>();
		if (SystemUtils.IS_OS_WINDOWS) {
			cmd.add("cmd");
			cmd.add("/c");
			cmd.add(getFile("src", "test", "resources", "script.bat").getAbsolutePath());
		} else {
			cmd.add("bash");
			cmd.add("-c");
			cmd.add(getFile("src", "test", "resources", "script.sh").getAbsolutePath());
		}
		processBuilder = new ProcessBuilder(cmd);
		log.debug("Build command: {}", cmd);
	}
}
