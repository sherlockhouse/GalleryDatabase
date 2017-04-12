#!/bin/sh

# 转换平台签名命令
./keytool-importkeypair -k droigallery_platform.jks -p zhuoyigallery -pk8 platform.pk8 -cert platform.x509.pem -alias droigallery

# droigallery_platform.jks : 签名文件
# zhuoyigallery : 签名文件密码
# platform.pk8、platform.x509.pem : 平台签名文件
# droigallery : 签名文件别名
