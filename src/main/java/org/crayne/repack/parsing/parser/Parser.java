package org.crayne.repack.parsing.parser;

import org.crayne.repack.logging.Logger;
import org.crayne.repack.logging.LoggingLevel;
import org.crayne.repack.parsing.ast.Node;
import org.crayne.repack.parsing.ast.NodeType;
import org.crayne.repack.parsing.lexer.Token;
import org.crayne.repack.parsing.lexer.Tokenizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Parser {

    private final Logger logger;
    private List<String> currentFileContent;
    private List<Token> tokens;
    private final Tokenizer tokenizer;
    private boolean encounteredError = false;

    private int currentTokenPos = -1;
    private int scopeLevel = 0;
    private Node parentNode;
    private final List<Node> currentScope;
    private Node lastScope;

    public Parser(@NotNull final Logger logger) {
        this.logger = logger;
        this.currentScope = new ArrayList<>();
        this.tokenizer = new Tokenizer(logger);
    }

    private void parserError(@NotNull final String message, @NotNull final String... help) {
        logger.traceback(message, LoggingLevel.PARSING_ERROR, help);
        encounteredError = true;
    }

    private void parserError(@NotNull final String message, @NotNull final Token at, @NotNull final String... help) {
        parserError(message, at, false, help);
    }

    private void parserError(@NotNull final String message, @NotNull final Token at, final boolean skipToEnd, @NotNull final String... help) {
        logger.traceback(message, at, currentFileContent.get(at.line() - 1), skipToEnd, LoggingLevel.PARSING_ERROR, help);
        encounteredError = true;
    }

    public Optional<Node> tree() {
        return Optional.ofNullable(parentNode);
    }

    public void parse(@NotNull final File file, @NotNull final String code, @NotNull final Collection<String> content) {
        this.currentFileContent = new ArrayList<>(content);
        this.tokens = tokenizer.tokenize(file, content, code);

        parse();
    }

    private void checkOpenScopes() {
        if (scopeLevel <= 0 || currentTokenPos <= 0) return;
        parserError("Missing scope end '}'", tokens.get(currentTokenPos - 1), true);

        final Node scopeBegin = currentScope.get(scopeLevel - 1);
        if (scopeBegin.children().isEmpty()) return;

        final Token scopeBeginToken = scopeBegin.child(0).value();
        if (scopeBeginToken == null) return;

        parserError("An open scope was started here; Close it where it should stop to fix this issue.", scopeBeginToken);
    }

    private void parse() {
        currentScope.clear();
        currentTokenPos = -1;

        parentNode = Node.of(NodeType.PARENT);
        parseScope(parentNode, false);

        if (!encounteredError) checkOpenScopes();

        if (encounteredError) // cant just be an 'else' because checkOpenScopes can call parserError, which can change encounteredError to true
            parentNode = null;
    }

    private Token currentToken() {
        return tokens.get(currentTokenPos);
    }

    private boolean expect(@NotNull final Token token, @NotNull final NodeType toBe) {
        if (NodeType.of(token) != toBe) {
            unexpectedToken(token, toBe.printableName());
            return true;
        }
        return false;
    }

    private void unexpectedToken(@NotNull final Token token) {
        unexpectedToken(token, null);
    }

    private void unexpectedToken(@NotNull final Token token, @Nullable final String expected) {
        if (expected == null) {
            parserError("Unexpected token '" + token.token() + "'.", token);
            return;
        }
        parserError("Unexpected token '" + token.token() + "'.", token, "'" + expected + "' expected.");
    }

    private void nextToken() {
        currentTokenPos++;
    }

    private void parseScope(@NotNull final Node parent, final boolean parsingScope) {
        if (tokens.isEmpty()) return;

        nextToken();
        while (currentTokenPos < tokens.size() && !encounteredError) {
            if (parseSingle(parent, parsingScope)) break;
        }
    }

    private boolean parseSingle(@NotNull final Node parent, final boolean parsingScope) {
        final Token current = currentToken();
        final NodeType n = NodeType.of(current);
        nextToken();

        if (parsingScope && NodeType.of(current) == NodeType.RBRACE && scopeLevel >= 1) {
            scopeLevel--;
            if (!currentScope.isEmpty()) {
                lastScope = currentScope.get(currentScope.size() - 1);
                currentScope.remove(currentScope.size() - 1);
            }
            return true;
        }

        tryAdd(parent, switch (n) {
            case LITERAL_LET       ->  parseLet       (current);
            case LITERAL_GLOBAL    ->  parseGlobal    (current);
            case LITERAL_MATCH     ->  parseMatch     (current);
            case LITERAL_FOR       ->  parseFor       (current);
            case LITERAL_ARMOR     ->  parseArmor     (current);
            case LITERAL_ITEMS     ->  parseItems     (current);
            case LITERAL_ELYTRAS   ->  parseElytras   (current);
            case IDENTIFIER        ->  parseIdentifier(current);
            case SET               ->  parseMapAll    (current);
            default -> {
                unexpectedToken(current);
                yield null;
            }
        });

        return false;
    }

    private void tryAdd(@NotNull final Node parent, @Nullable final Node child) {
        if (child == null) return;
        parent.addChildren(child);
    }

    private Node parseLet(@NotNull final Token current) {
        final Token ident = currentToken();
        nextToken();
        return parsePredicate(NodeType.LET_STATEMENT, ident, current);
    }

    private Node parseGlobal(@NotNull final Token current) {
        final Token ident = currentToken();
        nextToken();
        return parsePredicate(NodeType.GLOBAL_STATEMENT, ident, current);
    }

    private Node parseIdentifier(@NotNull final Token current) {
        final Token next = currentToken();
        final NodeType n = NodeType.of(next);
        if (n == NodeType.RBRACE || n == NodeType.IDENTIFIER) return parseIdentifierList(current);

        return parsePredicate(current);
    }

    private Node parsePredicate(@NotNull final Token current) {
        return parsePredicate(NodeType.PREDICATE_STATEMENT, current, null);
    }

    private Node parsePredicate(@NotNull final NodeType type, @NotNull final Token identifier, @Nullable final Token current) {
        final Token equals = currentToken();
        if (expect(equals, NodeType.SET)) return null;

        nextToken();

        final Token value = currentToken();
        if (expect(value, NodeType.STRING_LITERAL)) return null;

        nextToken();

        if (current != null) return Node.of(type, current, identifier, equals, value);
        return Node.of(type, identifier, equals, value);
    }

    private Node parseStatementScope(@NotNull final Token current, @NotNull final NodeType scopeType) {
        final Token lbrace = currentToken();
        if (expect(lbrace, NodeType.LBRACE)) return null;
        scopeLevel++;

        final Node scopeStatement = Node.of(scopeType, current);
        currentScope.add(scopeStatement);

        parseScope(scopeStatement, true);
        return scopeStatement;
    }

    private Node parseIdentifierList(@NotNull final Token current) {
        final Node result = Node.of(NodeType.IDENTIFIER_LIST, current);
        Token next;
        while (currentTokenPos < tokens.size() && NodeType.of(next = currentToken()) == NodeType.IDENTIFIER) {
            nextToken();
            result.addChildren(Node.of(next));
        }
        return result;
    }

    private Node parseMatch(@NotNull final Token current) {
        return parseStatementScope(current, NodeType.MATCH_STATEMENT);
    }

    private boolean checkLastScope(@NotNull final Token current, @NotNull final String scopeTypesStr, @NotNull final NodeType... scopeTypes) {
        if (lastScope == null || !List.of(scopeTypes).contains(lastScope.type())) {
            unexpectedToken(current);
            logger.log("'" + current.token() + "' can only be used right after a " + scopeTypesStr + " scope ends.", LoggingLevel.HELP);
            return true;
        }
        return false;
    }

    private Node parseFor(@NotNull final Token current) {
        if (checkLastScope(current, "match", NodeType.MATCH_STATEMENT)) return null;
        tryAdd(lastScope, parseStatementScope(current, NodeType.FOR_STATEMENT));
        return null;
    }

    private Node parseMapAll(@NotNull final Token current) {
        if (checkLastScope(current, "predicate identifier list",
                NodeType.ELYTRA_LISTING_PREDICATE, NodeType.ITEM_LISTING_PREDICATE, NodeType.ARMOR_LISTING_PREDICATE
        )) return null;

        final NodeType stringListOrPredicateMap = lastScope.children().size() >= 2 ? lastScope.child(1).type() : null;
        if (stringListOrPredicateMap != NodeType.IDENTIFIER_LIST && stringListOrPredicateMap != null) {
            parserError("Expected a predicate identifier list, not a map of predicates and values.", current, "Remove the values (e.g. '= \"something\") from the predicate map.");
            return null;
        }

        final Token value = currentToken();
        nextToken();
        tryAdd(lastScope, Node.of(NodeType.MAPALL_PREDICATE, value));
        return null;
    }

    private Node parseRetextureStatement(@NotNull final Token current, @NotNull final NodeType type, @NotNull final NodeType setall) {
        final Token equalsOrBrace = currentToken();

        if (NodeType.of(equalsOrBrace) == NodeType.SET) {
            nextToken();
            final Token value = currentToken();
            if (expect(value, NodeType.STRING_LITERAL)) return null;

            nextToken();
            return Node.of(setall, current, value);
        }
        return parseStatementScope(current, type);
    }

    private Node parseArmor(@NotNull final Token current) {
        return parseRetextureStatement(current, NodeType.ARMOR_LISTING_PREDICATE, NodeType.ARMOR_SETALL_PREDICATE);
    }

    private Node parseItems(@NotNull final Token current) {
        return parseRetextureStatement(current, NodeType.ITEM_LISTING_PREDICATE, NodeType.ITEM_SETALL_PREDICATE);
    }

    private Node parseElytras(@NotNull final Token current) {
        return parseRetextureStatement(current, NodeType.ELYTRA_LISTING_PREDICATE, NodeType.ELYTRA_SETALL_PREDICATE);
    }


}
