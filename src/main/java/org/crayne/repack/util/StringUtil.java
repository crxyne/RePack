package org.crayne.repack.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

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

    @NotNull
    public static <T> String stringOf(@NotNull final Collection<T> collection) {
        return "{" + (collection.isEmpty() ? "" : "\n") + collection.stream().map(Object::toString).collect(Collectors.joining(", \n")).indent(3) + "}";
    }

}
