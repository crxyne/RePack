package org.crayne.repack.parsing.parser;

import org.crayne.repack.logging.Logger;
import org.crayne.repack.logging.LoggingLevel;
import org.crayne.repack.parsing.ast.Node;
import org.crayne.repack.parsing.ast.NodeType;
import org.crayne.repack.parsing.lexer.Token;
import org.jetbrains.annotations.NotNull;

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
        logger.traceback(message, at, currentFileContent.get(at.line() - 1), LoggingLevel.ANALYZING_ERROR, help);
    }

    private static final Map<NodeType, Collection<NodeType>> validScopeStatements = new HashMap<>() {{
        this.put(NodeType.LET_STATEMENT, List.of(NodeType.PARENT));
        this.put(NodeType.MATCH_STATEMENT, List.of(NodeType.PARENT));
        this.put(NodeType.FOR_STATEMENT, List.of(NodeType.MATCH_STATEMENT));

        this.put(NodeType.ITEM_LISTING_NOVALUES, List.of(NodeType.FOR_STATEMENT));
        this.put(NodeType.ITEM_LISTING_PREDICATE, List.of(NodeType.FOR_STATEMENT));
        this.put(NodeType.ITEM_SETALL_PREDICATE, List.of(NodeType.FOR_STATEMENT));

        this.put(NodeType.ARMOR_LISTING_NOVALUES, List.of(NodeType.FOR_STATEMENT));
        this.put(NodeType.ARMOR_LISTING_PREDICATE, List.of(NodeType.FOR_STATEMENT));
        this.put(NodeType.ARMOR_SETALL_PREDICATE, List.of(NodeType.FOR_STATEMENT));

        this.put(NodeType.ELYTRA_LISTING_NOVALUES, List.of(NodeType.FOR_STATEMENT));
        this.put(NodeType.ELYTRA_LISTING_PREDICATE, List.of(NodeType.FOR_STATEMENT));
        this.put(NodeType.ELYTRA_SETALL_PREDICATE, List.of(NodeType.FOR_STATEMENT));

        this.put(NodeType.PREDICATE_STATEMENT, List.of(NodeType.MATCH_STATEMENT, NodeType.ITEM_LISTING_PREDICATE,
                NodeType.ARMOR_LISTING_PREDICATE, NodeType.ELYTRA_LISTING_PREDICATE));

        this.put(NodeType.IDENTIFIER_LIST, List.of(NodeType.ITEM_LISTING_PREDICATE,
                NodeType.ARMOR_LISTING_PREDICATE, NodeType.ELYTRA_LISTING_PREDICATE));

        this.put(NodeType.MAPALL_PREDICATE, List.of(NodeType.ITEM_LISTING_PREDICATE,
                NodeType.ARMOR_LISTING_PREDICATE, NodeType.ELYTRA_LISTING_PREDICATE));
    }};

    private boolean analyzeCurrentScope(@NotNull final NodeType scope, @NotNull final Collection<Node> children, final int sub) {
        // TODO check for invalid MAPALL_PREDICATE statements (missing the filename for example)
        for (@NotNull final Node child : children.stream().toList().subList(sub, children.size())) {
            if (validScopeStatements.get(child.type()).contains(scope) || child.children().isEmpty()) continue;

            final Token first = child.child(0).value();
            if (first == null) continue;

            analyzerError("Unexpected token '" + first.token() + "' in wrong scope", first);
            return false;
        }
        return true;
    }

}
