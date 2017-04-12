package com.freeme.bigmodel.filter;

import java.util.List;

public class Origin {
    /*
     * add by tyd heqianqin
     * */
    private String from;

    private String to;

    private List<Trans_result> trans_result;

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return this.to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<Trans_result> getTrans_result() {
        return this.trans_result;
    }

    public void setTrans_result(List<Trans_result> trans_result) {
        this.trans_result = trans_result;
    }

}
