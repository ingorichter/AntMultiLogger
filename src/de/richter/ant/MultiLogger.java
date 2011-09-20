/*
 * Copyright 2011 Ingo Richter 
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
 *  
 */
package de.richter.ant;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.ClasspathUtils;
import org.apache.tools.ant.util.FileUtils;

public class MultiLogger implements BuildLogger {
	private List<BuildLogger> loggerList = new ArrayList<BuildLogger>();
	private int msgOutputLevel;
	private PrintStream out;
	private PrintStream err;
	private boolean emacsMode;

	public void buildStarted(BuildEvent event) {
		final Project project = event.getProject();

		String loggerClasses = (String) project.getProperties().get(
				"MultiLogger.logger");

		if (loggerClasses == null) {
			loggerClasses = loadFromPropertiesFile();
		}

		final String[] loggerClassNames = getLoggerClassNames(loggerClasses);

		for (String loggerClassName : loggerClassNames) {
			try {
				BuildLogger logger = createLoggerInstance(loggerClassName
						.trim());

				// configure default logger
				configureLogger(logger);

				loggerList.add(logger);
			} catch (BuildException be) {
				project.log(String.format(
						"Can't create instance of BuildLogger '%s'",
						loggerClassName));
			}
		}

		// notify all buildLogger
		for (BuildLogger buildLogger : loggerList) {
			buildLogger.buildStarted(event);
		}
	}

	private void configureLogger(BuildLogger logger) {
		logger.setOutputPrintStream(out);
		logger.setErrorPrintStream(err);
		logger.setMessageOutputLevel(msgOutputLevel);
		logger.setEmacsMode(emacsMode);
	}

	private String loadFromPropertiesFile() {
		String loggerClasses = null;

		InputStream is = null;
		try {
			Properties fileProperties = new Properties();
			is = new FileInputStream("MultiLogger.properties");
			fileProperties.load(is);
			loggerClasses = fileProperties.getProperty("MultiLogger.listener");

			System.err.println("Found logger classes: " + loggerClasses);
		} catch (IOException ioe) {
			// ignore because properties file is not required
		} finally {
			FileUtils.close(is);
		}

		return loggerClasses;
	}

	private String[] getLoggerClassNames(String loggerClasses) {
		String[] loggerClassNames;

		if (loggerClasses == null) {
			loggerClassNames = new String[] { "org.apache.tools.ant.DefaultLogger" };
		} else {
			loggerClassNames = loggerClasses.split(",");
		}

		return loggerClassNames;
	}

	public void buildFinished(BuildEvent event) {
		// notify all buildLogger
		for (BuildLogger buildLogger : loggerList) {
			buildLogger.buildFinished(event);
		}
	}

	public void taskStarted(BuildEvent event) {
		// notify all buildLogger
		for (BuildLogger buildLogger : loggerList) {
			buildLogger.taskStarted(event);
		}
	}

	public void taskFinished(BuildEvent event) {
		// notify all buildLogger
		for (BuildLogger buildLogger : loggerList) {
			buildLogger.taskFinished(event);
		}
	}

	public void targetStarted(BuildEvent event) {
		// notify all buildLogger
		for (BuildLogger buildLogger : loggerList) {
			buildLogger.targetStarted(event);
		}
	}

	public void targetFinished(BuildEvent event) {
		// notify all buildLogger
		for (BuildLogger buildLogger : loggerList) {
			buildLogger.targetFinished(event);
		}
	}

	public void messageLogged(BuildEvent event) {
		// notify all buildLogger
		for (BuildLogger buildLogger : loggerList) {
			buildLogger.messageLogged(event);
		}
	}

	public List<BuildLogger> getLogger() {
		return Collections.unmodifiableList(loggerList);
	}

	public void setMessageOutputLevel(int level) {
		this.msgOutputLevel = level;
	}

	public void setOutputPrintStream(PrintStream output) {
		this.out = new PrintStream(output, true);
	}

	public void setErrorPrintStream(PrintStream err) {
		this.err = new PrintStream(err, true);
	}

	public void setEmacsMode(boolean emacsMode) {
		this.emacsMode = emacsMode;
	}

	protected BuildLogger createLoggerInstance(String loggerClassName) {
		return (BuildLogger) ClasspathUtils.newInstance(loggerClassName,
				MultiLogger.class.getClassLoader(), BuildLogger.class);
	}
}