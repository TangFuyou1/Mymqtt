package com.example.mymqtt.mode;

import android.os.Handler;
import android.os.Message;


import com.example.mymqtt.bean.Data;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 文件名: HttpServletRequestPost
 * 创建者: 友
 * 创建日期: 2020/6/21 11:01
 * 邮箱: 1738743304.com !
 * 描述: Post请求
 */
public class HttpServletRequest {

    public static String LED_ON  = "led_on";  //开灯
    public static String LED_OFF  = "led_off";//关灯
    public static String GET_DATA = "set_data";//获取服务器数据
    public static String CONNECTION_SERVER = "get_data";//获取服务器数据
    private  static String Request; //连接命令

    public HttpServletRequest(String Request) {
        this.Request = Request;
    }

    public  Data  RequestPost() {
      //  System.out.println("Chunna==开始连接");
        Data data = null;
        String path="http://134.175.221.21:8080/esp8266_war/"+Request;
        URL url = null;
        try{
            url=new URL(path); //获取连接路径
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection(); //连接服务器
            InputStreamReader in = new InputStreamReader(urlConnection.getInputStream(),"UTF-8");//获取输入流
            BufferedReader br = new BufferedReader(in);

            int i = 0;
            Gson gson = new Gson();
         ///   System.out.println("br============:"+br);
             data = gson.fromJson(br,Data.class);    //json解析

            in.close(); //关闭输入流
            urlConnection.disconnect(); //断开连接


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data; //返回Data对象
    }

}
