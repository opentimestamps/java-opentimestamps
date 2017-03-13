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

```

#### Stamp

Create timestamp `README.md.ots` from this `README.md` with the aid of a remote calendar.

```shell
$ java -jar target/OtsCli.jar stamp README.md
Submitting to remote calendar https://alice.btc.calendar.opentimestamps.org
Submitting to remote calendar https://bob.btc.calendar.opentimestamps.org
Submitting to remote calendar https://ots.eternitywall.it
The timestamp proof 'README.md.ots' has been created!
```

#### Info

Show information on a timestamp.

```shell
$ java -jar target/OtsCli.jar info examples/incomplete.txt.ots
File sha256 hash: 05c4f616a8e5310d19d938cfd769864d7f4ccdc2ca8b479b10af83564b097af9
Timestamp:
append e754bf93806a7ebaa680ef7bd0114bf4
sha256
append b573e8850cfd9e63d1f043fbb6fc250e
sha256
prepend 57cfa5c4
append 6fb1ac8d4e4eb0e7
verify PendingAttestation('https://alice.btc.calendar.opentimestamps.org')

```

#### Verify

Verify the timestamp attestations with the aid of remote block explorers.

```shell
$ java -jar target/OtsCli.jar verify examples/hello-world.txt.ots
Assuming target filename is 'examples/hello-world.txt'
Success! Bitcoin attests data existed as of Thu May 28 2015 17:41:18 GMT+0200 (CEST)
```

Note: This verification using block explorers is convenient but not as secure as asking to a local node.
To mitigate the risks, answer from block explorer is considered only if two different endpoint return the same result. Even by doing so this is not as secure as asking a local node.   

#### Upgrade

Upgrade incomplete remote calendar timestamps to be independently verifiable. This command overwrite the file `examples/incomplete.txt.ots` if needed and make a backup of the old content at `examples/incomplete.txt.ots`. 

```shell
$ java -jar target/OtsCli.jar upgrade examples/incomplete.txt.ots
Timestamp has been successfully upgraded!
```

## Testing

```
cd java-opentimestamps
mvn test
```

## License

LGPL3

