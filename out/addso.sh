/droi/SDK/build-tools/25.0.2/aapt add FreemeGallery.apk lib/arm64-v8a/*.so;

for f in `/droi/freemeos/1.53_6757/out/host/linux-x86/bin/aapt list FreemeGallery.apk |grep "META-INF"` ;
do
    /droi/freemeos/1.53_6757/out/host/linux-x86/bin/aapt remove FreemeGallery.apk $f;
done

/droi/SDK/build-tools/25.0.2/apksigner sign --ks signature/droigallery_platform.jks --out FreemeGallery_signed.apk FreemeGallery.apk;
