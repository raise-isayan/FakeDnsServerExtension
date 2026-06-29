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

Burp suite の Extension は以下の手順で読み込めます。

1. [Extensions]タブの[add]をクリック
2. [Select file ...]をクリックし、FakeDnsServerExtension.jar を選択する。
3. ｢Next｣をクリックし、エラーがでてないことを確認後、「Close」にてダイヤログを閉じる。

### Fake Dns タブ

Burp SuiteにFakeDnsServerExtensionタブが追加されます。

![ReDoSDetector Tab Scan](/image/FakeDnsServer.png)

<dl>
  <dt>[Start] ボタン:</dt>
  <dd>押下するとDNSスプーフィングが開始されます。停止するにはもう一度押してください。</dd>

  <dt>Bind Intarface:</dt>
  <dd>バインドインタフェースを指定します。</dd>

  <dt>Fake IP:</dt>
  <dd>偽装IP(IPv4およびIPv6)を指定します。</dd>

  <dt>Name servers:</dt>
  <dd>ネームサーバを指定します。</dd>
  <dd>(例) 8.8.8.8,8.8.8.4</dd>

  <dt>[resolv burp hosts] チェックボックス:</dt>
  <dd>この設定を有効にすると、ホスト名の解決はBurp Suiteのhosts設定に基づいて行われます。デフォルトでは、この設定は有効になっています。</dd>

  <dt>[resolv burp hosts] チェックボックス:</dt>
  <dd>この設定を有効にすると、ホスト名の解決はBurp Suiteのhosts設定に基づいて行われます。デフォルトでは、この設定は有効になっています。</dd>

  <dt>Fake domains:</dt>
  <dd>偽装ずるドメインを指定します。</dd>

  <dt>[Paste domains] ボタン:</dt>
  <dd>クリップボードからカンマ区切りもしくは改行区切りのドメインを追加します。</dd>

  <dt>[Add All] ボタン:</dt>
  <dd>複数行のドメインを追加します。</dd>

  <dt>[Add] ボタン:</dt>
  <dd>ドメインを追加します。</dd>

  <dt>[Edit] ボタン:</dt>
  <dd>選択したドメインを編集します。</dd>

  <dt>[Remove] ボタン:</dt>
  <dd>選択ドメインを削除します。</dd>

  <dt>[Remove] ボタン:</dt>
  <dd>全てのドメインを削除します。</dd>
</dl>

### ログ

<dl>
  <dt>[Extensions] -> [Output]:</dt>
  <dd>DNS解決のログなどを含む正常メッセージを出力します。</dd>

  <dt>[Extensions] -> [Error]:</dt>
  <dd>Exceptionのログなどを含むエラーメッセージを出力します。</dd>

</dl>

## CLI オプション

コマンドラインで起動するCLIモードが存在します。

````
java -jar FakeDnsServerExtension.jar -h

Usage: java -jar FakeDnsServerExtension.jar [option] [-i, --interface <interface>] [--fakeip <fakeip>] [--fakeipv6 <fakeip>] [--fakedomains <FakeDomains>] [--nameservers <NameServers>] [-p, --port <dnPport>]
[option]
        -h, --help - help show
        -v, --version - version show
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

[コマンド(例)]
````
java -jar FakeDnsServerExtension.jar -i 192.168.137.1 --fakeip  192.168.137.1 --fakedomains www.example.com,www.example.jp
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
* NetBean 30 (https://netbeans.apache.org/)
* Gradle 9.6.1 (https://gradle.org/)

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

* dnsjava(https://github.com/dnsjava/dnsjava)
  * BSD-3-Clause license
  * https://github.com/dnsjava/dnsjava/blob/master/LICENSE

以下のバージョンで動作確認しています。
* Burp suite v2026.3.3

## 注意事項
このツールは、私個人が勝手に開発したもので、PortSwigger社は一切関係ありません。本ツールを使用したことによる不具合等についてPortSwiggerに問い合わせないようお願いします。
