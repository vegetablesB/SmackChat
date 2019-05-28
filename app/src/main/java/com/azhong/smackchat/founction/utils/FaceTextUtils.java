package com.azhong.smackchat.founction.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * 项目名:    SmackChat
 * 包名       com.azhong.smackchat.founction.utils
 * 文件名:    FaceTextUtils
 * 创建者:    ZSY
 * 创建时间:  2017/3/2 on 08:58
 * 描述:     TODO 表情工具类
 */
public class FaceTextUtils {

    public static List<FaceText> faceTexts = new ArrayList<>();

    static {
        //ue056为表情图片的文件名，[困]是表情描述
        faceTexts.add(new FaceText("wx_01", "[微笑]"));
        faceTexts.add(new FaceText("pz_02", "[撇嘴]"));
        faceTexts.add(new FaceText("se_03", "[色]"));
        faceTexts.add(new FaceText("fd_04", "[发呆]"));
        faceTexts.add(new FaceText("dy_05", "[得意]"));
        faceTexts.add(new FaceText("ll_06", "[流泪]"));
        faceTexts.add(new FaceText("hx_07", "[害羞]"));
        faceTexts.add(new FaceText("bz_08", "[闭嘴]"));
        faceTexts.add(new FaceText("s_09", "[睡]"));
        faceTexts.add(new FaceText("dk_10", "[大哭]"));
        faceTexts.add(new FaceText("gg_11", "[尴尬]"));
        faceTexts.add(new FaceText("fn_12", "[发怒]"));
        faceTexts.add(new FaceText("tp_13", "[调皮]"));
        faceTexts.add(new FaceText("zy_14", "[呲牙]"));
        faceTexts.add(new FaceText("jy_15", "[惊讶]"));
        faceTexts.add(new FaceText("ng_16", "[难过]"));
        faceTexts.add(new FaceText("k_17", "[酷]"));
        faceTexts.add(new FaceText("lh_18", "[冷汗]"));
        faceTexts.add(new FaceText("zk_19", "[抓狂]"));
        faceTexts.add(new FaceText("t_20", "[吐]"));
        faceTexts.add(new FaceText("tx_21", "[偷笑]"));
        faceTexts.add(new FaceText("yk_22", "[愉快]"));
        faceTexts.add(new FaceText("by_23", "[白眼]"));
        faceTexts.add(new FaceText("am_24", "[傲慢]"));
        faceTexts.add(new FaceText("je_25", "[饥饿]"));
        faceTexts.add(new FaceText("k_26", "[困]"));
        faceTexts.add(new FaceText("jk_27", "[惊恐]"));
        faceTexts.add(new FaceText("lh_28", "[流汗]"));
        faceTexts.add(new FaceText("hx_29", "[憨笑]"));
        faceTexts.add(new FaceText("xx_30", "[休闲]"));
        faceTexts.add(new FaceText("fd_31", "[奋斗]"));
        faceTexts.add(new FaceText("zm_32", "[咒骂]"));
        faceTexts.add(new FaceText("yw_33", "[疑问]"));
        faceTexts.add(new FaceText("x_34", "[嘘]"));
        faceTexts.add(new FaceText("y_35", "[晕]"));
        faceTexts.add(new FaceText("fl_36", "[疯了]"));
        faceTexts.add(new FaceText("s_37", "[衰]"));
        faceTexts.add(new FaceText("kl_38", "[骷髅]"));
    }

    public static String parse(String s) {
        for (FaceText faceText : faceTexts) {
            s = s.replace("[" + faceText.fileName, faceText.fileName);
            s = s.replace(faceText.fileName, "[" + faceText.fileName);
        }
        return s;
    }

    public static class FaceText {
        public FaceText(String fileName, String emo) {
            this.fileName = fileName;
            this.emo = emo;
        }

        public String fileName;
        public String emo;
    }

    /**
     * toSpannableString
     *
     * @return SpannableString
     * @throws
     */
    public static SpannableString toSpannableString(Context context, String text) {
        if (!TextUtils.isEmpty(text)) {
            SpannableString spannableString = new SpannableString(text);
            int start = 0;
            Pattern pattern = Pattern.compile("\\\\ue[a-z0-9]{3}", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String faceText = matcher.group();
                String key = faceText.substring(1);
                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                        context.getResources().getIdentifier(key, "drawable", context.getPackageName()), options);
                ImageSpan imageSpan = new ImageSpan(context, bitmap);
                int startIndex = text.indexOf(faceText, start);
                int endIndex = startIndex + faceText.length();
                if (startIndex >= 0)
                    spannableString.setSpan(imageSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                start = (endIndex - 1);
            }

            return spannableString;
        } else {
            return new SpannableString("");
        }
    }

    public static SpannableString toSpannableString(Context context, String text, SpannableString spannableString) {

        int start = 0;
        Pattern pattern = Pattern.compile("\\\\ue[a-z0-9]{3}", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String faceText = matcher.group();
            String key = faceText.substring(1);
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), context.getResources()
                    .getIdentifier(key, "drawable", context.getPackageName()), options);
            ImageSpan imageSpan = new ImageSpan(context, bitmap);
            int startIndex = text.indexOf(faceText, start);
            int endIndex = startIndex + faceText.length();
            if (startIndex >= 0)
                spannableString.setSpan(imageSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = (endIndex - 1);
        }

        return spannableString;
    }


}
