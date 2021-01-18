# java-opentimestamps

[![OpenTimestamps logo][2]][1]

[1]: https://opentimestamps.org
[2]: https://raw.githubusercontent.com/opentimestamps/logo/master/white-bg/website-horizontal-350x75.png (OpenTimestamps logo)

[![Build Status](https://travis-ci.org/opentimestamps/java-opentimestamps.svg?branch=master)](https://travis-ci.org/opentimestamps/java-opentimestamps)

This repo host the Java implementation of OpenTimestamps.

It is a based on the python implementation at https://github.com/opentimestamps/python-opentimestamps and https://github.com/opentimestamps/opentimestamps-client

## Compatibility

Java 1.7+

## Maven dependency

```xml
<dependency>
    <groupId>com.eternitywall</groupId>
    <artifactId>java-opentimestamps</artifactId>
    <version>1.20</version>
</dependency>
```

## Installation

```
git clone https://github.com/opentimestamps/java-opentimestamps
cd java-opentimestamps
mvn install
```

#### SSL errors

If you get `SSLHandshakeException` during `mvn install` please refer to the following [issue](https://github.com/opentimestamps/java-opentimestamps/issues/1).

## Command line

#### Stamp

Create timestamp `README.md.ots` from this `README.md` with the aid of a remote calendar.

```shell
$ java -jar target/OtsCli.jar stamp README.md
Submitting to remote calendar https://alice.btc.calendar.opentimestamps.org
Submitting to remote calendar https://bob.btc.calendar.opentimestamps.org
Submitting to remote calendar https://finney.calendar.eternitywall.com
The timestamp proof 'README.md.ots' has been created!
```

##### Stamping only a hash

Create timestamp proof file from the `sha256` hash, equals to `03ba204e50d126e4674c005e04d82e84c21366780af1f43bd54a37816b6ab340`, with the aid of a remote calendar.

 ```shell
$ java -jar target/OtsCli.jar -H 03ba204e50d126e4674c005e04d82e84c21366780af1f43bd54a37816b6ab340 -a sha256 stamp
INFO: Submitting to remote calendar https://alice.btc.calendar.opentimestamps.org
INFO: Submitting to remote calendar https://bob.btc.calendar.opentimestamps.org
INFO: Submitting to remote calendar https://finney.calendar.eternitywall.com
The timestamp proof '03ba204e50d126e4674c005e04d82e84c21366780af1f43bd54a37816b6ab340.ots' has been created!
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

##### Shrink

When a proof is completed with at least one bitcoin attestation, information regarding other paths are redundant.
The oldest bitcoin attestation (the lower bitcoin block height) is the only relevant information, in this case we can shrink all the redundant informatio.

```shell
$ java -jar target/OtsCli.jar upgrade examples/merkle2.txt.ots 
Timestamp has been successfully upgraded!
$ java -jar target/OtsCli.jar info examples/merkle2.txt.ots
# ... have a look at the double path
$ java -jar target/OtsCli.jar upgrade examples/merkle2.txt.ots
$ java -jar target/OtsCli.jar info examples/merkle2.txt.ots
# ... now the ots receipt contains only the relevant information

```

## From code

#### Stamp and Info

Create timestamp with the aid of remote calendars.

```java
DetachedTimestampFile detached = DetachedTimestampFile.from(new OpSHA256(), file);
Timestamp stampResult = OpenTimestamps.stamp(detached);
String infoResult = OpenTimestamps.info(detached);
System.out.println(infoResult);
```

#### Verify

Verify the timestamp attestations.

```java
byte[] file = Utils.hexToBytes("48656c6c6f20576f726c64210a");
byte[] fileOts = Utils.hexToBytes("004f70656e54696d657374616d7073000050726f6f6600bf89e2e884e89294010803ba204e50d126e4674c005e04d82e84c21366780af1f43bd54a37816b6ab34003f1c8010100000001e482f9d32ecc3ba657b69d898010857b54457a90497982ff56f97c4ec58e6f98010000006b483045022100b253add1d1cf90844338a475a04ff13fc9e7bd242b07762dea07f5608b2de367022000b268ca9c3342b3769cdd062891317cdcef87aac310b6855e9d93898ebbe8ec0121020d8e4d107d2b339b0050efdd4b4a09245aa056048f125396374ea6a2ab0709c6ffffffff026533e605000000001976a9140bf057d40fbba6744862515f5b55a2310de5772f88aca0860100000000001976a914f00688ac000000000808f120a987f716c533913c314c78e35d35884cac943fa42cac49d2b2c69f4003f85f880808f120dec55b3487e1e3f722a49b55a7783215862785f4a3acb392846019f71dc64a9d0808f120b2ca18f485e080478e025dab3d464b416c0e1ecb6629c9aefce8c8214d0424320808f02011b0e90661196ff4b0813c3eda141bab5e91604837bdf7a0c9df37db0e3a11980808f020c34bc1a4a1093ffd148c016b1e664742914e939efabe4d3d356515914b26d9e20808f020c3e6e7c38c69f6af24c2be34ebac48257ede61ec0a21b9535e4443277be306460808f1200798bf8606e00024e5d5d54bf0c960f629dfb9dad69157455b6f2652c0e8de810808f0203f9ada6d60baa244006bb0aad51448ad2fafb9d4b6487a0999cff26b91f0f5360808f120c703019e959a8dd3faef7489bb328ba485574758e7091f01464eb65872c975c80808f020cbfefff513ff84b915e3fed6f9d799676630f8364ea2a6c7557fad94a5b5d7880808f1200be23709859913babd4460bbddf8ed213e7c8773a4b1face30f8acfdf093b7050808000588960d73d7190103f7ef15");

DetachedTimestampFile detached = DetachedTimestampFile.from(new OpSHA256(), file);
DetachedTimestampFile detachedOts = DetachedTimestampFile.deserialize(fileOts);

HashMap<VerifyResult.Chains, VerifyResult> result = OpenTimestamps.verify(detachedOts,detached);
if (result == null || result.isEmpty()) {
    System.out.println("Pending or Bad attestation");
} else {
    result.forEach((k, v) -> System.out.println("Success! " + k + " attests data existed as of "+ new Date(v.timestamp * 1000)));
}
```

Variable `fileOts` created from the hex representation of the file `test/hello-world.txt.ots` while `file` contains `test/hello-world.txt`

#### Upgrade

Upgrade incomplete remote calendar timestamps to be indipendently verifiable.

```java
byte[] ots = Utils.hexToBytes("004f70656e54696d657374616d7073000050726f6f6600bf89e2e884e89294010805c4f616a8e5310d19d938cfd769864d7f4ccdc2ca8b479b10af83564b097af9f010e754bf93806a7ebaa680ef7bd0114bf408f010b573e8850cfd9e63d1f043fbb6fc250e08f10457cfa5c4f0086fb1ac8d4e4eb0e70083dfe30d2ef90c8e2e2d68747470733a2f2f616c6963652e6274632e63616c656e6461722e6f70656e74696d657374616d70732e6f7267");
DetachedTimestampFile detachedOts = DetachedTimestampFile.deserialize(ots);
boolean changed = OpenTimestamps.upgrade(detachedOts);
if(!changed) {
   System.out.println("Timestamp not upgraded");
} else {
   System.out.println("Timestamp upgraded");
}
```

## Testing

```
cd java-opentimestamps
mvn test
```



## License

LGPL3
