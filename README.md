# java-opentimestamps


![OpenTimestamps logo](https://raw.githubusercontent.com/opentimestamps/logo/master/white-bg/website-horizontal-350x75.png)

[![Build Status](https://travis-ci.org/eternitywall/java-opentimestamps.svg?branch=master)](https://travis-ci.org/eternitywall/java-opentimestamps)

This repo host the Java implementation of OpenTimestamps.

It is a based on the python implementation at https://github.com/opentimestamps/python-opentimestamps and https://github.com/opentimestamps/opentimestamps-client

## Compatibility

Java 1.7+

## Installation

```
git clone https://github.com/eternitywall/java-opentimestamps
cd java-opentimestamps
mvn install
```

## Command line

```
java -jar target/OtsCli.jar stamp README.md
```

## Testing

```
cd java-opentimestamps
mvn test
```

## License

LGPL3

