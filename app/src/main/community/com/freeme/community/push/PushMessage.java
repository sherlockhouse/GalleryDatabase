package com.freeme.community.push;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * ClassName: PushMessage
 * Description:
 * Author: connorlin
 * Date: Created on 2016-3-7.
 */
public class PushMessage implements Serializable {


    /**
     * content :
     * id : 1194
     * username : Connor
     * bigurl : http://192.168.0.52:5353/images/photo/2016/03/08/bbb6a1ad-f703-4153-8a55-a44504326829.jpg
     * dateTime : 2016-03-09 09:58:32
     * avatarUrl : http://q.qlogo.cn/qqapp/101259746/2B60E90BD26917F8C0DCF0AB475E3F10/100
     * smallUrl : http://192.168.0.52:5353/images/photo/2016/03/08/38f7b971-7881-47dc-bb4e-454ab59cb12c.jpg
     * type : 1
     */

    private String content;
    private int    id;
    private String nickname;
    private String bigurl;
    private String dateTime;
    private String avatarUrl;
    private String smallUrl;
    private int    type;

    private String title;
    private String summary;
    private String openId;

    public static boolean contains(ArrayList<PushMessage> msglist, PushMessage msg) {
        if (msg == null) return false;

        for (PushMessage message : msglist) {
            if (equals(message, msg)) {
                return true;
            }
        }
        return false;
    }

    public static boolean equals(PushMessage msg1, PushMessage msg2) {
        if (msg1 == null || msg2 == null) return false;

        return (msg1.getId() == msg2.getId()
                && msg1.getType() == msg2.getType()
                && msg1.getContent().equals(msg2.getContent())
                && msg1.getNickname().equals(msg2.getNickname())
                && msg1.getSmallUrl().equals(msg2.getSmallUrl()));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSmallUrl() {
        return smallUrl;
    }

    public void setSmallUrl(String smallUrl) {
        this.smallUrl = smallUrl;
    }

    public String getBigurl() {
        return bigurl;
    }

    public void setBigurl(String bigurl) {
        this.bigurl = bigurl;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String string) {
        this.title = string;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String string) {
        this.summary = string;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }
}
