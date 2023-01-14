package org.crayne.repack.core;

import org.crayne.repack.core.single.PackFile;
import org.crayne.repack.core.single.PackVariable;
import org.crayne.repack.util.StringUtil;
import org.crayne.repack.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
    public String toString() {
        return "PackWorkspace {\n" +
                ("packFiles = " + StringUtil.stringOf(packFiles) +
                ", globalVariables = " + StringUtil.stringOf(globalVariables)).indent(3) +
                '}';
    }

}
