watchurl-maven-plugin
=====================

Maven plugin which waits for a certain URL to be available, optionally also waiting until the HTTP response contains a line matching a certain regular expression.

The plugin is bound by default to the "integration-test" phase of the main lifecycle, and has only one goal ("wait").

How to use
----------

You will first need to install this plugin in your local repository:

  cd watchurl-maven-plugin
  mvn install

Alternatively, you may want to deploy this to a centralised artefact manager: this is outside the scope of this document, though.

To wait until the URL http://machine:port/path becomes available and the HTTP contains a line with 'Hello!', add this to your <plugins>:

  <plugin>
    <groupId>es.uca.maven</groupId>
    <artifactId>watchurl-maven-plugin</artifactId>
    <version>1.0</version>
    <executions>
      <execution>
        <id>wait</id>
        <phase>integration-test</phase>
        <configuration>
          <url>http://machine:port/path</url>
          <regex>Hello!</regex>
        </configuration>
        <goals>
          <goal>wait</goal>
        </goals>
      </execution>
    </executions>
  </plugin>

You can find additional examples in the src/it/nowait* and src/it/wait* directories. These are integration tests for the plugin itself, so they should work fine.

Options for the <configuration> section
---------------------------------------

* <url> (required): the URL to be watched.
* <regex> (optional): if specified, once the URL becomes available, it will be queried using HTTP GET until one of the lines of the response contains a match for this regular expression.
* <retryDelay> (optional): delay between checks, in milliseconds. By default, it is set to 5 seconds.
* <retryCount> (optional): number of checks before giving up. By default, it is set to 12.
* <charEncoding> (optional): character encoding to be used to read the HTTP response. By default, it is UTF-8.
