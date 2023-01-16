package org.crayne.repack.core.single.predicate;

import org.crayne.repack.core.single.PredicateType;
import org.crayne.repack.parsing.lexer.Token;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PackItemModelPredicate implements PackPredicate {

    @NotNull
    private final List<Token> keys;

    @NotNull
    private final String textureIn;

    @NotNull
    private final String textureOut;

    @NotNull
    private final String json;

    public PackItemModelPredicate(@NotNull final Collection<Token> keys, @NotNull final String json,
                                  @NotNull final String textureIn, @NotNull final String textureOut) {
        this.keys = new ArrayList<>(keys);
        this.json = json;
        this.textureIn = textureIn;
        this.textureOut = textureOut;
    }

    @NotNull
    public String value() {
        throw new RuntimeException("Item model predicates do not have one single value");
    }

    @NotNull
    public List<Token> keys() {
        return keys;
    }

    @NotNull
    public PredicateType type() {
        return PredicateType.ITEMS;
    }

    @NotNull
    public String textureIn() {
        return textureIn;
    }

    @NotNull
    public String textureOut() {
        return textureOut;
    }

    @NotNull
    public String json() {
        return json;
    }

}
