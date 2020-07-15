package com.example.mymqtt.bean;

import java.util.List;

/**
 * 文件名: Data
 * 创建者: 友
 * 创建日期: 2020/7/8 10:43
 * 邮箱: 1738743304.com !
 * 描述: TODO
 */
public class Data {
    private String temp;
    private String humi;
    private String flame;
    private List<Data> dataList;

    public String getFlame() {
        return flame;
    }

    public void setFlame(String flame) {
        this.flame = flame;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getHumi() {
        return humi;
    }

    public void setHumi(String humi) {
        this.humi = humi;
    }

    public List<Data> getDataList() {
        return dataList;
    }

    public void setDataList(List<Data> dataList) {
        this.dataList = dataList;
    }

    public Data() {
    }
    public Data(String temp, String humi,String flame) {
        this.temp = temp;
        this.humi = humi;
        this.flame = flame;
    }
}
