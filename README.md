# cordova-plugin-beecloudsdk

Beecloud的cordova插件，暂时只有安卓版本，ios版本留待以后添加。
暂时只有支付宝app支付和微信app支付。

## 构建

安装以后，在源文件中填入自己的Beecloud相关参数。注意添加保护措施。

* android: 在Pay.java中

## api

### 插件声明对象
* `navigator.plugins.beecloud` beecloud对象，其中有多个功能对象。

### 各对象

#### beecloud
* `pay` 支付。以下为内部对象：
>* `init()` 调用各个支付渠道之前需要初始化beecloud参数等，相关参数在源文件中手动填入。注意，微信支付需要填入wxappid。
>* `ali_app(title, totalfee, orderNo, optional, cb(Error, info))` 支付宝app支付
>* `wx_app(title, totalfee, orderNo, optional, cb(Error, info))` 微信app支付