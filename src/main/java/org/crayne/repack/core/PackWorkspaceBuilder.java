package org.crayne.repack.core;

import org.crayne.repack.core.single.PackVariable;
import org.crayne.repack.logging.Logger;
import org.crayne.repack.logging.LoggingLevel;
import org.crayne.repack.parsing.ast.Node;
import org.crayne.repack.parsing.ast.NodeType;
import org.crayne.repack.parsing.parser.Parser;
import org.crayne.repack.parsing.parser.TreeAnalyzer;
import org.jetbrains.annotations.NotNull;

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

    public void setup(@NotNull final File directory) {
        logger.info("Parsing files...");
        final Set<Node> trees = parseAllOfDirectory(directory);
        if (encounteredError) {
            workspaceError("Could not open workspace due to previous error; aborting.");
            return;
        }
        if (trees.isEmpty()) {
            logger.info("Nothing was parsed; no operation was performed.");
            return;
        }
        logger.log("Successfully read all pack files of workspace.", LoggingLevel.SUCCESS);
        logger.info("Creating global variables...");
        final Set<PackVariable> variables = createGlobalVariables(trees);
        logger.info("" + variables);
    }

    @NotNull
    private Set<PackVariable> createGlobalVariables(@NotNull final Collection<Node> trees) {
        return trees.stream()
                .map(Node::children)
                .map(n -> n.stream()
                        .filter(n2 -> n2.type() == NodeType.GLOBAL_STATEMENT)
                        .collect(Collectors.toSet())
                )
                .flatMap(Set::stream)
                .map(PackVariable::of)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
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
