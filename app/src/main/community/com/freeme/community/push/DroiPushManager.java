package com.freeme.community.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.widget.RemoteViews;

import com.freeme.community.activity.ImageDetailActivity;
import com.freeme.community.entity.PhotoData;
import com.freeme.community.entity.PhotoItem;
import com.freeme.community.manager.JSONManager;
import com.freeme.community.net.RemoteRequest;
import com.freeme.community.net.RequestCallback;
import com.freeme.community.utils.AccountUtil;
import com.freeme.community.utils.AppConfig;
import com.freeme.community.utils.DateUtil;
import com.freeme.community.utils.FileUtil;
import com.freeme.community.utils.LogUtil;
import com.freeme.community.utils.Utils;
import com.freeme.gallery.R;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: DroiPushManager
 * Description: for droi push
 * Author: connorlin
 * Date: Created on 2016-3-7.
 */
public class DroiPushManager {

    public static final int TYPE_THUMB = 1;  //点赞
    public static final int MSG_TYPE_ADD = 101;//添加消息
    public static final int MSG_TYPE_LOGIN = 102;//登录刷新
    private static final String MESSAGES_PUSH = "messagepush";
    private static final String MESSAGES_NEW = "messagenew";
    private static final String MESSAGES_THUMB = "messagethumb";
    private static final String KEY_DEFAULT_DROI_PUSH = "default_droi_push";

    private static final int TYPE_MESSAGES_PUSH = 100;
    private static final int TYPE_MESSAGES_NEW = 101;
    private static final int TYPE_MESSAGES_THUMB = 102;
    private static final int NOTIFICATION_ID = 1000;
    //    private static final int TYPE_COMMENT    = 2;  //评论
    private static Context mContext;
    private NotificationManager mNotificationManager;
    private Handler mHandler = new MessageHandler();
    private ArrayList<PushMessage> mPushMessagesList = new ArrayList<>();
    private ArrayList<PushMessage> mNewMessageIdList = new ArrayList<>();
    private ArrayList<Integer> mNewMessageThumbnail = new ArrayList<>();

    private PushMessage mPushMessage;
    private PushMessage mTempMessage;
    private Thread mThread;

    private boolean mReceiveDroiPush = AppConfig.DEFAULT_DROI_PUSH;

    // ===========================================================================
    // for Messages List start
    // ===========================================================================
    private List<PushMessageCallback> mPushMessageCallbackList = new ArrayList<>();

    public DroiPushManager() {
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        setObserver();
    }

    private void setObserver() {
        ContentObserver toggleDroiPushObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                mReceiveDroiPush = Settings.System.getInt(mContext.getContentResolver(), Utils.KEY_TOGGLE_DROI_PUSH, 0) == 1;
            }
        };

        mContext.getContentResolver().unregisterContentObserver(toggleDroiPushObserver);
        mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Utils.KEY_TOGGLE_DROI_PUSH),
                true, toggleDroiPushObserver);

        if(mReceiveDroiPush && Settings.System.getInt(mContext.getContentResolver(), KEY_DEFAULT_DROI_PUSH, 0) == 0) {
            Settings.System.putInt(mContext.getContentResolver(), Utils.KEY_TOGGLE_DROI_PUSH, mReceiveDroiPush ? 1 : 0);
        }
        Settings.System.putInt(mContext.getContentResolver(), KEY_DEFAULT_DROI_PUSH, 1);
    }

    public static DroiPushManager getInstance(Context context) {
        mContext = context;
        return Singleton.instance;
    }

    public void init() {
        mThread = new Thread(new dataTask());
        mThread.start();
//        LogUtil.i("deviceid = " + LeoPush.getInstance(mContext).getUDID());
//        LeoPush.getInstance(mContext).setMessageHandler(new LeoPushMessageHandler() {
//            @Override
//            public void onCustomMessage(final Context context, final PushMsg msg) {
//                if(!mReceiveDroiPush) return;
//
//                mPushMessage = getPushMessage(msg.content);
//
//                String nickName = AccountUtil.getInstance(mContext).getNickName();
//                String openId = AccountUtil.getInstance(mContext).getOpenId();
//                if(nickName == null || openId == null){
//                    return;
//                }
//
//                // return if self or not current account
//                LogUtil.i("account nickname = " + AccountUtil.getInstance(mContext).getNickName());
//                LogUtil.i("message nickname = " + mPushMessage.getNickname());
//                if (mPushMessage == null
//                        || AccountUtil.getInstance(mContext).getNickName().equals(mPushMessage.getNickname())
//                        || !AccountUtil.getInstance(mContext).getOpenId().equals(mPushMessage.getOpenId())) {
//                    return;
//                }
//
//                // for ignore thumb repeat
//                if (!mNewMessageThumbnail.contains(mPushMessage.getId())
//                        || mPushMessage.getType() != TYPE_THUMB) {
//                    boolean equal = PushMessage.equals(mPushMessage, mTempMessage);
//                    if (!equal) {
//                        mPushMessage.setNickname(Utils.encryptMobileNO(mPushMessage.getNickname()));
//                        mTempMessage = mPushMessage;
//                        makeNotification(mPushMessage);
//                        mHandler.sendEmptyMessage(MSG_TYPE_ADD);
//                    }
//                }
//            }
//        });

//        if (AccountUtil.getInstance(mContext).checkAccount()) {
//            bindPushService();
//        }
    }

    private PushMessage getPushMessage(String content) {
        PushMessage pushMessage = new PushMessage();
        LogUtil.i("content = " + content);
        try {
            Gson gson = new Gson();
            pushMessage = gson.fromJson(content, PushMessage.class);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.i("pushMessage ex " + e);
        }

        return pushMessage;
    }

    private void makeNotification(PushMessage msg) {
        if (msg != null) {
            LogUtil.i("makeNotification ---------");

            StringBuilder stringBuilder = new StringBuilder();
            Resources res = mContext.getResources();
            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.droi_push_notification);

            // Title
            stringBuilder.append(msg.getNickname());
            stringBuilder.append("  ");
            stringBuilder.append(DateUtil.reFormatDate(msg.getDateTime(), DateUtil.dateFormatHM));
            msg.setTitle(stringBuilder.toString());
            //views.setTextViewText(R.id.title, msg.getTitle());

            // Summary
            String summary = msg.getType() == TYPE_THUMB ?
                    res.getString(R.string.thumb_for_you) : res.getString(R.string.comment_for_you);
            stringBuilder.setLength(0);
            stringBuilder.append(summary);
            stringBuilder.append(msg.getContent());
            summary = stringBuilder.toString();
            msg.setSummary(stringBuilder.toString());

            stringBuilder.setLength(0);
            stringBuilder.append(msg.getNickname());
            stringBuilder.append(" ");
            stringBuilder.append(summary);
            views.setTextViewText(R.id.summary, stringBuilder.toString());

            // DateTime
            views.setTextViewText(R.id.datetime,
                    DateUtil.reFormatDate(msg.getDateTime(), DateUtil.dateFormatHM));

            // Ticker
            Bundle bundle = new Bundle();
            bundle.putSerializable(AppConfig.PUSHMESSAGE, msg);
            Intent intent = new Intent(mContext, ImageDetailActivity.class);
            intent.putExtra(AppConfig.MESSAGE, bundle);
            intent.putExtra(AppConfig.FROM_OWNER, true);
            intent.putExtra(AppConfig.FROM_OWNER_MESSAGE, true);
            intent.putExtra(AppConfig.PHOTO_ID, msg.getId());
            intent.putExtra(AppConfig.BIG_URL, msg.getBigurl());
            intent.putExtra(AppConfig.SMALL_URL, msg.getSmallUrl());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Notification notification = new Notification.Builder(mContext)
                    .setTicker(stringBuilder.toString())
                    .setContentIntent(PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT))
                    .setSmallIcon(R.mipmap.ic_launcher_gallery)
                    .setWhen(System.currentTimeMillis())
                    .build();
            notification.contentView = views;
            notification.flags &= ~Notification.FLAG_NO_CLEAR;
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            mNotificationManager.cancel(NOTIFICATION_ID);
            mNotificationManager.notify(NOTIFICATION_ID, notification);
            LogUtil.i("makeNotification ---------end");
        }
    }

    public void bindPushService() {
//        RequestCallback callback = new RequestCallback(AppConfig.MSG_CODE_PUSH) {
//            @Override
//            public void onSuccess(PhotoData data) {
//                if (!dealError(data)) {
//                    LogUtil.i("bind push service success!");
//                }
//            }
//
//            @Override
//            public void onFailure(int type) {
////                Utils.dealResult(mContext, type);
//            }
//        };
//        JSONObject object = JSONManager.getInstance().getPushInfo(mContext);
//        RemoteRequest.getInstance().invoke(object, callback);
    }

    private boolean dealError(PhotoData data) {
        if (data == null) {
            return true;
        }

        boolean err = false;

        switch (data.getErrorCode()) {
            case AppConfig.ERROR_500:
            case AppConfig.ERROR_700:
                err = true;
                LogUtil.i(data.getmErrorMsg());
                break;
        }

        return err;
    }

    public void addMessageCallback(PushMessageCallback callback) {
        if (callback != null) {
            mPushMessageCallbackList.add(callback);
        }
    }

    public void removeMessageCallback(PushMessageCallback callback) {
        if (callback != null) {
            mPushMessageCallbackList.remove(callback);
        }
    }

    public ArrayList<PushMessage> getPushMessageList() {
        return mPushMessagesList;
    }

    public int getPushMessageListSize() {
        return mPushMessagesList.size();
    }

    private void addItem(PushMessage msg) {
        mPushMessagesList.add(0, msg);
        mNewMessageIdList.add(msg);
        if (msg.getType() == TYPE_THUMB) {
            mNewMessageThumbnail.add(msg.getId());
        }

        updateCallback();

        updateCache();
    }

    private void updateCallback() {
        for (PushMessageCallback callback : mPushMessageCallbackList) {
            callback.onUpdate();
        }
    }

    private void updateCache() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setCache();
            }
        });
    }

    private void setCache() {
        for (int type = TYPE_MESSAGES_PUSH; type <= TYPE_MESSAGES_THUMB; type++) {
            setCache(type);
        }
    }

//    public void removeItem(ArrayList<Integer> indexList) {
//        ArrayList<PushMessage> list = new ArrayList<>();
//        for (int index : indexList) {
//            list.add(mPushMessagesList.get(index));
//        }
//
//        for (PushMessage msg : mPushMessagesList) {
//            mPushMessagesList.remove(msg);
//        }
//
//        updateCallback();
//
//        updateCache(TYPE_MESSAGES_PUSH);
//    }

    private void setCache(int type) {
        switch (type) {
            case TYPE_MESSAGES_PUSH:
                if (mPushMessagesList != null) {
                    FileUtil.writeObjectToFile(mContext, mPushMessagesList,
                            AccountUtil.getInstance(mContext).getOpenId(), MESSAGES_PUSH);
                }
                break;

            case TYPE_MESSAGES_NEW:
                if (mNewMessageIdList != null) {
                    FileUtil.writeObjectToFile(mContext, mNewMessageIdList,
                            AccountUtil.getInstance(mContext).getOpenId(), MESSAGES_NEW);
                }
                break;

            case TYPE_MESSAGES_THUMB:
                if (mNewMessageThumbnail != null) {
                    FileUtil.writeObjectToFile(mContext, mNewMessageThumbnail,
                            AccountUtil.getInstance(mContext).getOpenId(), MESSAGES_THUMB);
                }
                break;
        }
    }
    // ===========================================================================
    // for Messages List end
    // ===========================================================================

    public void removeItem(int position) {
        LogUtil.i("mPushMessagesList size = " + mPushMessagesList.size());
        mNewMessageIdList.remove(mPushMessagesList.get(position));
        mPushMessagesList.remove(position);
        updateCallback();

        updateCache(TYPE_MESSAGES_PUSH);
    }

    private void updateCache(final int type) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setCache(type);
            }
        });
    }

    public void clearItem() {
        clear();

        clearCache();
    }

    private void clear() {
        mPushMessagesList.clear();
        mNewMessageIdList.clear();
        mNewMessageThumbnail.clear();
        updateCallback();
    }

    private void clearCache() {
        FileUtil.removeObjectFile(mContext, AccountUtil.getInstance(mContext).getOpenId(), MESSAGES_PUSH);
        FileUtil.removeObjectFile(mContext, AccountUtil.getInstance(mContext).getOpenId(), MESSAGES_NEW);
        FileUtil.removeObjectFile(mContext, AccountUtil.getInstance(mContext).getOpenId(), MESSAGES_THUMB);
    }

    public ArrayList<PushMessage> getNewMessageIdList() {
        return mNewMessageIdList;
    }

    public void readedMessage(PushMessage msg) {
        for (PushMessage message : mNewMessageIdList) {
            if (PushMessage.equals(message, msg)) {
                mNewMessageIdList.remove(message);
                break;
            }
        }

        updateCallback();

        updateCache(TYPE_MESSAGES_NEW);
    }

    public void removeMessageThumbnail(ArrayList<PhotoItem> list) {
        Integer id;
        for (PhotoItem item : list) {
            id = item.getId();
            if (mNewMessageThumbnail.contains(id)) {
                mNewMessageThumbnail.remove(id);
            }
        }
        updateCache(TYPE_MESSAGES_THUMB);
    }

    private ArrayList<PushMessage> getCache(String type) {
        ArrayList<PushMessage> list = null;
        Object object = FileUtil.readObjectFromFile(mContext, AccountUtil.getInstance(mContext).getOpenId(), type);
        LogUtil.i("object = " + object);
        if (object instanceof ArrayList) {
            list = (ArrayList) object;
        }
        LogUtil.i("list = " + list);

        return list;
    }

    private ArrayList<Integer> getIntegerCache(String type) {
        ArrayList<Integer> list = null;
        Object object = FileUtil.readObjectFromFile(mContext, AccountUtil.getInstance(mContext).getOpenId(), type);
        LogUtil.i("object = " + object);
        if (object instanceof ArrayList) {
            list = (ArrayList) object;
        }
        LogUtil.i("list = " + list);

        return list;
    }

    private void interruptThread() {
        if (mThread != null && mThread.isAlive()) {
            mThread.interrupt();
            mThread = null;
        }
    }

    public void login() {
        mThread = new Thread(new dataTask());
        mThread.start();
        updateCallback();
    }

    public void logout() {
        clear();
    }

    private static class Singleton {
        private static DroiPushManager instance = new DroiPushManager();
    }

    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_TYPE_ADD:
                    addItem(mPushMessage);
                    break;

                case MSG_TYPE_LOGIN:
                    updateCallback();
                    break;
            }
        }
    }

    // ==============================================================================
    // for store data
    // ==============================================================================
    class dataTask implements Runnable {
        @Override
        public void run() {
            ArrayList<PushMessage> list = getCache(MESSAGES_PUSH);
            if (list != null && list.size() != 0) {
                mPushMessagesList = list;
            }

            list = getCache(MESSAGES_NEW);
            if (list != null && list.size() != 0) {
                mNewMessageIdList = list;
            }

            ArrayList<Integer> thumb = getIntegerCache(MESSAGES_THUMB);
            if (thumb != null && thumb.size() != 0) {
                mNewMessageThumbnail = thumb;
            }

            if (mHandler != null) {
                mHandler.sendEmptyMessage(MSG_TYPE_LOGIN);
            }

            interruptThread();
        }
    }
}
