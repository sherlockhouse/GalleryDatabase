package com.freeme.community.entity;

import com.freeme.community.utils.Utils;

import java.util.ArrayList;

/**
 * PhotoData
 * Created by connorlin on 15-9-18.
 */
public class PhotoInfo {
    private String                 openId;
    private String                 nickname;
    private String                 intro;
    private String                 avatarUrl;
    private String                 createTime;
    private int                    thumbTotal;
    private ArrayList<ThumbsItem>  thumbLists;
    private int                    commentTotal;
    private ArrayList<CommentItem> commentList;

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getNickname() {
        return Utils.encryptMobileNO(nickname);
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getThumbTotal() {
        return thumbTotal;
    }

    public void setThumbTotal(int thumbTotal) {
        this.thumbTotal = thumbTotal;
    }

    public ArrayList<ThumbsItem> getThumbLists() {
        return thumbLists;
    }

    public void setThumbLists(ArrayList<ThumbsItem> thumbLists) {
        this.thumbLists = thumbLists;
    }

    public int getCommentTotal() {
        return commentTotal;
    }

    public void setCommentTotal(int commentTotal) {
        this.commentTotal = commentTotal;
    }

    public ArrayList<CommentItem> getCommentList() {
        return commentList;
    }

    public void setCommentList(ArrayList<CommentItem> commentList) {
        this.commentList = commentList;
    }
}
