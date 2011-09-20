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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.junit.Before;
import org.junit.Test;

public class MultiLoggerTestcase {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testInstancesCalledInOrder() {
		final TestLogger1 testLogger1 = mock(TestLogger1.class);
		final TestLogger2 testLogger2 = mock(TestLogger2.class);

		final Map<String, BuildLogger> testMapping = new HashMap<String, BuildLogger>();
		testMapping.put("de.richter.ant.TestLogger1", testLogger1);
		testMapping.put("de.richter.ant.TestLogger2", testLogger2);
		
		MultiLogger logger = setupMultiLogger(testMapping);

		Project testProject = setupTestProject();
		testProject.setProperty("MultiLogger.logger",
				"de.richter.ant.TestLogger1, de.richter.ant.TestLogger2");

		BuildEvent buildEvent = new BuildEvent(testProject);
		logger.buildStarted(buildEvent);
		
		verify(testLogger1).buildStarted(buildEvent);
		verify(testLogger2).buildStarted(buildEvent);
	}

	@Test
	public void NoLoggerProvidedThenUseTheDefaultLogger()
	{
		MultiLogger logger = new MultiLogger();
		
		logger.setOutputPrintStream(System.out);
		logger.setErrorPrintStream(System.out);
		
		Project testProject = setupTestProject();
		
		BuildEvent buildEvent = new BuildEvent(testProject);
		logger.buildStarted(buildEvent);

		List<BuildLogger> buildLoggerInstances = logger.getLogger();
		Assert.assertEquals("Should have one listeners", 1, buildLoggerInstances.size());
		Assert.assertTrue("Must be an instance of DefaultLogger", buildLoggerInstances.get(0) instanceof DefaultLogger);
	}
	
	@Test
	public void testAllBuildEventsFired() {
		final TestLogger1 testLogger1 = mock(TestLogger1.class);
		
		final Map<String, BuildLogger> testMapping = new HashMap<String, BuildLogger>();
		testMapping.put("de.richter.ant.TestLogger1", testLogger1);
		
		MultiLogger logger = setupMultiLogger(testMapping);
		
		Project testProject = setupTestProject();
		testProject.setProperty("MultiLogger.logger", "de.richter.ant.TestLogger1");
		
		BuildEvent buildEvent = new BuildEvent(testProject);
		logger.buildStarted(buildEvent);
		logger.buildFinished(buildEvent);
		logger.taskStarted(buildEvent);
		logger.taskFinished(buildEvent);
		logger.targetStarted(buildEvent);
		logger.targetFinished(buildEvent);
		logger.messageLogged(buildEvent);
		
		verify(testLogger1).buildStarted(buildEvent);
		verify(testLogger1).buildFinished(buildEvent);
		verify(testLogger1).taskStarted(buildEvent);
		verify(testLogger1).taskFinished(buildEvent);
		verify(testLogger1).targetStarted(buildEvent);
		verify(testLogger1).targetFinished(buildEvent);
		verify(testLogger1).messageLogged(buildEvent);
	}

	@Test
	public void testWithTwoListener() {
		final TestLogger1 testLogger1 = mock(TestLogger1.class);
		final TestLogger2 testLogger2 = mock(TestLogger2.class);

		final Map<String, BuildLogger> testMapping = new HashMap<String, BuildLogger>();
		testMapping.put("de.richter.ant.TestLogger1", testLogger1);
		testMapping.put("de.richter.ant.TestLogger2", testLogger2);
		
		MultiLogger logger = setupMultiLogger(testMapping);

		Project testProject = setupTestProject();
		testProject.setProperty("MultiLogger.logger",
				"de.richter.ant.TestLogger1, de.richter.ant.TestLogger2");

		BuildEvent buildEvent = new BuildEvent(testProject);

		logger.buildStarted(buildEvent);
		List<BuildLogger> buildLoggerInstances = logger.getLogger();
		Assert.assertEquals("Should have two listeners", 2, buildLoggerInstances.size());
		Assert.assertEquals("This should be TestLogger1", testLogger1, buildLoggerInstances.get(0));
		Assert.assertEquals("This should be TestLogger2", testLogger2, buildLoggerInstances.get(1));
	}

	// -------------------------------------------------------------- Test-Helper
	private MultiLogger setupMultiLogger(final Map<String, BuildLogger> buildLoggerNameToInstanceMapping) {
		MultiLogger logger = new MultiLogger()
		{
			protected org.apache.tools.ant.BuildLogger createLoggerInstance(String loggerClassName)
			{
				BuildLogger buildLogger = buildLoggerNameToInstanceMapping.get(loggerClassName);
				
				if (buildLogger != null)
				{
					return buildLogger;
				}
				else
					throw new IllegalArgumentException("Unsupported class.");
			};
		};
		
		return logger;
	}
	
	private Project setupTestProject() {
		Project testProject = new Project();
		testProject.setName("TestProject");
		return testProject;
	}
}
