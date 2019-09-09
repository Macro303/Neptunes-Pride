<img src="https://github.com/Macro303/Assets/blob/master/Neptunes-Dashboard/logo.png" align="left" width="120" height="115" alt="Neptune's Dashboard Logo"/>

# Neptune's Dashboard
[![Version](https://img.shields.io/github/tag-pre/Macro303/Neptunes-Dashboard.svg?label=version)](https://github.com/Macro303/Neptunes-Dashboard/releases)
[![Issues](https://img.shields.io/github/issues/Macro303/Neptunes-Dashboard.svg?label=issues)](https://github.com/Macro303/Neptunes-Dashboard/issues)
[![Contributors](https://img.shields.io/github/contributors/Macro303/Neptunes-Dashboard.svg?label=contributors)](https://github.com/Macro303/Neptunes-Dashboard/graphs/contributors)
[![License](https://img.shields.io/github/license/Macro303/Neptunes-Dashboard.svg?=label=license)](https://raw.githubusercontent.com/Macro303/Neptunes-Dashboard/master/LICENSE)

Pulls game information for Neptune's Pride and attempts to display it in a simple web interface and REST endpoints.

_Currently only supports **Proteus**_

## Built Using
 - [JDK: 11](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
 - [Gradle: 5.6.2](https://gradle.org/)
 - [kotlin-stdlib-jdk8: 1.3.50](https://kotlinlang.org/)
 - [ktor-server-netty: 1.2.4](https://github.com/ktorio/ktor)
 - [ktor-gson: 1.2.4](https://github.com/ktorio/ktor)
 - [ktor-freemarker: 1.2.4](https://github.com/ktorio/ktor)
 - [snakeyaml: 1.25](https://bitbucket.org/asomov/snakeyaml)
 - [exposed: 0.17.3](https://github.com/JetBrains/Exposed)
 - [unirest-java: 1.4.9](http://unirest.io/java.html)
 - [log4j-slf4j-impl: 2.12.1 (Runtime)](https://logging.apache.org/log4j/2.x/)
 - [sqlite-jdbc: 3.28.0 (Runtime)](https://github.com/xerial/sqlite-jdbc)
 
## Running
**To run from source:**
```bash
$ gradle clean run
```
_You can change basic proxy settings, server settings and the selected game in the generated `config.yaml`_