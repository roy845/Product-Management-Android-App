package com.example.myapplication.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.constants.Constants;

abstract public class SwipeToDeleteCallback extends ItemTouchHelper.Callback {

    private final Context mContext;
    private final Paint mClearPaint;
    private final ColorDrawable mBackground;
    private final int deleteBackgroundColor;
    private final int archiveBackgroundColor;
    private final Drawable deleteDrawable;
    private final Drawable archiveDrawable;
    private final int intrinsicWidth;
    private final int intrinsicHeight;

    public SwipeToDeleteCallback(Context context) {
        mContext = context;
        mBackground = new ColorDrawable();
        deleteBackgroundColor = Color.parseColor(Constants.SWIPE_DELETE_COLOR);  // Color for delete swipe
        archiveBackgroundColor = Color.parseColor(Constants.SWIPE_ARCHIVE_COLOR); // Dark yellow for archive swipe
        mClearPaint = new Paint();
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        deleteDrawable = ContextCompat.getDrawable(mContext, R.drawable.baseline_delete_24);  // Delete icon
        archiveDrawable = ContextCompat.getDrawable(mContext, R.drawable.baseline_archive_24); // Archive icon

        intrinsicWidth = deleteDrawable.getIntrinsicWidth();
        intrinsicHeight = deleteDrawable.getIntrinsicHeight();
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;  // Enable both left and right swipe
        return makeMovementFlags(0, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getHeight();

        boolean isCancelled = dX == 0 && !isCurrentlyActive;

        if (isCancelled) {
            clearCanvas(c, itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, false);
            return;
        }

        // Determine if we're swiping left or right
        if (dX < 0) {
            // Swiping left: Show delete background and icon
            mBackground.setColor(deleteBackgroundColor);
            mBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
            mBackground.draw(c);

            int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
            int deleteIconMargin = (itemHeight - intrinsicHeight) / 2;
            int deleteIconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
            int deleteIconRight = itemView.getRight() - deleteIconMargin;
            int deleteIconBottom = deleteIconTop + intrinsicHeight;

            deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
            deleteDrawable.draw(c);

        } else if (dX > 0) {
            // Swiping right: Show archive background and icon
            mBackground.setColor(archiveBackgroundColor);
            mBackground.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + (int) dX, itemView.getBottom());
            mBackground.draw(c);

            int archiveIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
            int archiveIconMargin = (itemHeight - intrinsicHeight) / 2;
            int archiveIconLeft = itemView.getLeft() + archiveIconMargin;
            int archiveIconRight = itemView.getLeft() + archiveIconMargin + intrinsicWidth;
            int archiveIconBottom = archiveIconTop + intrinsicHeight;

            archiveDrawable.setBounds(archiveIconLeft, archiveIconTop, archiveIconRight, archiveIconBottom);
            archiveDrawable.draw(c);
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void clearCanvas(Canvas c, Float left, Float top, Float right, Float bottom) {
        c.drawRect(left, top, right, bottom, mClearPaint);
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.7f;
    }
}
