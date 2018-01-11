package xatm.weather;

import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.*;
import java.util.concurrent.ExecutorService;  
import java.util.concurrent.Executors;  
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import android.app.IntentService;
import android.content.Intent; 
import android.net.*;
import android.os.IBinder;
import android.util.Log;
import android.util.Xml;

public class queryIntentService extends IntentService
{
    private String weatherinfo;
    private String fundinfo;
    private ExecutorService pool;  

    public queryIntentService()
    {
        super("queryIntentService");
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        pool = Executors.newCachedThreadPool();  
    }

    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) {  
        return super.onStartCommand(intent, flags, startId);
    }
    
    protected void onHandleIntent(Intent it) {
        String province = it.getStringExtra("province");
        if(province != null) {
            Intent weatherinfointent = new Intent();

            weatherinfo = "";
            String weatherurl = "http://flash.weather.com.cn/wmaps/xml/" + province + ".xml";
            String weathercontent = queryHTTP(weatherurl, "UTF-8");
            weatherinfo = parsexml(weathercontent);

            weatherinfointent.putExtra("weatherinfo", weatherinfo);
            weatherinfointent.setAction("android.intent.action.weather");
            sendBroadcast(weatherinfointent);
        }

        final ArrayList<String> buscardnumbers = it.getStringArrayListExtra("buscardnumbers");
        if(buscardnumbers != null) {
            for(int i = 0; i < buscardnumbers.size(); i++) {
                final JSONObject buscardsjson = new JSONObject();
                final String buscardnumber = buscardnumbers.get(i);
                try{
                    buscardsjson.put("卡号", buscardnumber);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }

                final String buscardurl = "http://www.cdgjbus.com/Card.aspx?Pid=96&CardNumder="+buscardnumber;

                new Thread(new Runnable() {
                    public void run() {
                        String buscardcontent = queryHTTP("http://cross-jeffw.c9.io/buscard/"+buscardnumber, "UTF-8");
                        if(buscardcontent.matches("\\[.+\\]$")) { 
                            parsecachebuscard(buscardsjson, buscardcontent);
                        }
                        else {
                            buscardcontent = queryHTTP(buscardurl, "UTF-8");
                            parsebuscard(buscardsjson, buscardcontent);
                        }
                        //Log.e("weather", buscardsjson.toString());

                        Intent buscardinfointent = new Intent();
                        buscardinfointent.putExtra("buscardinfo", buscardsjson.toString());
                        buscardinfointent.setAction("android.intent.action.buscard");
                        sendBroadcast(buscardinfointent);
                    }
                }).start();
            }
        }

        String[] fundnumbers = it.getStringArrayExtra("fundnumbers");
        if(fundnumbers != null) {
            Intent fundinfointent = new Intent();

            fundinfo = "";
            String fundurl = "http://hq.sinajs.cn/list=";
            for(String fundnumber : fundnumbers) {
                fundurl += fundnumber+",";
            }
            String fundcontent = queryHTTP(fundurl, "GB2312");

            fundinfo += parsefund(fundcontent);
            
            fundinfointent.putExtra("fundinfo", fundinfo);
            fundinfointent.setAction("android.intent.action.fund");
            sendBroadcast(fundinfointent);
        }
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        pool.shutdown();
    }

    @Override  
    public IBinder onBind(Intent intent) {  
        return null;  
    }

    public String queryHTTP(String queryurl, String encode) {
        String HTTPcontent = "";
        try {
            URL url = new URL(queryurl);
            HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
            InputStreamReader in = new InputStreamReader(urlConn.getInputStream(), encode);
            BufferedReader buffer = new BufferedReader(in);
            String inputLine = null;  
            while ((inputLine = buffer.readLine()) != null) {
                HTTPcontent += inputLine;
            }
            buffer.close();
            in.close();
            urlConn.disconnect();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return HTTPcontent;
    }

    public void parseweather(JSONArray weatherjsonarray, XmlPullParser parser) {
        try {
            JSONObject weatherjson = new JSONObject();
            weatherjson.put("cityname",parser.getAttributeValue(2));
            weatherjson.put("stateDetailed",parser.getAttributeValue(8));
            weatherjson.put("tem1",parser.getAttributeValue(9));
            weatherjson.put("tem2",parser.getAttributeValue(10));
            weatherjson.put("time",parser.getAttributeValue(16));

            weatherjsonarray.put(weatherjson);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String parsexml(String infoxml) {
        String parseresult = "";
        try {
            InputStream is = new ByteArrayInputStream(infoxml.getBytes("UTF-8"));  
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(is, "UTF-8");
            
            JSONArray weatherjsonarray = new JSONArray();

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if(parser.isEmptyElementTag()) {
                            parseweather(weatherjsonarray, parser);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = parser.next();
            }
            is.close();

            parseresult += (new JSONObject()).put("weather", weatherjsonarray).toString();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return parseresult;
    }

    public void parsecachebuscard(JSONObject buscardsjson, String buscardcontent) {
        try {
            JSONArray cachejsonarray = new JSONArray(buscardcontent);
            JSONArray buscardjsonarray = new JSONArray();
            JSONObject cachejson,buscardjson;
            for(int i = 0; i < cachejsonarray.length(); i++) {
                cachejson = new JSONObject();
                cachejson = cachejsonarray.getJSONObject(i);

                buscardjson = new JSONObject();
                buscardjson.put("线路", cachejson.getString("线路"));
                buscardjson.put("车号", cachejson.getString("车号"));
                buscardjson.put("消费时间", cachejson.getString("消费时间").replaceAll("(.+)T(.+)\\.[0-9]+Z$", "$1 $2"));
                buscardjson.put("消费次数", cachejson.getString("消费次数"));
                buscardjson.put("剩余次数", cachejson.getString("剩余次数"));
                buscardjson.put("电子钱包", cachejson.getString("电子钱包"));

                buscardjsonarray.put(buscardjson);
            }
            buscardsjson.put("buscard", buscardjsonarray);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void parsebuscard(JSONObject buscardsjson, String buscardcontent) {
        JSONArray buscardjsonarray = new JSONArray();
       
        // get every column of buscard related info
        Matcher m = Pattern.compile("<td\\swidth=\"[0-9]+\"\\salign=\"center\">([\\u4E00-\\u9FA5]+).*?</td>(?s)").matcher(buscardcontent);

        ArrayList<String> columnnames = new ArrayList<String>();
        while (m.find()) {
            String columnname = m.group(1);
            columnnames.add(columnname);
        }

        // get data
        Matcher mdata = Pattern.compile("<td\\salign=\"center\">\\s*([\\u4E00-\\u9FA50-9A-Z/\\-\\.]*\\s*?[0-9:]*)\\s*?</td>").matcher(buscardcontent);
        int datacount = 0;
        int columncount = columnnames.size();
        try {
            JSONObject buscardjson = new JSONObject();

            while (mdata.find()) {
                int index = datacount % columncount;
                if(index != 0) {
                    buscardjson.put(columnnames.get(index), mdata.group(1));
                }

                datacount++;
                if(datacount % columncount == 0) {
                    buscardjsonarray.put(buscardjson);
                    buscardjson = new JSONObject();
                }

                if(datacount == 16*columncount) break;
            }

            buscardsjson.put("buscard", buscardjsonarray);
        }
        catch(Exception e) {
        }
    }

    public String parsefund(String fundcontent) {
        String fundinfo = "";
        JSONArray fundjsonarray = new JSONArray();

        // pattern example  ----------- var hq_str_f_000001="华夏成长混合,1.327,3.528,1.323,2015-09-30,45.9707";
        Matcher m = Pattern.compile("\"([[\\u4E00-\\u9FA5][()\\d\\w]]+),([\\d\\.-]+),([\\d\\.-]+),([\\d\\.-]+),([\\d\\.-]+)").matcher(fundcontent);
        try {
            while (m.find()) {
                JSONObject fundjson = new JSONObject();
                fundjson.put("name",m.group(1));

                // calculate date difference between today and the day of newvalue
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                long diffDays = TimeUnit.DAYS.convert(((new Date()).getTime() - sdf.parse(m.group(5)).getTime()),(TimeUnit.MILLISECONDS));
                fundjson.put("time",m.group(5) + "(" + diffDays + "天前)");

                // get new and old value and calculate their rate
                Double newvalue = Double.valueOf(m.group(2));
                Double oldvalue = Double.valueOf(m.group(4));
                fundjson.put("newvalue",newvalue);
                fundjson.put("oldvalue",oldvalue);
                fundjson.put("rate",new DecimalFormat("#0.00").format(100*(newvalue/oldvalue - 1)) + "%");

                fundjsonarray.put(fundjson);
            }

            fundinfo += (new JSONObject()).put("fund",fundjsonarray).toString();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return fundinfo;
    }
}
