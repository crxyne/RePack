package org.crayne.repack;

import org.crayne.repack.conversion.cit.CITPropertyFile;
import org.crayne.repack.core.PackWorkspace;
import org.crayne.repack.core.PackWorkspaceBuilder;
import org.crayne.repack.core.single.PackFile;
import org.crayne.repack.core.single.predicate.PackMatchPredicate;
import org.crayne.repack.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

public class Main {

    public static void main(@NotNull final String... args) {
        final Optional<PackWorkspace> workspace = new PackWorkspaceBuilder().setup(new File("test-workspace"));
        if (workspace.isEmpty()) return;
        workspace.ifPresent(System.out::println);

        workspace.get().packFiles()
                .stream()
                .map(PackFile::matches)
                .flatMap(Collection::stream)
                .map(m -> CITPropertyFile.of((PackMatchPredicate) m, workspace.get().logger()))
                .forEach(s -> System.out.println(StringUtil.stringOf(s)));
    }

}