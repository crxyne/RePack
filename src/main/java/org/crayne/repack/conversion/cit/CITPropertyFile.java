package org.crayne.repack.conversion.cit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.crayne.repack.conversion.util.VanillaItem;
import org.crayne.repack.conversion.util.TextureType;
import org.crayne.repack.core.single.predicate.PackMatchPredicate;
import org.crayne.repack.core.single.predicate.PackPredicate;
import org.crayne.repack.core.single.predicate.PackSimplePredicate;
import org.crayne.repack.core.single.predicate.PackSupredicate;
import org.crayne.repack.parsing.lexer.Token;
import org.crayne.repack.util.logging.Logger;
import org.crayne.repack.util.logging.LoggingLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class CITPropertyFile {

    @NotNull
    private final TextureType type;

    @NotNull
    private final String textureFilePath;

    @NotNull
    private final ItemMatch itemMatch;

    @NotNull
    private final Set<PackSimplePredicate> predicates;

    public CITPropertyFile(@NotNull final TextureType type, @NotNull final String textureFilePath,
                           @NotNull final ItemMatch itemMatch, @NotNull final Collection<PackSimplePredicate> predicates) {
        this.type = type;
        this.textureFilePath = textureFilePath;
        this.itemMatch = itemMatch;
        this.predicates = new HashSet<>(predicates);
    }

    @NotNull
    public String textureFilePath() {
        return textureFilePath.endsWith(".png") ? textureFilePath : textureFilePath + ".png";
    }

    @NotNull
    public ItemMatch itemMatch() {
        return itemMatch;
    }

    @NotNull
    public TextureType type() {
        return type;
    }

    @NotNull
    public Set<PackSimplePredicate> predicates() {
        return predicates;
    }

    private record ItemMatch(@NotNull String texture, @Nullable TextureType type, @NotNull Collection<String> items, boolean matchAll) {

    }

    @NotNull
    private static ItemMatch findItemMatches(@NotNull final PackPredicate p, @NotNull final Logger logger) {
        final TextureType textureType = TextureType.of(p.type());
        if (textureType == null) throw new RuntimeException("Match statement was found inside another");

        return new ItemMatch(p.value(), textureType, new ArrayList<>(p.keys()
                .stream()
                .map(tok -> Pair.of(tok, VanillaItem.moddedItem(tok.token()) ? Collections.singletonList(tok.token()) : VanillaItem.allMatching(tok.token()).stream().map(Enum::name).toList()))
                .map(pair -> {
                    final List<String> matched = pair.getRight();
                    final Token tok = pair.getLeft();

                    if (matched.isEmpty()) logger.traceback("No item matches were found for predicate key = '" + tok.token() + "'", tok, LoggingLevel.WARN);
                    return matched;
                })
                .flatMap(Collection::stream)
                .toList()), false);
    }

    private static boolean isSettingAll(@NotNull final Collection<PackPredicate> packPredicates, @NotNull final PackMatchPredicate matchPredicate) {
        return packPredicates.stream().anyMatch(p -> p instanceof PackSupredicate
                || matchPredicate.keys().isEmpty()
                || p.keys().stream().anyMatch(t -> t.token().equals("*")));
    }

    @NotNull
    private static Set<ItemMatch> matchAll(@NotNull final Collection<PackPredicate> predicates) {
        return predicates.stream()
                .map(p -> new ItemMatch(p.value(), TextureType.of(p.type()), new HashSet<>(), true))
                .peek(i -> Objects.requireNonNull(i.type()))
                .collect(Collectors.toSet());
    }

    @NotNull
    private static Set<ItemMatch> findMatches(@NotNull final Collection<PackPredicate> predicates, @NotNull final Logger logger) {
        return predicates.stream()
                .map(p -> findItemMatches(p, logger))
                .collect(Collectors.toSet());
    }

    @NotNull
    private static Map<TextureType, List<ItemMatch>> groupItemMatches(@NotNull final Map<TextureType, List<ItemMatch>> ungrouped) {
        final Map<TextureType, List<ItemMatch>> itemMatchesGrouped = new HashMap<>();

        ungrouped.forEach((t, is) -> {
            final List<ItemMatch> found = ungrouped.get(t);
            found.forEach(i -> {
                final Optional<ItemMatch> existingMatch = itemMatchesGrouped.values().stream()
                        .map(i2 -> i2.stream().filter(i3 -> i3.texture().equals(i.texture())).findAny())
                        .filter(Optional::isPresent)
                        .flatMap(Optional::stream)
                        .findAny();

                itemMatchesGrouped.putIfAbsent(t, new ArrayList<>());

                if (existingMatch.isPresent()) {
                    existingMatch.get().items().addAll(i.items());
                    return;
                }
                itemMatchesGrouped.get(t).add(i);
            });
        });
        return itemMatchesGrouped;
    }

    @NotNull
    private static Map<TextureType, List<ItemMatch>> findMatches(@NotNull final Map<String, Set<PackPredicate>> textureFileMap,
                                                                 @NotNull final PackMatchPredicate matchPredicate, @NotNull final Logger logger) {
        return textureFileMap.keySet().stream().map(s -> {
                    final Collection<PackPredicate> packPredicates = textureFileMap.get(s);
                    final boolean setall = isSettingAll(packPredicates, matchPredicate);
                    return setall ? matchAll(packPredicates) : findMatches(packPredicates, logger);
                })
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(ItemMatch::type));
    }

    @NotNull
    private static Map<String, Set<PackPredicate>> textureFileMap(@NotNull final PackMatchPredicate matchPredicate) {
        final Map<String, Set<PackPredicate>> textureFileMap = new HashMap<>();
        matchPredicate.predicates().forEach(p -> {
            textureFileMap.putIfAbsent(p.value(), new HashSet<>());
            textureFileMap.get(p.value()).add(p);
        });
        return textureFileMap;
    }

    @NotNull
    public static Set<CITPropertyFile> of(@NotNull final PackMatchPredicate matchPredicate, @NotNull final Logger logger, @NotNull final File out) {
        final Map<String, Set<PackPredicate>> textureFileMap = textureFileMap(matchPredicate);
        final Map<TextureType, List<ItemMatch>> itemMatches = findMatches(textureFileMap, matchPredicate, logger);

        final Map<TextureType, List<ItemMatch>> itemMatchesGrouped = groupItemMatches(itemMatches);

        return itemMatchesGrouped.keySet()
                .stream()
                .map(t -> itemMatchesGrouped.get(t)
                        .stream()
                        .map(i -> new CITPropertyFile(t, i.texture(), i, matchPredicate.matchPredicates()))
                        .collect(Collectors.toSet()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @NotNull
    private String textureNamespacedKey(@NotNull final TextureType type, @NotNull final String s, @NotNull final String value) {
        return switch (type) {
            case ARMOR -> {
                final String armor = StringUtils.substringBefore(s, "_");
                yield "texture." + armor + "_layer_1" + "=" + value + "\n" + "texture." + armor + "_layer_2" + "=" + value;
            }
            case ELYTRAS -> "texture.elytra" + "=" + value;
            case ITEMS -> "texture." + s + "=" + value;
        };
    }

    @NotNull
    private String textureOverrideAsString() {
        if (itemMatch.matchAll) return "texture=" + itemMatch.texture();
        final List<String> overrides = itemMatch.items()
                .stream()
                .map(s -> {
                    assert itemMatch.type() != null;
                    return textureNamespacedKey(itemMatch.type(), s, itemMatch.texture());
                })
                .distinct()
                .toList();

        return String.join("\n", overrides) + "\n";
    }

    @NotNull
    private String itemMatchAsString() {
        return itemMatch.matchAll
                ? ""
                : "items=" + String.join(" ", itemMatch.items()) + "\n";
    }

    @NotNull
    private String nbtMatchAsString() {
        return predicates.stream()
                .map(p -> "nbt." + p.key().token() + "=" + p.value())
                .collect(Collectors.joining("\n"));
    }

    @NotNull
    private String textureTypeAsString() {
        return "type=" + type + "\n";
    }

    @NotNull
    public String compile() {
        return textureTypeAsString()
                + itemMatchAsString()
                + textureOverrideAsString()
                + nbtMatchAsString();
    }

    @NotNull
    public String toString() {
        return "CITPropertyFile {\n" + compile().indent(3) + "}";
    }

}
