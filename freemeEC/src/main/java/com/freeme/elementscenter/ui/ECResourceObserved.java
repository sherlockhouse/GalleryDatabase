package com.freeme.elementscenter.ui;

import java.util.ArrayList;
import java.util.List;

public class ECResourceObserved {
    private static ECResourceObserved mObserved;
    private List<DataDeleteListener> mList = new ArrayList<DataDeleteListener>();

    public interface DataDeleteListener {
        public void onDataDeleted(List<ECItemData> dataList);
    }

    public void registerListener(DataDeleteListener listener) {
        mList.add(listener);
    }

    public void unregisterListener(DataDeleteListener listener) {
        mList.remove(listener);
    }

    public void notifyDataDelete(List<ECItemData> dataList) {
        for (DataDeleteListener listener : mList) {
            listener.onDataDeleted(dataList);
        }
    }

    public static ECResourceObserved getInstance() {
        if (mObserved == null) {
            mObserved = new ECResourceObserved();
        }
        return mObserved;
    }
}
