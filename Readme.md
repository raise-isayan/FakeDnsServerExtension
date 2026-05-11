Burp suite Extension FakeDnsServerExtension
=============

Language/[Japanese](Readme-ja.md)

This tool is an extension of PortSwigger product, Burp Suite.
Supports Burp suite Professional/Community.

## Overview

This extension is a tool that provides a DNS spoofing server.

## About the latest version

The main repository (main) may contain code under development.
Please download the stable release version from the following.

* https://github.com/raise-isayan/FakeDnsServerExtension/releases

Please use the following versions

* Burp suite v2024.2.1.3 or above

## How to Use

The Burp Suite Extension can be loaded by following the steps below.

1. Click [add] on the [Extensions] tab
2. Click [Select file ...] and select FakeDnsServerExtension.jar
3. Click [Next], confirm that no error is occurring, and close the dialog with [Close].

### FakeDns Tab

FakeDnsServerExtension tab will be added to Burp Suite.

![FakeDnsServerExtension Tab Scan](/image/FakeDnsServer.png)

<dl>
  <dt>[Start] Button:</dt>
  <dd>Press this button to start DNS spoofing. Press it again to stop.</dd>

  <dt>Bind Intarface:</dt>
  <dd>Specify the bind interface.</dd>

  <dt>Fake IP:</dt>
  <dd>Specify the IP(IPv4 and IPv6) address to spoof</dd>

  <dt>Name servers:</dt>
  <dd>Specify the name server/dd>
  <dd>(ex) 8.8.8.8,8.8.8.4</dd>

  <dt>[resolv burp hosts] Check box:</dt>
  <dd>If you enable this setting, hostname resolution will be based on the hosts configuration in Burp Suite. By default, this setting is enabled.</dd>

  <dt>[resolv system hosts] Check box:</dt>
  <dd>If you enable this setting, hostname resolution will be based on the OS hosts file. By default, this setting is enabled.</dd>

  <dt>Fake domains:</dt>
  <dd>Specify the domain to spoof.</dd>

  <dt>[Paste domains] Button:</dt>
  <dd>Add domains from the clipboard, separated by commas or line breaks.</dd>

  <dt>[Add All] Button:</dt>
  <dd>Add multiple domain names.</dd>

  <dt>[Add] Button:</dt>
  <dd>Add a domain.</dd>

  <dt>[Edit] Button:</dt>
  <dd>Edit the selected domain.</dd>

  <dt>[Remove] Button:</dt>
  <dd>Remove the selected domain.</dd>

  <dt>[Remove] Button:</dt>
  <dd>Remove all domains.</dd>

</dl>

### Log

<dl>
  <dt>[Extensions] -> [Output]:</dt>
  <dd>Outputs status messages, including DNS resolution logs.</dd>

  <dt>[Extensions] -> [Error]:</dt>
  <dd>Outputs error message, including logs of exceptions.</dd>

</dl>

## CLI Option

There is a CLI mode that can be launched from the command line.

````
java -jar FakeDnsServerExtension.jar -h

Usage: java -jar FakeDnsServerExtension.jar [option] [-i, --interface <interface>] [--fakeip <fakeip>] [--fakeipv6 <fakeip>] [--fakedomains <FakeDomains>] [--nameservers <NameServers>] [-p, --port <dnPport>]
[option]
        -h - help show
        -h - version show
        -gui - GUI Mode
[command]
        -i, --interface <interface> - Specify the interface IP address.
        --fakeip <fakeip> - Specify the IPv4 address to spoof
        --fakeipv6 <fakeip> - Specify the IPv6 address to spoof
        --fakedomains <FakeDomains> - Specify the domain to spoof
        --nameservers <NameServers>  - Specify the name server
        --p, -port <dnsPort> - Specify the DNS port
        --disable-system-hosts - Disable DNS name resolution using the system hosts file
````

#### CAUTION:

In command-line mode, `resolv burp hosts` is always disabled.

## GUI Option

There is a standalone GUI mode that does not require Burp Suite.


````
java -jar FakeDnsServerExtension.jar -gui
````

## buid

```
gradlew release
```

## Runtime environment

.Java
* JRE (JDK) 21 (Open JDK is recommended) (https://openjdk.java.net/)

.Burp suite
* v2024.2.1.3 or higher (http://www.portswigger.net/burp/)

## Development environment
* NetBean 28 (https://netbeans.apache.org/)
* Gradle 8.5 (https://gradle.org/)

## Required Library
Building requires a [BurpExtensionCommons](https://github.com/raise-isayan/BurpExtensionCommons) library.
* BurpExtensionCommons v3.2.17 or higher

## Use Library

* google gson (https://github.com/google/gson)
  * Apache License 2.0
  * https://github.com/google/gson/blob/master/LICENSE

* Universal Chardet for java (https://code.google.com/archive/p/juniversalchardet/)
  * MPL 1.1
  * https://code.google.com/archive/p/juniversalchardet/

* dnsjava(https://github.com/dnsjava/dnsjava)
  * BSD-3-Clause license
  * https://github.com/dnsjava/dnsjava/blob/master/LICENSE

Operation is confirmed with the following versions.
* Burp suite v2026.3.3

## important
This tool developed by my own personal use, PortSwigger company is not related at all. Please do not ask PortSwigger about problems, etc. caused by using this tool.
