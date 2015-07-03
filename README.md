# spring-boot-starter-marklogic [![Build Status](https://travis-ci.org/nikos/spring-boot-starter-marklogic.svg?branch=master)](http://travis-ci.org/nikos/spring-boot-starter-marklogic)

This project acts as [Spring Boot starter](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using-boot-maven-parent-pom) 
for web applications which want to leverage the MarkLogic database server 
as their central repository for storing and searching data and documents.


## Use the starter in your Maven project

To make use of the Spring Boot starter for MarkLogic in your
own Maven-based project, please add in the `dependencies`
section the following dependency:

    <dependency>
        <groupId>de.nava</groupId>
        <artifactId>spring-boot-starter-marklogic</artifactId>
        <version>0.1.3</version>
    </dependency>

Add the following section to your `repositories` element to allow Maven
to retrieve the POM and jar file via github:    
    
    <repository>
        <id>nikos-spring-boot-starter-marklogic</id>
        <url>https://raw.github.com/nikos/spring-boot-starter-marklogic/mvn-repo/</url>
    </repository>    
    
    
## Use the starter in your Gradle project

Inside your `dependencies` please add

    compile("de.nava:spring-boot-starter-marklogic:0.1.3")
    
Pluse add the link to the location where to resolve this dependency as
part of `repositories` section:

    maven { url "https://raw.github.com/nikos/spring-boot-starter-marklogic/mvn-repo/" }
    

## Sample project using this Spring Boot Starter

If you are interested in a real-world project making use of the MarkLogic Spring Boot starter please visit the [wm14](https://github.com/jojrg/wm14) github project. 
    
