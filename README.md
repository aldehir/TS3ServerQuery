# TS3ServerQuery

A Java library for communicating with a Teamspeak 3 server through the
Teamspeak 3 Server Query.

## Obtaining TS3ServerQuery Library

### Jar Package

[TS3ServerQuery-1.0-SNAPSHOT.jar][snapshot] is available for download.

### Cloning from git

Alternatively, you can clone the master branch.

    $ git clone git://github.com/aldehir/TS3ServerQuery.git

Then you can either use maven to compile and package it as a jar archive in the
`target/` folder

    $ mvn package

Or, you can install it to your local maven repository

    $ mvn install

and add the package as a dependency in your own maven projects

```xml
<dependency>
  <groupId>net.visualcoding.ts3serverquery</groupId>
  <artifactId>TS3ServerQuery</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

## Dependencies

[SLF4J][slf4j] is used so applications implementing TS3ServerQuery
can use the logging library of their choice.

## Usage

`TS3ServerQueryClient` acts much like a simple telnet client. However, it also
adds the handling of notifications. Below is basic usage of the client, minus
exception handling for the sake of simplicity.

```java
TS3ServerQueryClient client = new TS3ServerQueryClient("localhost", 10011);

// Connect to the server
client.connect();

// Log in with your server query admin credentials
TS3Result result = client.execute("login username password");

if(result.hasError()) {
    System.out.println("Login failed");
} else {
    System.out.println("Successfully logged in");
}

// Connect to virtual server 1
client.execute("use sid=1");

// ... more code ...

// Disconnect from the server
client.disconnect();
```

## Documentation

* [Examples][examples]
* [Javadocs][javadocs]

[snapshot]: https://github.com/downloads/aldehir/TS3ServerQuery/TS3ServerQuery-1.0-SNAPSHOT.jar
[examples]: https://gist.github.com/3463717 "TS3ServerQuery Examples"
[javadocs]: http://aldehir.github.com/TS3ServerQuery/javadoc/1.0-SNAPSHOT
[slf4j]: http://www.slf4j.org/
