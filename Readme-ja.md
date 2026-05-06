Burp suite 拡張 FakeDnsServerExtension
=============

Language/[English](Readme.md)

このツールは、PortSwigger社の製品であるBurp Suiteの拡張になります。
Burp Pro/Communityに対応しています。

## 概要

この拡張はDNS偽装サーバを目的としたツールです。

## 最新版について

メインのリポジトリ(main)には開発中のコードが含まれている場合があります。
安定したリリース版は､以下よりダウンロードしてください。

* https://github.com/raise-isayan/FakeDnsServerExtension/releases

利用するバージョンは以下のものをご利用ください

* Burp suite v2023.1.2 より後のバージョン

## 利用方法

Burp suite の Extender は以下の手順で読み込めます。

1. [Extender]タブの[add]をクリック
2. [Select file ...]をクリックし、FakeDnsServerExtension.jar を選択する。
3. ｢Next｣をクリックし、エラーがでてないことを確認後、「Close」にてダイヤログを閉じる。

### FakeDns タブ

Burp SuiteにFakeDnsServerExtensionタブが追加されます。

![ReDoSDetector Tab Scan](/image/FakeDnsServer.png)

Bind Intarface::
  バインドインタフェースを指定します。

Fake IP::
  偽装IPを指定します。

Name servers::
  ネームサーバを指定します。

Fake Domains::
  偽装ドメインを指定します。

resolv burp hosts::
   この設定を有効にすると、ホスト名の解決はBurp Suiteのhosts設定に基づいて行われます。デフォルトでは、この設定は有効になっています。

resolv system hosts::
    この設定を有効にすると、ホスト名の解決はOSのhosts設定に基づいて行われます。デフォルトでは、この設定は有効になっています。

## CLI オプション

コマンドラインで起動するCLIモードが存在します。

````
java -jar FakeDnsServerExtension-v0.2.jar -h

Usage: java -jar FakeDnsServerExtension.jar [option] [-i, --interface <interface>] [--fakeip <fakeip>] [--fakedomains <FakeDomains>] [--nameservers <NameServers>] [-p, --port <dnPport>]
[option]
        -h - help show
        -h - version show
        -gui - GUI Mode
[command]
        -i, --interface <interface> - Specify the interface IP address.
        --fakeip <fakeip> - Specify the IP address to spoof
        --fakedomains <FakeDomains> - Specify the domain to spoof
        --nameservers <NameServers>  - Specify the name server
        --p, -port <dnsPort> - Specify the DNS port
        --disable-system-hosts - Disable DNS name resolution using the system hosts file
````

#### CAUTION:

コマンドラインモード時は、resolv burp hosts は常に無効です。

## GUI オプション

Burp Suite不要のスタンドアローンで起動するGUIモードが存在します。

````
java -jar FakeDnsServerExtension.jar -gui
````

## ビルド

```
gradlew release
```

## 実行環境

.Java
* JRE (JDK) 21 (Open JDK is recommended) (https://openjdk.java.net/)

.Burp suite
* v2024.2.1.3 or higher (http://www.portswigger.net/burp/)

## 開発環境
* NetBean 28 (https://netbeans.apache.org/)
* Gradle 8.5 (https://gradle.org/)

## 必須ライブラリ
ビルドには別途 [BurpExtensionCommons](https://github.com/raise-isayan/BurpExtensionCommons) のライブラリを必要とします。
* BurpExtensionCommons v3.2.17 以上
  * https://github.com/raise-isayan/BurpExtensionCommons

## 利用ライブラリ

* google gson (https://github.com/google/gson)
  * Apache License 2.0
  * https://github.com/google/gson/blob/master/LICENSE

* Universal Chardet for java (https://code.google.com/archive/p/juniversalchardet/)
  * MPL 1.1
  * https://code.google.com/archive/p/juniversalchardet/

* dnsjava
  * https://mvnrepository.com/artifact/dnsjava/dnsjava


以下のバージョンで動作確認しています。
* Burp suite v2026.3.3

## 注意事項
このツールは、私個人が勝手に開発したもので、PortSwigger社は一切関係ありません。本ツールを使用したことによる不具合等についてPortSwiggerに問い合わせないようお願いします。
