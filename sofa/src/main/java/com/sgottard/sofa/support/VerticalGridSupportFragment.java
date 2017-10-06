package com.sgottard.sofa.support;

import com.sgottard.sofa.ContentFragment;
import com.sgottard.sofa.R;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.transition.TransitionHelper;
import android.support.v17.leanback.widget.BrowseFrameLayout;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnChildLaidOutListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.TitleView;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.support.v17.leanback.widget.VerticalGridView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A fragment for creating leanback vertical grids.
 *
 * <p>Renders a vertical grid of objects given a {@link VerticalGridPresenter} and
 * an {@link ObjectAdapter}.
 */
public class VerticalGridSupportFragment extends BaseSupportFragment implements ContentFragment {

    private static final String TAG = "VerticalGridSupportFragment";

    private ObjectAdapter mAdapter;
    private VerticalGridPresenter mGridPresenter;
    private VerticalGridPresenter.ViewHolder mGridViewHolder;
    private OnItemViewSelectedListener mOnItemViewSelectedListener;
    private OnItemViewClickedListener mOnItemViewClickedListener;
    private Object mSceneAfterEntranceTransition;
    private int mSelectedPosition = -1;

    private final OnItemViewSelectedListener mViewSelectedListener
            = new OnItemViewSelectedListener() {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                RowPresenter.ViewHolder rowViewHolder, Row row) {
            int position = mGridViewHolder.getGridView().getSelectedPosition();
            gridOnItemSelected(position);
            if (mOnItemViewSelectedListener != null) {
                mOnItemViewSelectedListener.onItemSelected(itemViewHolder, item, rowViewHolder,
                        row);
            }
        }
    };

    private final OnChildLaidOutListener mChildLaidOutListener = new OnChildLaidOutListener() {
        @Override
        public void onChildLaidOut(ViewGroup parent, View view, int position, long id) {
            if (position == 0 && !isAttachedToBrowseFragment()) {
                showOrHideTitle();
            }
        }
    };

    /**
     * Sets the grid presenter.
     */
    public void setGridPresenter(VerticalGridPresenter gridPresenter) {
        if (gridPresenter == null) {
            throw new IllegalArgumentException("Grid presenter may not be null");
        }
        mGridPresenter = gridPresenter;
        mGridPresenter.setOnItemViewSelectedListener(mViewSelectedListener);
        if (mOnItemViewClickedListener != null) {
            mGridPresenter.setOnItemViewClickedListener(mOnItemViewClickedListener);
        }
    }

    /**
     * Returns the grid presenter.
     */
    public VerticalGridPresenter getGridPresenter() {
        return mGridPresenter;
    }

    /**
     * Sets the object adapter for the fragment.
     */
    public void setAdapter(ObjectAdapter adapter) {
        mAdapter = adapter;
        updateAdapter();
    }

    /**
     * Returns the object adapter.
     */
    public ObjectAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * Sets an item selection listener.
     */
    public void setOnItemViewSelectedListener(OnItemViewSelectedListener listener) {
        mOnItemViewSelectedListener = listener;
    }

    private void gridOnItemSelected(int position) {
        if (position != mSelectedPosition) {
            mSelectedPosition = position;
            if (!isAttachedToBrowseFragment())
                showOrHideTitle();
        }
    }

    @Nullable
    public VerticalGridView getVerticalGridView() {
        return mGridViewHolder == null ? null : mGridViewHolder.getGridView();
    }

    public int getSelectedRow() {
        return (mGridViewHolder == null || mGridPresenter == null) ? 0
                : mGridViewHolder.getGridView().getSelectedPosition()
                        / mGridPresenter.getNumberOfColumns();
    }

    public int getSelectedColumn() {
        return (mGridViewHolder == null || mGridPresenter == null) ? 0
                : mGridViewHolder.getGridView().getSelectedPosition()
                        % mGridPresenter.getNumberOfColumns();
    }

    public int getFirstPositionInRow(int row) {
        return (mGridViewHolder == null || mGridPresenter == null) ? 0
                : row * mGridPresenter.getNumberOfColumns();
    }

    private void showOrHideTitle() {
        if (mGridViewHolder.getGridView().findViewHolderForAdapterPosition(mSelectedPosition)
                == null) {
            return;
        }
        if (!mGridViewHolder.getGridView().hasPreviousViewInSameRow(mSelectedPosition)) {
            showTitle(true);
        } else {
            showTitle(false);
        }
    }

    /**
     * Sets an item clicked listener.
     */
    public void setOnItemViewClickedListener(OnItemViewClickedListener listener) {
        mOnItemViewClickedListener = listener;
        if (mGridPresenter != null) {
            mGridPresenter.setOnItemViewClickedListener(mOnItemViewClickedListener);
        }
    }

    /**
     * Returns the item clicked listener.
     */
    public OnItemViewClickedListener getOnItemViewClickedListener() {
        return mOnItemViewClickedListener;
    }

    protected boolean isAttachedToBrowseFragment() {
        return getParentFragment() != null && getParentFragment() instanceof BrowseSupportFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.lb_vertical_grid_fragment, container,
                false);
        setTitleView(isAttachedToBrowseFragment() ? null
                : (TitleView) root.findViewById(R.id.browse_title_group));
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup gridDock = (ViewGroup) view.findViewById(R.id.browse_grid_dock);
        mGridViewHolder = mGridPresenter.onCreateViewHolder(gridDock);
        gridDock.addView(mGridViewHolder.view);
        mGridViewHolder.getGridView().setOnChildLaidOutListener(mChildLaidOutListener);

        mSceneAfterEntranceTransition = TransitionHelper.createScene(gridDock, new Runnable() {
            @Override
            public void run() {
                setEntranceTransitionState(true);
            }
        });

        updateAdapter();
    }

    private void setupFocusSearchListener() {
        BrowseFrameLayout browseFrameLayout = (BrowseFrameLayout) getView().findViewById(
                R.id.grid_frame);
        if (browseFrameLayout != null && getTitleHelper() != null)
            browseFrameLayout.setOnFocusSearchListener(getTitleHelper().getOnFocusSearchListener());
    }

    @Override
    public void onStart() {
        super.onStart();
        setupFocusSearchListener();
        if (isEntranceTransitionEnabled()) {
            setEntranceTransitionState(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mGridViewHolder = null;
    }

    /**
     * Sets the selected item position.
     */
    public void setSelectedPosition(int position) {
        setSelectedPosition(position, true);
    }

    public void setSelectedPosition(int position, boolean smooth) {
        mSelectedPosition = position;
        if (mGridViewHolder != null && mGridViewHolder.getGridView().getAdapter() != null) {
            if (smooth) {
                mGridViewHolder.getGridView().setSelectedPositionSmooth(position);
            } else {
                mGridViewHolder.getGridView().setSelectedPosition(position);
            }
        }
    }

    private void updateAdapter() {
        if (mGridViewHolder != null) {
            mGridPresenter.onBindViewHolder(mGridViewHolder, mAdapter);
            if (mSelectedPosition != -1) {
                mGridViewHolder.getGridView().setSelectedPosition(mSelectedPosition);
            }
        }
    }

    @Override
    protected Object createEntranceTransition() {
        return TransitionHelper.loadTransition(getActivity(),
                R.transition.lb_vertical_grid_entrance_transition);
    }

    @Override
    protected void runEntranceTransition(Object entranceTransition) {
        TransitionHelper.runTransition(mSceneAfterEntranceTransition, entranceTransition);
    }

    void setEntranceTransitionState(boolean afterTransition) {
        mGridPresenter.setEntranceTransitionState(mGridViewHolder, afterTransition);
    }

    @Override
    public boolean isScrolling() {
        return false;
    }

    @Override
    public View getFocusRootView() {
        return null;
    }

    @Override
    public void setExtraMargin(int marginTop, int marginLeft) {

    }
}