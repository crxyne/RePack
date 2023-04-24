package org.crayne.repack.conversion;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.crayne.repack.conversion.cit.CITModelPropertyFile;
import org.crayne.repack.conversion.cit.CITPropertyFile;
import org.crayne.repack.core.PackWorkspaceBuilder;
import org.crayne.repack.core.single.PackFile;
import org.crayne.repack.core.single.PackVariable;
import org.crayne.repack.core.single.predicate.PackCopyPredicate;
import org.crayne.repack.core.single.predicate.PackMatchPredicate;
import org.crayne.repack.util.StringUtil;
import org.crayne.repack.util.logging.Logger;
import org.crayne.repack.util.logging.LoggingLevel;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class PackWorkspace {

    @NotNull
    private final Set<PackFile> packFiles;

    @NotNull
    private final List<PackVariable> globalVariables;

    @NotNull
    private final Logger logger;

    public PackWorkspace(@NotNull final Logger logger) {
        this.logger = logger;
        this.packFiles = new HashSet<>();
        this.globalVariables = new ArrayList<>();
    }

    public PackWorkspace(@NotNull final Logger logger, @NotNull final Collection<PackFile> packFiles, @NotNull final Collection<PackVariable> globalVariables) {
        this.logger = logger;
        this.packFiles = new HashSet<>(packFiles);
        this.globalVariables = new ArrayList<>(globalVariables);
    }

    @NotNull
    public Logger logger() {
        return logger;
    }

    @NotNull
    public Set<PackFile> packFiles() {
        return packFiles;
    }

    @NotNull
    public List<PackVariable> globalVariables() {
        return globalVariables;
    }

    public void defineVariable(@NotNull final PackVariable variable) {
        globalVariables.add(variable);
    }

    public boolean variableDefined(@NotNull final String name) {
        return globalVariables.stream().anyMatch(v -> v.name().equals(name))
                || packFiles.stream().anyMatch(p -> p.variableDefined(name));
    }

    @NotNull
    public static Optional<PackWorkspace> of(@NotNull final Path inPath) {
        return new PackWorkspaceBuilder().setup(inPath.toFile());
    }

    @NotNull
    public static Optional<PackWorkspace> of(@NotNull final String inPath) {
        return new PackWorkspaceBuilder().setup(new File(inPath));
    }

    @NotNull
    public static Optional<PackWorkspace> of(@NotNull final File in) {
        return new PackWorkspaceBuilder().setup(in);
    }

    @NotNull
    public static Optional<PackWorkspace> of(@NotNull final Logger logger, @NotNull final File in) {
        return new PackWorkspaceBuilder(logger).setup(in);
    }

    @NotNull
    public static Optional<PackWorkspace> of(@NotNull final Logger logger, @NotNull final Path inPath) {
        return new PackWorkspaceBuilder(logger).setup(inPath.toFile());
    }

    @NotNull
    public static Optional<PackWorkspace> of(@NotNull final Logger logger, @NotNull final String inPath) {
        return new PackWorkspaceBuilder(logger).setup(new File(inPath));
    }

    public boolean compile(@NotNull final Path outPath) {
        return compile(outPath.toFile());
    }

    public boolean compile(@NotNull final String outPath) {
        return compile(new File(outPath));
    }

    public boolean compile(@NotNull final File out) {
        logger.info("Compiling workspace (" + packFiles.size() + " pack files)...");
        try {
            if (out.isDirectory()) {
                logger.info("Deleting old pack output...");
                FileUtils.deleteDirectory(out);
            }
        } catch (final IOException e) {
            logger.error("Could not delete old pack output directory '" + out.getAbsolutePath() + "': " + e.getMessage());
            e.printStackTrace(logger);
            return false;
        }
        if (!out.mkdirs()) {
            logger.error("Could not create new pack output directory '" + out.getAbsolutePath() + "'.");
            return false;
        }
        final File cit = new File(out, "assets/minecraft/optifine/cit");
        if (!cit.mkdirs()) {
            logger.error("Could not create pack CIT directory '" + cit.getAbsolutePath() + "'.");
            return false;
        }

        final AtomicBoolean success = new AtomicBoolean(true);
        packFiles.stream()
                .map(p -> Pair.of(p, p.matches().stream()
                        .filter(pr -> pr instanceof PackCopyPredicate)
                        .map(pr -> (PackCopyPredicate) pr)
                        .map(pr -> pr.copyFiles().entrySet())
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet())))
                .forEach(packFilePair -> {
                    final Set<Map.Entry<String, String>> copyFiles = packFilePair.getValue();
                    final PackFile packFile = packFilePair.getLeft();
                    copyFiles.forEach(copyFile -> {
                        final File from = new File(packFile.root(), copyFile.getKey());
                        final File to = new File(out, copyFile.getValue());

                        if (!from.isFile()) {
                            logger.error("Could not execute copy statement: file source was not found (source = " + from.getAbsolutePath() + ", destination = " + to.getAbsolutePath() + ")");
                            success.set(false);
                            return;
                        }
                        if (to.isFile()) {
                            logger.warn("Copy statement warning: file destination already exists and will be replaced (source = " + from.getAbsolutePath() + ", destination = " + to.getAbsolutePath() + ")");
                            return;
                        }
                        try {
                            //noinspection ResultOfMethodCallIgnored
                            to.mkdirs();
                            Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            logger.error("Could not execute copy statement (source = " + from.getAbsolutePath() + ", destination = " + to.getAbsolutePath() + "): " + e.getMessage());
                            e.printStackTrace(logger);
                            success.set(false);
                            return;
                        }
                        logger.info("Copied file from " + from.getAbsolutePath() + " to " + to.getAbsolutePath() + " successfully");
                    });
                });

        if (!success.get()) {
            logger.error("Could not compile workspace; see above error.");
            return false;
        }

        final Set<Pair<PackFile, Set<CITPropertyFile>>> propertiesFiles = packFiles.stream().map(p -> Pair.of(p, p.matches()
                        .stream()
                        .filter(pr -> pr instanceof PackMatchPredicate)
                        .map(m -> CITPropertyFile.of((PackMatchPredicate) m, logger))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet())))
                .collect(Collectors.toSet());

        propertiesFiles.forEach(pair -> {
                    final PackFile p = pair.getLeft();
                    logger.info("\tCompiling pack file '" + p.file().getAbsolutePath() + "'...");
                    final int amt = pair.getRight().size();
                    if (amt == 0) {
                        logger.log("Successfully compiled pack file (no operation was performed).", LoggingLevel.SUCCESS);
                        return;
                    }
                    logger.info("\tCreating " + amt + " CIT properties file" + (amt == 1 ? "" : "s") + "...");

                    pair.getRight().forEach(property -> {
                        final File file = property.finalizedFile(cit);
                        try {
                            Files.writeString(file.toPath(), property.compile());
                        } catch (final IOException e) {
                            logger.error("\tCould not create output pack file '" + file.getAbsolutePath() + "': " + e.getMessage());
                            e.printStackTrace(logger);
                            success.set(false);
                            return;
                        }
                        if (property instanceof final CITModelPropertyFile modelPropertyFile) return;

                        final String destinationName = property.fileName();
                        logger.info("\t\tCopying texture (" + destinationName + ")...");

                        final File sourceTextureFile = new File(p.root(), property.filePath());
                        final File destinationTextureFile = new File(file.getParentFile(),
                                destinationName.endsWith(".png")
                                        ? destinationName
                                        : destinationName + ".png"
                        );

                        //noinspection ResultOfMethodCallIgnored
                        destinationTextureFile.getParentFile().mkdirs();
                        try {
                            Files.copy(sourceTextureFile.toPath(), destinationTextureFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (final IOException e) {
                            logger.error("Could not copy pack file texture '" + sourceTextureFile.getAbsolutePath() + "' to '" + destinationTextureFile.getAbsolutePath() + "': " + e.getMessage());
                            e.printStackTrace(logger);
                            success.set(false);
                        }
                    });
                    logger.log("\tSuccessfully compiled pack file.", LoggingLevel.SUCCESS);
                });
        if (success.get()) logger.log("Successfully compiled workspace to '" + out.getAbsolutePath() + "'.", LoggingLevel.SUCCESS);
        else logger.error("Could not compile workspace; see above error.");
        return success.get();
    }

    @NotNull
    public String toString() {
        return "PackWorkspace {\n" +
                ("packFiles = " + StringUtil.stringOf(packFiles) +
                ", globalVariables = " + StringUtil.stringOf(globalVariables)).indent(3) +
                '}';
    }

}
