
package com.freeme.elementscenter.ui;

import java.util.ArrayList;
import java.util.List;
import com.freeme.elementscenter.data.ECUtil;
import com.freeme.elementscenter.R;
import android.app.ActionBar;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;

public class ECResourceManager extends ECBackHandledFragment {
    private boolean hadIntercept;
    private View mContent;
    private GridView mGridView;
    private boolean mIsEditMode;
    private List<ECItemData> mDataList;
    private ECResourceAdapter mAdapter;
    private View mPrompt;
    private View mBody;
    private boolean mIsShowPrompt;
    private MenuItem mEditMenuItem;
    private MenuItem mDeleteMenuItem;
    private MenuItem mSelectMenuItem;
    private ActionBar mActionBar;
    private List<String> mMarkedList;
    private boolean mIsChildMode;

    private void handleSettingPrompt(int pos) {
        if (mAdapter == null || mGridView == null || pos < 0 || pos >= mDataList.size()) {
            return;
        }
        ECItemData item = mDataList.get(pos);
        if (item.mTypeCode == ECUtil.JIGSAW_TYPE_CODE) {
            ECFragmentUtil.showMessageDailog(getActivity(), "请到相册拼图设置界面中设置");
        } else {
            ECFragmentUtil.showMessageDailog(getActivity(), "请到相机设置界面中设置");
        }
    }

    public void allOrNoneMarkedToggle() {
        // do mark *none*
        if (mMarkedList.size() == mDataList.size()) {
            synchronized (mMarkedList) {
                mMarkedList.clear();
            }
            if (mSelectMenuItem != null) {
                mSelectMenuItem.setIcon(R.drawable.action_all_selected);
            }
            mDeleteMenuItem.setVisible(false);
            // do mark *all*
        } else {
            synchronized (mMarkedList) {
                mMarkedList.clear();
                for (ECItemData data : mDataList) {
                    mMarkedList.add(data.mCode);
                }
            }
            if (mSelectMenuItem != null) {
                mSelectMenuItem.setIcon(R.drawable.action_clear_selected);
            }
            mDeleteMenuItem.setVisible(true);
        }
        String title = "";
        int selectCnt = mMarkedList.size();
        if (mMarkedList.size() > 1) {
            title = getResources().getString(R.string.markup_btn_delete_onemore, new Object[] {
                selectCnt
            });
        } else {
            title = getResources().getString(R.string.markup_btn_delete_one_or_zero, new Object[] {
                selectCnt
            });
        }
        mActionBar.setTitle(title);
        mAdapter.notifyDataSetChanged();
    }

    private synchronized void handleDeleteConfirm() {
        List<ECItemData> deleteList = new ArrayList<ECItemData>();
        List<ECItemData> remainedList = new ArrayList<ECItemData>();
        for (ECItemData data : mDataList) {
            if (mMarkedList.contains(data.mCode)) {
                deleteList.add(data);
            } else {
                remainedList.add(data);
            }
        }
        mDataList.clear();
        mDataList = null;
        mDataList = remainedList;
        mMarkedList.clear();
        for (int i = 0; i < deleteList.size(); i++) {
            ECItemData item = deleteList.get(i);
            ECUtil.removeItem(item);
        }
        if (deleteList.size() > 0) {
            ECResourceObserved.getInstance().notifyDataDelete(deleteList);
        }
        if (mIsEditMode) {
            exitEditMode();
        }
    }

    private void updateSelectedState(boolean isNull, boolean isSelected, String code) {
        if (!isNull) {
            synchronized (mMarkedList) {
                if (isSelected) {
                    if (!mMarkedList.contains(code)) {
                        mMarkedList.add(code);
                    }
                } else {
                    if (mMarkedList.contains(code)) {
                        mMarkedList.remove(code);
                    }
                }
            }
        }
        String title = "";
        int selectCnt = mMarkedList.size();
        if (mMarkedList.size() > 1) {
            title = getResources().getString(R.string.markup_btn_delete_onemore, new Object[] {
                selectCnt
            });
        } else {
            title = getResources().getString(R.string.markup_btn_delete_one_or_zero, new Object[] {
                selectCnt
            });
        }
        mActionBar.setTitle(title);
        if (selectCnt == mDataList.size()) {
            if (mSelectMenuItem != null) {
                mSelectMenuItem.setIcon(R.drawable.action_clear_selected);
            }

        } else {
            if (mSelectMenuItem != null) {
                mSelectMenuItem.setIcon(R.drawable.action_all_selected);
            }
        }
        if (selectCnt == 0) {
            mDeleteMenuItem.setVisible(false);
        } else {
            mDeleteMenuItem.setVisible(true);
        }
        mAdapter.setDatas(mDataList, mMarkedList, mIsEditMode);
    }

    private void changedItemSelected(int pos, boolean selected) {
        if (mAdapter == null || mGridView == null || pos < 0 || pos >= mDataList.size()) {
            return;
        }
        ECItemData item = mDataList.get(pos);
        updateSelectedState(false, selected, item.mCode);
    }

    private void entryEditMode(int pos) {
        if (mAdapter == null || mGridView == null || pos < 0 || pos >= mDataList.size()) {
            return;
        }
        mEditMenuItem.setVisible(false);
        mSelectMenuItem.setVisible(true);
        mIsEditMode = true;
        changedItemSelected(pos, true);
    }

    private void entryEditMode() {
        mEditMenuItem.setVisible(false);
        mSelectMenuItem.setVisible(true);
        mIsEditMode = true;
        updateSelectedState(true, false, "");
    }

    private void exitEditMode() {
        if (mGridView == null || mAdapter == null) {
            return;
        }
        mIsEditMode = false;
        mActionBar.setTitle(R.string.ec_titlebar_right);
        if (mDataList == null || mDataList.isEmpty()) {
            mEditMenuItem.setVisible(false);
        } else {
            mEditMenuItem.setVisible(true);
        }
        mSelectMenuItem.setVisible(false);
        mDeleteMenuItem.setVisible(false);
        synchronized (mMarkedList) {
            mMarkedList.clear();
        }
        mAdapter.setDatas(mDataList, mMarkedList, mIsEditMode);
        showNotPrompt();
    }

    private void initGridView() {
        mGridView = (GridView) mContent.findViewById(R.id.ec_gridview);
        mAdapter = new ECResourceAdapter(getActivity());
        mAdapter.setDatas(mDataList, mMarkedList, false);
        mAdapter.setIsChildMode(mIsChildMode);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (!mIsEditMode) {
                    entryEditMode(arg2);
                }
                return true;
            }
        });
        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (mIsEditMode) {
                    CheckBox box = (CheckBox) arg1.findViewById(R.id.ec_check);
                    changedItemSelected(arg2, !box.isChecked());
                } else {
                    // handleSettingPrompt(arg2);
                }

            }
        });
    }

    private void initPromptAndBody() {
        mPrompt = mContent.findViewById(R.id.ec_resouce_prompt);
        mBody = mContent.findViewById(R.id.ec_resource_body);
    }

    private void showNotPrompt() {
        if (mDataList == null || mDataList.isEmpty()) {
            mIsShowPrompt = true;
        } else {
            mIsShowPrompt = false;
        }
        if (mIsShowPrompt) {
            mPrompt.setVisibility(View.VISIBLE);
            mBody.setVisibility(View.GONE);
        } else {
            mPrompt.setVisibility(View.GONE);
            mBody.setVisibility(View.VISIBLE);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar = getActivity().getActionBar();
        mMarkedList = new ArrayList<String>();
        mDataList = (List<ECItemData>) getArguments().getSerializable("itemDataList");
        mIsChildMode = false;
        if (mDataList != null && mDataList.size() > 0) {
            mIsChildMode = (mDataList.get(0).mTypeCode == ECUtil.CHILDMODE_TYPE_CODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActionBar.setTitle(R.string.ec_titlebar_right);
        setHasOptionsMenu(true);
        mContent = inflater.inflate(R.layout.ec_resource_manager, container, false);
        mContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
            }
        });
        initGridView();
        initPromptAndBody();
        showNotPrompt();
        return mContent;
    }

    @Override
    public boolean onBackPressed() {
        if (mIsEditMode) {
            exitEditMode();
            return true;
        }
        if (hadIntercept) {
            return false;
        } else {
            hadIntercept = true;
            ECFragmentUtil.popFragment(getActivity());
            return true;
        }
    }

    void handleBack() {
        if (mIsEditMode) {
            exitEditMode();
        } else {
            ECFragmentUtil.popFragment(getActivity());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
        mEditMenuItem = menu.findItem(R.id.action_edit);
        mDeleteMenuItem = menu.findItem(R.id.action_delete);
        mSelectMenuItem = menu.findItem(R.id.action_select);
        if (mDataList.size() > 0) {
            mEditMenuItem.setVisible(true);
        } else {
            mEditMenuItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        boolean isHandle = false;
        if (id == R.id.action_edit) {
            entryEditMode();
            isHandle = true;
        } else if (id == R.id.action_delete) {
            handleDeleteConfirm();
            isHandle = true;
        } else if (id == R.id.action_select) {
            allOrNoneMarkedToggle();
            isHandle = true;
        } else if (id == android.R.id.home) {
            handleBack();
            isHandle = true;
        }
        if (isHandle) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
