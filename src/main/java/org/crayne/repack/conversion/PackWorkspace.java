package org.crayne.repack.conversion;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.crayne.repack.conversion.cit.CITPropertyFile;
import org.crayne.repack.core.PackWorkspaceBuilder;
import org.crayne.repack.core.single.PackFile;
import org.crayne.repack.core.single.PackVariable;
import org.crayne.repack.core.single.predicate.PackMatchPredicate;
import org.crayne.repack.util.StringUtil;
import org.crayne.repack.util.logging.Logger;
import org.crayne.repack.util.logging.LoggingLevel;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    public static Optional<PackWorkspace> of(@NotNull final File in) {
        return new PackWorkspaceBuilder().setup(in);
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
        final Set<Pair<PackFile, Set<CITPropertyFile>>> propertiesFiles = packFiles.stream().map(p -> Pair.of(p, p.matches()
                        .stream()
                        .map(m -> CITPropertyFile.of((PackMatchPredicate) m, logger, out))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet())))
                .collect(Collectors.toSet());

        final AtomicBoolean success = new AtomicBoolean(true);
        propertiesFiles.forEach(pair -> {
                    final PackFile p = pair.getLeft();
                    logger.info("   Compiling pack file '" + p.file().getAbsolutePath() + "'...");
                    final int amt = pair.getRight().size();
                    if (amt == 0) {
                        logger.log("Successfully compiled pack file (no operation was performed).", LoggingLevel.SUCCESS);
                        return;
                    }
                    logger.info("   Creating " + amt + " CIT properties files...");

                    pair.getRight().forEach(property -> {
                        final File file = new File(cit, StringUtils.substringAfterLast(property.textureFilePath(), "/") + ".properties");
                        try {
                            Files.writeString(file.toPath(), property.compile());
                        } catch (final IOException e) {
                            logger.error("  Could not create output pack file '" + file.getAbsolutePath() + "': " + e.getMessage());
                            e.printStackTrace(logger);
                            success.set(false);
                            return;
                        }
                        final String destinationPath = property.textureFilePath();
                        logger.info("   Copying texture (" + destinationPath + ")...");
                        final File originalTextureFile = new File(p.root(), destinationPath);

                        final File textureFile = new File(file.getParentFile(),
                                destinationPath.endsWith(".png")
                                        ? destinationPath
                                        : destinationPath + ".png"
                        );

                        //noinspection ResultOfMethodCallIgnored
                        textureFile.getParentFile().mkdirs();
                        try {
                            Files.copy(originalTextureFile.toPath(), textureFile.toPath());
                        } catch (final IOException e) {
                            logger.error("  Could not copy pack file texture '" + originalTextureFile.getAbsolutePath() + "' to '" + textureFile.getAbsolutePath() + "': " + e.getMessage());
                            e.printStackTrace(logger);
                            success.set(false);
                        }
                    });
                    logger.log("Successfully compiled pack file.", LoggingLevel.SUCCESS);
                });
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
