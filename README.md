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

### Ansible task to create sample properties ###

    - name: Create sample properties
      aws_ssm_parameter_store:
        name: "{{ item.name }}"
        description: "{{ item.description | default(omit) }}"
        string_type: "{{ item.type | default('String') }}"
        value: "{{ item.value }}"
      with_items:
        - { name: "/prefix/test.param1", value: "value1", type: "String", description: "param with description" }
        - { name: "/prefix/test.param2", value: "value2", type: "String" }
        - { name: "/prefix/test.param3", value: "valuea,valueb", type: "StringList" }
        - { name: "/prefix/test.param4", value: "secret", type: "SecureString" }
        - { name: "/prefix/test/test.param1", value: "valuetest" }
        - { name: "/prefix/prod/test.param1", value: "valueprod" }
        - { name: "noprefix.param1", value: "value1" }
        - { name: "noprefix.param2", value: "value2" }
        - { name: "/prod/noprefix.param1", value: "valueprod" }

### Ansible task to show how sample properties are resolved ###

    - name: lookup defaults
      debug: msg="{{ lookup('aws_ssm', '/', bypath=true, shortnames=true) }}"

    - name: lookup defaults with prefix
      debug: msg="{{ lookup('aws_ssm', '/prefix', bypath=true, shortnames=true) }}"

    - name: lookup prod profile with prefix
      debug: msg="{{ lookup('aws_ssm', '/prefix/prod', bypath=true, shortnames=true) }}"

    - name: lookup combined properties with prefix
      debug: msg="{{ lookup('aws_ssm', '/prefix', bypath=true, shortnames=true) | combine(lookup('aws_ssm', '/prefix/prod', bypath=true, shortnames=true)) }}"

    - name: lookup combined properties without prefix
      debug: msg="{{ lookup('aws_ssm', '/', bypath=true, shortnames=true) | combine(lookup('aws_ssm', '/prod', bypath=true, shortnames=true)) }}"
