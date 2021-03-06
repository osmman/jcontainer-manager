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
package org.jboss.qa.jcontainer.eap;

import org.jboss.qa.jcontainer.wildfly.WildflyConfiguration;

public class EapConfiguration extends WildflyConfiguration {

	public static final int DEFAULT_MANAGEMENT_NATIVE_PORT = 9999;

	protected int managementNativePort;

	public EapConfiguration(Builder<?> builder) {
		super(builder);
		managementNativePort = builder.managementNativePort;
	}

	public int getManagementNativePort() {
		return managementNativePort;
	}

	public static Builder<?> builder() {
		return new Builder2();
	}

	public abstract static class Builder<T extends Builder<T>> extends WildflyConfiguration.Builder<T> {

		protected int managementNativePort;

		public Builder() {
			xms = "1303m";
			xmx = "1303m";
			maxPermSize = "256m";
			this.managementNativePort = DEFAULT_MANAGEMENT_NATIVE_PORT;
		}

		public T managementNativePort(int managementNativePort) {
			this.managementNativePort = managementNativePort;
			return self();
		}

		public EapConfiguration build() {
			super.build();
			// Set JAVA_OPTS
			final StringBuilder javaOpts = new StringBuilder();
			final String oldJavaOpts = envProps.get(JAVA_OPTS_ENV_NAME);
			if (oldJavaOpts != null) {
				javaOpts.append(oldJavaOpts);
			}
			javaOpts.append(" -Djboss.modules.policy-permissions=true");
			javaOpts.append(" -Djboss.management.native.port=").append(managementNativePort);
			envProps.put(JAVA_OPTS_ENV_NAME, javaOpts.toString());
			return new EapConfiguration(this);
		}
	}

	private static class Builder2 extends Builder<Builder2> {
		@Override
		protected Builder2 self() {
			return this;
		}
	}
}
