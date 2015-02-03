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
package org.jboss.qa.jcontainer.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class ContainerTest {

	public static final String JBOSS_HOME = getProperty("jboss.home");
	public static final String EAP_HOME = getProperty("eap.home");
	public static final String KARAF_HOME = getProperty("karaf.home");
	public static final String FUSE_HOME = getProperty("fuse.home");
	private static final Logger logger = LoggerFactory.getLogger(ContainerTest.class);

	public static String getProperty(String key) {
		final String testPropertiesFile = "src/test/resources/test.properties";
		String value = null;
		final Properties props = new Properties();
		try (InputStream is = new FileInputStream(testPropertiesFile)) {
			props.load(is);
		} catch (IOException e) {
			logger.warn("File {} does not exist", testPropertiesFile);
		}
		if (props.get(key) != null) {
			value = (String) props.get(key);
		} else if (System.getProperty(key) != null) {
			value = System.getProperty(key);
		}
		return value;
	}
}