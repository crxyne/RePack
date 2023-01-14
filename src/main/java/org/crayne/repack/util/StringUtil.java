package org.crayne.repack.util;

import org.jetbrains.annotations.NotNull;

public class StringUtil {

    private StringUtil() {}

    public static boolean matchPattern(@NotNull final String pattern, @NotNull final String str) {
        if (pattern.length() == 0 && str.length() == 0) return true;
        if (pattern.length() > 1 && pattern.charAt(0) == '*' && str.length() == 0) return false;

        if ((pattern.length() > 1 && pattern.charAt(0) == '?')
                || (pattern.length() != 0 && str.length() != 0 && pattern.charAt(0) == str.charAt(0)))
            return matchPattern(pattern.substring(1), str.substring(1));

        if (pattern.length() > 0 && pattern.charAt(0) == '*')
            return matchPattern(pattern.substring(1), str) || matchPattern(pattern, str.substring(1));
        return false;
    }

}
