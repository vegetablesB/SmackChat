package com.azhong.smackchat.founction.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.widget.EditText;

import com.azhong.smackchat.founction.utils.FaceTextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmoticonsEditText extends EditText {

    public EmoticonsEditText(Context context) {
        super(context);
    }

    public EmoticonsEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EmoticonsEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (!TextUtils.isEmpty(text)) {
            super.setText(replace(text.toString()), type);
        } else {
            super.setText(text, type);
        }
    }

    /**
     * 匹配 [这里是汉字] * 中间多个汉字
     */
    private Pattern buildPattern() {
        return Pattern.compile("[\\[][\\u4e00-\\u9fa5]*[\\]]", Pattern.CASE_INSENSITIVE);
    }

    private CharSequence replace(String text) {
        try {
            SpannableString spannableString = new SpannableString(text);
            int start = 0;
            String key = "";
            Pattern pattern = buildPattern();
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String faceText = matcher.group();
                for (FaceTextUtils.FaceText face : FaceTextUtils.faceTexts) {
                    if (face.emo.equals(faceText)) {
                        key = face.fileName;
                        break;
                    }
                }
                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(),
                        getContext().getResources().getIdentifier(key, "drawable", getContext().getPackageName()), options);
                ImageSpan imageSpan = new ImageSpan(getContext(), bitmap);
                int startIndex = text.indexOf(faceText, start);
                int endIndex = startIndex + faceText.length();
                if (startIndex >= 0)
                    spannableString.setSpan(imageSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                start = (endIndex - 1);
            }
            return spannableString;
        } catch (Exception e) {
            return text;
        }
    }
}
