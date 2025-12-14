package com.ng.dtogen.util;

public class CaseUtil {
    public static String toCamel(String input) {
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : input.toCharArray()) {
            if (c == '_' || c == '-' || c == '.' || c == ':') {
                upper = true;
            } else {
                sb.append(upper ? Character.toUpperCase(c) : Character.toLowerCase(c));
                upper = false;
            }
        }
        return sb.toString();
    }
}
