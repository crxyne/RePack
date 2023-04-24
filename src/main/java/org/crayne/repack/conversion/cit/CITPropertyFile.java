package org.crayne.repack.conversion.cit;

import org.apache.commons.lang3.tuple.Pair;
import org.crayne.repack.conversion.match.ItemMatch;
import org.crayne.repack.conversion.util.VanillaItem;
import org.crayne.repack.core.single.predicate.PackMatchPredicate;
import org.crayne.repack.core.single.predicate.PackPredicate;
import org.crayne.repack.core.single.predicate.PackSimplePredicate;
import org.crayne.repack.core.single.predicate.PackSupredicate;
import org.crayne.repack.parsing.lexer.Token;
import org.crayne.repack.util.logging.Logger;
import org.crayne.repack.util.logging.LoggingLevel;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public interface CITPropertyFile {


    @NotNull
    String filePath();

    @NotNull
    String fileName();

    @NotNull
    String fileNameNoFiletype();

    @NotNull
    ItemMatch itemMatch();

    @NotNull
    Set<PackSimplePredicate> predicates();

    int weight();

    @NotNull
    String compile();

    @NotNull
    default String itemMatchAsString() {
        return itemMatch().matchAll()
                ? ""
                : "items=" + String.join(" ", itemMatch().items()) + "\n";
    }

    @NotNull
    default String nbtMatchAsString() {
        return predicates().stream()
                .map(p -> "nbt." + p.key().token() + "=" + p.value())
                .collect(Collectors.joining("\n")) + (weight() == 0 ? "" : "\n");
    }

    @NotNull
    default String weightAsString() {
        return weight() == 0 ? "" : "weight=" + weight();
    }

    static boolean isSettingAll(@NotNull final Collection<PackPredicate> packPredicates, @NotNull final PackMatchPredicate matchPredicate) {
        return packPredicates.stream().anyMatch(p -> p instanceof PackSupredicate
                || matchPredicate.keys().isEmpty()
                || p.keys().stream().anyMatch(t -> t.token().equals("*")));
    }

    @NotNull
    default File finalizedFile(@NotNull final File cit) {
        File file;
        int copyNumber = 0;
        do {
            file = new File(cit, fileNameNoFiletype() + (copyNumber == 0 ? "" : String.valueOf(copyNumber)) + ".properties");
            copyNumber++;
        } while (file.exists());
        return file;
    }

    @NotNull
    static List<String> findItemsMatchingPredicate(@NotNull final PackPredicate p, @NotNull final Logger logger) {
        return p.keys()
                .stream()
                .map(tok -> Pair.of(tok, VanillaItem.moddedItem(tok.token())
                        ? Collections.singletonList(tok.token())
                        : VanillaItem.allMatching(tok.token())
                        .stream()
                        .map(Enum::name)
                        .toList())
                )
                .map(pair -> {
                    final List<String> matched = pair.getRight();
                    final Token tok = pair.getLeft();

                    if (matched.isEmpty()) logger.traceback("No item matches were found for predicate key = '" + tok.token() + "'", tok, LoggingLevel.WARN);
                    return matched;
                })
                .flatMap(Collection::stream)
                .toList();
    }

    @NotNull
    static Set<CITPropertyFile> of(@NotNull final PackMatchPredicate matchPredicate, @NotNull final Logger logger) {
        final Set<CITTexturePropertyFile> textureFiles = CITTexturePropertyFile.of(matchPredicate, logger);
        final Set<CITModelPropertyFile> modelFiles = CITModelPropertyFile.of(matchPredicate, logger);

        final Set<CITPropertyFile> result = new HashSet<>();
        result.addAll(textureFiles);
        result.addAll(modelFiles);
        return result;
    }

}
