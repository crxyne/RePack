package org.crayne.repack.conversion.match;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ItemMatch {

    boolean matchAll();

    @NotNull
    Collection<String> items();

}
