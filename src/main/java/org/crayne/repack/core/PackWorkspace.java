package org.crayne.repack.core;

import org.crayne.repack.core.single.PackFile;
import org.crayne.repack.core.single.PackVariable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PackWorkspace {

    @NotNull
    private final Set<PackFile> packFiles;

    @NotNull
    private final List<PackVariable> globalVariables;

    public PackWorkspace() {
        this.packFiles = new HashSet<>();
        this.globalVariables = new ArrayList<>();
    }

    public PackWorkspace(@NotNull final Collection<PackFile> packFiles, @NotNull final Collection<PackVariable> globalVariables) {
        this.packFiles = new HashSet<>(packFiles);
        this.globalVariables = new ArrayList<>(globalVariables);
    }

    @NotNull
    public Set<PackFile> packFiles() {
        return packFiles;
    }

    @NotNull
    public List<PackVariable> globalVariables() {
        return globalVariables;
    }


}
