package com.xengine.android.utils;

import android.text.TextUtils;

import java.io.*;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Beryl.
 * Date: 12-3-3
 * Time: 下午7:35
 */
public class XStringUtil {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static boolean equals(String str1, String str2) {
        return str1 != null && str2 != null && str1.equals(str2);
    }

    public static boolean equalsIgnoreCase(String str1, String str2) {
        return str1 != null && str2 != null && str1.equalsIgnoreCase(str2);
    }

    public static boolean isContains(String str1, String str2) {
        return str1.contains(str2);
    }

    public static String getString(String str) {
        return str == null ? "" : str;
    }

    public static String unquote(String s, String quote) {
        if (!TextUtils.isEmpty(s) && !TextUtils.isEmpty(quote)) {
            if (s.startsWith(quote) && s.endsWith(quote)) {
                return s.substring(1, s.length() - quote.length());
            }
        }
        return s;
    }

    public static boolean equals(String contentType1, String contentType2, boolean ignoreCase) {
        if (contentType1 != null && contentType2 != null) {
            if (ignoreCase) {
                return contentType1.equalsIgnoreCase(contentType2);
            } else {
                return contentType1.equals(contentType2);
            }
        } else {
            return ((contentType1 == null && contentType2 == null) ? true : false);
        }
    }

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + LINE_SEPARATOR);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * 过滤HTML标签，取出文本内的相关数据
     *
     * @param inputString
     * @return
     */
    public static String filterHtmlTag(String inputString) {
        String htmlStr = inputString; // 含html标签的字符串
        String textStr = "";
        Pattern p_script;
        Matcher m_script;
        Pattern p_style;
        Matcher m_style;
        Pattern p_html;
        Matcher m_html;

        try {
            String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>"; // 定义script的正则表达式{�?script[^>]*?>[\\s\\S]*?<\\/script>
            // }
            String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>"; // 定义style的正则表达式{�?style[^>]*?>[\\s\\S]*?<\\/style>
            // }
            String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式

            p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
            m_script = p_script.matcher(htmlStr);
            htmlStr = m_script.replaceAll(""); // 过滤script标签

            p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
            m_style = p_style.matcher(htmlStr);
            htmlStr = m_style.replaceAll(""); // 过滤style标签

            p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
            m_html = p_html.matcher(htmlStr);
            htmlStr = m_html.replaceAll(""); // 过滤html标签

            textStr = htmlStr;

        } catch (Exception e) {
            System.err.println("Html2Text: " + e.getMessage());
        }

        return textStr;// 返回文本字符
    }

    /**
     * 将字符串数组转化为用逗号连接的字符串
     *
     * @param values
     * @return
     */
    public static String arrayToString(String[] values) {
        String result = "";
        if (values != null) {
            if (values.length > 0) {
                for (String value : values) {
                    result += value + ",";
                }
                result = result.substring(0, result.length() - 1);
            }
        }
        return result;
    }

    /**
     * 验证字符串是否符合email格式
     *
     * @param email 验证的字符串
     * @return 如果字符串为空或者为Null返回true
     *         如果不为空或Null则验证其是否符合email格式，符合则返回true,不符合则返回false
     */
    public static boolean isEmail(String email) {
        boolean flag = true;
        if (!TextUtils.isEmpty(email)) {
            //通过正则表达式验证Emial是否合法
            flag = email.matches("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@" +
                    "([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");

            return flag;
        }
        return flag;
    }

    /**
     * 验证字符串是否是数字（允许以0开头的数字）
     * 通过正则表达式验证。
     * @param numStr
     * @return
     */
    public static boolean isNumber(String numStr) {
        Pattern pattern=Pattern.compile("[0-9]*");
        Matcher match=pattern.matcher(numStr);
        if (match.matches()==false) {
            return false;
        } else {
            return true;
        }
    }

    public static String date2str(long time) {
        return date2str(new Date(time));
    }
    
    public static String date2str(Date date) {
        if (date == null) {
            return null;
        }
        return date2str(date.getYear() + 1900, date.getMonth() + 1,
                date.getDate(), date.getHours(), date.getMinutes());
    }

    public static String date2str(int year, int month, int day, int hour, int minute) {
        String hourStr = ""+hour;
        String minStr = ""+minute;
        if (hour < 10) {
            hourStr = "0" + hourStr;
        }
        if (minute < 10) {
            minStr = "0" +minStr;
        }
        return year+"-"+month+"-"+day+" "+hourStr+":"+minStr;
    }



    public static String date2calendarStr(Date date) {
        return date2calendarStr(date.getYear() + 1900, date.getMonth() + 1, date.getDate());
    }

    public static String date2calendarStr(int year, int month, int day) {
        return year+"-"+month+"-"+day;
    }


    public static String calendar2str(Calendar c) {
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        return year+"-"+month+"-"+day;
    }
    
    public static Calendar str2calendar(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        String[] strList = str.split("-");
        int year = Integer.parseInt(strList[0]);
        int month = Integer.parseInt(strList[1]);
        int day = Integer.parseInt(strList[2]);
        Calendar result = Calendar.getInstance();
        result.set(year, month-1, day);
        return result;
    }

    /**
     * 返回字符串的字节个数（中文当2个字节计算）
     * @param s
     * @return
     */
    public static int stringSize(String s) {
        try {
            String anotherString = new String(s.getBytes("GBK"), "ISO8859_1");
            return anotherString.length();
        }
        catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    /**
     * 将文件大小转换（保留两位小数）
     * @param size 文件大小，单位:byte
     * @return 文件大小
     */
    public static String fileSize2String(long size) {
        return fileSize2String(size, 2);
    }

    private static final float KB = 1024;
    private static final float MB = 1024*1024;
    private static final float GB = 1024*1024*1024;
    /**
     * 将文件大小转换
     * @param size 文件大小，单位:byte
     * @param scale 小数位数
     * @return 文件大小
     */
    public static String fileSize2String(long size, int scale) {
        float result;
        String unit;
        if (size <= 0) {
            result = 0;
            unit = "B";
        } else if (size < KB) {
            result = size;
            unit = "B";
        } else if (size < MB) {
            result = size/KB;
            unit = "K";
        } else if (size < GB) {
            result = size/MB;
            unit = "M";
        } else {
            result = size/GB;
            unit = "G";
        }
        BigDecimal bg = new BigDecimal(result);
        float f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        return f1 + unit;
    }

    public static String percent2str(double percent) {
        int percentInt = (int) (percent * 100);
        return percentInt + "%";
    }

    /**
     * 将毫秒转换为 "*日*小时*分钟*秒"
     * @param mss 要转换的毫秒数
     * @return
     */
    public static String formatDuring(long mss) {
        long days = mss / (1000 * 60 * 60 * 24);
        long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (mss % (1000 * 60)) / 1000;

        if (days != 0) {
            if(hours > 12) {
                return days+"天+";
            }else {
                return days+"天";
            }
        } else if(hours !=0) {
            if (minutes >30) {
                return hours + "小时+";
            } else {
                return hours + "小时";
            }
        } else if (minutes != 0) {
            return minutes + "分钟";
        } else {
            return seconds + "秒";
        }
    }

    /**
     * 将两日期的间隔转换为 "*日*小时*分钟*秒"
     * @param begin 时间段的开始
     * @param end   时间段的结束
     * @return  输入的两个Date类型数据之间的时间间格用* days * hours * minutes * seconds的格式展示
     */
    public static String formatDuring(Date begin, Date end) {
        return formatDuring(end.getTime() - begin.getTime());
    }

    /**
     * 处理文件名，去掉后缀
     * @param srcName
     * @return
     */
    public static String removeFileNameSuffix(String srcName) {
        int dotIndex = srcName.lastIndexOf(".");
        if (dotIndex == -1) {
            return srcName;
        }
        return srcName.substring(0, dotIndex);
    }
}
