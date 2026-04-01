#PSP Server Logs

### Instantiate logger
```private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MyClass.class);```    
```private static final SpcfLogger LOGGER = Application.getLogger(MyClass.class);```


### Use logger
* ```LOGGER.error("Tag, key1={}, key2={}", val1, val2, exceptionObject);```
* ```LOGGER.info("Tag, key1={}, key2={}", val1, val2);```
* ```if (LOGGER.isDebugEnabled) { LOGGER.debug("Tag, key1={}, key2={}", val1, val2) };```

Note: in order to use log formatter `{}`, use `org.slf4j.Logger`

#### About PSP Loggers
* SpcfLogger Logger is a thin wrapper around slf4j API, and is used for backward compatibility
* We can directly use slf4j API as well.
* Underneath, it leverages log4j2 core logging implementation
* Log4j properties are loaded from ```app/PSE/configuration/src/main/resources/templates/spcf-logger-conf.xml```
  * Appenders
    * Console
    * File (`/usr/local/tomcat/logs/psp.log`)
  * In order to change log level for specific class / package, make an entry like `<Logger name="abc.xyz" level="warn"/>` in spcf-logger-conf.xml, where `abc.xyz` is the package name, and expected log level is `warn`
* Logger Types:
  * Synchronous Logger (currently used)
  * Asynchronous Logger -
    * How to enable:
      * add this VM param & all loggers would convert to : ```-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector```
    * There are some [Trade-Offs](https://logging.apache.org/log4j/log4j-2.3/manual/async.html#Trade-offs) as well!
* Log4j2 system properties [here](https://logging.apache.org/log4j/2.x/manual/configuration.html#SystemProperties])

## Splunk Logging Style
>* Use clear key-value pairs
>* Use timestamps for every event (taken care by default log pattern layout)
>* Use unique identifiers (IDs)
>* Use categories diligently (INFO/WARN/DEBUG)
>* Guard Debug log statements with ```LOGGER.isDebugEnabled()``` for better performance
>* Splunk recommended [Good Logging Practices](https://dev.splunk.com/enterprise/docs/developapps/addsupport/logging/loggingbestpractices/)
>* [Log4j IKS best practices](https://github.intuit.com/pages/kubernetes/modern-saas-docs/developer/log4j2-best-practices/)


## Log Transmission
* All service logs should go to console appender
* Cluster level FluentD daemon captures all logs from console.
* FluentD then 'zip & ship' to Splunk servers via S3
  * FluentD > S3 > LogWriter > Splunk-ALB > Splunk
* Refer [IKS Logging Document](https://github.intuit.com/pages/kubernetes/modern-saas-docs/iks/iks_logging/)

