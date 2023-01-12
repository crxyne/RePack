package org.crayne.repack.parsing.parser;

import org.crayne.repack.logging.Logger;
import org.crayne.repack.logging.LoggingLevel;
import org.crayne.repack.parsing.ast.Node;
import org.crayne.repack.parsing.ast.NodeType;
import org.crayne.repack.parsing.lexer.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeAnalyzer {

    private Node AST;
    private final Logger logger;
    private List<String> currentFileContent;

    public TreeAnalyzer(@NotNull final Logger logger) {
        this.logger = logger;
    }

    public boolean analyze(@NotNull final Node AST, @NotNull final List<String> currentFileContent) {
        this.AST = AST;
        this.currentFileContent = currentFileContent;
        return analyzeCurrentScope(this.AST.type(), this.AST.children(), 0);
    }

    private void analyzerError(@NotNull final String message, @NotNull final String... help) {
        logger.traceback(message, LoggingLevel.ANALYZING_ERROR, help);
    }

    private void analyzerError(@NotNull final String message, @NotNull final Token at, @NotNull final String... help) {
        analyzerError(message, at, false, help);
    }

    private void analyzerError(@NotNull final String message, @NotNull final Token at, final boolean skipToEnd, @NotNull final String... help) {
        logger.traceback(message, at, currentFileContent.get(at.line() - 1), skipToEnd, LoggingLevel.PARSING_ERROR, help);
    }

    private static final Map<NodeType, Collection<NodeType>> validScopeStatements = new HashMap<>() {{
        this.put(NodeType.LET_STATEMENT,            List.of(NodeType.PARENT));
        this.put(NodeType.GLOBAL_STATEMENT,         List.of(NodeType.PARENT));
        this.put(NodeType.MATCH_STATEMENT,          List.of(NodeType.PARENT));
        this.put(NodeType.FOR_STATEMENT,            List.of(NodeType.MATCH_STATEMENT));

        this.put(NodeType.ITEM_LISTING_PREDICATE,   List.of(NodeType.FOR_STATEMENT));
        this.put(NodeType.ITEM_SETALL_PREDICATE,    List.of(NodeType.FOR_STATEMENT));

        this.put(NodeType.ARMOR_LISTING_PREDICATE,  List.of(NodeType.FOR_STATEMENT));
        this.put(NodeType.ARMOR_SETALL_PREDICATE,   List.of(NodeType.FOR_STATEMENT));

        this.put(NodeType.ELYTRA_LISTING_PREDICATE, List.of(NodeType.FOR_STATEMENT));
        this.put(NodeType.ELYTRA_SETALL_PREDICATE,  List.of(NodeType.FOR_STATEMENT));

        this.put(NodeType.PREDICATE_STATEMENT,      List.of(NodeType.MATCH_STATEMENT, NodeType.ITEM_LISTING_PREDICATE, NodeType.ARMOR_LISTING_PREDICATE, NodeType.ELYTRA_LISTING_PREDICATE));
        this.put(NodeType.IDENTIFIER_LIST,          List.of(NodeType.ITEM_LISTING_PREDICATE, NodeType.ARMOR_LISTING_PREDICATE, NodeType.ELYTRA_LISTING_PREDICATE));
        this.put(NodeType.MAPALL_PREDICATE,         List.of(NodeType.ITEM_LISTING_PREDICATE, NodeType.ARMOR_LISTING_PREDICATE, NodeType.ELYTRA_LISTING_PREDICATE));
    }};

    private boolean hasChildNode(@NotNull final Node parent, @Nullable final NodeType requiredParentType, @NotNull final NodeType childType) {
        return requiredParentType == null || parent.type() != requiredParentType || hasChildNode(parent, childType);
    }

    private boolean hasChildNode(@NotNull final Node parent, @NotNull final NodeType childType) {
        return parent.children().stream().anyMatch(n -> n.type() == childType);
    }

    private boolean checkChildNode(@NotNull final Node parent, @NotNull final NodeType requiredParentType, @NotNull final NodeType childType) {
        if (hasChildNode(parent, requiredParentType, childType)) return true;

        final Token first = parent.child(0).value();
        if (first == null) return false;

        analyzerError("Missing " + childType.printableName() + " after " + requiredParentType.printableName() + " scope.", first,
                "The given " + requiredParentType.printableName() + " has to follow directly with a " + childType.printableName() + ".");
        return false;
    }

    private boolean checkMapAllPredicate(@NotNull final Node child, @NotNull final NodeType type) {
        if (hasChildNode(child, NodeType.IDENTIFIER_LIST))
            return checkChildNode(child, type, NodeType.MAPALL_PREDICATE);
        return true;
    }

    private boolean analyzeCurrentScope(@NotNull final NodeType scope, @NotNull final Collection<Node> children, final int sub) {
        for (@NotNull final Node child : children.stream().toList().subList(sub, children.size())) {
            if (validScopeStatements.get(child.type()).contains(scope) || child.children().isEmpty()) {
                // check nested nodes and ignore first token, which simply is there for file, line and column information
                final boolean noerror = switch (child.type()) {
                    case MATCH_STATEMENT, FOR_STATEMENT, ITEM_LISTING_PREDICATE,
                            ARMOR_LISTING_PREDICATE, ELYTRA_LISTING_PREDICATE -> analyzeCurrentScope(child.type(), child.children(), 1);
                    default -> true;
                };
                if (!noerror) return false;

                // check if chained scopes have all they need (match + for, as an example)
                final boolean noMissingStatements = checkChildNode(child, NodeType.MATCH_STATEMENT, NodeType.FOR_STATEMENT)
                        && checkMapAllPredicate(child, NodeType.ITEM_LISTING_PREDICATE)
                        && checkMapAllPredicate(child, NodeType.ARMOR_LISTING_PREDICATE)
                        && checkMapAllPredicate(child, NodeType.ELYTRA_LISTING_PREDICATE);
                if (!noMissingStatements) return false;

                continue;
            }

            final Token first = child.child(0).value();
            if (first == null) continue;

            analyzerError("Unexpected token '" + first.token() + "' in wrong scope", first);
            return false;
        }
        return true;
    }

}
