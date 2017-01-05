package com.example.xyzreader.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


/**
 * Created by 'Nagarjuna' on 5/1/17.
 */

public class ArticleFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private long mItemId;
    private Cursor mCursor;

    private View mRootView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Toolbar toolbar;
    private ImageView mImageBanner;
    private TextView newsTextView;

    public static final String ARG_ITEM_ID = "item_id";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleFragment() {
    }

    public static ArticleFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleFragment fragment = new ArticleFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        setHasOptionsMenu(true);
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.article_details, container, false);

        mImageBanner = (ImageView) mRootView.findViewById(R.id.movie_image_banner);

        toolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        if (((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        bindViews();
        return mRootView;
    }
    private void bindViews() {
        if (mRootView != null) {
            collapsingToolbarLayout = (CollapsingToolbarLayout) mRootView
                    .findViewById(R.id.collapsing_toolbar);

            newsTextView = (TextView) mRootView.findViewById(R.id.article_textView);

            if (mCursor != null) {
                collapsingToolbarLayout.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));
//                newsTextView.setText(Html.fromHtml(
//                        DateUtils.getRelativeTimeSpanString(
//                                mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
//                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
//                                DateUtils.FORMAT_ABBREV_ALL).toString()
//                                + " by <font color='#ffffff'>"
//                                + mCursor.getString(ArticleLoader.Query.AUTHOR)
//                                + "</font>"));
                newsTextView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));
                Picasso.with(getActivity()).load(mCursor.getString(ArticleLoader.Query.THUMB_URL))
                        .into(mImageBanner, new Callback() {
                            @Override
                            public void onSuccess() {
                                Bitmap myBitmap = ((BitmapDrawable) mImageBanner.getDrawable()).getBitmap();
                                if (myBitmap != null && !myBitmap.isRecycled()) {
                                    Palette.from(myBitmap).generate(paletteListener);
                                }
                            }

                            @Override
                            public void onError() {

                            }
                        });

            } else {
                mRootView.setVisibility(View.GONE);
            }
        }
    }

    Palette.PaletteAsyncListener paletteListener = new Palette.PaletteAsyncListener() {
        public void onGenerated(Palette palette) {
            int defaultColor = 0x000000;
            int vibrant = palette.getVibrantColor(defaultColor);
            int vibrantLight = palette.getLightVibrantColor(defaultColor);
            int vibrantDark = palette.getDarkVibrantColor(defaultColor);
            collapsingToolbarLayout.setBackgroundColor(vibrant);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                collapsingToolbarLayout.setStatusBarScrimColor(vibrant);
                collapsingToolbarLayout.setContentScrimColor(vibrant);
            }
        }
    };

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(this.getClass().getSimpleName(), "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        mCursor = null;
        bindViews();
    }

}
