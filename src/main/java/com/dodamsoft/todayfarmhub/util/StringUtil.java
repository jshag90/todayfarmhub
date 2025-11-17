package com.dodamsoft.todayfarmhub.util;

public class StringUtil {

    public static boolean containsAlphabet(String code) {
        return code.matches(".*[a-zA-Z].*");
    }
}
