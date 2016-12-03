package com.abnd.mdiaz.popularmovies.utils;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.abnd.mdiaz.popularmovies.R;

public class MarginDecoration extends RecyclerView.ItemDecoration {

    private static final String TAG = MarginDecoration.class.getSimpleName();

    private Context mContext;

    public MarginDecoration(Context context) {
        mContext = context;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {

        /*
        This took way too much work to figure out... I mean, if anyone is using a GridLayoutManager...
        is it really that hard to add a bool parameter to center elements in the spans?
        */

        int cardWidth = mContext.getResources().getDimensionPixelSize(R.dimen.movie_list_card_width);
        int managerWidth = parent.getLayoutManager().getWidth();
        GridLayoutManager manager = (GridLayoutManager) parent.getLayoutManager();
        int managerSpans = manager.getSpanCount();

        int availableSpace = managerWidth - (cardWidth * managerSpans);
        int mTrueMargin = availableSpace / (managerSpans * 2);
        int cardPosition = parent.getChildAdapterPosition(view);

        double itemCount = parent.getAdapter().getItemCount();

        double bottomCalculation = itemCount / managerSpans;
        double fractionalPart = bottomCalculation % 1;
        double integralPart = bottomCalculation - fractionalPart;

        if (fractionalPart == 0 && ((cardPosition + 1) > (managerSpans * (integralPart - 1)))) {

            outRect.set(mTrueMargin, mTrueMargin / 2, mTrueMargin, mTrueMargin);

        } else if (fractionalPart > 0 && ((cardPosition + 1) > managerSpans * integralPart)) {

            outRect.set(mTrueMargin, mTrueMargin / 2, mTrueMargin, mTrueMargin);

        } else if (cardPosition + 1 > managerSpans) {

            outRect.set(mTrueMargin, mTrueMargin / 2, mTrueMargin, mTrueMargin / 2);

        } else {

            outRect.set(mTrueMargin, mTrueMargin, mTrueMargin, mTrueMargin / 2);

        }

    }
}
