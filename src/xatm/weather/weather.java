package xatm.weather;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.animation.Animation;  
import android.view.animation.TranslateAnimation;  
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;

public class weather extends FragmentActivity implements OnClickListener
{
    private FragmentManager fm = getSupportFragmentManager();  
    private int[] buttonsId = {R.id.weather, R.id.buscard};//, R.id.fund, R.id.traceloc};
    private ViewPager viewpager;  
    
    public class FragmentFactory {
        private Fragment fragment = null;
        public Fragment createFragment(int bid) {
            switch(bid) {
                case R.id.weather:
                    fragment = weatherFragment.getInstance();
                    break;
                case R.id.buscard:
                    fragment = buscardFragment.getInstance();
                    break;
/*                case R.id.fund:
                    fragment = fundFragment.getInstance();
                    break;
                case R.id.traceloc:
                    fragment = tracelocFragment.getInstance();
                    break;*/
            }

            return fragment;
        }
    }

    public void onClick(View v) {
        int bid = v.getId();

        for(int i = 0; i < buttonsId.length; i++ ){
            if(buttonsId[i] == bid) {
                viewpager.setCurrentItem(i);
            }
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);
            
        for(int id : buttonsId)
        {
            ((Button)findViewById(id)).setOnClickListener(this);
        }

        InitViewPager();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroy()
    {
        SQLiteHelper sqlitehelper = SQLiteHelper.getInstance(this);
        sqlitehelper.close();
        super.onDestroy();
    }
  
    public class wFragmentPagerAdapter extends FragmentPagerAdapter{  
        private int[] buttonsId;

        public wFragmentPagerAdapter(FragmentManager fm,int[] buttonsId) {  
            super(fm);  
            this.buttonsId = buttonsId;  
        }  
          
        @Override  
        public int getCount() {  
            return buttonsId.length;  
        }  
          
        @Override  
        public Fragment getItem(int position) {  
            return new FragmentFactory().createFragment(buttonsId[position]);  
        }  
    }
  
    public class wPagerAdapter extends PagerAdapter{  
        private FragmentManager fm;
        private FragmentTransaction ft;
        private int[] buttonsId;
        private Fragment[] fragments;

        public wPagerAdapter(FragmentManager fm,int[] buttonsId) {  
            super();
            this.fm = fm;
            this.buttonsId = buttonsId;
            this.fragments = new Fragment[buttonsId.length]; 
        }
          
        @Override  
        public int getCount() {  
            return buttonsId.length;  
        }  
          
        @Override  
        public boolean isViewFromObject (View view, Object object) {
            return view == ((Fragment)object).getView();
        }
          
        @Override  
        public Object instantiateItem(ViewGroup container, int position) {  
            if(fragments[position] == null) {
                fragments[position] = new FragmentFactory().createFragment(buttonsId[position]);
            }

            ft = fm.beginTransaction();
            if(!fragments[position].isAdded()) {
                ft.add(container.getId(), fragments[position]);
            }
            ft.show(fragments[position]).commit();

            return fragments[position];
        }  
          
        @Override  
        public void destroyItem(ViewGroup container, int position, Object object) {  
            ft = fm.beginTransaction();
            ft.hide(fragments[position]).commit();
        }  
    }

    private void InitViewPager() {
        viewpager = (ViewPager)findViewById(R.id.viewpager);
//        viewpager.setAdapter(new wFragmentPagerAdapter(fm, buttonsId));
        viewpager.setAdapter(new wPagerAdapter(fm, buttonsId));
        viewpager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                for(int i = 0; i < buttonsId.length; i++ ){
                    ((Button)findViewById(buttonsId[i])).setTextColor(android.graphics.Color.BLACK);
                }
                ((Button)findViewById(buttonsId[position])).setTextColor(android.graphics.Color.RED);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        viewpager.setCurrentItem(0);
        ((Button)findViewById(buttonsId[0])).setTextColor(android.graphics.Color.RED);
        viewpager.setOffscreenPageLimit(1);
    }
}
