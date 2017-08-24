package com.freeme.community.utils;

import com.freeme.gallery.BuildConfig;

/**
 * AppConfig
 * Created by connorlin on 15-9-6.
 */
public class AppConfig {

    public final static boolean DEBUG = BuildConfig.DEBUG;

    // Base on 720p
    public final static int UI_WIDTH  = 720;
    public final static int UI_HEIGHT = 1280;

    // Base on density 2
    public final static int UI_DENSITY = 2;

    public final static boolean DEFAULT_DROI_PUSH = true;
    public final static boolean DEFAULT_BARRAGE = true;

    // SharePreferences path
    public final static String SHARED_PATH = "app_share";

    // SharePreferences keys
    public final static String SENSITIVE       = "sensitive";
    public final static String SENSITIVE_SET   = "sensitive_set";
    public final static String LOGIN_TIMES     = "login_times";
    public final static String USER_AVATAR_URL = "user_avatar_url";

    public final static String UPDATE_PHOTO_LIST = "update_photo_list";
    public final static String PUBLISH_PHOTO     = "publish_photo";

    public final static String COMMUNITY_CACHE = "community";
    public final static String CACHE_LATEST = "cacheLatest";
    public final static String CACHE_TOP    = "cacheTop";
    public final static String CACHE_PLAZA  = "cacheUser";

    // Default download root directory.
    public final static String ANDROID_DATA_DIR = "Android/data";
    public final static String IMAGE_ROOT_DIR   = "DCIM";

    // Default download image directory.
    public final static String UPLOAD_IMAGE_DIR = "Flock";//"\u56FE\u5427";//图吧
    public final static String DOWNLOAD_IMAGE_DIR = "Flock_Download";//"\u56FE\u5427\u4E0B\u8F7D";//图吧下载

    // Default download file directory.
    public final static String DOWNLOAD_FILE_DIR = "files";

    // Cache directory
    public final static String CACHE_DIR = "cache";

    // DB directory
    public final static String DB_DIR = "db";

    public final static int SENSITIVE_DURATION = 30; // 30 day

    // Max cache size 10M.
    public final static int MAX_CACHE_SIZE_INBYTES = 10 * 1024 * 1024;

    // EditText max length
    public final static int MAX_EMS = 50;

    public final static String CONNECT_EXCEPTION         = "无法连接到网络";
    public final static String UNKNOWN_HOST_EXCEPTION    = "连接远程地址失败";
    public final static String SOCKET_EXCEPTION          = "网络连接出错，请重试";
    public final static String SOCKET_TIMEOUT_EXCEPTION  = "连接超时，请重试";
    public final static String NULL_POINTER_EXCEPTION    = "抱歉，远程服务出错了";
    public final static String NULL_MESSAGE_EXCEPTION    = "抱歉，程序出错了";
    public final static String CLIENT_PROTOCOL_EXCEPTION = "Http请求参数错误";
    public final static String MISSING_PARAMETERS        = "参数没有包含足够的值";
    public final static String REMOTE_SERVICE_EXCEPTION  = "抱歉，远程服务出错了";
    public final static String NOT_FOUND_EXCEPTION       = "页面未找到";
    public final static String UNTREATED_EXCEPTION       = "未处理的异常";

    public static final String BASE_URL                 = "http://lapi.tt286.com:7892";
    public static final String ACCOUNT_GET_USER_INFO    = BASE_URL + "/lapi/userinfo";
    public static final String AVATAR_USER_SPECIFY      = "avatarurl";
    public static final String AVATAR_URL_QQ_WB_DEFAULT = "avatar";

    public static final String JSON_OPENID = "openid";
    public static final String JSON_TOKEN  = "token";
    public static final String TIME_OUT    = "time_out";
    public static final String USER_ABORT  = "abort";

    public final static int MSG_CODE_ACCOUNT      = 100001;
    public final static int MSG_CODE_PUSH         = 100002;
    public final static int MSG_CODE_PHOTO_LIST   = 100051;
    public final static int MSG_CODE_PHOTO_UPLOAD = 100052;
    public final static int MSG_CODE_PHOTO_DETAIL = 100053;
    public final static int MSG_CODE_PHOTO_USER   = 100054;
    public final static int MSG_CODE_PHOTO_DELETE = 100055;
    public final static int MSG_CODE_THUMBS       = 100060;
    public final static int MSG_CODE_COMMENT      = 100065;
    public final static int MSG_CODE_SENSITIVE    = 100066;
    public final static int MSG_CODE_REPORT       = 100070;

    public final static int CONNECT_TIMEOUT = 10000;
    public final static int READ_TIMEOUT    = 20000;


    public final static int CONNECT_RESULT_EXCEPTION = 3001;
    public final static int CONNECT_RESULT_NULL      = 3002;
    public final static int CONNECT_RESULT_TIMEOUT   = 3003;

    //public final static String REQUEST_URL       = "http://snsdc.yy845.com:7003"; // Official server
//    public final static String REQUEST_URL       = "http://192.168.0.52:6000";  // Test server
    public final static String ENCODE_DECODE_KEY = "l_cl_q87";

    public final static String LAN_CHINESE = "zh_CN";

    public final static String JSON_TAG  = "tag";          // 公共参数tag
    public final static String CHANNEL   = "channel";       // 渠道，长度:[2,32]
    public final static String CUSTOMER  = "customer";     // 客户
    public final static String MODEL     = "model";           // 机型
    public final static String PROJECT   = "project";       // 项目
    public final static String VERSION   = "version";       // 版本
    public final static String FOVERSION = "foVersion";   // Freeme OS 版本

    public final static String JSON_COMMON = "common";    // 公共参数common
    public final static String OPENID      = "openId";         // 用户唯一标识，长度:[24,32]
    public final static String LANGUAGE    = "language";     // 国家语言缩写，长度:[2,10]
    public final static String TIMES       = "times";           // 账号同步标识

    public final static String FROM     = "from";             // 分页，从多少开始
    public final static String TO       = "to";                 // 多找结束
    public final static String PAGESIZE = "to";           // 每页多少张
    public final static String SORT     = "sort";             // 排序：1.热度（点赞数+吐槽数） 2.最新（创建时间）

    public final static int PAGE_SIZE      = 20;
    public final static int PAGE_SIZE_USER = 30;
    public final static int SORT_BY_TOP    = 1;
    public final static int SORT_BY_LATEST = 2;

    public final static String FROM_OWNER         = "owner";
    public final static String FROM_OWNER_MESSAGE = "owner_msg";
    public final static String MESSAGE            = "message";
    public final static String PUSHMESSAGE        = "pushmessage";
    public final static String PHOTO_ID           = "photoId";
    public final static String BIG_URL            = "bigUrl";
    public final static String SMALL_URL          = "smallUrl";
    public final static String REQUEST_EDIT       = "RequestEdit";

    public final static int ERROR_500 = 500;
    public final static int ERROR_700 = 700;
    public final static int ERROR_800 = 800;            //登录已失效，请重新登录

    public final static int THUMBS_ADD    = 1;
    public final static int THUMBS_CANCEL = 2;
}