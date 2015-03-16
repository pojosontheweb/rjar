# rjar : Recursive Java Archive (jar) Tool #

**rjar** is a command-line tool that acts like the regular **jar** tool, but _recursively_ ! It is very useful when e.g. using _ear_ or _war_ files, or any other "jars of jars"...

Supported features :
  * **list jar entries recursively** : all entries are displayed including entries of nested jars if any
  * **expand a jar recursively** : expand the jar and its nested jars to the specified directory
  * **recreate a jar from an expanded rjar** : allows to rebuild a jar with nested jars from an expanded directory structure
  * **graphical front-end** : tree view of your jars, with regexp filtering

## Examples ##

List the contents of an ear :
```
mybox:~ vankeisb$ rjar t ./projects/jarutils/src/test/resources/test.ear 
test.ear/META-INF/
test.ear/META-INF/MANIFEST.MF
test.ear/META-INF/application.xml
test.ear/mywebapp.war/
test.ear/mywebapp.war/META-INF/
test.ear/mywebapp.war/META-INF/MANIFEST.MF
test.ear/mywebapp.war/WEB-INF/
test.ear/mywebapp.war/WEB-INF/lib/
test.ear/mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar/
test.ear/mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar/META-INF/
test.ear/mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar/META-INF/MANIFEST.MF
test.ear/mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar/javax/
test.ear/mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar/javax/servlet/
test.ear/mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar/javax/servlet/jsp/
test.ear/mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar/javax/servlet/jsp/jstl/
test.ear/mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar/javax/servlet/jsp/jstl/core/
test.ear/mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar/javax/servlet/jsp/jstl/fmt/
test.ear/mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar/javax/servlet/jsp/jstl/sql/
test.ear/mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar/javax/servlet/jsp/jstl/tlv/
test.ear/mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar/javax/servlet/jsp/jstl/core/ConditionalTagSupport.class
...
```

Extract the whole ear recursively :
```
mybox:~ vankeisb$ rjar x ./projects/jarutils/src/test/resources/test.ear /tmp
mybox:~ vankeisb$ find /tmp/test.ear 
/tmp/test.ear
/tmp/test.ear/META-INF
/tmp/test.ear/META-INF/application.xml
/tmp/test.ear/META-INF/MANIFEST.MF
/tmp/test.ear/mywebapp.war
/tmp/test.ear/mywebapp.war/META-INF
/tmp/test.ear/mywebapp.war/META-INF/MANIFEST.MF
/tmp/test.ear/mywebapp.war/test.jsp
/tmp/test.ear/mywebapp.war/WEB-INF
/tmp/test.ear/mywebapp.war/WEB-INF/lib
/tmp/test.ear/mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar
/tmp/test.ear/mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar/javax
/tmp/test.ear/mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar/javax/servlet
/tmp/test.ear/mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar/javax/servlet/jsp
/tmp/test.ear/mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar/javax/servlet/jsp/jstl
/tmp/test.ear/mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar/javax/servlet/jsp/jstl/core
/tmp/test.ear/mywebapp.war/WEB-INF/lib/jstl-1.1.0.jar/javax/servlet/jsp/jstl/core/ConditionalTagSupport.class
...
```

Rebuild a previously expanded (r)jar :
```
mybox:~ vankeisb$ rjar r /tmp/test.ear /tmp/recreated
mybox:~ vankeisb$ ls /tmp/recreated
test.ear
```

As any other command line utility, rjar can be combined with other unix commands. Here's how you can find a particular class in a set of jar files :
```
mybox:~ vankeisb$ cd $ECLIPSE_HOME
mybox:eclipse-3.3.2 vankeisb$ find . -name *.jar -exec rjar t {} \; | grep plugin.properties
com.ibm.icu_3.6.1.v20070906.jar/plugin.properties
com.jcraft.jsch_0.1.31.jar/plugin.properties
javax.servlet.jsp_2.0.0.v200706191603.jar/plugin.properties
javax.servlet_2.4.0.v200706111738.jar/plugin.properties
javax.wsdl_1.4.0.v200706111329.jar/plugin.properties
...
```

You can also use the GUI front end to open the jar(s) in a tree view :
```
mybox:~ vankeisb$ rjar ui ./projects/jarutils/src/test/resources/test.ear ./commons-logging.jar
```

![http://rjar.googlecode.com/files/rjar-screenshot1.png](http://rjar.googlecode.com/files/rjar-screenshot1.png)

And even filter using regular expressions :

![http://rjar.googlecode.com/files/rjar-screenshot2.png](http://rjar.googlecode.com/files/rjar-screenshot2.png)

## From Groovy ##

rjar is implemented as a set of Groovy classes. It basically augments the JarFile class from the JDK, via a Category class, adding methods to crawl the jar file recursively.

Check out the code from the SVN and look at the unit tests for usage examples.


