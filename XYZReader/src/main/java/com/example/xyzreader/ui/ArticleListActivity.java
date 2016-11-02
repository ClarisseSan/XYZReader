package com.example.xyzreader.ui;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;

import java.util.Random;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private ImageView mImageLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        startLogoAnimation();
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            refresh();
        }
    }


    private void startLogoAnimation() {

        mImageLogo = (ImageView) findViewById(R.id.image_logo);


        final long DEFAULT_ANIMATION_DURATION = 1500L;

        int appBarHeight = (int) getResources().getDimension(R.dimen.app_bar_height);


        //Create an instance of ValueAnimator by calling the static method ofFloat.
        // In this case, the values start at 0 and end with appBarHeight.
        // Android starts screen coordinates at the top-left corner,
        // it moves  top to bottom.
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, appBarHeight);

        //ValueAnimator calls this listener with every update to the animated value
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //Get the current value from the animator and cast it to float;
                // current value type is float because you created the ValueAnimator with ofFloat.
                float value = (float) animation.getAnimatedValue();
                //Change the image's position by using the setTranslationY().
                mImageLogo.setTranslationY(value / 3);
            }
        });

        //Set up the animator’s duration and interpolator.
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(DEFAULT_ANIMATION_DURATION);
        //start animation
        valueAnimator.start();
    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    private boolean mIsRefreshing = false;

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Adapter adapter = new Adapter(cursor, ArticleListActivity.this);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private Cursor mCursor;
        private Activity mHost;

        public Adapter(Cursor cursor, Activity activity) {
            mCursor = cursor;
            mHost = activity;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(ArticleLoader.Query._ID);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Uri contentUri = ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition()));
                    Intent intent = new Intent(mHost, ArticleDetailActivity.class);
                    intent.setData(contentUri);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mHost.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(
                                mHost, vh.thumbnailView, vh.thumbnailView.getTransitionName()).toBundle());
                    } else {
                        mHost.startActivity(intent);
                    }
                }
            });


            //this cardview animation only supports devices with API 21+
            if (Build.VERSION.SDK_INT >= 21) {
                startListItemAnimation(vh.cardView);
            }
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            holder.subtitleView.setText(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR));
            holder.thumbnailView.setImageUrl(
                    mCursor.getString(ArticleLoader.Query.THUMB_URL),
                    ImageLoaderHelper.getInstance(ArticleListActivity.this).getImageLoader());
            holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        }

        private void startListItemAnimation(CardView cardView) {

            float maxWidthOffset = 2f * getResources().getDisplayMetrics().widthPixels;
            float maxHeightOffset = 2f * getResources().getDisplayMetrics().heightPixels;
            Interpolator interpolator = AnimationUtils.loadInterpolator(getApplicationContext(), android.R.interpolator.linear_out_slow_in);
            Random random = new Random();
            int count = getItemCount();

            for (int i = 0; i < count; i++) {
                cardView.setVisibility(View.VISIBLE);
                cardView.setAlpha(0.85f);
                float xOffset = random.nextFloat() * maxWidthOffset;
                if (random.nextBoolean()) {
                    xOffset *= -1;
                }
                cardView.setTranslationX(xOffset);
                float yOffset = random.nextFloat() * maxHeightOffset;
                if (random.nextBoolean()) {
                    yOffset *= -1;
                }
                cardView.setTranslationY(yOffset);

                // now animate them back into their natural position
                cardView.animate()
                        .translationY(0f)
                        .translationX(0f)
                        .alpha(1f)
                        .setInterpolator(interpolator)
                        .setDuration(1000)
                        .start();
            }

        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        DynamicHeightNetworkImageView thumbnailView;
        TextView titleView;
        TextView subtitleView;
        CardView cardView;

        ViewHolder(View view) {
            super(view);
            thumbnailView = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
            cardView = (CardView) view.findViewById(R.id.card_view);
        }
    }
}
