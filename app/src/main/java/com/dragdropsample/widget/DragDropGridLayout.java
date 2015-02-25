package com.dragdropsample.widget;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.support.v7.widget.GridLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.dragdropsample.R;

import java.util.ArrayList;
import java.util.List;

public class DragDropGridLayout extends GridLayout implements View.OnLongClickListener, View.OnDragListener {

    private List<View> childs;

    public DragDropGridLayout(Context context) {
        this(context, null);
    }

    public DragDropGridLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragDropGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        childs = new ArrayList<>();
        for (View child : getAllChildrenBFS(this)) {
            if (child instanceof ImageView) {
                child.setOnLongClickListener(this);
                child.setOnDragListener(this);
                child.setTag("yo");
                childs.add(child);
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
        ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
        ClipData dragData = new ClipData(v.getTag().toString(), mimeTypes, item);

        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
        v.startDrag(dragData, shadowBuilder, v, 0);
        v.setVisibility(View.INVISIBLE);
        return true;
    }

    @Override
    public boolean onDrag(View dropView, DragEvent event) {
        final View dragView = (View) event.getLocalState();
        switch (event.getAction()) {
            case DragEvent.ACTION_DROP:
                clearShake(dropView);
                swapView(dragView, dropView);
                return true;

            case DragEvent.ACTION_DRAG_STARTED:
                setVisibility(dragView, false);
                return true;

            case DragEvent.ACTION_DRAG_ENDED:
                setVisibility(dragView, true);
                return true;

            case DragEvent.ACTION_DRAG_ENTERED:
                shakeView(dropView);
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                clearShake(dropView);
                return true;

            case DragEvent.ACTION_DRAG_LOCATION:
                return true;

            default:
                Log.e("DragDrop Example", "Unknown action type received by OnDragListener.");
                return false;
        }
    }

    private void setVisibility(final View v, final boolean isVisible) {
        v.post(new Runnable() {
            @Override
            public void run() {
                v.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    private void swapView(View dragView, View dropView) {
        LayoutParams dragParams = (LayoutParams) dragView.getLayoutParams();
        LayoutParams dropParams = (LayoutParams) dropView.getLayoutParams();

        int dragIndex = indexOfChild(dragView);
        int dropIndex = indexOfChild(dropView);

        dragView.setLayoutParams(dropParams);
        dropView.setLayoutParams(dragParams);

        childs.remove(dragIndex);
        childs.add(dragIndex, dropView);

        childs.remove(dropIndex);
        childs.add(dropIndex, dragView);

        removeAllViews();
        for (View child : childs) {
            addView(child);
        }
    }

    public void shakeView(View v) {
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
        v.startAnimation(animation);
    }

    public void clearShake(View v) {
        v.clearAnimation();
    }

    private List<View> getAllChildrenBFS(View v) {
        List<View> visited = new ArrayList<View>();
        List<View> unvisited = new ArrayList<View>();
        unvisited.add(v);

        while (!unvisited.isEmpty()) {
            View child = unvisited.remove(0);
            visited.add(child);
            if (!(child instanceof ViewGroup)) continue;
            ViewGroup group = (ViewGroup) child;
            final int childCount = group.getChildCount();
            for (int i = 0; i < childCount; i++) unvisited.add(group.getChildAt(i));
        }

        return visited;
    }
}
