package org.crayne.repack.parsing.ast;

import org.crayne.repack.parsing.lexer.Token;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Node {

    private final List<Node> children;
    private final Node parent;
    private NodeType type;
    private Token value;

    public static Node of(@NotNull final NodeType type) {
        return new Node(type);
    }

    public static Node of(@NotNull final NodeType type, @NotNull final Node... children) {
        return new Node(type, children);
    }

    public static Node of(@NotNull final NodeType type, @NotNull final Token... children) {
        return new Node(type, Arrays.stream(children).map(Node::of).toList());
    }

    public static Node of(@NotNull final Token token) {
        return new Node(token);
    }

    public Node(@NotNull final NodeType type, final Token value) {
        this.type = type;
        this.value = value;
        this.parent = null;
        children = new ArrayList<>();
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

    public Node(@NotNull final NodeType type, final Token value, @NotNull final Collection<Node> children) {
        this.type = type;
        this.value = value;
        this.parent = null;
        this.children = new ArrayList<>();
        this.children.addAll(children);
    }

    public Node(@NotNull final NodeType type, @NotNull final Collection<Node> children) {
        this.type = type;
        this.value = null;
        this.parent = null;
        this.children = new ArrayList<>();
        this.children.addAll(children);
    }

    public Node(@NotNull final NodeType type, final Token value, @NotNull final Node... children) {
        this.type = type;
        this.value = value;
        this.parent = null;
        this.children = new ArrayList<>();
        this.children.addAll(List.of(children));
    }

    public Node(@NotNull final NodeType type, @NotNull final Node... children) {
        this.type = type;
        this.value = null;
        this.parent = null;
        this.children = new ArrayList<>();
        this.children.addAll(List.of(children));
    }

    public Node(@NotNull final Node parent, @NotNull final NodeType type, @NotNull final Node... children) {
        this.type = type;
        this.value = null;
        this.parent = parent;
        this.children = new ArrayList<>();
        this.children.addAll(List.of(children));
    }

    public Node(@NotNull final Node parent, @NotNull final NodeType type, @NotNull final Token value, @NotNull final Node... children) {
        this.type = type;
        this.value = value;
        this.parent = parent;
        this.children = new ArrayList<>();
        this.children.addAll(List.of(children));
    }

    public Node(@NotNull final Node parent, @NotNull final NodeType type, @NotNull final Collection<Node> children) {
        this.type = type;
        this.value = null;
        this.parent = parent;
        this.children = new ArrayList<>();
        this.children.addAll(children);
    }

    public Node parent() {
        return parent;
    }

    public void addChildren(@NotNull final Collection<Node> children) {
        this.children.addAll(children);
    }

    public void addChildren(@NotNull final Node... children) {
        this.children.addAll(List.of(children));
    }

    public Node child(final int index) {
        return children.get(index);
    }

    public void child(final int index, @NotNull final Node child) {
        children.set(index, child);
    }

    public Token value() {
        return value;
    }

    public void value(@NotNull final Token token) {
        this.value = token;
    }

    public NodeType type() {
        return type;
    }
    public void type(@NotNull final NodeType type) {
        this.type = type;
    }

    public List<Node> children() {
        return children;
    }

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
