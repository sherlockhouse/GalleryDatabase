#!/usr/bin/expect

set timeout 10000

spawn ./addso.sh

expect "Keystore password"
send zhuoyigallery\r


expect eof
