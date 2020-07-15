package com.example.mymqtt.mode;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 定时执行任务
 */
public class Timing   {
    public static boolean ismysql = true;
    public static   void timer() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,7); // 控制时
        calendar.set(Calendar.MINUTE, 33);       // 控制分
        calendar.set(Calendar.SECOND, 0);       // 控制秒
        Date time = calendar.getTime();         // 得出执行任务的时间,此处为今天的12：00：00
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {

                HttpServletRequest httpServletRequest = new HttpServletRequest(HttpServletRequest.GET_DATA);
                httpServletRequest.RequestPost();

            }
        }, time, 1000*5 );// 这里设定将延时每天固定执行
    }

}
