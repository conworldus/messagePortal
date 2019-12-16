package com.tutorgenie.messageportal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toolbar;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity
{
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Context context = this;
    private Button deleteButton;
    private Toolbar toolbar;
    private fragment_mailview inbox_frag, sent_frag;
    private fragment_profile profile_frag;
    private fragment_calendar frag2;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GlobalData.initializeScreenSize(MainActivity.this);

        tabLayout = findViewById(R.id.ControlTab);
        viewPager = findViewById(R.id.mainPager);
        deleteButton = findViewById(R.id.delete_btn);
        viewPager.setAdapter(new viewPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        inbox_frag = new fragment_mailview(GlobalData.DataCache.getInbox_messages(),
                context, deleteButton);
        sent_frag = new fragment_mailview(GlobalData.DataCache.getSent_messages(), context,
                deleteButton);
        profile_frag = new fragment_profile(context);
        frag2 = new fragment_calendar(context);

        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                switch (tab.getPosition())
                {
                    case 0:
                        if(inbox_frag.getDeleteListSize()==0)
                            deleteButton.setVisibility(View.INVISIBLE);
                        else deleteButton.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        if(sent_frag.getDeleteListSize()==0)
                            deleteButton.setVisibility(View.INVISIBLE);
                        else deleteButton.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        deleteButton.setVisibility(View.INVISIBLE);
                        break;
                    case 3:
                        deleteButton.setVisibility(View.INVISIBLE);
                        break;
                }
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
                    return frag2;
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
