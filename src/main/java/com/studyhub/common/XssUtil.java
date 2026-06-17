package com.studyhub.common;

import java.util.regex.Pattern;

/**
 * XSS 防护工具类
 * 对用户输入的内容进行 HTML 转义，防止 XSS 攻击
 */
public class XssUtil {

    /**
     * XSS 危险字符正则
     */
    private static final Pattern XSS_PATTERN = Pattern.compile(
            "<script[^>]*>.*?</script>|" +
            "on\\w+\\s*=|" +
            "javascript\\s*:|" +
            "vbscript\\s*:|" +
            "expression\\s*\\(|" +
            "<[^>]*>",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 过滤 XSS 危险内容
     * 把 HTML 特殊字符转义为 HTML 实体
     */
    public static String filter(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        String result = XSS_PATTERN.matcher(input).replaceAll("");

        StringBuilder sb = new StringBuilder(result.length());
        for (int i = 0; i < result.length(); i++) {
            char c = result.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#x27;");
                default -> sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * 判断是否包含 XSS 危险内容
     */
    public static boolean containsXss(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        return XSS_PATTERN.matcher(input).find();
    }
}
