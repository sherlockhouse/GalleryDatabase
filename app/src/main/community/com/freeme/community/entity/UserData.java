package com.freeme.community.entity;

/**
 * PhotoData
 * Created by connorlin on 15-9-18.
 */
public class UserData {

    /**
     * nickname : Freeme OS
     * openid : 54042265c2309958230002ef
     * exchange : 1
     * lottery_count : 3
     * shared : true
     * openweibo : 2151965330
     * lotteryed_count : 0
     * gotlottery : true
     * apk_install_count : 2
     * gender : 男
     * avatarurl2 : /ui/avatars/108141bb2ffc329ea322510b717b3788.png
     * gotlotteryday : 2015-08-31
     * checkindaily_max : 22
     * cancheckin : true
     * avatar : http://tp3.sinaimg.cn/2151965330/180/5625402770/1
     * weiboavatar : http://tp3.sinaimg.cn/2151965330/180/5625402770/1
     * checkintotal : 71
     * ascore : 0
     * spend : -202860
     * score : 4450
     * level : 0
     * avatarurl : http://lapi.tt286.com:8082/ui/avatars/108141bb2ffc329ea322510b717b3788.png
     * passwdval : 0
     * result : 0
     * charge : 32
     * lastlotteryday : 2015-08-31
     * createtime : 1409557093350
     * apk_download_count : 2
     * checkin5days : 1
     * zmall_purchase : 1
     * checkindaily : 1
     * contact : {"address":"Freeme OS 林观荣","tel":"18501653946","cname":"林观荣"}
     * username : 18501653946
     * firstlotteryday : 2014-09-01
     * birthday : 未选择
     */

    private String        nickname;
    private String        openid;
    private int           exchange;
    private int           lottery_count;
    private boolean       shared;
    private String        openweibo;
    private int           lotteryed_count;
    private boolean       gotlottery;
    private int           apk_install_count;
    private String        gender;
    private String        avatarurl2;
    private String        gotlotteryday;
    private int           checkindaily_max;
    private boolean       cancheckin;
    private String        avatar;
    private String        weiboavatar;
    private int           checkintotal;
    private int           ascore;
    private int           spend;
    private int           score;
    private int           level;
    private String        avatarurl;
    private int           passwdval;
    private int           result;
    private long          charge;
    private String        lastlotteryday;
    private long          createtime;
    private int           apk_download_count;
    private int           checkin5days;
    private int           zmall_purchase;
    private int           checkindaily;
    private ContactEntity contact;
    private String        username;
    private String        firstlotteryday;
    private String        birthday;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public int getExchange() {
        return exchange;
    }

    public void setExchange(int exchange) {
        this.exchange = exchange;
    }

    public int getLottery_count() {
        return lottery_count;
    }

    public void setLottery_count(int lottery_count) {
        this.lottery_count = lottery_count;
    }

    public boolean getShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public String getOpenweibo() {
        return openweibo;
    }

    public void setOpenweibo(String openweibo) {
        this.openweibo = openweibo;
    }

    public int getLotteryed_count() {
        return lotteryed_count;
    }

    public void setLotteryed_count(int lotteryed_count) {
        this.lotteryed_count = lotteryed_count;
    }

    public boolean getGotlottery() {
        return gotlottery;
    }

    public void setGotlottery(boolean gotlottery) {
        this.gotlottery = gotlottery;
    }

    public int getApk_install_count() {
        return apk_install_count;
    }

    public void setApk_install_count(int apk_install_count) {
        this.apk_install_count = apk_install_count;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAvatarurl2() {
        return avatarurl2;
    }

    public void setAvatarurl2(String avatarurl2) {
        this.avatarurl2 = avatarurl2;
    }

    public String getGotlotteryday() {
        return gotlotteryday;
    }

    public void setGotlotteryday(String gotlotteryday) {
        this.gotlotteryday = gotlotteryday;
    }

    public int getCheckindaily_max() {
        return checkindaily_max;
    }

    public void setCheckindaily_max(int checkindaily_max) {
        this.checkindaily_max = checkindaily_max;
    }

    public boolean getCancheckin() {
        return cancheckin;
    }

    public void setCancheckin(boolean cancheckin) {
        this.cancheckin = cancheckin;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getWeiboavatar() {
        return weiboavatar;
    }

    public void setWeiboavatar(String weiboavatar) {
        this.weiboavatar = weiboavatar;
    }

    public int getCheckintotal() {
        return checkintotal;
    }

    public void setCheckintotal(int checkintotal) {
        this.checkintotal = checkintotal;
    }

    public int getAscore() {
        return ascore;
    }

    public void setAscore(int ascore) {
        this.ascore = ascore;
    }

    public int getSpend() {
        return spend;
    }

    public void setSpend(int spend) {
        this.spend = spend;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getAvatarurl() {
        return avatarurl;
    }

    public void setAvatarurl(String avatarurl) {
        this.avatarurl = avatarurl;
    }

    public int getPasswdval() {
        return passwdval;
    }

    public void setPasswdval(int passwdval) {
        this.passwdval = passwdval;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public long getCharge() {
        return charge;
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    public String getLastlotteryday() {
        return lastlotteryday;
    }

    public void setLastlotteryday(String lastlotteryday) {
        this.lastlotteryday = lastlotteryday;
    }

    public long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(long createtime) {
        this.createtime = createtime;
    }

    public int getApk_download_count() {
        return apk_download_count;
    }

    public void setApk_download_count(int apk_download_count) {
        this.apk_download_count = apk_download_count;
    }

    public int getCheckin5days() {
        return checkin5days;
    }

    public void setCheckin5days(int checkin5days) {
        this.checkin5days = checkin5days;
    }

    public int getZmall_purchase() {
        return zmall_purchase;
    }

    public void setZmall_purchase(int zmall_purchase) {
        this.zmall_purchase = zmall_purchase;
    }

    public int getCheckindaily() {
        return checkindaily;
    }

    public void setCheckindaily(int checkindaily) {
        this.checkindaily = checkindaily;
    }

    public ContactEntity getContact() {
        return contact;
    }

    public void setContact(ContactEntity contact) {
        this.contact = contact;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstlotteryday() {
        return firstlotteryday;
    }

    public void setFirstlotteryday(String firstlotteryday) {
        this.firstlotteryday = firstlotteryday;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "UserData{" +
                "nickname='" + nickname + '\'' +
                ", openid='" + openid + '\'' +
                ", exchange=" + exchange +
                ", lottery_count=" + lottery_count +
                ", shared=" + shared +
                ", openweibo='" + openweibo + '\'' +
                ", lotteryed_count=" + lotteryed_count +
                ", gotlottery=" + gotlottery +
                ", apk_install_count=" + apk_install_count +
                ", gender='" + gender + '\'' +
                ", avatarurl2='" + avatarurl2 + '\'' +
                ", gotlotteryday='" + gotlotteryday + '\'' +
                ", checkindaily_max=" + checkindaily_max +
                ", cancheckin=" + cancheckin +
                ", avatar='" + avatar + '\'' +
                ", weiboavatar='" + weiboavatar + '\'' +
                ", checkintotal=" + checkintotal +
                ", ascore=" + ascore +
                ", spend=" + spend +
                ", score=" + score +
                ", level=" + level +
                ", avatarurl='" + avatarurl + '\'' +
                ", passwdval=" + passwdval +
                ", result=" + result +
                ", charge=" + charge +
                ", lastlotteryday='" + lastlotteryday + '\'' +
                ", createtime=" + createtime +
                ", apk_download_count=" + apk_download_count +
                ", checkin5days=" + checkin5days +
                ", zmall_purchase=" + zmall_purchase +
                ", checkindaily=" + checkindaily +
                ", contact=" + contact +
                ", username='" + username + '\'' +
                ", firstlotteryday='" + firstlotteryday + '\'' +
                ", birthday='" + birthday + '\'' +
                '}';
    }

    public static class ContactEntity {
        private String address;
        private String tel;
        private String cname;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getTel() {
            return tel;
        }

        public void setTel(String tel) {
            this.tel = tel;
        }

        public String getCname() {
            return cname;
        }

        public void setCname(String cname) {
            this.cname = cname;
        }
    }
}
