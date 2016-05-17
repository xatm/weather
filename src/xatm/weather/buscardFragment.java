package xatm.weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import android.widget.CompoundButton;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.LayoutInflater;
import android.content.BroadcastReceiver; 
import android.content.Context;
import android.content.Intent; 
import android.content.IntentFilter; 
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.util.Log;

public class buscardFragment extends Fragment implements OnClickListener
{
    private static buscardFragment buscardfragment = new buscardFragment();
    public static buscardFragment getInstance() {return buscardfragment;}
    private View view;   

    private EditText buscardnumbertext;
    private Button buscardnumberaddbutton;
    private Button buscardnumberdelbutton;
    private Button querybuscardbutton;
    private TableLayout buscardnumberlayout;
    private ArrayList<String> buscardnumbers;
    private CheckBox numbercb;
    private SQLiteHelper sqlitehelper;

    private LinearLayout buscardinfolayout;
    private buscardtextview waitview;
    private String buscardinfo;
    private BuscardReceiver buscardreceiver;
    private Intent queryintent;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override 
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
        view = inflater.inflate(R.layout.buscardfragment, container, false);   

        buscardnumbertext = (EditText)view.findViewById(R.id.buscardnumbertext);
        buscardnumberaddbutton = (Button)view.findViewById(R.id.buscardnumberadd);
        buscardnumberdelbutton = (Button)view.findViewById(R.id.buscardnumberdel);
        querybuscardbutton = (Button)view.findViewById(R.id.querybuscard);
        buscardnumberlayout = (TableLayout)view.findViewById(R.id.buscards);
        buscardinfolayout = (LinearLayout)view.findViewById(R.id.buscardinfo);

        waitview = new buscardtextview(getActivity(), getString(R.string.buscardinfo));

        sqlitehelper = SQLiteHelper.getInstance(getActivity());
        buscardnumbers = sqlitehelper.readbuscardtable();//= new ArrayList<String>();//Arrays.asList("", ""));

        createbuscardnumbercb(buscardnumbers);
        buscardnumberaddbutton.setOnClickListener(this);
        buscardnumberdelbutton.setOnClickListener(this);
        querybuscardbutton.setOnClickListener(this);

        buscardreceiver = new BuscardReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.buscard");
        getActivity().registerReceiver(buscardreceiver,filter);

        return view;
    }   

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        getActivity().unregisterReceiver(buscardreceiver);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    public class BuscardReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.buscard")) {
                Bundle bundle = intent.getExtras();
                buscardinfo = bundle.getString("buscardinfo");
                if(buscardinfo != null && buscardinfo.length() > 0) {
                    buscardtextview buscardnumberview = new buscardtextview(getActivity(), "卡号 ");
                    buscardnumberview.setId(1);

                    buscardlayout timelayout = new buscardlayout(getActivity(), 2, "消费时间"); 
                    buscardlayout linelayout = new buscardlayout(getActivity(), 3, "线路"); 
                    buscardlayout countlayout = new buscardlayout(getActivity(), 4, "剩余次数"); 
                    buscardlayout walletlayout = new buscardlayout(getActivity(), 5, "电子钱包"); 
                    buscardlayout nolayout = new buscardlayout(getActivity(), 6, "车号"); 

                    try {
                        JSONObject buscardsjson = new JSONObject(buscardinfo);
                        JSONArray buscardjsonarray = buscardsjson.getJSONArray("buscard");
                        JSONObject buscardjson = new JSONObject();

                        buscardnumberview.setText("卡号 " + buscardsjson.get("卡号"));
                        // append detailed info for one buscard
                        for(int i = 0; i < buscardjsonarray.length(); i++) {
                            buscardjson = buscardjsonarray.getJSONObject(i);

                            timelayout.appendbuscard(getActivity(), buscardjson.getString("消费时间"));
                            linelayout.appendbuscard(getActivity(), buscardjson.getString("线路"));
                            countlayout.appendbuscard(getActivity(), buscardjson.getString("剩余次数"));
                            walletlayout.appendbuscard(getActivity(), buscardjson.getString("电子钱包"));
                            nolayout.appendbuscard(getActivity(), buscardjson.getString("车号"));
                        }
                    }
                    catch(JSONException e) {
                        e.printStackTrace();
                    }

                    OneBuscardDetailLayout onebuscarddetaillayout = new OneBuscardDetailLayout(getActivity(),  timelayout, linelayout, countlayout, walletlayout, nolayout);
                    OneBuscardLayout onebuscardlayout = new OneBuscardLayout(getActivity(),  buscardnumberview, onebuscarddetaillayout);

                    // use modified horizontal scroll view layout for each buscard
                    HorizontalScrollView onebuscardscroll = new HorizontalScrollView(getActivity());

                    onebuscardscroll.addView(onebuscardlayout);
                    onebuscardscroll.setOnTouchListener(new View.OnTouchListener() {
                        private float lastx;  
                        private float lasty;  

                        ViewConfiguration configuration = ViewConfiguration.get(getActivity());  
                        final int mTouchSlop = configuration.getScaledTouchSlop();  
                        public boolean onTouch(View v, MotionEvent ev)
                        {
                            int action = ev.getAction();
                            switch(action) {
                                case MotionEvent.ACTION_DOWN:
                                    lasty = ev.getY();  
                                    lastx = ev.getX();  
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    final float x = ev.getX();  
                                    final int xDiff = (int) Math.abs(x - lastx);  
                                    boolean xMoved = xDiff > mTouchSlop;  
                                      
                                    final float y = ev.getY();  
                                    final int yDiff = (int) Math.abs(y - lasty);  
                                    boolean yMoved = yDiff > mTouchSlop;  
                                    if (xMoved) {  
                                        if(xDiff>=yDiff)  
                                        lastx = x;  
                                        v.getParent().requestDisallowInterceptTouchEvent(true);
                                    }  
                  
                                    if (yMoved) {  
                                        if(yDiff>xDiff)  
                                        lasty = y;  
                                    }  
                                    break;
                                default:
                                    break;
                            }
                            return false;
                        }
                    });

                    // add one buscard at one time
                    buscardinfolayout.addView(onebuscardscroll, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                    waitview.setText("");
                }
            }
        }
    }

    public void createbuscardnumbercb(ArrayList<String> buscardnumbers) {
        buscardnumberlayout.removeAllViews();
        TableRow tablerow = new TableRow(getActivity());
        int col = 0;

        for(int i = 0; i < buscardnumbers.size(); i++) {
            final String number = buscardnumbers.get(i);
            numbercb = new CheckBox(getActivity());
            numbercb.setId(Integer.parseInt(number));
            numbercb.setText(number);
            numbercb.setTextColor(android.graphics.Color.BLUE);

            numbercb.performClick();

            tablerow.addView(numbercb);
            col++;
            col %= 3;
            if(col == 0) {
                buscardnumberlayout.addView(tablerow);
                tablerow = new TableRow(getActivity());
            }
        }

        if(col != 0) {
            buscardnumberlayout.addView(tablerow);
        }
    }
    
    public void onClick(View v)
    {  
        String bnstring = buscardnumbertext.getText().toString();
        switch(v.getId()) {  
            case R.id.buscardnumberadd:
                try {
                    sqlitehelper.appendbuscardtable(bnstring);
                    buscardnumbers.add(bnstring);
                    createbuscardnumbercb(buscardnumbers);
                    Toast.makeText(getActivity(), bnstring+" add", Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {
                    Toast.makeText(getActivity(), "duplicate buscardnumber", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.buscardnumberdel:
                sqlitehelper.deletebuscardtable(bnstring);
                buscardnumbers.remove(bnstring);
                createbuscardnumbercb(buscardnumbers);
                Toast.makeText(getActivity(), bnstring+" del", Toast.LENGTH_LONG).show();
                CheckBox numbercb = (CheckBox)getView().findViewById(Integer.parseInt(bnstring));
                buscardnumberlayout.removeView(numbercb);
                break;
            case R.id.querybuscard:
                // find all checked buscardnumber
                ArrayList<String> checkedbuscardnumbers = new ArrayList<String>();
                for(int i = 0; i < buscardnumbers.size(); i++) {
                    String buscardnumber = buscardnumbers.get(i);
                    if( ((CheckBox)getView().findViewById(Integer.parseInt(buscardnumber))).isChecked() ) {
                        checkedbuscardnumbers.add(buscardnumber);
                    }
                }

                queryintent = new Intent(getActivity(), queryIntentService.class);
                queryintent.putExtra("buscardnumbers", checkedbuscardnumbers);
                getActivity().startService(queryintent);

                // set waiting text before queried info return
                buscardinfolayout.removeAllViews();
                buscardinfolayout.addView(waitview);
                break;
        }
    }

    // detailed info layout of one buscard, including time, count, wallet, line and N.O.
    private class OneBuscardDetailLayout extends LinearLayout {
        public OneBuscardDetailLayout(Context context, buscardlayout timelayout, buscardlayout linelayout, buscardlayout countlayout, buscardlayout walletlayout, buscardlayout nolayout) { 
            super(context);
            setOrientation(LinearLayout.HORIZONTAL);

            addView(timelayout, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            addView(countlayout, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            addView(walletlayout, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            
            LinearLayout.LayoutParams LinearLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            LinearLayoutParams.setMargins(10, 0, 10, 0);
            addView(linelayout, LinearLayoutParams);

            addView(nolayout, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }
    }

    private class buscardtextview extends TextView {
        public buscardtextview(Context context, String buscard) { 
            super(context);
            setText(buscard);
            setTextSize(18);
            setTextColor(android.graphics.Color.BLACK);
            setGravity(Gravity.CENTER);
        }
    }

    // one column for one buscard
    private class buscardlayout extends LinearLayout {
        public buscardlayout(Context context, int id, String buscardcolumn) { 
            super(context);
            setId(id);
            setOrientation(LinearLayout.VERTICAL);
            appendbuscard(context, buscardcolumn);
        }

        public void appendbuscard(Context context, String buscardcolumn) { 
            addView(new buscardtextview(context, buscardcolumn), new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
    }

    // layout of one buscard, including number as title and other detailed info
    private class OneBuscardLayout extends RelativeLayout {
        private RelativeLayout.LayoutParams relativeLayoutParams;

        public OneBuscardLayout(Context context, TextView buscardnumberview, OneBuscardDetailLayout onebuscarddetaillayout) {
            super(context);
            relativeLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            relativeLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            addView(buscardnumberview, relativeLayoutParams);

            relativeLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            relativeLayoutParams.addRule(RelativeLayout.BELOW, buscardnumberview.getId());
            addView(onebuscarddetaillayout, relativeLayoutParams);
        }
    }
}
