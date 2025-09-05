package com.soroushrasti.nahj;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

import com.soroushrasti.nahj.fragments.HomeFragment;
import com.soroushrasti.nahj.fragments.ListFragment;
import com.soroushrasti.nahj.recyclerlist.Item;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener {

    ToggleButton nightModeToggle;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        sharedPref = getSharedPreferences("com.soroushrasti.nahj.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        if (sharedPref.getBoolean("THEME_NIGHT_MODE", false)) {
            setTheme(R.style.NightTheme_NoActionBar);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }

        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_main);

        // Add a Toolbar to an Activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        // Apply top inset to toolbar for status bar overlap
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), sb.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        setSupportActionBar(toolbar);

        // NavigationDrawer
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Inset bottom for content container to avoid nav bar overlap
        View frg = findViewById(R.id.frg_container);
        ViewCompat.setOnApplyWindowInsetsListener(frg, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), sys.bottom);
            return insets;
        });

        // NightMode Toggle Button
        nightModeToggle = navigationView.getHeaderView(0).findViewById(R.id.night_mode_toggle);
        nightModeToggle.setTextOff("");
        nightModeToggle.setTextOn("");
        nightModeToggle.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.ic_moon_black));
        if (sharedPref.getBoolean("THEME_NIGHT_MODE", false)) {
            nightModeToggle.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_moon_green));
            nightModeToggle.setChecked(true);
        } else {
            nightModeToggle.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_moon_black));
            nightModeToggle.setChecked(false);
        }
        nightModeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor = sharedPref.edit();
                if (isChecked) {
                    editor.putBoolean("THEME_NIGHT_MODE", true);
                    nightModeToggle.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_moon_green));
                } else {
                    editor.putBoolean("THEME_NIGHT_MODE", false);
                    nightModeToggle.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_moon_black));
                }
                editor.apply();
                Intent i = new Intent(MainActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.frg_container, new HomeFragment())
                    .commit();
        }


    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frg_container);
            if (fragment instanceof HomeFragment) {
                if (doubleBackToExitPressedOnce) {
                    //int pid = android.os.Process.myPid();
                    //android.os.Process.killProcess(pid);
                    finish();
                }
                this.doubleBackToExitPressedOnce = true;

                Snackbar snackbar = Snackbar.make(drawer, "برای خروج دوباره بازگشت را لمس کنید", Snackbar.LENGTH_SHORT);
                snackbar.setActionTextColor(Color.WHITE);
                View snackbarView = snackbar.getView();
                int snackbarTextId = com.google.android.material.R.id.snackbar_text;
                TextView textView = snackbarView.findViewById(snackbarTextId);
                textView.setTextColor(Color.WHITE);
                ViewCompat.setLayoutDirection(snackbarView,ViewCompat.LAYOUT_DIRECTION_RTL);
                if (sharedPref.getBoolean("THEME_NIGHT_MODE", false))
                    snackbarView.setBackgroundColor(getResources().getColor(R.color.colorNightPrimary));
                else
                    snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                snackbar.show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.frg_container, new HomeFragment())
                    .commit();
        } else if (id == R.id.nav_favorites) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("FAV", true);
            Fragment newFragment = new ListFragment();
            newFragment.setArguments(bundle);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
            transaction.replace(R.id.frg_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (id == R.id.nav_about) {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_about);
            dialog.show();
        }
//        else if (id == R.id.nav_rate) {
//            try {
//                Intent intent = new Intent(Intent.ACTION_EDIT);
//                intent.setData(Uri.parse("bazaar://details?id=" + "com.soroushrasti.nahj"));
//                intent.setPackage("com.farsitel.bazaar");
//                startActivity(intent);
//            } catch (Exception e) {
//                Toast.makeText(this, "خطا", Toast.LENGTH_SHORT).show();
//            }
//        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String q = Utils.normalize(newText);
        ArrayList<Item> newList = new ArrayList<>();
        for (Item item : ListFragment.mItem) {
            String t = Utils.normalize(item.getuTitle());
            String s = Utils.normalize(item.getuShort());
            if (t.contains(q) || s.contains(q)) {
                newList.add(item);
            }
        }
        ListFragment.mAdapter.setFilter(newList);
        return true;
    }

}