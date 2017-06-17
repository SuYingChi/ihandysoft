package com.ihs.feature.common;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;

public class StringUtils {
    private static char[] ELLIPSIS_NORMAL = { '\u2026' }; // this is "..."
    private static final String ELLIPSIS_STRING = new String(ELLIPSIS_NORMAL);
    private static final char[] ELLIPSIS_TWO_DOTS = { '\u2025' }; // this is ".."
    private static final String ELLIPSIS_TWO_DOTS_STRING = new String(ELLIPSIS_TWO_DOTS);

    /**
     * Convert String with ellipsis to two dot ellipsis. Eg:
     * text:HelloWorldTestText
     * ellipsizeText:HelloWorldTestTe...
     * return HelloWorldTestTex..
     */
    public static String convertEllipsizeToTwoDot(String text, String ellipsizeText) {
        String twoDotsEllipsizeText = ellipsizeText;
        if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(ellipsizeText)) {
            boolean isEndsWithEllipsis = ellipsizeText.endsWith(ELLIPSIS_STRING);
            if (isEndsWithEllipsis) {
                String beforeEllipsizeText = ellipsizeText.substring(0, ellipsizeText.length() - 1);
                String textEnd = (text.length() > ellipsizeText.length() && text.length() >= (beforeEllipsizeText.length() + 1)) ? text.substring(beforeEllipsizeText.length(), beforeEllipsizeText.length() + 1) : "";
                String realBeforeEllipsizeText = beforeEllipsizeText + textEnd;
                twoDotsEllipsizeText = realBeforeEllipsizeText.trim() + ELLIPSIS_TWO_DOTS_STRING;
            }
        }
        return twoDotsEllipsizeText;
    }

    /**
     * Substring text with ellipsis. Eg:
     * text:(1)HelloWorldTestText (2)CM Launcher (3)DiScrollview Sample
     * ellipsizeText:(1)HelloWorldTes... (2)CM Launch... (3)DiScroll...
     * return (1)HelloWorldTest Text (2)CM Launcher (3)DiScroll viewSam..
     */
    public static String[] convertEllipsizeToTwoLine(String text, String ellipsizeText) {
        String[] twoLineTexts = new String[2];
        String lineOneText = ellipsizeText;
        String lineTwoText = "";
        if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(ellipsizeText)) {
            boolean isEndsWithEllipsis = ellipsizeText.endsWith(ELLIPSIS_STRING);
            if (isEndsWithEllipsis) {
                String beforeEllipsizeText = ellipsizeText.substring(0, ellipsizeText.length() - 1);
                int subIndex = beforeEllipsizeText.length() + 1;
                if (text.trim().contains(" ")) {
                    String[] textSplit = text.trim().split(" ");
                    String lineOneTextClone = textSplit[0].trim();
                    if (lineOneTextClone.length() > subIndex) {
                        lineOneText = lineOneTextClone.substring(0, subIndex).trim();
                        lineTwoText = lineOneTextClone.substring(subIndex, lineOneTextClone.length()).trim() + " " + textSplit[1].trim();
                    } else {
                        lineOneText = lineOneTextClone;
                        lineTwoText = textSplit[1].trim();
                    }
                } else {
                    lineOneText = text.length() >= subIndex ? text.substring(0, subIndex).trim() : ellipsizeText;
                    lineTwoText = text.length() > subIndex ? text.substring(subIndex, text.length()).trim() : "";
                }
            }
        }
        twoLineTexts[0] = lineOneText;
        twoLineTexts[1] = lineTwoText;
        return twoLineTexts;
    }

    public static String format(String text){
        if (TextUtils.isEmpty(text)) {
            return text;
        }

        text = text.trim();
        String symbols[] = {":", "：", "-", "–"};
        for (String symbol : symbols) {
            if (text.contains(symbol)) {
                text = text.substring(0, text.lastIndexOf(symbol)).trim();
                break;
            }
        }
        return text;
    }

    public static Spannable getTextWithBoldSpan(String fullString, String boldSubstring) {
        int start = fullString.indexOf(boldSubstring);
        if (start == -1) {
            throw new IllegalArgumentException("boldSubstring is not a substring of fullString.");
        }
        Spannable spannable = new SpannableString(fullString);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), start, start + boldSubstring.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    /**
     * Return an HTML string with matched string emphasized by corresponding color (default: black).
     *
     * @param strSource String to be emphasized.
     * @param strFilter Filter string.
     *
     * @return Emphasized string in HTML.
     */
    public static String emphasizeMatchedString(String strSource, String strFilter) {
        return emphasizeMatchedStringWithColor(strSource, strFilter, "black");
    }

    public static String emphasizeMatchedStringWithColor(String strSource, String strFilter, String color) {
        if (TextUtils.isEmpty(strSource)) {
            return null;
        }
        String strPrefix = "<font color='" + color + "'>";
        String strSuffix = "</font>";

        String strSourceLowerCase = strSource.toLowerCase();
        String strFilterLowerCase = strFilter.toLowerCase();
        int i = strSourceLowerCase.indexOf(strFilterLowerCase);
        if (i >= 0) {
            StringBuilder builder = new StringBuilder(strSource.length());
            int len = strFilter.length();
            builder.append(strSource, 0, i).append(strPrefix).append(strSource, i, i + len).append(strSuffix);
            i += len;
            int j = i;
            while ((i = strSourceLowerCase.indexOf(strFilterLowerCase, i)) > 0) {
                builder.append(strSource, j, i).append(strPrefix).append(strSource, i, i + len).append(strSuffix);
                i += len;
                j = i;
            }
            builder.append(strSource, j, strSource.length());
            return builder.toString();
        }
        return strSource;
    }
}
