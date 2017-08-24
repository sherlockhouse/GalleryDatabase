
package com.freeme.statistic;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public class StatisticData {
    public final static String ID                = "_id";
    public final static String OPTION_ID         = "op";
    public final static String OPTION_NUM        = "n";
    public final static String OPTION_TIMES      = "s";
    public final static String OPTION_TIMES_EXIT = "e";
    public final static String VERSION_CODE      = "vc";
    public final static String VERSION_NAME      = "vn";
    public final static String NETWORK_TYPE      = "network";

    public static final String DEFAULT_SORT_ORDER = "_id asc";
    public static final String AUTHORITY          = "com.freeme.gallery.statistic";

    public static final int ITEM    = 1;
    public static final int ITEM_ID = 2;

    public static final String CONTENT_TYPE      = "vnd.android.cursor.dir/vnd.com.freeme.gallery.statistic";
    public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.com.freeme.gallery.statistic";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/item");

    /**
     * 相册数据统计字段
     */
    public static final String OPTION_ENTER            = "00050001";   //启动相册
    public static final String OPTION_EXIT             = "00050002";   //退出相册
    public static final String OPTION_BABY             = "00050003";   //点击生活画报_亲子相册
    public static final String OPTION_BABY_ADD         = "00050004";   //点击亲子相册_添加
    public static final String OPTION_LOVE             = "00050005";   //点击生活画报_恋爱相册
    public static final String OPTION_LOVE_ADD         = "00050006";   //点击恋爱相册_添加
    public static final String OPTION_ALBUM_ADD        = "00050007";   //点击生活画报_添加相册
    public static final String OPTION_ALBUM_HIDE       = "00050008";   //点击全部相册_隐藏相册
    public static final String OPTION_JIGSAW           = "00050009";   //点击全部相册_拼图
//    public static final String OPTION_COMMUNITY        = "00050010";   //点击社区化广场
//    public static final String OPTION_COMMUNITY_TOP    = "00050011";   //点击社区化广场_热门页面
//    public static final String OPTION_COMMUNITY_LATEST = "00050012";   //点击社区化广场_最新页面
//    public static final String OPTION_COMMUNITY_UPLOAD = "00050013";   //点击社区化广场_上传图片
    public static final String OPTION_BIGMODE          = "00050014";   //点击大片功能
    public static final String OPTION_SHARE            = "00050015";   //点击分享功能
    public static final String OPTION_DELETE           = "00050016";   //点击删除功能
//    public static final String OPTION_DELETE_PLAZA     = "00050017";   //点击我的广场删除功能
    public static final String OPTION_SLIDESHOW        = "00050018";   //点击幻灯片功能
    public static final String OPTION_EDIT             = "00050019";   //点击编辑功能
    public static final String OPTION_CAMERA           = "00050020";   //点击相机
//    public static final String OPTION_THUMB_LATEST     = "00050021";   //美图广场最新列表点赞
//    public static final String OPTION_THUMB_TOP        = "00050022";   //美图广场热门列表点赞
//    public static final String OPTION_THUMB_DETAIL     = "00050023";   //美图广场弹慕详情点赞

    /**
     * 图吧数据统计字段
     */
    public static final String COMMUNITY_ENTER      = "00140001";   //启动图吧
    public static final String COMMUNITY_EXIT       = "00140002";   //退出图吧
    public static final String COMMUNITY_LATEST     = "00140003";   //点击图吧_最新页面
    public static final String COMMUNITY_TOP        = "00140004";   //点击图吧_热门页面
    public static final String COMMUNITY_USER       = "00140005";   //点击图吧_我的页面

    public static final String COMMUNITY_IMAGE_DETAIL                   = "00140006";   //进入图片详情
    public static final String COMMUNITY_IMAGE_DETAIL_THUMB             = "00140007";   //图片详情_点击喜欢
    public static final String COMMUNITY_IMAGE_DETAIL_UNTHUMB           = "00140008";   //图片详情_点击取消喜欢
    public static final String COMMUNITY_IMAGE_DETAIL_COMMENT           = "00140009";   //图片详情_点击吐槽
    public static final String COMMUNITY_IMAGE_DETAIL_SHARE             = "00140010";   //图片详情_点击分享  保留给FreemeOS 7.0
    public static final String COMMUNITY_IMAGE_DETAIL_REPORT            = "00140011";   //图片详情_举报
    public static final String COMMUNITY_IMAGE_DETAIL_REPORT_SUCCESS    = "00140012";   //图片详情_举报(举报成功)
    public static final String COMMUNITY_IMAGE_DETAIL_COMMENT_SUCCESS   = "00140013";   //图片详情_发送评论

    public static final String COMMUNITY_USER_LOGIN             = "00140014";   //我的_登录
    public static final String COMMUNITY_USER_LOGIN_SUCCESS     = "00140015";   //我的_登录(登录成功)
    public static final String COMMUNITY_PUBLISH                = "00140016";   //点击【+】发布图片
    public static final String COMMUNITY_PUBLISH_SUCCESS        = "00140017";   //点击【+】发布图片（发送成功）

    public static final String COMMUNITY_USER_PHOTOS            = "00140018";   //我的_图片
    public static final String COMMUNITY_USER_PHOTOS_DELETE     = "00140019";   //删除图片
    public static final String COMMUNITY_USER_MESSAGE           = "00140020";   //我的_消息
    public static final String COMMUNITY_USER_MESSAGE_CLEAR     = "00140021";   //我的_清空消息
    public static final String COMMUNITY_SETTING_CACHE_CLEAR    = "00140022";   //设置_清理缓存
    public static final String COMMUNITY_SETTING_NOTIFICATION   = "00140023";   //设置_接收消息

    public static final String COMMUNITY_THUMB_LATEST           = "00140024";   //美图广场最新列表点赞
    public static final String COMMUNITY_THUMB_TOP              = "00140025";   //美图广场热门列表点赞

    public static class StatisticInfo {
        public String optionId;
        public int    optionNum;
        public long   optionTimes;
        public long   optionTimesExit;
        public int    versionCode;
        public String versionName;
        public String networkType;
    }

    public static void insertStatistic(Context context, StatisticInfo info) {
        if (context == null) {
            return;
        }

        ContentResolver resolver = context.getContentResolver();
        if (resolver == null || info == null) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(OPTION_ID, info.optionId);
        values.put(OPTION_NUM, info.optionNum);
        values.put(OPTION_TIMES, info.optionTimes);
        values.put(OPTION_TIMES_EXIT, info.optionTimesExit);
        values.put(VERSION_CODE, info.versionCode);
        values.put(VERSION_NAME, info.versionName);
        values.put(NETWORK_TYPE, info.networkType);
        resolver.insert(CONTENT_URI, values);
    }
}
