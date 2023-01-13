package org.crayne.repack.core.single;

import org.crayne.repack.parsing.ast.Node;
import org.crayne.repack.parsing.lexer.Token;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PackVariable {


    @NotNull
    private final String name;

    @NotNull
    private final String value;

    public PackVariable(@NotNull final String name, @NotNull final String value) {
        this.name = name;
        this.value = value;
    }

    @NotNull
    public String name() {
        return name;
    }

    @NotNull
    public String value() {
        return value;
    }

    @NotNull
    public static Optional<PackVariable> of(@NotNull final Node node) {
        if (node.children().size() < 4) return Optional.empty();
        final Token ident = node.child(1).value();
        final Token value = node.child(3).value();

        if (ident == null || value == null) return Optional.empty();
        return Optional.of(new PackVariable(ident.token(), value.token().substring(1, value.token().length() - 1)));
    }

    @NotNull
    public String toString() {
        return "PackVariable{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
