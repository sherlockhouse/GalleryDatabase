package com.freeme.community.view;

import android.content.Context;
import android.content.res.Resources;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Toast;

import com.freeme.community.utils.InputFilterUtil;
import com.freeme.gallery.R;

/**
 * ClassName: UnEmojiEditText
 * Description:
 * Author: connorlin
 * Date: Created on 2015-10-15.
 */
public class UnEmojiEditText extends EditText {
    //输入表情前的光标位置
    private int     cursorPos;
    //输入表情前EditText中的文本
    private String  inputAfterText;
    //是否重置了EditText的内容
    private boolean resetText;

    private Context             mContext;
    private TextChangedListener mListener;

    public UnEmojiEditText(Context context) {
        this(context, null);
    }

    public UnEmojiEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UnEmojiEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, R.style.Widget_EditText);
        mContext = context;

        initEditText();

        Resources res = context.getResources();

        setTextSize(res.getInteger(R.integer.edittext_size));
        setHintTextColor(res.getColor(R.color.hint_color));
    }

    private void initEditText() {
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int before, int count) {
                if (!resetText) {
                    cursorPos = getSelectionEnd();
                    // 这里用s.toString()而不直接用s是因为如果用s，
                    // 那么，inputAfterText和s在内存中指向的是同一个地址，s改变了，
                    // inputAfterText也就改变了，那么表情过滤就失败了
                    inputAfterText = s.toString();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!resetText) {
                    if (count - before >= 2) {//表情符号的字符长度最小为2
                        CharSequence input = s.subSequence(start, start + count);
                        if (InputFilterUtil.containsEmoji(input.toString())) {
                            resetText = true;
                            Toast.makeText(mContext, "不支持Emoji表情", Toast.LENGTH_SHORT).show();
                            //是表情符号就将文本还原为输入表情符号之前的内容
                            setText(inputAfterText);
                            CharSequence text = getText();
                            if (text instanceof Spannable) {
                                Spannable spanText = (Spannable) text;
                                Selection.setSelection(spanText, text.length());
                            }
                        }
                    }
                } else {
                    resetText = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mListener != null) {
                    mListener.afterTextChanged(editable);
                }
            }
        });
    }

    public void setTextChangedListener(TextChangedListener listener) {
        mListener = listener;
    }

    public interface TextChangedListener {
        //void beforeTextChanged();
        //void onTextChanged();
        void afterTextChanged(Editable editable);
    }
}
