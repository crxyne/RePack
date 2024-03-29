package org.crayne.repack.parsing.ast;

import org.apache.commons.lang3.StringUtils;
import org.crayne.repack.parsing.lexer.Token;
import org.crayne.repack.parsing.lexer.Tokenizer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public enum NodeType {

    //anything without direct value
    PARENT(null),
    IDENTIFIER(null),
    LET_STATEMENT(null),
    GLOBAL_STATEMENT(null),
    MATCH_STATEMENT(null),
    WEIGHT_STATEMENT(null),
    FOR_STATEMENT(null),
    ANY_STATEMENT(null),
    IDENTIFIER_LIST(null),
    ITEM_LISTING_PREDICATE(null),
    ARMOR_LISTING_PREDICATE(null),
    ARMOR_L1_LISTING_PREDICATE(null),
    ARMOR_L2_LISTING_PREDICATE(null),
    ELYTRA_LISTING_PREDICATE(null),
    ITEM_SETALL_PREDICATE(null),
    ARMOR_SETALL_PREDICATE(null),
    ARMOR_L1_SETALL_PREDICATE(null),
    ARMOR_L2_SETALL_PREDICATE(null),
    ELYTRA_SETALL_PREDICATE(null),
    MAPALL_PREDICATE(null),
    PREDICATE_STATEMENT(null),
    MODEL_STATEMENT(null),
    COPY_STATEMENT(null),
    COPY_FROM_TO_STATEMENT(null),
    STRING_LITERAL(null),

    // keywords
    LITERAL_TEMPLATE("template"),
    LITERAL_USE("use"),
    LITERAL_MATCH("match"),
    LITERAL_WEIGHT("#weight"),
    LITERAL_FOR("for"),
    LITERAL_LET("let"),
    LITERAL_GLOBAL("global"),
    LITERAL_ANY("any"),
    LITERAL_ITEMS("items"),
    LITERAL_ELYTRAS("elytras"),
    LITERAL_ARMOR("armor"),
    LITERAL_ARMOR_L1("armor_l1"),
    LITERAL_ARMOR_L2("armor_l2"),
    LITERAL_MODEL("model"),
    LITERAL_COPY("copy"),

    // tokens
    LBRACE("{"),
    RBRACE("}"),
    LPAREN("("),
    RPAREN(")"),
    SET("="),
    MOVE("=>");

    private static final Map<String, NodeType> tokenToNode = new HashMap<>() {{
        for (final NodeType type : NodeType.values()) {
            final String asString = type.asString;
            if (asString != null) this.put(asString, type);
        }
    }};

    private final String asString;

    NodeType(final String asString) {
        this.asString = asString;
    }

    public String getAsString() {
        return asString;
    }

    public String printableName() {
        if (asString != null) return asString;
        return StringUtils.capitalize(name().toLowerCase().replace("_", " "));
    }

    public boolean isKeyword() {
        return switch (this) {
            case LITERAL_FOR, LITERAL_MATCH, LITERAL_LET, LITERAL_TEMPLATE,
                    LITERAL_USE, LITERAL_ANY, LITERAL_ITEMS, LITERAL_ELYTRAS,
                    LITERAL_ARMOR, LITERAL_GLOBAL, LITERAL_ARMOR_L1, LITERAL_ARMOR_L2,
                    LITERAL_WEIGHT, LITERAL_MODEL, LITERAL_COPY -> true;
            default -> false;
        };
    }

    public static NodeType of(@NotNull final Token token) {
        return of(token.token());
    }

    public static NodeType of(@NotNull final String token) {
        if (Tokenizer.isChar(token) != null || Tokenizer.isString(token) != null) return STRING_LITERAL;

        final NodeType type = tokenToNode.get(token);
        return type == null ? IDENTIFIER : type;
    }

    public String toString() {
        return asString == null ? name() : asString;
    }
}
