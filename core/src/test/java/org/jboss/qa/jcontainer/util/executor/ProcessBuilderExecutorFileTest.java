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

import static org.apache.commons.io.FileUtils.readFileToString;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcessBuilderExecutorFileTest extends ProcessBuilderExecutorWriterTest {

	@Test
	public void start() throws Exception {
		final ProcessBuilderExecutor executor = createProcessBuilderExecutor();
		executor.start();
		executor.waitFor();

		assertThat(readFileToString(stdoutFile), is(equalToIgnoringWhiteSpace(OUTPUT_TEXT + " " + ERROR_TEXT)));
	}

	@Override
	protected ProcessBuilderExecutor createProcessBuilderExecutor() throws IOException {
		return new ProcessBuilderExecutorFile(processBuilder, stdoutFile);
	}
}
