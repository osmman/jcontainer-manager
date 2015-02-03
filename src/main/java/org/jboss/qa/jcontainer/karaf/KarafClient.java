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
package org.jboss.qa.jcontainer.karaf;

import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.agent.SshAgent;
import org.apache.sshd.agent.local.AgentImpl;
import org.apache.sshd.agent.local.LocalAgentFactory;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.common.RuntimeSshException;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;

import org.jboss.qa.jcontainer.Client;

import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.security.KeyPair;
import java.util.concurrent.TimeUnit;

public class KarafClient<T extends KarafConfiguration> extends Client<T> {

	private static final Logger logger = LoggerFactory.getLogger(KarafClient.class);
	private static final String NEW_LINE = System.getProperty("line.separator");
	protected SshClient client;
	protected ClientSession session;

	public KarafClient(T configuration) {
		super(configuration);
	}

	@Override
	protected void closeInternal() throws IOException {
		session.close(true);
		session = null;
		client.stop();
		client = null;
	}

	@Override
	public boolean isConnected() {
		return (session != null && !session.isClosed());
	}

	@Override
	protected void connectInternal() throws Exception {
		logger.info("Connecting to server {}:{}", configuration.getHost(), configuration.getPort());
		client = SshClient.setUpDefaultClient();
		setupAgent(configuration.getUsername(), configuration.getKeyFile(), client);
		client.start();
		connect(client);
		if (configuration.getPassword() != null) {
			session.addPasswordIdentity(configuration.getPassword());
		}
		session.auth().verify();
	}

	@Override
	protected boolean executeInternal(String command) throws Exception {
		// https://github.com/apache/karaf/blob/master/shell/core/src/main/java/org/apache/karaf/shell/support/
		// ShellUtil.java#L158
		final String exceptionMsg = "Command not found";
		// https://github.com/apache/karaf/blob/master/shell/console/src/main/java/org/apache/felix/gogo/commands/
		// CommandException.java#L58
		final String failMsg = "Error executing command";

		boolean success = true;
		final ClientChannel channel = session.createChannel("exec", command.concat(NEW_LINE));
		channel.setIn(new ByteArrayInputStream(new byte[0]));
		final ByteArrayOutputStream sout = new ByteArrayOutputStream();
		final ByteArrayOutputStream serr = new ByteArrayOutputStream();
		channel.setOut(AnsiConsole.wrapOutputStream(sout));
		channel.setErr(AnsiConsole.wrapOutputStream(serr));
		channel.open();
		channel.waitFor(ClientChannel.CLOSED, 0);
		sout.writeTo(System.out);
		serr.writeTo(System.err);
		final boolean isError = (channel.getExitStatus() != null && channel.getExitStatus() != 0);
		if (isError) {
			logger.error(sout.toString());
			if (sout.toString().contains(failMsg)) {
				success = false;
			} else {
				throw new UnsupportedOperationException("Unsupported operation: " + command);
			}
		}
		return success;
	}

	protected void setupAgent(String user, File keyFile, SshClient client) {
		final URL builtInPrivateKey = KarafClient.class.getClassLoader().getResource("karaf.key");
		final SshAgent agent = startAgent(user, builtInPrivateKey, keyFile);
		client.setAgentFactory(new LocalAgentFactory(agent));
		client.getProperties().put(SshAgent.SSH_AUTHSOCKET_ENV_NAME, "local");
	}

	protected SshAgent startAgent(String user, URL privateKeyUrl, File keyFile) {
		try (InputStream is = privateKeyUrl.openStream()) {
			final SshAgent agent = new AgentImpl();
			final ObjectInputStream r = new ObjectInputStream(is);
			final KeyPair keyPair = (KeyPair) r.readObject();
			is.close();
			agent.addIdentity(keyPair, user);
			if (keyFile != null) {
				final String[] keyFiles = new String[] {keyFile.getAbsolutePath()};
				final FileKeyPairProvider fileKeyPairProvider = new FileKeyPairProvider(keyFiles);
				for (KeyPair key : fileKeyPairProvider.loadKeys()) {
					agent.addIdentity(key, user);
				}
			}
			return agent;
		} catch (Throwable e) {
			logger.error("Error starting ssh agent for: " + e.getMessage(), e);
			return null;
		}
	}

	protected void connect(SshClient client) throws IOException, InterruptedException {
		int attempts = 10;
		do {
			final ConnectFuture future = client.connect(configuration.getUsername(), configuration.getHost(),
					configuration.getPort());
			future.await();
			try {
				session = future.getSession();
			} catch (RuntimeSshException ex) {
				if (--attempts > 0) {
					Thread.sleep(TimeUnit.SECONDS.toMillis(2));
					logger.info("Waiting for SSH connection...");
				} else {
					throw ex;
				}
			}
		} while (session == null);
	}
}