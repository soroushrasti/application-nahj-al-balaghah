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
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

import com.soroushrasti.nahj.fragments.HomeFragment;
import com.soroushrasti.nahj.fragments.ListFragment;
import com.soroushrasti.nahj.recyclerlist.Item;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import android.util.Log;
import com.soroushrasti.nahj.database.SqlLiteDbHelper;
import org.json.JSONArray;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import androidx.appcompat.app.AppCompatDelegate;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener {

    ToggleButton nightModeToggle;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    DrawerLayout drawer;
    private String currentLang;

    private void applyLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources res = getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        getApplicationContext().createConfigurationContext(config);
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sharedPref = getSharedPreferences("com.soroushrasti.nahj.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        currentLang = sharedPref.getString("LANG", "fa");
        applyLocale(currentLang);

        // Apply night mode via AppCompatDelegate before inflating views
        if (sharedPref.getBoolean("THEME_NIGHT_MODE", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);

        if (sharedPref.getBoolean("THEME_NIGHT_MODE", false)) {
            setTheme(R.style.SpiritualNightTheme);
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
        // Language switcher - REMOVED (now using menu-based switcher only)

        // Adjust layout direction dynamically
        drawer = findViewById(R.id.drawer_layout);
        if (currentLang.equals("en")) {
            ViewCompat.setLayoutDirection(drawer, ViewCompat.LAYOUT_DIRECTION_LTR);
        } else {
            ViewCompat.setLayoutDirection(drawer, ViewCompat.LAYOUT_DIRECTION_RTL);
        }
        // NavigationDrawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Refresh menu when back stack changes to keep language icon only on Home
        getSupportFragmentManager().addOnBackStackChangedListener(() -> invalidateOptionsMenu());

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
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    nightModeToggle.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_moon_green));
                } else {
                    editor.putBoolean("THEME_NIGHT_MODE", false);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    nightModeToggle.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_moon_black));
                }
                editor.apply();
                // Recreate to apply theme changes across the activity
                recreate();
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Only show language switch icon on home fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frg_container);
        MenuItem languageItem = menu.findItem(R.id.action_language);
        if (languageItem != null) {
            languageItem.setVisible(currentFragment instanceof HomeFragment);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_language) {
            // Switch language
            String newLang = currentLang.equals("fa") ? "en" : "fa";
            editor = sharedPref.edit();
            editor.putString("LANG", newLang);
            editor.apply();
            Toast.makeText(this, getString(R.string.language_changed), Toast.LENGTH_SHORT).show();
            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void dumpUntranslated() {
        SqlLiteDbHelper db = new SqlLiteDbHelper(this);
        db.openDataBase();
        var list = db.getUntranslated();
        if (list.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_untranslated), Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            var m = list.get(i);
            sb.append("{\"id\":").append(m.getId())
              .append(",\"title_en\":\"\",\"cnt_en\":\"\"}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        Log.d("TranslationsDump", sb.toString());
        Toast.makeText(this, getString(R.string.dump_success_message), Toast.LENGTH_SHORT).show();
    }

    private byte[] readAll(InputStream is) throws IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while ((n = is.read(buffer)) != -1) baos.write(buffer, 0, n);
        return baos.toByteArray();
    }

    private void importTranslations() {
        try {
            InputStream is = getAssets().open("translations_en.json");
            byte[] buf = readAll(is);
            is.close();
            String json = new String(buf, StandardCharsets.UTF_8);
            JSONArray arr = new JSONArray(json);
            SqlLiteDbHelper db = new SqlLiteDbHelper(this);
            db.openDataBase();
            int success = db.importTranslationsFromJsonArray(arr);
            int fail = arr.length() - success;
            Toast.makeText(this, getString(R.string.import_result, success, fail), Toast.LENGTH_LONG).show();
            recreate();
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.import_file_not_found), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.import_parse_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        FragmentTransaction transaction;
        Fragment newFragment;
        if (id == R.id.nav_home) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.frg_container, new HomeFragment())
                    .commit();
            // Invalidate menu to show language switch icon on home
            invalidateOptionsMenu();
        } else if (id == R.id.nav_wisdoms) {
            Bundle bundle = new Bundle();
            bundle.putInt("CAT", 3);
            bundle.putString("TOOLBAR_TITLE", getString(R.string.wisdoms));
            newFragment = new ListFragment();
            newFragment.setArguments(bundle);
            transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
            transaction.replace(R.id.frg_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            // Invalidate menu to hide language switch icon on other fragments
            invalidateOptionsMenu();
        } else if (id == R.id.nav_sermons) {
            Bundle bundle = new Bundle();
            bundle.putInt("CAT", 1);
            bundle.putString("TOOLBAR_TITLE", getString(R.string.sermons));
            newFragment = new ListFragment();
            newFragment.setArguments(bundle);
            transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
            transaction.replace(R.id.frg_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            invalidateOptionsMenu();
        } else if (id == R.id.nav_letters) {
            Bundle bundle = new Bundle();
            bundle.putInt("CAT", 2);
            bundle.putString("TOOLBAR_TITLE", getString(R.string.letters));
            newFragment = new ListFragment();
            newFragment.setArguments(bundle);
            transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
            transaction.replace(R.id.frg_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            invalidateOptionsMenu();
        } else if (id == R.id.nav_strange_words) {
            Bundle bundle = new Bundle();
            bundle.putInt("CAT", 4);
            bundle.putString("TOOLBAR_TITLE", getString(R.string.strange_words));
            newFragment = new ListFragment();
            newFragment.setArguments(bundle);
            transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
            transaction.replace(R.id.frg_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            invalidateOptionsMenu();
        } else if (id == R.id.nav_favorites) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("FAV", true);
            newFragment = new ListFragment();
            newFragment.setArguments(bundle);
            transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
            transaction.replace(R.id.frg_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            invalidateOptionsMenu();
        }

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