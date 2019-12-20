package com.tutorgenie.messageportal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toolbar;

import com.google.android.material.tabs.TabLayout;

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

    message_update_receiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GlobalData.initializeScreenSize(MainActivity.this);

        tabLayout = findViewById(R.id.ControlTab);
        viewPager = findViewById(R.id.mainPager);
     //   deleteButton = findViewById(R.id.delete_btn);
        viewPager.setAdapter(new viewPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        inbox_frag = new fragment_mailview(GlobalData.DataCache.getInbox_messages(),
                context);
        sent_frag = new fragment_mailview(GlobalData.DataCache.getSent_messages(), context);
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
        receiver.setAction(new message_update_receiver.action()
        {
            @Override
            public void execute()
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
            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction(CONST.mailUpdateAction);
        registerReceiver(receiver, filter);
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
