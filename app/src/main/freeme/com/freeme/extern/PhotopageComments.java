/*
 * File name: PhotoPageCommentsControls.java
 * 
 * Description: the comments layout
 *
 * Author: Theobald_zhang, contact with zhangmingjun@tydtech.com
 * 
 * Date: 2015-04-24   
 * 
 * Copyright (C) 2014 TYD Technology Co.,Ltd.
 * 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.freeme.extern;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.freeme.gallery.R;
import com.freeme.gallery.data.Path;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class PhotopageComments {
    private ViewGroup mParentLayout;
    private TextView  mInfoText;
    private TextView  mCommenttime;
    private String    time;
    private Context   mContext;
    private ViewGroup mContainertext;
    private String    id;
    private int       mType;

    public PhotopageComments(Context context,
                             ViewGroup layout, Path path, int type) {
        mContext = context;
        mParentLayout = layout;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContainertext = (ViewGroup) inflater.inflate(
                R.layout.photopage_comments, mParentLayout, false);
        mParentLayout.addView(mContainertext);

        mInfoText = (TextView) mContainertext.findViewById(R.id.button1);
        mCommenttime = (TextView) mContainertext
                .findViewById(R.id.comment_time);
        time = getTime();
        updateText(path, type);
        mContainertext.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                LayoutInflater inflater = LayoutInflater.from(mContext);
                final View textEntryView = inflater.inflate(R.layout.freeme_alert_custom, null);
                final AlertDialog dlg = new AlertDialog.Builder(
                        new ContextThemeWrapper(mContext,
                                android.R.style.Theme_Holo_Light)).create();
                dlg.setView(textEntryView);
                dlg.show();
                Button mCancel = (Button) textEntryView
                        .findViewById(R.id.cancel);
                mCancel.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dlg.cancel();
                    }
                });

                final EditText mInfoEdit = (EditText) textEntryView
                        .findViewById(R.id.detail_text);
                if (!mInfoText.getText().equals(
                        mContext.getResources().getString(
                                R.string.freeme_comment_comments))) {
                    mInfoEdit.setText(mInfoText.getText());
                }
                mInfoEdit.setFocusable(true);
                mInfoEdit.setFocusableInTouchMode(true);
                mInfoEdit.requestFocus();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    public void run() {
                        InputMethodManager inputManager = (InputMethodManager) mInfoEdit
                                .getContext().getSystemService(
                                        Context.INPUT_METHOD_SERVICE);
                        inputManager.showSoftInput(mInfoEdit, 0);
                    }
                }, 500);
                mInfoEdit.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1,
                                                  int arg2, int arg3) {

                    }

                    @Override
                    public void onTextChanged(CharSequence arg0, int arg1,
                                              int arg2, int arg3) {

                    }

                    @Override
                    public void afterTextChanged(Editable arg0) {
                        if (arg0.length() >= 50) {
                            Toast.makeText(mContext,
                                    R.string.freeme_comment_count_limit,
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                Button mContinue = (Button) textEntryView
                        .findViewById(R.id.mcontinue);
                mContinue.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (mInfoEdit.getText().toString().equals("")) {
                            Toast.makeText(
                                    mContext,
                                    mContext.getResources().getString(
                                            R.string.freeme_comment_input_text),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            ContentValues cv = new ContentValues(1);
                            cv.put(MediaStore.Images.Media.DESCRIPTION,
                                    mInfoEdit.getText().toString() + time);
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(MediaStore.Images.Media._ID);
                            stringBuilder.append(" = " + id);

                            String selection = stringBuilder.toString();
                            Uri base;
                            ContentResolver resolver = mContext
                                    .getContentResolver();
                            if (mType == 2) {
                                base = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                            } else {
                                base = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                            }
                            int result = resolver.update(base, cv, selection,
                                    null);
                            mInfoText.setText(mInfoEdit.getText().toString());
                            dlg.cancel();
                        }
                    }
                });

            }
        });
    }

    public String getTime() {
        long time = System.currentTimeMillis();// long now =
        // android.os.SystemClock.uptimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date d1 = new Date(time);
        String t1 = format.format(d1);
        mCommenttime.setText(t1);
        return t1;
    }

    public void updateText(Path path, int type) {
        String[] imgid = path.toString().split("/");
        if (imgid.length < 4) return;
        boolean uriImg = path.toString().startsWith("/uri/");
        id = imgid[imgid.length - 1];
        mType = type;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(MediaStore.Images.Media._ID);
        stringBuilder.append(" =" + id);
        String selection = stringBuilder.toString();
        String description = null;
        Cursor recordingFileCursor = null;
        if (uriImg) {
            mContainertext.setVisibility(View.GONE);
        } else {
            if (mType == 2) {
                recordingFileCursor = mContext.getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Images.Media.DESCRIPTION},
                        selection, null, null);
            } else {
                recordingFileCursor = mContext.getContentResolver().query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Images.Media.DESCRIPTION},
                        selection, null, null);
            }
            if (recordingFileCursor != null && recordingFileCursor.getCount() > 0) {
                if (recordingFileCursor.moveToLast()) {
                    description = recordingFileCursor
                            .getString(recordingFileCursor
                                    .getColumnIndex("description"));
                }
            }
        }
        String info = mContext.getResources().getString(
                R.string.freeme_comment_comments);
        String times = getTime();
        if (description != null) {
            if (description.length() > 10) {
                info = description.substring(0, description.length() - 10);
                times = description.substring(description.length() - 10,
                        description.length());
            } else {
                info = description;
            }
        }
        mInfoText.setText(info);
        mCommenttime.setText(times);
        if (recordingFileCursor != null) {
            recordingFileCursor.close();
        }
    }

    public void setvisible(boolean isvisible) {
        if (isvisible) {
            mContainertext.setVisibility(View.VISIBLE);
        } else {
            mContainertext.setVisibility(View.GONE);
        }
    }

    public void cleanup() {
        mParentLayout.removeView(mContainertext);
    }
}
