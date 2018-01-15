package com.yzx.chat.view.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.contract.ContactContract;
import com.yzx.chat.presenter.ContactPresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.view.activity.FindNewContactActivity;
import com.yzx.chat.view.activity.ContactProfileActivity;
import com.yzx.chat.view.activity.RemarkInfoActivity;
import com.yzx.chat.widget.adapter.ContactAdapter;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.widget.adapter.ContactSearchAdapter;
import com.yzx.chat.widget.listener.AutoEnableOverScrollListener;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.util.AnimationUtil;
import com.yzx.chat.widget.view.BadgeImageView;
import com.yzx.chat.widget.view.IndexBarView;
import com.yzx.chat.widget.view.LetterSegmentationItemDecoration;
import com.yzx.chat.widget.animation.NoAnimations;
import com.yzx.chat.widget.view.OverflowMenuShowHelper;
import com.yzx.chat.widget.view.OverflowPopupMenu;
import com.yzx.chat.widget.view.SegmentedControlView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年06月28日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ContactFragment extends BaseFragment<ContactContract.Presenter> implements ContactContract.View {

    public static final String TAG = ContactFragment.class.getSimpleName();

    private RecyclerView mContactRecyclerView;
    private ContactAdapter mContactAdapter;
    private ContactSearchAdapter mSearchAdapter;
    private IndexBarView mIndexBarView;
    private AutoCompleteTextView mSearchHarderView;
    private TextView mTvIndexBarHint;
    private Toolbar mToolbar;
    private SmartRefreshLayout mSmartRefreshLayout;
    private SegmentedControlView mSegmentedControlView;
    private BadgeImageView mContactRequestBadge;
    private FloatingActionButton mFBtnAdd;
    private LinearLayoutManager mLinearLayoutManager;
    private AutoEnableOverScrollListener mAutoEnableOverScrollListener;
    private LetterSegmentationItemDecoration mLetterSegmentationItemDecoration;
    private OverflowPopupMenu mContactMenu;
    private List<ContactBean> mContactList;


    @Override
    protected int getLayoutID() {
        return R.layout.fragment_contact;
    }

    @Override
    protected void init(View parentView) {
        mToolbar = (Toolbar) parentView.findViewById(R.id.ContactFragment_mToolbar);
        mContactRecyclerView = (RecyclerView) parentView.findViewById(R.id.ContactFragment_mContactRecyclerView);
        mIndexBarView = (IndexBarView) parentView.findViewById(R.id.ContactFragment_mIndexBarView);
        mTvIndexBarHint = (TextView) parentView.findViewById(R.id.ContactFragment_mTvIndexBarHint);
        mFBtnAdd = (FloatingActionButton) parentView.findViewById(R.id.ContactFragment_mFBtnAdd);
        mContactRequestBadge = (BadgeImageView) parentView.findViewById(R.id.ContactFragment_mContactRequestBadge);
        mSegmentedControlView = (SegmentedControlView) parentView.findViewById(R.id.ContactFragment_mSegmentedControlView);
        mSmartRefreshLayout = (SmartRefreshLayout) parentView.findViewById(R.id.ContactFragment_mSmartRefreshLayout);
        mSearchHarderView = (AutoCompleteTextView) LayoutInflater.from(mContext).inflate(R.layout.item_contact_search, (ViewGroup) parentView, false);
        mContactMenu = new OverflowPopupMenu(mContext);
        mAutoEnableOverScrollListener = new AutoEnableOverScrollListener(mSmartRefreshLayout);
        mContactList = new ArrayList<>(128);
        mContactAdapter = new ContactAdapter(mContactList);
        mSearchAdapter = new ContactSearchAdapter(mContactList);
    }

    @Override
    protected void setView() {
        mToolbar.setTitle("微信");
        mToolbar.setTitleTextColor(Color.WHITE);

        mLetterSegmentationItemDecoration = new LetterSegmentationItemDecoration();
        mLetterSegmentationItemDecoration.setLineColor(ContextCompat.getColor(mContext, R.color.divider_color_black));
        mLetterSegmentationItemDecoration.setLineWidth(1);
        mLetterSegmentationItemDecoration.setTextColor(ContextCompat.getColor(mContext, R.color.divider_color_black));
        mLetterSegmentationItemDecoration.setTextSize(AndroidUtil.sp2px(16));

        mLinearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mContactRecyclerView.setLayoutManager(mLinearLayoutManager);
        mContactRecyclerView.setAdapter(mContactAdapter);
        mContactRecyclerView.setHasFixedSize(true);
        mContactRecyclerView.setItemAnimator(new NoAnimations());
        mContactRecyclerView.addItemDecoration(mLetterSegmentationItemDecoration);
        mContactRecyclerView.addOnScrollListener(mAutoEnableOverScrollListener);
        mContactRecyclerView.addOnItemTouchListener(mOnRecyclerViewItemClickListener);

        mContactRequestBadge.setBadgeTextPadding((int) AndroidUtil.dip2px(2));
        mContactRequestBadge.setBadgePadding(0, (int) AndroidUtil.dip2px(6), (int) AndroidUtil.dip2px(4), 0);
        mContactRequestBadge.setOnClickListener(mOnContactRequestBadgeClick);


        mIndexBarView.setSelectedTextColor(ContextCompat.getColor(mContext, R.color.text_secondary_color_black));
        mIndexBarView.setOnTouchSelectedListener(mIndexBarSelectedListener);

        mSegmentedControlView
                .setColors(Color.WHITE, ContextCompat.getColor(mContext, R.color.colorPrimary))
                .setItems(new String[]{"好友", "群组"})
                .setDefaultSelectedPosition(0)
                .setStretch(true)
                .update();

        setHarderView();
        setOverflowMenu();

    }

    private void setHarderView() {
        mSearchHarderView.setAdapter(mSearchAdapter);
        mSearchHarderView.setDropDownVerticalOffset((int) AndroidUtil.dip2px(8));
        mSearchHarderView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mOnRecyclerViewItemClickListener.onItemClick(position + 1, null);
                mSearchHarderView.setText(null);
            }
        });

        mContactAdapter.addHeaderView(mSearchHarderView);
    }

    private void setOverflowMenu() {
        mContactMenu.setWidth((int) AndroidUtil.dip2px(128));
        mContactMenu.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(mContext, R.color.theme_background_color)));
        mContactMenu.setElevation(AndroidUtil.dip2px(2));
        mContactMenu.inflate(R.menu.menu_contact_overflow);
        mContactMenu.setOnMenuItemClickListener(new OverflowPopupMenu.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, int menuID) {
                int index = (int) mContactRecyclerView.getTag();
                if (index < mContactList.size()) {
                    switch (menuID) {
                        case R.id.ContactMenu_UpdateRemarkInfo:
                            Intent intent = new Intent(mContext, RemarkInfoActivity.class);
                            intent.putExtra(RemarkInfoActivity.INTENT_EXTRA_CONTACT, mContactList.get(index));
                            startActivityForResult(intent,0);
                            break;
                    }
                }
            }
        });
    }

    @Override
    protected void onFirstVisible() {
        mPresenter.refreshAllContact(mContactList);
    }

    private final View.OnClickListener mOnContactRequestBadgeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(mContext, FindNewContactActivity.class));
        }
    };


    private final OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener = new OnRecyclerViewItemClickListener() {

        @Override
        public void onItemClick(final int position, RecyclerView.ViewHolder viewHolder) {
            if (position == 0 && mContactAdapter.isHasHeaderView()) {
                return;
            }
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(mContext, ContactProfileActivity.class);
                    intent.putExtra(ContactProfileActivity.INTENT_EXTRA_CONTACT, mContactList.get(position - 1));
                    ActivityOptionsCompat compat = ActivityOptionsCompat.makeCustomAnimation(mContext, R.anim.avtivity_slide_in_right, R.anim.activity_slide_out_left);
                    startActivity(intent, compat.toBundle());
                }
            });

        }

        @Override
        public void onItemLongClick(int position, RecyclerView.ViewHolder viewHolder, float touchX, float touchY) {
            if (position == 0 && mContactAdapter.isHasHeaderView()) {
                return;
            }
            OverflowMenuShowHelper.show(viewHolder.itemView, mContactMenu, mContactRecyclerView.getHeight(), (int) touchX, (int) touchY);
        }
    };


    private final IndexBarView.OnTouchSelectedListener mIndexBarSelectedListener = new IndexBarView.OnTouchSelectedListener() {
        @Override
        public void onSelected(int position, String text) {
            final int scrollPosition = mContactAdapter.findPositionByLetter(text);
            if (scrollPosition >= 0) {
                mContactRecyclerView.scrollToPosition(scrollPosition);
                mContactRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        int firstPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
                        if (scrollPosition > firstPosition) {
                            View childView = mContactRecyclerView.getChildAt(scrollPosition - firstPosition);
                            int scrollY = childView.getTop() - mLetterSegmentationItemDecoration.getSpace();
                            mContactRecyclerView.scrollBy(0, scrollY);
                        }
                    }
                });
            }
            if (mFBtnAdd.getTag() == null) {
                AnimationUtil.scaleAnim(mFBtnAdd, 0, 0, 300);
                AnimationUtil.scaleAnim(mTvIndexBarHint, 1f, 1f, 300);
                mFBtnAdd.setTag(true);
            }
            mTvIndexBarHint.setText(text);
            mAutoEnableOverScrollListener.setEnableOverScroll(false);

        }

        @Override
        public void onCancelSelected() {
            mFBtnAdd.setTag(null);
            AnimationUtil.scaleAnim(mTvIndexBarHint, 0, 0, 250);
            AnimationUtil.scaleAnim(mFBtnAdd, 1f, 1f, 250);
            mAutoEnableOverScrollListener.setEnableOverScroll(true);
        }

        @Override
        public void onMove(int offsetPixelsY) {
            int startOffset = mTvIndexBarHint.getHeight() / 2;
            if (startOffset > offsetPixelsY) {
                mTvIndexBarHint.setTranslationY(0);
            } else if (offsetPixelsY > mIndexBarView.getHeight() - startOffset) {
                mTvIndexBarHint.setTranslationY(mIndexBarView.getHeight() - startOffset * 2);
            } else {
                mTvIndexBarHint.setTranslationY(offsetPixelsY - startOffset);
            }
        }
    };

    @Override
    public ContactContract.Presenter getPresenter() {
        return new ContactPresenter();
    }

    @Override
    public void updateUnreadBadge(int unreadCount) {
        if (unreadCount == 0) {
            mContactRequestBadge.setBadgeMode(BadgeImageView.MODE_HIDE);
        } else {
            mContactRequestBadge.setBadgeText(unreadCount);
            mContactRequestBadge.setBadgeMode(BadgeImageView.MODE_SHOW);
        }
    }

    @Override
    public void updateContact(ContactBean contactBean) {
        int updatePosition = mContactList.indexOf(contactBean);
        if (updatePosition != -1) {
            mContactList.set(updatePosition, contactBean);
            mContactAdapter.notifyItemChangedEx(updatePosition);
        }
    }

    @Override
    public void updateContactListView(DiffUtil.DiffResult diffResult, List<ContactBean> newFriendList) {
        mContactList.clear();
        mContactList.addAll(newFriendList);
        diffResult.dispatchUpdatesTo(new BaseRecyclerViewAdapter.ListUpdateCallback(mContactAdapter));
    }
}
