package com.freeme.bigmodel.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.freeme.gallery.R;
import com.freeme.gallery.filtershow.filters.ImageFilterFx;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends BaseAdapter {
    public List<View> myv = new ArrayList<View>();
    /**
     * @author heqianqian
     * for filtershow adapter
     */
    private Context mContext;
    private List<Filterinfo> mList = new ArrayList<Filterinfo>();
    private RelativeLayout img_relayout;
    private int mSelectItem = 0;

    public MyAdapter(Context context) {
        this.mContext = context;

        mList.add(new Filterinfo(getbitmap(context, 0), context.getString(R.string.filternormal)));
        mList.add(new Filterinfo(getbitmap(context, 1), context.getString(R.string.filterchongya)));
        mList.add(new Filterinfo(getbitmap(context, 2), context.getString(R.string.filterold)));
        mList.add(new Filterinfo(getbitmap(context, 3), context.getString(R.string.filterbandw)));
        mList.add(new Filterinfo(getbitmap(context, 4), context.getString(R.string.filternremove)));
        mList.add(new Filterinfo(getbitmap(context, 5), context.getString(R.string.filteroutphoto)));
        mList.add(new Filterinfo(getbitmap(context, 6), context.getString(R.string.filtercoffee)));
        mList.add(new Filterinfo(getbitmap(context, 7), context.getString(R.string.filterblue)));
        mList.add(new Filterinfo(getbitmap(context, 8), context.getString(R.string.filterfuchong)));
    }

    public Bitmap getbitmap(Context context, int m) {
        Bitmap photo = BitmapFactory.decodeResource(context.getResources(), R.drawable.filternormal);
        ImageFilterFx fx = BigModeFilterHelper.getInstance(context).getParamFilter(m);
        Bitmap tempBitMap = photo.copy(Config.ARGB_8888, false);
        Bitmap ret = fx.apply(tempBitMap, 1.0f, 2);
        return ret;
    }

    public int getmSelectItem() {
        return mSelectItem;
    }

    public void setmSelectItem(int mSelectItem) {
        this.mSelectItem = mSelectItem;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HolderView holderView = null;
        View view = convertView;
        if (view == null) {
            holderView = new HolderView();
            view = LayoutInflater.from(mContext).inflate(R.layout.match_league_round_item, parent, false);
            img_relayout = (RelativeLayout) view.findViewById(R.id.img_relayout);
            holderView.imageView = (ImageView) view.findViewById(R.id.img_list_item);
            holderView.textView = (TextView) view.findViewById(R.id.text_list_item);
            view.setTag(holderView);
        } else {
            holderView = (HolderView) view.getTag();
        }
        img_relayout = (RelativeLayout) view.findViewById(R.id.img_relayout);
        if (position == mSelectItem) {
            img_relayout.setBackgroundResource(R.drawable.filterselect);
        } else {
            img_relayout.setBackgroundDrawable(null);
        }
        holderView.imageView.setImageBitmap(mList.get(position).filterId);
        holderView.imageView.setTag(position);
        holderView.textView.setText(mList.get(position).filterName);
        myv.add(view);

        return view;
    }

    class HolderView {
        ImageView imageView;
        TextView  textView;
    }


}
