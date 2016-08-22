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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.is;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcessBuilderExecutorWriterTest extends AbstractProcessBuilderExecutorTest {

	protected Writer stdout;
	protected Writer stderr;

	protected File stdoutFile;
	protected File stderrFile;

	@Before
	public void setUp() throws IOException {
		stdoutFile = getTemporaryFolder().newFile();
		stderrFile = getTemporaryFolder().newFile();
		stdout = new FileWriter(stdoutFile);
		stderr = new FileWriter(stderrFile);
	}

	@After
	public void cleanUp() throws IOException {
		stdout.close();
		stderr.close();
	}

	@Test
	public void start() throws Exception {
		final ProcessBuilderExecutor executor = createProcessBuilderExecutor();
		executor.start();
		executor.waitFor();

		stdout.flush();
		stderr.flush();

		assertThat(readFileToString(stdoutFile), is(equalToIgnoringWhiteSpace(OUTPUT_TEXT)));
		assertThat(readFileToString(stderrFile), is(equalToIgnoringWhiteSpace(ERROR_TEXT)));
	}

	@Test(expected = IllegalStateException.class)
	public void getProcessWhenIsNotStarted() throws Exception {
		final ProcessBuilderExecutor executor = createProcessBuilderExecutor();
		executor.getProcess();
	}

	@Test
	public void getProcess() throws Exception {
		final ProcessBuilderExecutor executor = createProcessBuilderExecutor();
		executor.start();
		final Process process = executor.getProcess();
		executor.waitFor();
		assertNotNull(process);
		assertEquals("Response code should be 0. ", 0, process.exitValue());
	}

	protected ProcessBuilderExecutor createProcessBuilderExecutor() throws IOException {
		return new ProcessBuilderExecutorWriter(processBuilder, stdout, stderr);
	}
}
