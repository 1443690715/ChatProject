package com.yzx.chat.view.fragment;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.PopupMenuCompat;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.yzx.chat.R;
import com.yzx.chat.contract.ConversationContract;
import com.yzx.chat.presenter.ConversationPresenter;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.LogUtil;
import com.yzx.chat.view.activity.ChatActivity;
import com.yzx.chat.widget.adapter.ConversationAdapter;
import com.yzx.chat.base.BaseFragment;
import com.yzx.chat.widget.adapter.ConversationMenuAdapter;
import com.yzx.chat.widget.listener.AutoEnableOverScrollListener;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.OverflowPopupMenu;

import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.model.Conversation;

/**
 * Created by YZX on 2017年06月03日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */
public class ConversationFragment extends BaseFragment<ConversationContract.Presenter> implements ConversationContract.View {

    public static final String TAG = ConversationFragment.class.getSimpleName();

    private static final int ACTIVITY_REQUEST_CODE = 10000;

    private RecyclerView mRecyclerView;
    private SmartRefreshLayout mSmartRefreshLayout;
    private ImageView mIvEmptyHintImage;
    private TextView mITvEmptyHintText;
    private ConversationAdapter mAdapter;
    private Toolbar mToolbar;
    private OverflowPopupMenu mConversationMenu;
    private AutoEnableOverScrollListener mAutoEnableOverScrollListener;
    private List<Conversation> mConversationList;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_conversation;
    }

    @Override
    protected void init(View parentView) {
        mRecyclerView = parentView.findViewById(R.id.ConversationFragment_mRecyclerView);
        mToolbar = parentView.findViewById(R.id.ConversationFragment_mToolbar);
        mSmartRefreshLayout = parentView.findViewById(R.id.ConversationFragment_mSmartRefreshLayout);
        mIvEmptyHintImage = parentView.findViewById(R.id.ConversationFragment_mIvEmptyHintImage);
        mITvEmptyHintText = parentView.findViewById(R.id.ConversationFragment_mITvEmptyHintText);
        mConversationMenu = new OverflowPopupMenu(mContext);
        mConversationList = new ArrayList<>(128);
        mAdapter = new ConversationAdapter(mConversationList);
        mAutoEnableOverScrollListener = new AutoEnableOverScrollListener(mSmartRefreshLayout);
    }

    @Override
    protected void setView() {
        mToolbar.setTitle("微信");
        mToolbar.inflateMenu(R.menu.menu_home_top);
        mToolbar.setOnMenuItemClickListener(onOptionsItemSelectedListener);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnItemTouchListener(mOnRecyclerViewItemClickListener);
        mRecyclerView.addOnScrollListener(mAutoEnableOverScrollListener);

        setOverflowMenu();

    }

    private void setOverflowMenu() {
        mConversationMenu.setWidth((int) AndroidUtil.dip2px(154));
        mConversationMenu.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(mContext, R.color.theme_main_color)));
    }

    @Override
    protected void onFirstVisible() {
        mPresenter.refreshAllConversations();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != ACTIVITY_REQUEST_CODE || resultCode != ChatActivity.ACTIVITY_RESPONSE_CODE) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            if (data != null) {
                Conversation conversation = data.getParcelableExtra(ChatActivity.INTENT_EXTRA_CONVERSATION);
                if (conversation != null) {
                    mPresenter.refreshSingleConversation(conversation.getConversationType(), conversation.getTargetId());
                } else {
                    LogUtil.e("Conversation is null");
                }
            } else {
                LogUtil.e("Intent is null");
            }
        }
    }

    private void enableEmptyListHint(boolean isEnable) {
        if (isEnable) {
            mIvEmptyHintImage.setVisibility(View.VISIBLE);
            mITvEmptyHintText.setVisibility(View.VISIBLE);
        } else {
            mIvEmptyHintImage.setVisibility(View.INVISIBLE);
            mITvEmptyHintText.setVisibility(View.INVISIBLE);
        }
    }

    private final Toolbar.OnMenuItemClickListener onOptionsItemSelectedListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.HomeMenu_more:
                    //mOverflowPopupWindow.show();
                    break;
                default:
                    return false;
            }
            return true;
        }
    };

    private final OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(final int position, RecyclerView.ViewHolder viewHolder) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(mContext, ChatActivity.class);
                    intent.putExtra(ChatActivity.INTENT_EXTRA_CONVERSATION, mConversationList.get(position));
                    ActivityOptionsCompat compat = ActivityOptionsCompat.makeCustomAnimation(mContext, R.anim.avtivity_slide_in_right, R.anim.activity_slide_out_left);
                    startActivityForResult(intent, ACTIVITY_REQUEST_CODE, compat.toBundle());
                }
            });
        }

        @Override
        public void onItemLongClick(int position, RecyclerView.ViewHolder viewHolder,float touchX,float touchY) {
            View anchorView = viewHolder.itemView;
            
            if(AndroidUtil.getScreenWidth()/2>touchX){
                mConversationMenu.setDropDownGravity(Gravity.END);
                mConversationMenu.setHorizontalOffset((int) -(AndroidUtil.getScreenWidth()-touchX-mConversationMenu.getWidth()));
            }else {
                mConversationMenu.setDropDownGravity(Gravity.START);
                mConversationMenu.setHorizontalOffset((int) (touchX-mConversationMenu.getWidth()));
            }

            mConversationMenu.inflate(R.menu.menu_home_top);
            mConversationMenu.setAnchorView(anchorView);
            mConversationMenu.setVerticalOffset((int) (touchY-anchorView.getTop()));
            mConversationMenu.show();

        }

    };

    @Override
    public ConversationContract.Presenter getPresenter() {
        return new ConversationPresenter();
    }


    @Override
    public void updateConversationListView(DiffUtil.DiffResult diffResult, List<Conversation> newConversationList) {
        diffResult.dispatchUpdatesTo(mAdapter);
        mConversationList.clear();
        mConversationList.addAll(newConversationList);
        enableEmptyListHint(mConversationList.size() == 0);
    }

    @Override
    public void updateConversationListViewByPosition(int position, Conversation conversation) {
        if (position == 0) {
            mConversationList.set(0, conversation);
        } else {
            mConversationList.remove(position);
            mConversationList.add(0, conversation);
            mAdapter.notifyItemMoved(position, 0);
        }
        mAdapter.notifyItemChanged(0);
    }

    @Override
    public void addConversationView(Conversation conversation) {
        mConversationList.add(0, conversation);
        mAdapter.notifyItemInserted(0);
    }
}
