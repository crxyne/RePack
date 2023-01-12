package org.crayne.repack.parsing.ast;

import org.crayne.repack.parsing.lexer.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
public class Node {

    @NotNull
    private final List<Node> children;

    @Nullable
    private final Node parent;

    @NotNull
    private final NodeType type;

    @Nullable
    private Token value;

    @NotNull
    public static Node of(@NotNull final NodeType type) {
        return new Node(type);
    }

    @NotNull
    public static Node of(@NotNull final NodeType type, @NotNull final Token... children) {
        return new Node(type, Arrays.stream(children).map(Node::of).toList());
    }

    @NotNull
    public static Node of(@NotNull final Token token) {
        return new Node(token);
    }

    public Node(@NotNull final NodeType type) {
        this.type = type;
        this.value = null;
        this.parent = null;
        children = new ArrayList<>();
    }

    public Node(@NotNull final Token token) {
        this.type = NodeType.of(token);
        this.value = token;
        this.parent = null;
        children = new ArrayList<>();
    }

    public Node(@NotNull final NodeType type, @NotNull final Collection<Node> children) {
        this.type = type;
        this.value = null;
        this.parent = null;
        this.children = new ArrayList<>();
        this.children.addAll(children);
    }

    public Node(@NotNull final NodeType type, @NotNull final Node... children) {
        this.type = type;
        this.value = null;
        this.parent = null;
        this.children = new ArrayList<>();
        this.children.addAll(List.of(children));
    }

    @Nullable
    public Node parent() {
        return parent;
    }

    public void addChildren(@NotNull final Collection<Node> children) {
        this.children.addAll(children);
    }

    public void addChildren(@NotNull final Node... children) {
        this.children.addAll(List.of(children));
    }

    @NotNull
    public Node child(final int index) {
        return children.get(index);
    }

    public void child(final int index, @NotNull final Node child) {
        children.set(index, child);
    }

    @Nullable
    public Token value() {
        return value;
    }

    public void value(@NotNull final Token token) {
        this.value = token;
    }

    @NotNull
    public NodeType type() {
        return type;
    }

    @NotNull
    public List<Node> children() {
        return children;
    }

    @NotNull
    public String toString() {
        final StringBuilder result = new StringBuilder(type.name());
        if (value != null) {
            result
                    .append(value.line() > 0 ? " [" + value.line() : "")
                    .append(value.column() > 0 ? ":" + value.column() + "" : "")
                    .append(value.line() > 0 ? "]" : "")
                    .append(" -> ").append(value.token());
        }
        if (!children.isEmpty()) {
            result.append(" [ \n");
            for (final Node child : children) {
                result.append(child.toString().indent(4));
            }
            result.append("]");
        } else if (value == null && !type.name().startsWith("LITERAL_")) {
            result.append(" []");
        }
        return result.toString();
    }

}
