package com.example.mymqtt.mqtt;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.mymqtt.bean.Data;
import com.example.mymqtt.mode.Mode1;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * 文件名: MymqttCallback
 * 创建者: 友
 * 创建日期: 2020/7/8 10:41
 * 邮箱: 1738743304.com !
 * 描述: TODO
 */
public class MymqttCallback implements MqttCallback {
    private Handler handler;
    private MyMqtt myMqtt;

    public MymqttCallback(Handler handler) {
        this.handler = handler;
    }

    public void connectionLost(Throwable cause) {
        Mode1.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                MyMqtt myMqtt = new MyMqtt();
                myMqtt.Connection(); //重连
            }
        });


        // 连接丢失后，一般在这里面进行重连
        //  System.out.println("连接断开，可以做重连");
    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // subscribe后得到的消息会执行到这里面
        //   System.out.println("接收消息主题:" + topic);
        System.out.println("接收消息Qos:" + message.getQos());
        //System.out.println("接收消息内容:" + new String(message.getPayload()));

        if (topic.equals("chuan")) {
            String data = new String(message.getPayload());
            set_data( data ,  handler,1);
        }
        if (topic.equals("huo"))
        {
            System.out.println("接收消息内容:" + new String(message.getPayload()));
            String data = new String(message.getPayload());
            set_data( data ,  handler,2);
        }


    }

    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("deliveryComplete---------" + token.isComplete());
    }
    private void set_data(String data , Handler handler,int what)
    {

        Bundle bundle = new Bundle();
        bundle.putSerializable("data", data);
        Message msg = Message.obtain();
        msg.setData(bundle);
        msg.what = what;
        handler.sendMessage(msg);
    }
}
