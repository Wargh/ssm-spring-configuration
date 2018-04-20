# README #

### What is this repository for? ###

This repository is used to automatically load application properties from the AWS Parameter Store table.

### How do I use ssm-spring-configuration ###

Simply include ssm-spring-configuration as a project dependency. 
On application startup Spring will automatically load entries from the AWS Parameter Store in the following order:

* default
* active spring profiles, in order

The properties loaded by ssm-spring-configuration will override any specified in application yml or properties files included in the application but will be overridden by system properties.

It is possible to disable ssm-spring-configuration by specifying -Dssm.configuration.enabled=false.

### How does it work ###

ssm-spring-configuration is implemented as a standard Spring EnvironmentPostProcessor. It is registered for loading in META-INF/spring.factories as normal.
