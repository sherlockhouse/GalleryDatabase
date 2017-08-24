package com.freeme.community.push;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.freeme.community.manager.ImageLoadManager;
import com.freeme.community.utils.DateUtil;
import com.freeme.community.view.CircleImageView;
import com.freeme.gallery.R;

public class DroiPushListAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private int mResourceId;
    private int colorTitle;
    private int colorSummary;

    private ImageLoadManager mImgLoader;

    public DroiPushListAdapter(Context context, int resourceId) {
        mContext = context;
        mResourceId = resourceId;

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImgLoader = ImageLoadManager.getInstance(mContext);

        colorTitle = context.getResources().getColor(R.color.droi_push_item_title);
        colorSummary = context.getResources().getColor(R.color.droi_push_item_summary);
    }

    @Override
    public int getCount() {
        return DroiPushManager.getInstance(mContext.getApplicationContext())
                .getPushMessageList().size();
    }

    @Override
    public PushMessage getItem(int position) {
        return DroiPushManager.getInstance(mContext.getApplicationContext())
                .getPushMessageList().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(mResourceId, parent, false);
            holder.user_thumbnail = (CircleImageView) convertView.findViewById(R.id.user_thumbnail);
            holder.content_thumbnail = (ImageView) convertView.findViewById(R.id.content_thumbnail);
            holder.new_message = (ImageView) convertView.findViewById(R.id.new_message);
            holder.type = (ImageView) convertView.findViewById(R.id.type);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.datetime = (TextView) convertView.findViewById(R.id.datetime);
            holder.summary = (TextView) convertView.findViewById(R.id.summary);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

//        holder.user_thumbnail.setImageBitmap(null);
        holder.user_thumbnail.setImageResource(R.drawable.default_user_icon);
//        holder.user_thumbnail.setImageBitmap(getItem(position).getBitmapAvatar());
        holder.content_thumbnail.setImageBitmap(null);
//        holder.content_thumbnail.setImageBitmap(getItem(position).getBitmapSmall());

        mImgLoader.displayImage(getItem(position).getSmallUrl(), ImageLoadManager.OPTIONS_TYPE_USERICON,
                holder.content_thumbnail, R.drawable.default_image_small);
        mImgLoader.displayImage(getItem(position).getAvatarUrl(), ImageLoadManager.OPTIONS_TYPE_USERICON,
                holder.user_thumbnail, R.drawable.default_user_icon);

        holder.type.setImageBitmap(null);
        if (getItem(position).getType() == DroiPushManager.TYPE_THUMB) {
            holder.type.setImageResource(R.drawable.ic_thumbs_normal);
        } else {
            holder.type.setImageResource(R.drawable.ic_comment_normal);
        }

        holder.title.setText(null);
        holder.title.setTextColor(colorTitle);
        holder.title.setText(getItem(position).getNickname());

        holder.datetime.setText(null);
        holder.datetime.setText(DateUtil.reFormatDate(getItem(position).getDateTime(), DateUtil.dateFormatHM));
        holder.datetime.setTextColor(colorSummary);

        holder.summary.setText(null);
        holder.summary.setTextColor(colorSummary);
        holder.summary.setText(getItem(position).getSummary());
        if (PushMessage.contains(DroiPushManager.getInstance(mContext.getApplicationContext())
                .getNewMessageIdList(), getItem(position))) {
            holder.new_message.setVisibility(View.VISIBLE);
        } else {
            holder.new_message.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    static class ViewHolder {
        CircleImageView user_thumbnail;
        ImageView content_thumbnail;
        ImageView new_message;
        ImageView type;
        TextView title;
        TextView datetime;
        TextView summary;
    }
}
