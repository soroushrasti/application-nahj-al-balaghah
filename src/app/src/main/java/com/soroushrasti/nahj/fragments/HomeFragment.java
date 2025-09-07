package com.soroushrasti.nahj.fragments;

import android.util.Log;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.soroushrasti.nahj.MainActivity;
import com.soroushrasti.nahj.R;
import com.google.android.material.transition.MaterialSharedAxis;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.viewpager2.widget.ViewPager2;
import com.soroushrasti.nahj.ui.HeroPagerAdapter;
import com.soroushrasti.nahj.ui.FullscreenImageActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    private Handler sliderHandler;
    private Runnable sliderRunnable;
    private ViewPager2 heroPager;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.app_name));

        CardView fhcWisdoms = view.findViewById(R.id.fhc_wisdoms);
        CardView fhcSermons = view.findViewById(R.id.fhc_sermons);
        CardView fhcLetters = view.findViewById(R.id.fhc_letters);
        CardView fhcStrangeWords = view.findViewById(R.id.fhc_strange_words);

        fhcWisdoms.setOnClickListener(this);
        fhcSermons.setOnClickListener(this);
        fhcLetters.setOnClickListener(this);
        fhcStrangeWords.setOnClickListener(this);

        // Setup hero pager from assets/images with fallback
        heroPager = view.findViewById(R.id.hero_pager);
        // Removed TabLayout heroDots reference since we removed the slide bar
        if (heroPager != null) {
            List<String> items = new ArrayList<>();

            try {
                AssetManager am = requireContext().getAssets();
                String[] files = am.list("images");
                if (files != null && files.length > 0) {
                    // Clear default placeholders if we have actual images
                    items.clear();
                    for (String f : files) {
                        String lf = f.toLowerCase();
                        if (lf.endsWith(".png") || lf.endsWith(".jpg") || lf.endsWith(".jpeg") || lf.endsWith(".webp")) {
                            // Filter out problematic files
                            if (!isProblematicFile(f)) {
                                // Validate that the image can actually be loaded
                                if (validateImageFile(am, f)) {
                                    Log.d("HomeFragment", "Adding valid image: " + f);
                                    items.add(f);
                                } else {
                                    Log.w("HomeFragment", "Skipping invalid image: " + f);
                                }
                            } else {
                                Log.w("HomeFragment", "Skipping problematic file: " + f);
                            }
                        }
                    }
                }
            } catch (IOException ignored) { }


            HeroPagerAdapter adapter = new HeroPagerAdapter(requireContext(), items, position -> {
                Intent intent = new Intent(requireContext(), FullscreenImageActivity.class);
                intent.putStringArrayListExtra("ITEMS", new ArrayList<>(items));
                intent.putExtra("START", position);
                startActivity(intent);
            });
            heroPager.setAdapter(adapter);

            // Crossfade + subtle zoom transformer
            heroPager.setPageTransformer(new ViewPager2.PageTransformer() {
                @Override
                public void transformPage(View page, float position) {
                    float abs = Math.abs(position);
                    page.setAlpha(1f - 0.3f * abs);
                    float scale = 0.9f + (1f - abs) * 0.1f;
                    page.setScaleX(scale);
                    page.setScaleY(scale);
                }
            });


            // Auto slide every 4s
            sliderHandler = new Handler();
            sliderRunnable = new Runnable() {
                @Override
                public void run() {
                    if (heroPager == null || heroPager.getAdapter() == null) return;
                    int count = heroPager.getAdapter().getItemCount();
                    if (count <= 1) return;
                    int next = (heroPager.getCurrentItem() + 1) % count;
                    heroPager.setCurrentItem(next, true);
                    sliderHandler.postDelayed(this, 4000);
                }
            };
            sliderHandler.postDelayed(sliderRunnable, 4000);

            heroPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    if (sliderHandler != null) {
                        sliderHandler.removeCallbacks(sliderRunnable);
                        sliderHandler.postDelayed(sliderRunnable, 4000);
                    }
                }
            });
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
        heroPager = null;
    }

    // Handle Toolbar Items in a Fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_favorite).setVisible(false);
        menu.findItem(R.id.action_font).setVisible(false);
        menu.findItem(R.id.action_share).setVisible(false);
    }

    int cat;
    String toolbarTitle;

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.fhc_wisdoms) {
            cat = 3;
            toolbarTitle = getString(R.string.wisdoms);
        } else if (viewId == R.id.fhc_sermons) {
            cat = 1;
            toolbarTitle = getString(R.string.sermons);
        } else if (viewId == R.id.fhc_letters) {
            cat = 2;
            toolbarTitle = getString(R.string.letters);
        } else if (viewId == R.id.fhc_strange_words) {
            cat = 4;
            toolbarTitle = getString(R.string.strange_words);
        } else {
            cat = 0;
            toolbarTitle = getString(R.string.app_name);
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Bundle bundle = new Bundle();
                bundle.putInt("CAT", cat);
                bundle.putString("TOOLBAR_TITLE", toolbarTitle);
                Fragment fragment = new ListFragment();
                fragment.setArguments(bundle);
                // Apply Material shared-axis (X) for list navigation
                fragment.setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
                fragment.setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setReorderingAllowed(true);
                fragmentTransaction.replace(R.id.frg_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        }, 100);
    }

    private boolean isProblematicFile(String fileName) {
        // Filter out known problematic files that cause black/bad pictures
        String lf = fileName.toLowerCase();
        return lf.contains("progress_font") ||
               lf.contains("android-logo") ||
               lf.contains("clock_font") ||
               lf.startsWith(".") || // Hidden files
               lf.contains("thumb") || // Thumbnail files
               lf.contains("cache"); // Cache files
    }

    private boolean validateImageFile(AssetManager am, String fileName) {
        // Validate the image file by attempting to decode it
        try (InputStream is = am.open("images/" + fileName)) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true; // Only decode image dimensions, not the full bitmap
            BitmapFactory.decodeStream(is, null, options);
            // Check if image has valid dimensions and is a recognized format
            return options.outWidth > 0 && options.outHeight > 0 && options.outMimeType != null;
        } catch (Exception e) {
            Log.w("HomeFragment", "Failed to validate image: " + fileName + " - " + e.getMessage());
            return false;
        }
    }

}