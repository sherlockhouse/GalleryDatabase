LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

# Module name should match apk name to be installed
LOCAL_PACKAGE_NAME := FreemeGallery
LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_ENABLED := disabled

LOCAL_SRC_FILES := \
    $(call all-java-files-under, app/src/main/java) \
    $(call all-renderscript-files-under, app/src/main/java)
LOCAL_SRC_FILES += $(call all-java-files-under, app/src/main/freeme)
LOCAL_SRC_FILES += $(call all-java-files-under, app/src/main/src_utils)
LOCAL_SRC_FILES += $(call all-java-files-under, app/src/main/src_pd)
LOCAL_SRC_FILES += $(call all-java-files-under, app/src/main/bigModel)
LOCAL_SRC_FILES += $(call all-java-files-under, app/src/international/freeme)
# make plugin @{
LOCAL_SRC_FILES += $(call all-java-files-under, app/src/main/gallerycommon)
# @}

LOCAL_ASSET_DIR += $(LOCAL_PATH)/app/src/main/assets


LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/app/src/main/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/app/src/main/bigModel/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/app/src/main/freeme/res


LOCAL_STATIC_JAVA_LIBRARIES += gsonlib
LOCAL_STATIC_JAVA_LIBRARIES += gesturesensor
LOCAL_STATIC_JAVA_LIBRARIES += greendao
LOCAL_STATIC_JAVA_LIBRARIES += mp4parser
LOCAL_STATIC_JAVA_LIBRARIES += xmp

LOCAL_STATIC_JAVA_LIBRARIES += android-support-multidex

LOCAL_STATIC_JAVA_LIBRARIES += \
    android-support-v4

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages com.freeme.support.design


# LOCAL_JNI_SHARED_LIBRARIES := libjni_eglfence libjni_filtershow_filters libjni_jpegstream


include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := gsonlib:libs/gson-2.6.2.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += gesturesensor:libs/gestureSensor.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += greendao:libs/greendao-2.1.0.jar
include $(BUILD_MULTI_PREBUILT)


include $(call all-makefiles-under, jni)

# Use the following include to make gallery test apk
include $(call all-makefiles-under, $(LOCAL_PATH))

