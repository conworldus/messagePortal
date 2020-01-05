package com.tutorgenie.messageportal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class MainActivity extends AppCompatActivity
{
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Context context = this;
   // private Button deleteButton;
    private Toolbar toolbar;
    private fragment_mailview inbox_frag, sent_frag;
    private fragment_profile profile_frag;
    private fragment_calendar Schedule_frag;
    private TextView calendarBadge;

    public static MutableLiveData<Integer> pendingCount = new MutableLiveData<>();

    message_update_receiver receiver;

    private int[] tabIcons =
            {
            R.drawable.ic_inbox_black_24dp,
            R.drawable.ic_send_black_24dp,
            R.drawable.ic_date_range_black_24dp,
            R.drawable.ic_person_outline_black_24dp
            };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GlobalData.initializeScreenSize(MainActivity.this);

        tabLayout = findViewById(R.id.ControlTab);
        viewPager = findViewById(R.id.mainPager);
        calendarBadge = findViewById(R.id.calendar_badge);
        viewPager.setAdapter(new viewPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        //=======UI Adjustment for calendarBadge======================
        tabLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                LinearLayout tabItem =
                        (LinearLayout)((LinearLayout)tabLayout.getChildAt(0)).getChildAt(2);
                Log.i("TabChildren", tabLayout.getChildCount()+"");
                Log.i("TabSubChildren", ((LinearLayout) tabLayout.getChildAt(0)).getChildCount()+
                        "");
                int childCount = tabItem.getChildCount();
                View tabItemText = (tabItem.getChildAt(0));
                int textWidth = tabItemText.getWidth();
                int textHeight = tabItemText.getHeight();
                int w = tabLayout.getWidth(), h = tabLayout.getHeight();
                float absoluteX =
                        tabItemText.getX()+(float)tabItemText.getWidth()+(float)w/2+(float)calendarBadge.getWidth()/2,
                        absoluteY =
                        tabItemText.getY()-(float)calendarBadge.getHeight()/2;
                //10 is the badge displacement

                calendarBadge.setX(absoluteX); calendarBadge.setY(absoluteY);
                tabLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });


        for(int i = 0; i<4; i++)
        {
            Objects.requireNonNull(tabLayout.getTabAt(i)).setIcon(tabIcons[i]);
            Objects.requireNonNull(Objects.requireNonNull(tabLayout.getTabAt(i)).getIcon()).setTint(Color.WHITE);
        }

        inbox_frag = new fragment_mailview(GlobalData.DataCache.getInbox_messages(),
                context);
        inbox_frag.setType(fragment_mailview.mailType.INBOX);
        sent_frag = new fragment_mailview(GlobalData.DataCache.getSent_messages(), context);
        sent_frag.setType(fragment_mailview.mailType.SENT);
        profile_frag = new fragment_profile(context);
        Schedule_frag = new fragment_calendar(context);

        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {

            }
        });

        receiver = new message_update_receiver();
        receiver.setAction(() ->
        {
            Log.e("Receiver", "Activated");
            if(inbox_frag!=null)
            {
                inbox_frag.updateMailList();
            }
            if(sent_frag!=null)
            {
                sent_frag.updateMailList();
            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction(CONST.mailUpdateAction);
        registerReceiver(receiver, filter);

        //Other Data Initialization
        pendingCount.setValue(GlobalData.DataCache.getPendingList().size());
        pendingCount.observe(this, integer ->
        {
            calendarBadge.setText(String.valueOf(pendingCount.getValue()));
            if(pendingCount.getValue()>0)
            {
                calendarBadge.getBackground().setTint(Color.RED);
                calendarBadge.setVisibility(View.VISIBLE);
            }
            else
            {
                calendarBadge.setVisibility(View.INVISIBLE);
            }

        });
    }

    public void setPagerMailFragment()
    {
        viewPager.setCurrentItem(4);
    }

    public class viewPagerAdapter extends FragmentPagerAdapter
    {

        viewPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int i)
        {
            switch (i)
            {
                case 0:
                    return inbox_frag;
                case 1:
                    return sent_frag;
                case 2:
                    return Schedule_frag;
                case 3:
                    return profile_frag;
                default:
                    break;
            }
            return null;
        }

        @Override
        public int getCount()
        {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            switch (position)
            {
                case 0: return getString(R.string.inbox);
                case 1: return getString(R.string.sent);
                case 2: return getString(R.string.calendar);
                case 3: return getString(R.string.profile);
            }

            return null;
        }
    }
}
