package org.crayne.repack.core;

import org.apache.commons.lang3.tuple.Pair;
import org.crayne.repack.conversion.PackWorkspace;
import org.crayne.repack.core.single.PackFile;
import org.crayne.repack.core.single.PackVariable;
import org.crayne.repack.core.single.PredicateType;
import org.crayne.repack.core.single.predicate.*;
import org.crayne.repack.util.logging.Logger;
import org.crayne.repack.util.logging.LoggingLevel;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    public PackWorkspaceBuilder(@NotNull final Logger logger) {
        this.logger = logger;
        this.parser = new Parser(this.logger);
        this.encounteredError = false;
    }

    private void workspaceError(@NotNull final String message) {
        logger.error(message, LoggingLevel.CONVERTING_ERROR);
        encounteredError = true;
    }

    @NotNull
    public Optional<PackWorkspace> setup(@NotNull final File directory) {
        try {
            logger.info("Setting up workspace...");
            logger.info("Parsing files...");
            workspace = new PackWorkspace(logger);
            final Set<Pair<File, Node>> trees = parseAllOfDirectory(directory);
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
            readPackFiles(trees, directory);

            if (encounteredError) {
                workspaceError("Could not read parsed files due to previous error.");
                return Optional.empty();
            }
            logger.log("Successfully finished loading all parsed files into workspace.", LoggingLevel.SUCCESS);
            return Optional.ofNullable(workspace);
        } catch (final Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace(logger);
            return Optional.empty();
        }
    }

    @NotNull
    private PackFile readPackFileNode(@NotNull final Pair<File, Node> tree, @NotNull final File root) {
        final PackFile preprocessed = preprocessPackFile(tree, null, root);
        return readPackFileNode(tree, preprocessed, root);
    }

    private boolean checkTextureExists(@NotNull final File root, @NotNull final String child, @NotNull final Token at) {
        if (!new File(root, child).isFile() && !new File(root, child + ".png").isFile()) {
            logger.traceback("Could not find texture file '" + child + "' in '" + root.getAbsolutePath() + "'.", at, LoggingLevel.ANALYZING_ERROR, "Did you spell the filename correctly?");
            encounteredError = true;
            return true;
        }
        return false;
    }

    @Nullable
    private PackCopyFromToPredicate defineCopyFromToPredicate(@NotNull final Node statement, @NotNull final PackFile addTo, @NotNull final File root) {
        final Token from = statement.child(0).value();
        final Token to = statement.child(2).value();

        if (from == null || to == null) {
            workspaceError("An unexpected error occurred, invalid AST node encountered for value statement");
            return null;
        }

        final String finalFrom = replaceVariables(from.noStringLiterals(), from, workspace.globalVariables(), addTo.variables());
        final String finalTo = replaceVariables(to.noStringLiterals(), to, workspace.globalVariables(), addTo.variables());
        return new PackCopyFromToPredicate(finalFrom, finalTo);
    }

    @Nullable
    private PackSimplePredicate defineSimplePredicate(@NotNull final Node statement, @NotNull final PackFile addTo, @Nullable final PackMatchPredicate matchPredicate, @NotNull final File root) {
        final Token ident = statement.child(0).value();
        final Token value = statement.child(2).value();

        final Node parent = statement.parent();
        final NodeType parentType = parent == null ? null : parent.type();

        if (ident == null || value == null || parentType == null) {
            workspaceError("An unexpected error occurred, invalid AST node encountered for value statement");
            return null;
        }
        final Node parentTemp = parent.parent();

        if (parentTemp != null && parentTemp.type() != NodeType.PARENT && parentTemp.type() != NodeType.FOR_STATEMENT
                && parentTemp.type() != NodeType.ANY_STATEMENT) {
            workspaceError("An unexpected error occurred, invalid AST node encountered for value statement (" + parentTemp.type() + ")");
            return null;
        }

        final PredicateType type = switch (parentType) {
            case ITEM_LISTING_PREDICATE -> PredicateType.ITEMS;
            case ARMOR_LISTING_PREDICATE -> PredicateType.ARMOR;
            case ARMOR_L1_LISTING_PREDICATE -> PredicateType.ARMOR_L1;
            case ARMOR_L2_LISTING_PREDICATE -> PredicateType.ARMOR_L2;
            case ELYTRA_LISTING_PREDICATE -> PredicateType.ELYTRAS;
            case MATCH_STATEMENT -> PredicateType.MATCH;
            default -> null;
        };
        final String finalValue = replaceVariables(value.noStringLiterals(), value, workspace.globalVariables(), addTo.variables());
        if (type != PredicateType.MATCH && checkTextureExists(root, finalValue, value)) return null;

        final PackSimplePredicate predicate = new PackSimplePredicate(ident, finalValue, type);
        if (matchPredicate == null) return predicate;

        if (type == PredicateType.MATCH) matchPredicate.matchPredicates().add(predicate);
        else matchPredicate.predicates().add(predicate);

        return predicate;
    }

    private void defineSetAllPredicate(@NotNull final Node statement, @NotNull final PackFile addTo, @Nullable final PackMatchPredicate matchPredicate, @NotNull final File root) {
        final Token value = statement.child(1).value();
        final NodeType type = statement.type();

        if (value == null) {
            workspaceError("An unexpected error occurred, invalid AST node encountered for setall-value statement");
            return;
        }
        final PredicateType predicateType = switch (type) {
            case ITEM_SETALL_PREDICATE -> PredicateType.ITEMS;
            case ARMOR_SETALL_PREDICATE -> PredicateType.ARMOR;
            case ARMOR_L1_SETALL_PREDICATE -> PredicateType.ARMOR_L1;
            case ARMOR_L2_SETALL_PREDICATE -> PredicateType.ARMOR_L2;
            case ELYTRA_SETALL_PREDICATE -> PredicateType.ELYTRAS;
            default -> null;
        };
        if (predicateType == null) {
            workspaceError("An unexpected error occurred, invalid value type for setall-value statement");
            return;
        }
        final String finalValue = replaceVariables(value.noStringLiterals(), value, workspace.globalVariables(), addTo.variables());
        if (checkTextureExists(root, finalValue, value)) return;

        final PackPredicate predicate = new PackSupredicate(predicateType, finalValue);

        if (matchPredicate == null) addTo.definePredicate(predicate);
        else matchPredicate.predicates().add(predicate);
    }

    private void defineMapAllPredicate(@NotNull final Node statement, @NotNull final PackFile addTo, @Nullable final PackMatchPredicate matchPredicate, @NotNull final File root) {
        final Token value = statement.child(0).value();
        final Node parent = statement.parent();
        final NodeType parentType = parent == null ? null : parent.type();

        if (value == null || parentType == null) {
            workspaceError("An unexpected error occurred, invalid AST node encountered for mapall-value statement");
            return;
        }
        final PredicateType type = switch (parentType) {
            case ITEM_LISTING_PREDICATE -> PredicateType.ITEMS;
            case ARMOR_LISTING_PREDICATE -> PredicateType.ARMOR;
            case ARMOR_L1_LISTING_PREDICATE -> PredicateType.ARMOR_L1;
            case ARMOR_L2_LISTING_PREDICATE -> PredicateType.ARMOR_L2;
            case ELYTRA_LISTING_PREDICATE -> PredicateType.ELYTRAS;
            default -> null;
        };
        if (type == null) {
            workspaceError("An unexpected error occurred, invalid value type for mapall-value statement");
            return;
        }
        final Node identList = parent.child(1);
        final NodeType identType = identList.type();
        if (identType != NodeType.IDENTIFIER_LIST && identType != NodeType.MAPALL_PREDICATE) {
            workspaceError("An unexpected error occurred, invalid AST node with no identifier list for mapall-value statement");
            return;
        }
        final Set<Token> keys = identType == NodeType.MAPALL_PREDICATE
                ? Collections.emptySet()
                : identList.children()
                        .stream()
                        .map(Node::value)
                        .map(Objects::requireNonNull)
                        .collect(Collectors.toSet());

        final String finalValue = replaceVariables(value.noStringLiterals(), value, workspace.globalVariables(), addTo.variables());
        if (checkTextureExists(root, finalValue, value)) return;

        final PackPredicate predicate = new PackMapAllPredicate(type, keys, finalValue);

        if (matchPredicate == null) addTo.definePredicate(predicate);
        else matchPredicate.predicates().add(predicate);
    }

    @SafeVarargs
    @NotNull
    private String replaceVariables(@NotNull final String originalString, @NotNull final Token at, @NotNull final Collection<PackVariable>... variables) {
        String temp = originalString;

        final Matcher variableMatcher = Pattern.compile(("\\$\\((.*?)\\)")).matcher(originalString);
        while (variableMatcher.find()) {
            final String variableName = variableMatcher.group(1);
            final Optional<PackVariable> variable = Arrays.stream(variables)
                    .map(s -> s.stream().filter(p -> p.name().equals(variableName)).findAny())
                    .filter(Optional::isPresent)
                    .findFirst()
                    .orElse(Optional.empty());

            if (variable.isEmpty()) {
                logger.traceback("Variable '" + variableName + "' was not found. Did you spell the name correctly?", at, LoggingLevel.ANALYZING_ERROR,
                        "Variables are used like so: '$(variablename)'.");
                encounteredError = true;
                temp = "";
                break;
            }
            temp = temp.replace("$(" + variableName + ")", variable.get().value());
        }
        return temp;
    }

    private void variableRedefinedError(@NotNull final String name, @NotNull final Token at) {
        logger.traceback("The variable '" + name + "' was already defined. Cannot redefine existing variables.", at, LoggingLevel.ANALYZING_ERROR,
                "Try renaming your old variable, or alternatively, your new variable, to something unique.");
        encounteredError = true;
    }

    private void definePackVariable(@NotNull final Node statement, @NotNull final PackFile addTo) {
        final boolean global = statement.type() == NodeType.GLOBAL_STATEMENT;
        final Token let = statement.child(0).value();
        final Token ident = statement.child(1).value();
        final Token value = statement.child(3).value();

        if (ident == null || value == null || let == null) {
            workspaceError("An unexpected error occurred, invalid variable definition node");
            return;
        }
        final String finalValue = replaceVariables(value.noStringLiterals(), value, workspace.globalVariables(), addTo.variables());
        final PackVariable variable = new PackVariable(ident.token(), finalValue);
        final String name = variable.name();

        if (global) {
            if (workspace.variableDefined(name)) variableRedefinedError(name, let);
            workspace.defineVariable(variable);
            return;
        }
        if (addTo.variableDefined(name)) variableRedefinedError(name, let);
        addTo.defineVariable(variable);
    }

    private void readMatchStatement(@NotNull final Node statement, @NotNull final PackFile addTo, @NotNull final File root) {
        final List<PackSimplePredicate> matchPredicates = statement.children()
                .stream()
                .filter(n -> n.type() == NodeType.PREDICATE_STATEMENT)
                .map(p -> defineSimplePredicate(p, addTo, null, root))
                .filter(Objects::nonNull)
                .toList();

        final Optional<Node> customWeightStatement = statement.children()
                .stream()
                .filter(n -> n.type() == NodeType.WEIGHT_STATEMENT)
                .findFirst();

        boolean couldNotGetToken;
        Optional<Token> customWeightToken = Optional.empty();
        try {
            customWeightToken = customWeightStatement.map(n -> n.child(2).value());
            couldNotGetToken = customWeightStatement.isPresent() && customWeightToken.isEmpty();
        } catch (final IndexOutOfBoundsException e) {
            couldNotGetToken = true;
        }
        if (couldNotGetToken) {
            workspaceError("An unexpected error occurred, could not retrieve custom weight from match statement");
            return;
        }

        final Optional<Integer> customWeight;
        try {
            customWeight = customWeightToken
                    .map(t -> replaceVariables(t.noStringLiterals(), t, workspace.globalVariables(), addTo.variables()))
                    .map(Integer::parseInt);
        } catch (final NumberFormatException e) {
            customWeightToken.ifPresent(t -> logger.traceback("Could not parse custom weight '" + t.noStringLiterals() + "', not a valid integer.", t, LoggingLevel.ANALYZING_ERROR));
            encounteredError = true;
            return;
        }

        final PackMatchPredicate matchPredicate = new PackMatchPredicate(matchPredicates, customWeight.orElse(0));
        final Optional<Node> forStatement = statement.children().stream().filter(n -> n.type() == NodeType.FOR_STATEMENT).findFirst();

        if (forStatement.isEmpty()) {
            workspaceError("An unexpected error occurred, match scope does not have corresponding for scope after it");
            return;
        }
        readForStatement(forStatement.get(), addTo, matchPredicate, root);
        addTo.definePredicate(matchPredicate);
    }

    private void readAnyStatement(@NotNull final Node statement, @NotNull final PackFile addTo, @NotNull final File root) {
        final PackAnyPredicate matchPredicate = new PackAnyPredicate();

        readForStatement(statement, addTo, matchPredicate, root);
        addTo.definePredicate(matchPredicate);
    }

    private void readCopyStatement(@NotNull final Node statement, @NotNull final PackFile addTo, @NotNull final File root) {
        final List<PackCopyFromToPredicate> copyPredicates = statement.children()
                .stream()
                .filter(n -> n.type() == NodeType.COPY_FROM_TO_STATEMENT)
                .map(p -> defineCopyFromToPredicate(p, addTo, root))
                .filter(Objects::nonNull)
                .toList();

        addTo.definePredicate(new PackCopyPredicate(copyPredicates));
    }

    private void defineModel(@NotNull final Node statement, @NotNull final PackFile addTo, @NotNull final PackMatchPredicate matchPredicate) {
        final Token jsonToken = statement.child(2).value();
        if (jsonToken == null) {
            workspaceError("An unexpected error occurred, could not get value of JSON file in custom model specification node");
            return;
        }
        final Node parentNode = statement.parent();
        if (parentNode == null) {
            workspaceError("An unexpected error occurred, could not get parent of malformed custom model specification node");
            return;
        }
        final List<Optional<Token>> optionalIdentList = parentNode
                .children()
                .stream()
                .filter(n -> n.type() == NodeType.IDENTIFIER_LIST)
                .map(n -> n.children().stream().map(n2 -> Optional.ofNullable(n2.value())).toList())
                .flatMap(Collection::stream)
                .toList();

        if (optionalIdentList.stream().anyMatch(Optional::isEmpty)) {
            workspaceError("An unexpected error occurred, could not retrieve full identifier list of custom model specification node");
            return;
        }
        final List<Token> keys = optionalIdentList
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get).toList();

        final String json = replaceVariables(jsonToken.noStringLiterals(), jsonToken, workspace.globalVariables(), addTo.variables());
        final PackItemModelPredicate itemModelPredicate = new PackItemModelPredicate(keys, json);
        matchPredicate.predicates().add(itemModelPredicate);
    }

    private void readForStatement(@NotNull final Node forStatement, @NotNull final PackFile addTo, @NotNull final PackMatchPredicate matchPredicate, @NotNull final File root) {
        forStatement.children().forEach(s -> {
            if (encounteredError) return;
            switch (s.type()) {
                case ITEM_LISTING_PREDICATE, ARMOR_LISTING_PREDICATE,
                        ARMOR_L1_LISTING_PREDICATE, ARMOR_L2_LISTING_PREDICATE, ELYTRA_LISTING_PREDICATE -> readForStatement(s, addTo, matchPredicate, root);
                case PREDICATE_STATEMENT -> defineSimplePredicate(s, addTo, matchPredicate, root);
                case ARMOR_SETALL_PREDICATE, ARMOR_L1_SETALL_PREDICATE, ARMOR_L2_SETALL_PREDICATE,
                        ITEM_SETALL_PREDICATE, ELYTRA_SETALL_PREDICATE -> defineSetAllPredicate(s, addTo, matchPredicate, root);
                case MAPALL_PREDICATE -> defineMapAllPredicate(s, addTo, matchPredicate, root);
                case MODEL_STATEMENT -> defineModel(s, addTo, matchPredicate);
                case LITERAL_FOR, LITERAL_ARMOR, LITERAL_ARMOR_L1, LITERAL_ARMOR_L2, LITERAL_ELYTRAS, LITERAL_ITEMS, LITERAL_ANY, IDENTIFIER_LIST -> {}
                default -> workspaceError("An unexpected error occurred, invalid match-for node: unexpected sub-node " + s.type().name());
            }
        });
    }

    @NotNull
    private PackFile readPackFileNode(@NotNull final Pair<File, Node> tree, @Nullable final PackFile alreadyExisting, @NotNull final File root) {
        final PackFile addTo = alreadyExisting == null ? new PackFile(tree.getLeft(), root) : alreadyExisting;
        tree.getRight().children().forEach(statement -> {
            switch (statement.type()) {
                case MATCH_STATEMENT -> readMatchStatement(statement, addTo, root);
                case ANY_STATEMENT -> readAnyStatement(statement, addTo, root);
                case COPY_STATEMENT -> readCopyStatement(statement, addTo, root);
            }
        });
        return addTo;
    }

    @NotNull
    private PackFile preprocessPackFile(@NotNull final Pair<File, Node> tree, @Nullable final PackFile alreadyExisting, @NotNull final File root) {
        final PackFile addTo = alreadyExisting == null ? new PackFile(tree.getLeft(), root) : alreadyExisting;
        tree.getRight().children().forEach(statement -> {
            switch (statement.type()) {
                case LET_STATEMENT, GLOBAL_STATEMENT -> definePackVariable(statement, addTo);
            }
        });
        return addTo;
    }

    private void readPackFiles(@NotNull final Collection<Pair<File, Node>> trees, @NotNull final File root) {
        trees.stream().map(t -> readPackFileNode(t, root)).forEach(workspace.packFiles()::add);
    }

    @NotNull
    private Set<Pair<File, Node>> parseAllOfDirectory(@NotNull final File directory) {
        logger.info("Parsing all files of directory '" + directory.getAbsolutePath() + "'...");

        if (!directory.isDirectory()) throw new IllegalArgumentException("Could not find directory: " + directory);
        try (final Stream<Path> paths = Files.walk(directory.toPath())) {
            final List<File> files = paths.map(Path::toFile)
                    .filter(File::isFile)
                    .filter(f -> f.getName().endsWith(".rep")).toList();

            final List<Pair<File, Optional<Node>>> optionalNodes = files.stream().map(f -> Pair.of(f, parse(f))).toList();
            if (optionalNodes.stream().anyMatch(p -> p.getRight().isEmpty())) throw new Exception("See previous error");

            return optionalNodes.stream().filter(p -> p.getRight().isPresent()).map(p -> Pair.of(p.getLeft(), p.getRight().get())).collect(Collectors.toSet());
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
            workspaceError("Could not parse pack file '" + packFile.getAbsolutePath() + "'.");
            return Optional.empty();
        }
        final long singleEndTime = System.currentTimeMillis();

        logger.log("\t\tSuccessfully parsed pack file '" + packFile.getAbsolutePath() + "' in " + (singleEndTime - singleStartTime) + "ms.", LoggingLevel.SUCCESS);
        return tree;
    }


}
