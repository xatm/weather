package xatm.weather;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.support.v4.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent; 
import android.content.IntentFilter; 
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

public class weatherFragment extends Fragment
{
    private static weatherFragment weatherfragment = new weatherFragment();
    public static weatherFragment getInstance() {return weatherfragment;}
    private View view;   
    private Spinner provincespinner;
    private TextView weatherinfoview;
    private Button sendweatherbutton;
    private CheckBox citycheckbox;
    private LinearLayout citieslayout;
    private Intent queryintent;

    private String weatherinfo;
    private JSONArray weatherjsonarray;

    private WeatherReceiver weatherreceiver;
    private ArrayList<String> checkedcitynames;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        weatherjsonarray = new JSONArray();
        checkedcitynames = new ArrayList<String>();
    }

    @Override 
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.weatherfragment, container, false);   

        provincespinner = (Spinner)view.findViewById(R.id.province);
        weatherinfoview = (TextView)view.findViewById(R.id.weatherinfo);
        sendweatherbutton = (Button)view.findViewById(R.id.sendweather);
        citieslayout = (LinearLayout)view.findViewById(R.id.cities);

        weatherreceiver = new WeatherReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.weather");
        getActivity().registerReceiver(weatherreceiver,filter);

        String[] provinces = getResources().getStringArray(R.array.provinces);
        ArrayAdapter<String> _adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, provinces);
        _adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        provincespinner.setAdapter(_adapter);
        provincespinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                queryintent = new Intent(getActivity(), queryIntentService.class);
                queryintent.putExtra("province", provincesmap(parent.getItemAtPosition(position).toString()));
                getActivity().startService(queryintent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        sendweatherbutton.setOnClickListener(new View.OnClickListener()  
        {  
            public void onClick(View v)  
            {  
                sendsysSMS(weatherinfoview.getText().toString());
            }  
        });

        return view;
    }

    @Override
    public void onDestroyView()
    {
        checkedcitynames.clear();
        getActivity().unregisterReceiver(weatherreceiver);
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    public class WeatherReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.weather")) {
                Bundle bundle = intent.getExtras();
                weatherinfo = bundle.getString("weatherinfo");
                if(weatherinfo != null && weatherinfo.length() > 0) {
                    weatherinfoview.setText("");
                    weatherjsonarray = new JSONArray();
                    try {
                        weatherjsonarray = (new JSONObject(weatherinfo)).getJSONArray("weather");
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                    createcitycheckbox(weatherjsonarray);
                }
            }
        }
    }

    public void createcitycheckbox(JSONArray weatherjsonarray) {
        checkedcitynames.clear();
        citieslayout.removeAllViews();
        LinearLayout cityrow = new LinearLayout(getActivity());
        cityrow.setOrientation(LinearLayout.HORIZONTAL);
        int columnwidth = 0;
        try {
            JSONObject weatherjson = new JSONObject();

            final JSONArray wjarray = weatherjsonarray;
            for(int i = 0; i < weatherjsonarray.length(); i++) {
                weatherjson = weatherjsonarray.getJSONObject(i);
                final String cityname = weatherjson.getString("cityname");

                citycheckbox = new CheckBox(getActivity());
                citycheckbox.setText(cityname);
                citycheckbox.setTextColor(android.graphics.Color.BLUE);
                citycheckbox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonview, boolean checked) {
                        if (checked) {
                            checkedcitynames.add(cityname);
                        }
                        else {
                            checkedcitynames.remove(cityname);
                        }
                        weatherinfo = filtercity(checkedcitynames, wjarray);
                        weatherinfoview.setText(weatherinfo);
                    }
                });

                if( cityname.equals("成都")) {
                    citycheckbox.performClick();
                }

                // measure in order to get the width of each city checkbox
                citycheckbox.measure(View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED),
                                  View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));

                // check if new city checkbox exceeds the width of one row
                if(columnwidth + citycheckbox.getMeasuredWidth() > citieslayout.getWidth()) {
                    citieslayout.addView(cityrow);
                    cityrow = new LinearLayout(getActivity());
                    columnwidth = 0;
                }
                cityrow.addView(citycheckbox);
                columnwidth += citycheckbox.getMeasuredWidth();
            }

            if(columnwidth != 0) {
                citieslayout.addView(cityrow);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String provincesmap(String province) {
        HashMap<String, String> provinceshashmap = new HashMap<String, String>();
        provinceshashmap.put("四川", "sichuan");
        provinceshashmap.put("重庆", "chongqing");
        provinceshashmap.put("贵州", "guizhou");
        provinceshashmap.put("云南", "yunnan");
        provinceshashmap.put("西藏", "xizang");
        provinceshashmap.put("新疆", "xinjiang");
        provinceshashmap.put("内蒙古", "neimenggu");
        provinceshashmap.put("黑龙江", "heilongjiang");
        provinceshashmap.put("吉林", "jilin");
        provinceshashmap.put("辽宁", "liaoning");
        provinceshashmap.put("河北", "hebei");
        provinceshashmap.put("河南", "henan");
        provinceshashmap.put("青海", "qinghai");
        provinceshashmap.put("宁夏", "ningxia");
        provinceshashmap.put("甘肃", "gansu");
        provinceshashmap.put("陕西", "sanxi");
        provinceshashmap.put("山西", "shanxi");
        provinceshashmap.put("山东", "shandong");
        provinceshashmap.put("湖北", "hubei");
        provinceshashmap.put("湖南", "hunan");
        provinceshashmap.put("广西", "guangxi");
        provinceshashmap.put("广东", "guangdong");
        provinceshashmap.put("福建", "fujian");
        provinceshashmap.put("江西", "jiangxi");
        provinceshashmap.put("浙江", "zhejiang");
        provinceshashmap.put("江苏", "jiangsu");
        provinceshashmap.put("安徽", "anhui");
        provinceshashmap.put("北京", "beijing");
        provinceshashmap.put("天津", "tianjin");
        provinceshashmap.put("上海", "shanghai");
        provinceshashmap.put("海南", "hainan");
        provinceshashmap.put("香港", "xianggang");
        provinceshashmap.put("澳门", "aomen");
        provinceshashmap.put("台湾", "taiwan");

        String mapresult = provinceshashmap.get(province);
        if(mapresult != null) {
            return mapresult;
        }

        return province;
    }

    public String filtercity(ArrayList<String> checkedcitynames, JSONArray weatherjsonarray) {
        ArrayList<String> tempcitynames = new ArrayList<String>(checkedcitynames);
        String weatherforecast = "中央台预报：\n";
        try {
            JSONObject weatherjson = new JSONObject();

            for(int i = 0; i < weatherjsonarray.length(); i++) {
                weatherjson = weatherjsonarray.getJSONObject(i);
                String cityname = weatherjson.getString("cityname");

                for(int j = 0; j < tempcitynames.size(); j++) {
                    if( cityname.equals(tempcitynames.get(j)) ) {
                        weatherforecast += cityname + weatherjson.getString("stateDetailed") + "，"
                                                    + weatherjson.getString("tem1") + "到" 
                                                    + weatherjson.getString("tem2") + "度，\n";

                        tempcitynames.remove(j);
                        break;
                    }
                }

                if(tempcitynames.size() == 0) {
                    break;
                }
            }
            weatherforecast = weatherforecast.substring(0, weatherforecast.length() - 1);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return weatherforecast;
    }

    public void sendsysSMS(String message) {
        Uri uri = Uri.parse("smsto:");            
        Intent it = new Intent(Intent.ACTION_SENDTO, uri);            
        it.putExtra("sms_body", message);            
        this.startActivity(it); 
    }
}
