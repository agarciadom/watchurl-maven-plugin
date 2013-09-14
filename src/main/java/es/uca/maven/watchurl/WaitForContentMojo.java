/**
 * Copyright 2013 Antonio García-Domínguez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.uca.maven.watchurl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which waits for a certain URL to become available. Optionally, it can
 * also wait until the response contains a line with the specified Java regular
 * expression.
 */
@Mojo(name = "wait", defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class WaitForContentMojo extends AbstractMojo {
	/**
	 * URL to watch.
	 */
	@Parameter(required = true)
	private URL url;

	/**
	 * Delay between retries, in milliseconds.
	 */
	@Parameter(defaultValue = "5000", required = true)
	private Integer retryDelay;

	/**
	 * Number of retries.
	 */
	@Parameter(defaultValue = "12", required = true)
	private Integer retryCount;

	/**
	 * Character encoding to be used to read the contents of the URL.
	 */
	@Parameter(defaultValue = "UTF-8", required = true)
	private String charEncoding;

	/**
	 * Optional regular expression that must be matched by some line in the
	 * response.
	 */
	@Parameter
	private String regex;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (retryDelay < 0) {
			throw new MojoExecutionException("Retry delay cannot be negative");
		}
		if (retryCount < 0) {
			throw new MojoExecutionException("Retry count cannot be negative");
		}

		Pattern pat;
		try {
			pat = regex != null ? Pattern.compile(regex) : null;
		} catch (PatternSyntaxException ex) {
			throw new MojoExecutionException(
					"Bad regular expression: " + regex, ex);
		}

		for (int tries = 0; tries < retryCount; ++tries) {
			InputStream urlStream = null;
			BufferedReader reader = null;
			try {
				urlStream = url.openStream();
				if (pat == null) {
					// We successfully opened the stream, but we're not interested in its contents
					getLog().info(String.format("Fetched %s successfully", url));
					return;
				}

				reader = new BufferedReader(new InputStreamReader(urlStream, charEncoding));
				String line;
				while ((line = reader.readLine()) != null) {
					getLog().debug(line);
					if (pat.matcher(line).find()) {
						return;
					}
				}
				getLog().debug(String.format("Did not find a line with a match for regex '%s'. ", regex));
			} catch (Exception ex) {
				// current try didn't work, wait a bit more
				getLog().debug("Could not connect. ");
			} finally {
				try {
					if (reader != null) {
						reader.close();
					} else if (urlStream != null) {
						urlStream.close();
					}
				} catch (Exception ex) {
					throw new MojoExecutionException(
							"I/O error while closing the connection", ex);
				}
			}

			getLog().info(String.format(
				"Waiting %d ms for '%s'... (try %d/%d)",
				retryDelay, url, tries, retryCount));
			synchronized (this) {
				try {
					this.wait(retryDelay);
				} catch (InterruptedException e) {
					throw new MojoExecutionException("Wait interrupted", e);
				}
			}
		}

		getLog().error("Timeout while waiting for ActiveBPEL to start up");
		throw new MojoFailureException(String.format("Timeout while watching the URL '%s'", url));
	}
}
