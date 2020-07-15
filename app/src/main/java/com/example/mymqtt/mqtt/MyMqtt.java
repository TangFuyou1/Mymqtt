package com.example.mymqtt.mqtt;

import android.os.Handler;

import com.example.mymqtt.util.SystemUtil;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Random;

/**
 * 文件名: MyMqtt
 * 创建者: 友
 * 创建日期: 2020/7/8 10:40
 * 邮箱: 1738743304.com !
 * 描述: TODO
 */
public class MyMqtt {
    private String pubTopic = "chuan";//主题

    private int qos = 0; //质量等级

    private static  String broker = "tcp://134.175.221.21:1883"; //服务器地址


    private MemoryPersistence persistence;

    private static  MqttClient client; //Mqtt操作实体类

    private static  MqttConnectOptions connOpts; // MQTT 连接类

    private MqttMessage message;  //发布消息类

    public MyMqtt() {
    }

    public MyMqtt(Handler handler) throws MqttException {
        try {
           String  clientId = SystemUtil.getSystemModel()+"_"+createRandomCharData(4); //获取一个由设备型号开头的随机id（因为id不能重复）
            persistence = new MemoryPersistence();
            client = new MqttClient(broker, clientId, persistence); //参数一：连接路径  参数二： id 参数三：MemoryPersistence对象
            // MQTT 连接选项
            connOpts = new MqttConnectOptions();
            connOpts.setUserName(clientId);      //账号
            connOpts.setPassword("emqx_test_password".toCharArray());   //密码
            // 保留会话
            connOpts.setCleanSession(true);
            // 设置回调
            client.setCallback(new MymqttCallback(handler));
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    /**
     * 建立连接
     */
    public   void Connection() {

        System.out.println("Connecting to broker: " + broker);
        try {
            // 建立连接
            client.connect(connOpts);
            // 订阅
            client.subscribe("chuan");
            client.subscribe("huo");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 消息发送
     *
     * @param data 发送的内容
     */
    public void sedMessage(String data) {
        if (client == null) {
            return;
        }
        message = new MqttMessage(data.getBytes());
        message.setQos(qos);  //质量等级 0
        // 消息发布所需参数
        try {
            client.publish(pubTopic, message);  //发布 参数一： 主题  参数二：MqttMessage对象
        } catch (MqttException e) {
            e.printStackTrace();
        }
     //   System.out.println("Message published");

        //   client.disconnect();
     //   System.out.println("Disconnected");
        //   client.close();

        //  System.exit(0);
    }

    //根据指定长度生成字母和数字的随机数
    //0~9的ASCII为48~57
    //A~Z的ASCII为65~90
    //a~z的ASCII为97~122
    public String createRandomCharData(int length) {
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();//随机用以下三个随机生成器
        Random randdata = new Random();
        int data = 0;
        for (int i = 0; i < length; i++) {
            int index = rand.nextInt(3);
            //目的是随机选择生成数字，大小写字母
            switch (index) {
                case 0:
                    data = randdata.nextInt(10);//仅仅会生成0~9
                    sb.append(data);
                    break;
                case 1:
                    data = randdata.nextInt(26) + 65;//保证只会产生65~90之间的整数
                    sb.append((char) data);
                    break;
                case 2:
                    data = randdata.nextInt(26) + 97;//保证只会产生97~122之间的整数
                    sb.append((char) data);
                    break;
            }
        }
        String result = sb.toString();
        return result;
    }
}
