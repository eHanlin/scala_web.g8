
## 環境佈置

###初始化

```shell
sbt
>changeConfig
>exit
npm install
gulp
```

### gulp

```shell
$ sudo npm install -g gulp
$ cd [專案目錄]
$ sudo npm install
$ gulp watch
```

### LiveReload

```
https://chrome.google.com/webstore/detail/livereload/jnihajbhpnppcggbcgedagnkighmdlei
https://addons.mozilla.org/zh-tw/firefox/addon/livereload/
```

### sbt

```shell
$ brew install sbt
$ cd [專案目錄]
$ sbt
> container:start
```

### sbt console 互動式 debug service

```shell
$ cd [專案目錄]
$ sbt
> console
scala> val mainService = ctx.getBean("mainService").asInstanceOf[MainService]
scala> mainService.echo("abc")
```

### sbt 相關自定指令

```shell

###同時加入一個 html, less 及 coffee 文件
addView [viewpath...]

###同時刪除一個 html, less 及 coffee 文件
removeView [viewpath...]

###變更環境相關的 config ，預設值是 default ，會依序從 src/config/default 再到 src/config/[site] 把 src/main/ 下相同路徑的檔案蓋掉
changeConfig [site]

###依 src/main/resources/aws.properties 中的設定，將 src/main/webapp/lib 及 src/main/webapp/resource 和 專案的 war 檔 上傳至 s3
deploy

###上傳專案的 war 檔到 current 版本
deploy current

###設定專案版本號，建議使用 x.x.x 的格式
setVersion

###遞增第一個版本號。 原本 1.2.3 執行後變成 2.0.0
incFirstVersion

###遞增第二個版本號。 原本 1.2.3 執行後變成 1.3.0
incMiddleVersion

###遞增第三個版本號。 原本 1.2.3 執行後變成 1.2.4
incLastVersion


```

### 佈署

```shell
$ sbt
> changeConfig [site]
> exit
$ gulp build
$ sbt
> package
> deploy
```
