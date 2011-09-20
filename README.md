# Ant MultiLogger #

The Ant MultiLogger provides a way to specify more than one build logger. A list of available build logger can be found here [Ant Build Logger|http://ant.apache.org/manual/listeners.html]

## Usage ##

To use the MultiLogger, make sure to add the AntMultiLogger-version.jar to the library path for ant. You can do this by either placing the jar in ~/.ant/lib or provide it with -lib AntMultiLogger-version.jar when you start your build.

# Example #
ant -lib AntMultiLogger-version.jar -logger de.richter.ant.AntMultiLogger

To specify the logger you want to use, you need to provide a properties file *MultiLogger.properties* which contains one line:

# Example #
MultiLogger.logger=org.apache.tools.ant.listener.AnsiColorLogger,org.apache.tools.ant.listener.ProfileLogger

This will use the AnsiColorLogger and the ProfileLogger at the same time.