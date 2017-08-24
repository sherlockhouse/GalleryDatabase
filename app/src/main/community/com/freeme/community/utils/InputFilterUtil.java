package com.freeme.community.utils;

import java.util.ArrayList;

/**
 * ClassName: InputFilterUtil
 * Description:
 * Author: connorlin
 * Date: Created on 2016-3-24.
 */
public class InputFilterUtil {

    //    private static InputFilter emojiFilter = new InputFilter() {
//        Pattern emoji = Pattern.compile(
//                "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
//                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
//
//        @Override
//        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
//            Matcher emojiMatcher = emoji.matcher(source);
//            if (emojiMatcher.find()) {
//                return "";
//            }
//            return null;
//        }
//    };
//
//    public static String filterEmojiString(String source) {
//        return emojiFilter.filter(source, 0, source.length(), null, 0, source.length()).toString();
//    }
    public static String filterEmojiString(String source) {
        if (source == null) return null;

        ArrayList<Character> charList = new ArrayList<>();

        int len = source.length();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            if (!isEmojiCharacter(codePoint)) { //如果不能匹配,则该字符是Emoji表情
                charList.add(codePoint);
            }
        }

        for (Character character : charList) {
            source = source.replace(character, ' ');
        }

        return source.trim();
    }

    /**
     * 判断是否是Emoji
     */
    public static boolean isEmojiCharacter(char codePoint) {
        return (codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA) ||
                (codePoint == 0xD) || ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) || ((codePoint >= 0x10000)
                && (codePoint <= 0x10FFFF));
    }

    /**
     * 检测是否有emoji表情
     */
    public static boolean containsEmoji(String source) {
        int len = source.length();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            if (!isEmojiCharacter(codePoint)) { //如果不能匹配,则该字符是Emoji表情
                return true;
            }
        }
        return false;
    }
}
