package com.example.mymqtt.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymqtt.AAChartCoreLib.AAChartCreator.AAChartModel;
import com.example.mymqtt.AAChartCoreLib.AAChartCreator.AAChartView;
import com.example.mymqtt.AAChartCoreLib.AAChartCreator.AAOptionsConstructor;
import com.example.mymqtt.AAChartCoreLib.AAChartCreator.AASeriesElement;
import com.example.mymqtt.AAChartCoreLib.AAChartEnum.AAChartLineDashStyleType;
import com.example.mymqtt.AAChartCoreLib.AAChartEnum.AAChartSymbolStyleType;
import com.example.mymqtt.AAChartCoreLib.AAChartEnum.AAChartSymbolType;
import com.example.mymqtt.AAChartCoreLib.AAChartEnum.AAChartType;
import com.example.mymqtt.AAChartCoreLib.AAOptionsModel.AACrosshair;
import com.example.mymqtt.AAChartCoreLib.AAOptionsModel.AADataLabels;
import com.example.mymqtt.AAChartCoreLib.AAOptionsModel.AAItemStyle;
import com.example.mymqtt.AAChartCoreLib.AAOptionsModel.AALabels;
import com.example.mymqtt.AAChartCoreLib.AAOptionsModel.AAOptions;
import com.example.mymqtt.AAChartCoreLib.AAOptionsModel.AAStyle;
import com.example.mymqtt.AAChartCoreLib.AAOptionsModel.AATitle;
import com.example.mymqtt.AAChartCoreLib.AAOptionsModel.AAYAxis;
import com.example.mymqtt.AAChartCoreLib.AATools.AAColor;
import com.example.mymqtt.AAChartCoreLib.AATools.AAGradientColor;
import com.example.mymqtt.AAChartCoreLib.AATools.AALinearGradientDirection;
import com.example.mymqtt.R;
import com.example.mymqtt.bean.Data;
import com.example.mymqtt.mode.HttpServletRequest;
import com.example.mymqtt.mode.Mode1;
import com.example.mymqtt.mode.Timing;
import com.example.mymqtt.mqtt.MyMqtt;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Object[] temp_data = new Object[10];
    private Object[] humi_data = new Object[10];
    private boolean isled = true;
    private AAChartView aaChartView;
    private SimpleDateFormat jsontime; // json 后面的时间
    private Date date; // json 后面的时间
    private int countjson = 1; //记录json的标号
    private ImageView iv_swchit;
    private ImageView iv_light;
    private ImageView iv_lianjie;
    private Button bt_getdata, bt_humi, choice_temperature; //获取按键
    private TextView tv_temp, tv_humi, tv_jsondata;  //温度和湿度
    private MyMqtt myMqtt; //MyMqtt对象
    private StringBuilder stringBuilder;  //存取json
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {

            switch (msg.what) {
                case 1://温湿度显示
                    Bundle bundle = msg.getData();  //获取由MymqttCallback回调函数发过来的数据包对象
                    String data1 = (String) bundle.getSerializable("data"); //获取json数据
                    date = new Date();
                    jsontime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String dateNowStr = jsontime.format(date); //当前时间
                    tv_jsondata.setText(stringBuilder.append((countjson++) + " :" + data1 + "  " + dateNowStr + "\r\n"));
                    Data data = new Gson().fromJson(data1, Data.class); //解析json对象
                    tv_temp.setText(data.getTemp()); //Andorid界面显示温度
                    tv_humi.setText(data.getHumi());//Andorid界面显示湿度
                    break;
                case 2://火焰监测
                    Bundle bundle2 = msg.getData();  //获取由MymqttCallback回调函数发过来的数据包对象
                    String data2 = (String) bundle2.getSerializable("data"); //获取json数据
                    Data data3 = new Gson().fromJson(data2, Data.class); //解析json对象
                    if ( data3.getFlame().equals("1"))
                    {
                        iv_swchit.setImageResource(R.drawable.huoyan_ok);
                    }
                    else
                    {
                        iv_swchit.setImageResource(R.drawable.huoyan_no);
                    }
                    break;
            }


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        //    bt_getdata = findViewById(R.id.bt_gerdata);
        stringBuilder = new StringBuilder(); //

        findInit();//



        getdata(); //实时显示温湿度连接emqx

    }

    /**
     * 控件初始化
     */
    private void findInit() {
        tv_temp = findViewById(R.id.tv_temp);//温度
        tv_humi = findViewById(R.id.tv_humi);//湿度
        bt_humi = findViewById(R.id.bt_humi);//湿度按钮
        choice_temperature = findViewById(R.id.choice_temperature);//温度按钮


        iv_swchit = findViewById(R.id.iv_swchit);//火焰
        aaChartView = findViewById(R.id.AAChartView); //图表
        tv_jsondata = findViewById(R.id.tv_jsondata);//没解析的json数据


        iv_light = findViewById(R.id.iv_light); //开灯
        iv_lianjie = findViewById(R.id.iv_lianjie); //连接服务器
        iv_light.setOnClickListener(this);   //开灯点击事件
        iv_lianjie.setOnClickListener(this);   //连接服务器点击事件
        bt_humi.setOnClickListener(this);   //湿度点击事件
        choice_temperature.setOnClickListener(this);   //湿度点击事件

        aaChartView.aa_drawChartWithChartOptions(configure_DataLabels_XAXis_YAxis_Legend_Style_temp(temp_data)); //加载图表
        timer();//定时获取数据

        shuaxin();//刷新图表数据

        server_time(); //监测服务器是否断开
    }

    @Override
    protected void onResume() {
        super.onResume();
//        try {
//            if(myMqtt !=null)
//            {
//                return;
//            }
//            myMqtt = new MyMqtt(handler);
//            myMqtt.Connection();
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }

    }

    private void getdata() {
        Mode1.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    myMqtt = new MyMqtt(handler);
                    myMqtt.Connection();//启动连接
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 温度图表加载
     *
     * @param data
     * @return
     */
    private AAOptions configure_DataLabels_XAXis_YAxis_Legend_Style_temp(Object[] data) {

        Map backgroundColorGradientColor = AAGradientColor.linearGradient(
                AALinearGradientDirection.ToBottom,
                "#4F00BC",
                "#29ABE2"//颜色字符串设置支持十六进制类型和 rgba 类型
        );

        Map fillColorGradientColor = AAGradientColor.linearGradient(
                AALinearGradientDirection.ToBottom,
                "rgba(60,159,200,0.3)",
                "rgba(20,188,212,0.8)"//颜色字符串设置支持十六进制类型和 rgba 类型
        );


        Calendar now = Calendar.getInstance();
        int rr = now.get(Calendar.HOUR_OF_DAY);
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateNowStr = sdf.format(d);
        String[] timestr = new String[20];
        for (int i = 0; i < 10; i++) {
            timestr[i] = rr - i + ":00";

            if (rr < 0) {
                rr += 24;
            }
        }
        AAChartModel aaChartModel = new AAChartModel()
                .chartType(AAChartType.Areaspline)
                .title("温湿度监测")
                .subtitle("最近更新" + dateNowStr)
                .backgroundColor(backgroundColorGradientColor)
                .yAxisVisible(true)
                .yAxisTitle("")
                ///  .categories(new String[] {, "二", "三月", "四月", "五月", "六月",
                //         "七月", "八月", "九月", "十月", "十一月", "十二月"})

                .categories(timestr)
                .markerRadius(0f)
                .series(new AASeriesElement[]{
                        new AASeriesElement()
                                .name("温度")
                                .color(AAColor.grayColor())
                                .lineWidth(2.f)  //数据线条宽度
                                .fillColor(fillColorGradientColor)
                                //dataLabelsFontColor
                                //   .data(new Object[]{30.0, 6.9, 2.5, 14.5, 18.2, 21.5, 5.2, 26.5, 23.3, 45.3, 13.9, 9.6}),
                                .data(data),//数据源
                });

        AAOptions aaOptions = AAOptionsConstructor.configureChartOptions(aaChartModel);
        aaOptions.yAxis(tubiao_Y_Settint(aaChartModel, "单位：℃", 50));
        aaOptions.plotOptions.areaspline
                .dataLabels(new AADataLabels()
                        .enabled(true)
                        .style(new AAStyle()
                                .color(AAColor.orangeColor())   //数据标签颜色
                                .fontSize(10.f)                 //数据标签字体大小
                                .fontWeight("thin")
                                .textOutline("0px 0px contrast")//文字轮廓描边
                        ));


        AACrosshair aaCrosshair = new AACrosshair()
                .dashStyle(AAChartLineDashStyleType.LongDashDot)
                .color(AAColor.whiteColor())
                .width(1.f);

        AALabels aaLabels = new AALabels()
                .useHTML(true)
                .style(new AAStyle()
                        .fontSize(10.f)
                        .fontWeight("bold")
                        .color(AAColor.whiteColor())//轴文字颜色
                );

        aaOptions.yAxis
                .opposite(true)
                .tickWidth(2.f)
                .lineWidth(1.5f)//Y轴轴线颜色
                .lineColor(AAColor.whiteColor())//Y轴轴线颜色
                .gridLineWidth(0.f)//Y轴网格线宽度
                .crosshair(aaCrosshair)
                .labels(aaLabels);

        aaOptions.xAxis
                .tickWidth(2.f)//X轴刻度线宽度
                .lineWidth(1.5f)//X轴轴线宽度
                .lineColor(AAColor.whiteColor())//X轴轴线颜色
                .crosshair(aaCrosshair)
                .labels(aaLabels);


        //设定图例项的CSS样式。只支持有关文本的CSS样式设定。
        /*默认是：{
         "color": "#333333",
         "cursor": "pointer",
         "fontSize": "12px",
         "fontWeight": "bold"
         }
         */

        aaOptions.legend
                .itemStyle(new AAItemStyle()
                        .color(AAColor.whiteColor())//字体颜色
                        .fontSize(13.f)//字体大小
                        .fontWeight("thin")//字体为细体字

                );

        return aaOptions;
    }


    /**
     * 湿度图表
     *
     * @return
     */

    private AAOptions configure_DataLabels_XAXis_YAxis_Legend_Style_humi(Object[] data) {

        Map backgroundColorGradientColor = AAGradientColor.linearGradient(
                AALinearGradientDirection.ToBottom,
                "#4F00BC",
                "#29ABE2"//颜色字符串设置支持十六进制类型和 rgba 类型
        );

        Map fillColorGradientColor = AAGradientColor.linearGradient(
                AALinearGradientDirection.ToBottom,
                "rgba(60,159,200,0.3)",
                "rgba(20,188,212,0.8)"//颜色字符串设置支持十六进制类型和 rgba 类型
        );


        Calendar now = Calendar.getInstance();
        int rr = now.get(Calendar.HOUR_OF_DAY);
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateNowStr = sdf.format(d);
        String[] timestr = new String[20];
        for (int i = 0; i < 10; i++) {
            timestr[i] = rr - i + ":00";

            if (rr < 0) {
                rr += 24;
            }
        }
        AAChartModel aaChartModel = new AAChartModel()
                .chartType(AAChartType.Areaspline)
                .title("湿度监测")
                .subtitle("最近更新" + dateNowStr)
                .backgroundColor(backgroundColorGradientColor)
                .yAxisVisible(true)
                .yAxisTitle("")
                ///  .categories(new String[] {, "二", "三月", "四月", "五月", "六月",
                //         "七月", "八月", "九月", "十月", "十一月", "十二月"})

                .categories(timestr)
                .markerRadius(0f)
                .series(new AASeriesElement[]{
                        new AASeriesElement()
                                .name("湿度")
                                .color(AAColor.orangeColor())
                                .lineWidth(2.f)  //数据线条宽度
                                .fillColor(fillColorGradientColor)
                                //dataLabelsFontColor
                                //   .data(new Object[]{30.0, 6.9, 2.5, 14.5, 18.2, 21.5, 5.2, 26.5, 23.3, 45.3, 13.9, 9.6}),
                                .data(data),//数据源
                });


        AAOptions aaOptions = AAOptionsConstructor.configureChartOptions(aaChartModel);
        aaOptions.yAxis(tubiao_Y_Settint(aaChartModel, "单位：%RH", 100));


        aaOptions.plotOptions.areaspline
                .dataLabels(new AADataLabels()
                        .enabled(true)
                        .style(new AAStyle()
                                .color(AAColor.orangeColor())   //数据标签颜色
                                .fontSize(10.f)                 //数据标签字体大小
                                .fontWeight("thin")
                                .textOutline("0px 0px contrast")//文字轮廓描边
                        ));


        AACrosshair aaCrosshair = new AACrosshair()
                .dashStyle(AAChartLineDashStyleType.LongDashDot)
                .color(AAColor.whiteColor())
                .width(1.f);

        AALabels aaLabels = new AALabels()
                .useHTML(true)
                .style(new AAStyle()
                        .fontSize(10.f)
                        .fontWeight("bold")
                        .color(AAColor.whiteColor())//轴文字颜色
                );

        aaOptions.yAxis
                .opposite(true)
                .tickWidth(2.f)
                .lineWidth(1.5f)//Y轴轴线颜色
                .lineColor(AAColor.whiteColor())//Y轴轴线颜色
                .gridLineWidth(0.f)//Y轴网格线宽度
                .crosshair(aaCrosshair)
                .labels(aaLabels);

        aaOptions.xAxis
                .tickWidth(2.f)//X轴刻度线宽度
                .lineWidth(1.5f)//X轴轴线宽度
                .lineColor(AAColor.whiteColor())//X轴轴线颜色
                .crosshair(aaCrosshair)
                .labels(aaLabels);


        //设定图例项的CSS样式。只支持有关文本的CSS样式设定。
        /*默认是：{
         "color": "#333333",
         "cursor": "pointer",
         "fontSize": "12px",
         "fontWeight": "bold"
         }
         */

        aaOptions.legend
                .itemStyle(new AAItemStyle()
                        .color(AAColor.whiteColor())//字体颜色
                        .fontSize(13.f)//字体大小
                        .fontWeight("thin")//字体为细体字

                );

        return aaOptions;
    }

    /**
     * 定时刷新图标
     */
    private void shuaxin() {

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                aaChartView.aa_drawChartWithChartOptions(configure_DataLabels_XAXis_YAxis_Legend_Style_temp(temp_data)); //刷新温度
                // aaChartView.aa_drawChartWithChartOptions(configure_DataLabels_XAXis_YAxis_Legend_Style_humi(humi_data)); //刷新湿度
                handler.postDelayed(this::run, 1000 * 60 * 60);
            }
        };
        handler.postDelayed(runnable, 500);

    }

    /**
     * 定时获取数据
     */
    public void timer() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 6); // 控制时
        calendar.set(Calendar.MINUTE,58);       // 控制分
        calendar.set(Calendar.SECOND, 0);       // 控制秒
        Date time = calendar.getTime();         // 得出执行任务的时间,此处为今天的12：00：00
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                get_temp_data();

            }
        }, time, 1000 * 60*60);// 这里设定将延时每天固定执行
    }



    private void get_temp_data() {
        int i = 0;
        HttpServletRequest httpServletRequest = new HttpServletRequest(HttpServletRequest.GET_DATA); //获取服务器数据
        Data data = httpServletRequest.RequestPost();
        for (Data data2 : data.getDataList()) {
            Double temp = new Double(data2.getTemp());  //将温度字符串转化为 Double
            Double humi = new Double(data2.getHumi());  //将湿度字符串转化为 Double
            temp_data[i] = temp; //向图表存入温度数据
            humi_data[i] = humi;//向图表存入湿度数据
            System.out.printf("value----------" + temp_data[i]);
            i++;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_light:
                if (isled)//开灯
                {
                    LED_Controller(HttpServletRequest.LED_ON, true);
                    isled = false;
                } else { //关灯
                    LED_Controller(HttpServletRequest.LED_OFF, false);
                    isled = true;
                }

                break;

            case R.id.iv_lianjie:
                // server_time(); //监测服务器是否断开
                break;
            case R.id.bt_humi:
                aaChartView.aa_drawChartWithChartOptions(configure_DataLabels_XAXis_YAxis_Legend_Style_humi(humi_data)); //加载湿度图表
                break;
            case R.id.choice_temperature:
                aaChartView.aa_drawChartWithChartOptions(configure_DataLabels_XAXis_YAxis_Legend_Style_temp(temp_data)); //加载温度图表
                break;
        }
    }

    /**
     * 检查服务器是否断开
     */
    private  void server_time()
    {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                connection_server();
            }
        }, 1,1000);// 这里设定将延时每天固定执行
    }

    /**
     * 服务器数据获取
     *
     * @param ulr
     * @return
     */
    public Data controller(String ulr) {
        Data data = null;
        HttpServletRequest httpServletRequest = new HttpServletRequest(ulr); //获取服务器数据
        data = httpServletRequest.RequestPost();
        return data;
    }

    /**
     * 连接服务器
     */
    private void connection_server()
    {
        Mode1.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                Data data = controller(HttpServletRequest.CONNECTION_SERVER);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!data.getTemp().equals("connection_ok"))
                        {
                            iv_lianjie.setImageResource(R.drawable.server_yun_no);
                            Toast.makeText(MainActivity.this, "服务器连接断开", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            iv_lianjie.setImageResource(R.drawable.server_yun_ok);
                            //Toast.makeText(MainActivity.this, "服务器连接chengg", Toast.LENGTH_SHORT).show();

                        }

                    }
                });


            }
        });
    }

    /**
     * LED灯控制
     *
     * @param url
     */
    private void LED_Controller(String url, boolean isled) {
        String text = isled ? "开灯成功" : "关灯成功";
        int imageId = isled ? R.drawable.ct_light1 : R.drawable.ct_light0;
        Mode1.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                Data data = controller(url);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (data.getTemp().equals("成功")) {
                            Log.e("led", data.getTemp());
                            Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
                            iv_light.setImageResource(imageId);
                        }
                    }
                });

            }
        });
    }

    /**
     * 设置图表Y轴的值和单位
     */
    private AAYAxis tubiao_Y_Settint(AAChartModel aaChartModel, String danwei, float Y_max_value) {
        Boolean aaYAxisLabelsEnabled = aaChartModel.yAxisLabelsEnabled;
        AALabels aaYAxisLabels = new AALabels()
                .enabled(aaChartModel.yAxisLabelsEnabled);
        if (aaYAxisLabelsEnabled) {
            aaYAxisLabels.style(new AAStyle()
                    .color(aaChartModel.axesTextColor)
            );
        }

        AAYAxis aaYAxis = new AAYAxis()
                .labels(aaYAxisLabels) //设置 y 轴是否显示数字
                .min(aaChartModel.yAxisMin) //设置 y 轴最小值,最小值等于零就不能显示负值了
                .max(Y_max_value) //y轴最大值
                .allowDecimals(aaChartModel.yAxisAllowDecimals) //是否允许显示小数
                .reversed(aaChartModel.yAxisReversed)
                .gridLineWidth(aaChartModel.yAxisGridLineWidth) //y轴网格线宽度
                //axesTextColor

                .title(new AATitle()
                        .text(danwei)
                        .style(new AAStyle()
                                .color(aaChartModel.axesTextColor))
                ) //y 轴标题
                .lineWidth(aaChartModel.yAxisLineWidth) //设置 y轴轴线的宽度,为0即是隐藏 y轴轴线
                .visible(aaChartModel.yAxisVisible);
        return aaYAxis;
    }
}