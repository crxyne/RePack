package org.crayne.repack.core;

import org.crayne.repack.core.single.PackFile;
import org.crayne.repack.core.single.PackVariable;
import org.crayne.repack.core.single.PredicateType;
import org.crayne.repack.core.single.predicate.PackMapAllPredicate;
import org.crayne.repack.core.single.predicate.PackSimplePredicate;
import org.crayne.repack.core.single.predicate.PackSupredicate;
import org.crayne.repack.logging.Logger;
import org.crayne.repack.logging.LoggingLevel;
import org.crayne.repack.parsing.ast.Node;
import org.crayne.repack.parsing.ast.NodeType;
import org.crayne.repack.parsing.lexer.Token;
import org.crayne.repack.parsing.parser.Parser;
import org.crayne.repack.parsing.parser.TreeAnalyzer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PackWorkspaceBuilder {

    private final Logger logger;
    private final Parser parser;
    private PackWorkspace workspace;
    private boolean encounteredError;

    public PackWorkspaceBuilder() {
        this.logger = new Logger();
        this.parser = new Parser(logger);
        this.encounteredError = false;
    }

    private void workspaceError(@NotNull final String message) {
        logger.error(message, LoggingLevel.CONVERTING_ERROR);
        encounteredError = true;
    }

    @NotNull
    public Optional<PackWorkspace> setup(@NotNull final File directory) {
        logger.info("Setting up workspace...");
        logger.info("Parsing files...");
        workspace = new PackWorkspace();
        final Set<Node> trees = parseAllOfDirectory(directory);
        if (encounteredError) {
            workspaceError("Could not open workspace due to previous error; aborting.");
            return Optional.empty();
        }
        if (trees.isEmpty()) {
            logger.info("Nothing was parsed; no operation was performed.");
            return Optional.empty();
        }
        logger.log("Successfully parsed all pack files of workspace.", LoggingLevel.SUCCESS);
        logger.info("Loading workspace, reading parsed files...");
        readPackFiles(trees);



        if (encounteredError) {
            workspaceError("Could not read parsed files due to previous error.");
            return Optional.empty();
        }
        logger.log("Successfully finished loading all parsed files into workspace.", LoggingLevel.SUCCESS);
        return Optional.ofNullable(workspace);
    }

    @NotNull
    private PackFile readPackFileNode(@NotNull final Node tree) {
        return readPackFileNode(tree, null);
    }

    private void defineSimplePredicate(@NotNull final Node statement, @NotNull final PackFile addTo) {
        final Token ident = statement.child(0).value();
        final Token value = statement.child(2).value();
        final Node parent = statement.parent();
        final NodeType parentType = parent == null ? null : parent.type();

        if (ident == null || value == null || parentType == null) {
            workspaceError("An unexpected error occurred, invalid AST node encountered for predicate statement");
            return;
        }
        final PredicateType type = switch (parentType) {
            case ITEM_LISTING_PREDICATE -> PredicateType.ITEMS;
            case ARMOR_LISTING_PREDICATE -> PredicateType.ARMOR;
            case ELYTRA_LISTING_PREDICATE -> PredicateType.ELYTRAS;
            case MATCH_STATEMENT -> PredicateType.MATCH;
            default -> null;
        };
        addTo.definePredicate(new PackSimplePredicate(ident.token(), value.noStringLiterals(), type));
    }

    private void defineSetAllPredicate(@NotNull final Node statement, @NotNull final PackFile addTo) {
        final Token value = statement.child(1).value();
        final NodeType type = statement.type();

        if (value == null) {
            workspaceError("An unexpected error occurred, invalid AST node encountered for setall-predicate statement");
            return;
        }
        final PredicateType predicateType = switch (type) {
            case ITEM_SETALL_PREDICATE -> PredicateType.ITEMS;
            case ARMOR_SETALL_PREDICATE -> PredicateType.ARMOR;
            case ELYTRA_SETALL_PREDICATE -> PredicateType.ELYTRAS;
            default -> null;
        };
        if (predicateType == null) {
            workspaceError("An unexpected error occurred, invalid predicate type for setall-predicate statement");
            return;
        }
        addTo.definePredicate(new PackSupredicate(predicateType, value.noStringLiterals()));
    }

    private void defineMapAllPredicate(@NotNull final Node statement, @NotNull final PackFile addTo) {
        final Token value = statement.child(0).value();
        final Node parent = statement.parent();
        final NodeType parentType = parent == null ? null : parent.type();

        if (value == null || parentType == null) {
            workspaceError("An unexpected error occurred, invalid AST node encountered for mapall-predicate statement");
            return;
        }
        final PredicateType type = switch (parentType) {
            case ITEM_LISTING_PREDICATE -> PredicateType.ITEMS;
            case ARMOR_LISTING_PREDICATE -> PredicateType.ARMOR;
            case ELYTRA_LISTING_PREDICATE -> PredicateType.ELYTRAS;
            default -> null;
        };
        if (type == null) {
            workspaceError("An unexpected error occurred, invalid predicate type for mapall-predicate statement");
            return;
        }
        final Node identList = parent.child(1);
        final NodeType identType = identList.type();
        if (identType != NodeType.IDENTIFIER_LIST && identType != NodeType.MAPALL_PREDICATE) {
            workspaceError("An unexpected error occurred, invalid AST node with no identifier list for mapall-predicate statement");
            return;
        }
        final Set<String> keys = identType == NodeType.MAPALL_PREDICATE
                ? Collections.emptySet()
                : identList.children()
                        .stream()
                        .map(Node::value)
                        .map(Objects::requireNonNull)
                        .map(Token::noStringLiterals)
                        .collect(Collectors.toSet());
        addTo.definePredicate(new PackMapAllPredicate(type, keys, value.noStringLiterals()));
    }

    private void variableRedefinedError(@NotNull final String name, @NotNull final Token at) {
        logger.traceback("The variable '" + name + "' was already defined. Cannot redefine existing variables.", at, LoggingLevel.ANALYZING_ERROR,
                "Try renaming your old variable, or alternatively, your new variable, to something unique.");
        encounteredError = true;
    }

    private void definePackVariable(@NotNull final Node statement, @NotNull final PackFile addTo) {
        final boolean global = statement.type() == NodeType.GLOBAL_STATEMENT;
        final Token let = statement.child(0).value();
        final Optional<PackVariable> variable = PackVariable.of(statement);

        if (let == null || variable.isEmpty()) {
            workspaceError("An unexpected error occurred, invalid variable definition node");
            return;
        }
        final String name = variable.get().name();
        if (global) {
            if (workspace.variableDefined(name)) variableRedefinedError(name, let);
            workspace.defineVariable(variable.get());
            return;
        }
        if (addTo.variableDefined(name)) variableRedefinedError(name, let);
        addTo.defineVariable(variable.get());
    }

    @NotNull
    private PackFile readPackFileNode(@NotNull final Node tree, @Nullable final PackFile alreadyExisting) {
        final PackFile addTo = alreadyExisting == null ? new PackFile() : alreadyExisting;
        tree.children().forEach(statement -> {
            switch (statement.type()) {
                case MATCH_STATEMENT, FOR_STATEMENT, ITEM_LISTING_PREDICATE,
                        ARMOR_LISTING_PREDICATE, ELYTRA_LISTING_PREDICATE -> readPackFileNode(statement, addTo);
                case PREDICATE_STATEMENT -> defineSimplePredicate(statement, addTo);
                case ARMOR_SETALL_PREDICATE, ITEM_SETALL_PREDICATE, ELYTRA_SETALL_PREDICATE -> defineSetAllPredicate(statement, addTo);
                case MAPALL_PREDICATE -> defineMapAllPredicate(statement, addTo);
                case LET_STATEMENT, GLOBAL_STATEMENT -> definePackVariable(statement, addTo);
            }
        });
        return addTo;
    }

    private void readPackFiles(@NotNull final Collection<Node> trees) {
        trees.stream().map(this::readPackFileNode).forEach(workspace.packFiles()::add);
    }

    @NotNull
    private Set<Node> parseAllOfDirectory(@NotNull final File directory) {
        logger.info("Parsing all files of directory '" + directory.getAbsolutePath() + "'...");

        if (!directory.isDirectory()) throw new IllegalArgumentException("Not a directory: " + directory);
        try (final Stream<Path> paths = Files.walk(directory.toPath())) {
            final Set<Optional<Node>> optionalNodes = paths.map(Path::toFile).filter(File::isFile).map(this::parse).collect(Collectors.toSet());
            if (optionalNodes.stream().anyMatch(Optional::isEmpty)) throw new Exception("See previous error");

            return optionalNodes.stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
        } catch (final Exception e) {
            workspaceError("Could not read pack workspace '" + directory.getAbsolutePath() + "': " + e.getMessage());
            if (!e.getMessage().equals("See previous error")) e.printStackTrace(logger);
        }
        return Collections.emptySet();
    }

    @NotNull
    private Optional<Node> parse(@NotNull final File packFile) {
        if (encounteredError) return Optional.empty();
        logger.info("\t\tParsing pack file '" + packFile.getAbsolutePath() + "'...");
        final long singleStartTime = System.currentTimeMillis();

        final String code;
        logger.info("\t\tReading pack file content...");
        try {
            code = Files.readString(packFile.toPath());
        } catch (final IOException e) {
            workspaceError("\t\tCould not read pack file '" + packFile.getAbsolutePath() + "': " + e.getMessage());
            e.printStackTrace(logger);
            return Optional.empty();
        }
        final List<String> content = Arrays.stream(code.split("\n")).toList();

        parser.parse(packFile, code, content);
        final Optional<Node> tree = parser.tree();

        final boolean success = tree.isPresent() && new TreeAnalyzer(logger).analyze(tree.get(), content);
        if (!success) {
            workspaceError("\t\tCould not parse pack file '" + packFile.getAbsolutePath() + "'.");
            return Optional.empty();
        }
        final long singleEndTime = System.currentTimeMillis();

        logger.log("\t\tSuccessfully parsed pack file '" + packFile.getAbsolutePath() + "' in " + (singleEndTime - singleStartTime) + "ms.", LoggingLevel.SUCCESS);
        return tree;
    }


}
