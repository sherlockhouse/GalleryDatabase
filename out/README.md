#1 add so to apk
$/droi/SDK/build-tools/25.0.2/aapt add FreemeGallery.apk lib/arm64-v8a/*.so
 'lib/arm64-v8a/libjni_eglfence.so'...
 'lib/arm64-v8a/libjni_filtershow_filters.so'...
 'lib/arm64-v8a/libjni_jpegstream.so'...

#2 remove old signature
$ for f in `/droi/freemeos/1.53_6757/out/host/linux-x86/bin/aapt list FreemeGallery.apk |grep "META-INF"` ; do /droi/freemeos/1.53_6757/out/host/linux-x86/bin/aapt remove FreemeGallery.apk $f; done

#3 sign the apk
$ /droi/SDK/build-tools/25.0.2/apksigner sign --ks signature/droigallery_platform.jks --out FreemeGallery_signed.apk FreemeGallery.apk

#4 the sign info
/droi/SDK/build-tools/25.0.1/apksigner verify -v FreemeGallery_signed.apk