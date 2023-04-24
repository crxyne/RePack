package org.crayne.repack.conversion.cit;

import org.apache.commons.lang3.StringUtils;
import org.crayne.repack.conversion.match.ItemTextureMatch;
import org.crayne.repack.conversion.util.TextureType;
import org.crayne.repack.core.single.predicate.PackItemModelPredicate;
import org.crayne.repack.core.single.predicate.PackMatchPredicate;
import org.crayne.repack.core.single.predicate.PackPredicate;
import org.crayne.repack.core.single.predicate.PackSimplePredicate;
import org.crayne.repack.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class CITTexturePropertyFile implements CITPropertyFile {

    @NotNull
    private final TextureType type;

    @NotNull
    private final String filePath;

    @NotNull
    private final ItemTextureMatch itemMatch;

    @NotNull
    private final Set<PackSimplePredicate> predicates;

    private final int weight;

    public CITTexturePropertyFile(@NotNull final TextureType type, @NotNull final String filePath,
                                  @NotNull final ItemTextureMatch itemMatch, @NotNull final Collection<PackSimplePredicate> predicates,
                                  final int weight) {
        this.type = type;
        this.filePath = filePath;
        this.itemMatch = itemMatch;
        this.predicates = new HashSet<>(predicates);
        this.weight = weight;
    }

    @NotNull
    public String filePath() {
        return filePath.endsWith(".png") ? filePath : filePath + ".png";
    }

    @NotNull
    public String fileName() {
        final String path = filePath();
        return path.contains("/") ? StringUtils.substringAfterLast(path, "/") : path;
    }

    @NotNull
    public String fileNameNoFiletype() {
        final String name = fileName();
        return name.contains(".") ? StringUtils.substringBefore(name, ".") : name;
    }

    @NotNull
    public String textureFileNameNoPNG() {
        final String name = fileName();
        return name.toLowerCase().endsWith(".png") ? name.substring(0, name.length() - ".png".length()) : name;
    }

    @NotNull
    public ItemTextureMatch itemMatch() {
        return itemMatch;
    }

    @NotNull
    public TextureType type() {
        return type;
    }

    public int weight() {
        return weight;
    }

    @NotNull
    public Set<PackSimplePredicate> predicates() {
        return predicates;
    }

    @NotNull
    public String compile() {
        return textureTypeAsString()
                + itemMatchAsString()
                + textureOverrideAsString()
                + nbtMatchAsString()
                + weightAsString();
    }

    @NotNull
    public String toString() {
        return "CITTexturePropertyFile {\n" + compile().indent(3) + "}";
    }

    @NotNull
    private static ItemTextureMatch findItemMatches(@NotNull final PackPredicate p, @NotNull final Logger logger) {
        final TextureType textureType = TextureType.of(p.type());
        if (textureType == null) throw new RuntimeException("Match statement was found inside another");

        return new ItemTextureMatch(p.value(), textureType, new ArrayList<>(CITPropertyFile.findItemsMatchingPredicate(p, logger)), false);
    }

    @NotNull
    private static Set<ItemTextureMatch> matchAll(@NotNull final Collection<PackPredicate> predicates) {
        return predicates.stream()
                .map(p -> new ItemTextureMatch(p.value(), TextureType.of(p.type()), new HashSet<>(), true))
                .peek(i -> Objects.requireNonNull(i.type()))
                .collect(Collectors.toSet());
    }

    @NotNull
    private static Set<ItemTextureMatch> findMatches(@NotNull final Collection<PackPredicate> predicates, @NotNull final Logger logger) {
        return predicates.stream()
                .map(p -> findItemMatches(p, logger))
                .collect(Collectors.toSet());
    }

    @NotNull
    private static Map<TextureType, List<ItemTextureMatch>> groupItemTextureMatches(@NotNull final Map<TextureType, List<ItemTextureMatch>> ungrouped) {
        final Map<TextureType, List<ItemTextureMatch>> itemMatchesGrouped = new HashMap<>();

        ungrouped.forEach((t, is) -> {
            final List<ItemTextureMatch> found = ungrouped.get(t);
            found.forEach(i -> {
                final Optional<ItemTextureMatch> existingMatch = itemMatchesGrouped.values().stream()
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
    private static Map<TextureType, List<ItemTextureMatch>> findMatches(@NotNull final Map<String, Set<PackPredicate>> textureFileMap,
                                                                        @NotNull final PackMatchPredicate matchPredicate, @NotNull final Logger logger) {
        return textureFileMap.keySet().stream().map(s -> {
                    final Collection<PackPredicate> packPredicates = textureFileMap.get(s);
                    final boolean setall = CITPropertyFile.isSettingAll(packPredicates, matchPredicate);
                    return setall ? matchAll(packPredicates) : findMatches(packPredicates, logger);
                })
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(ItemTextureMatch::type));
    }

    @NotNull
    private static Map<String, Set<PackPredicate>> textureFileMap(@NotNull final PackMatchPredicate matchPredicate) {
        final Map<String, Set<PackPredicate>> textureFileMap = new HashMap<>();
        matchPredicate.predicates()
                .stream()
                .filter(p -> !(p instanceof PackItemModelPredicate))
                .forEach(p -> {
            textureFileMap.putIfAbsent(p.value(), new HashSet<>());
            textureFileMap.get(p.value()).add(p);
        });
        return textureFileMap;
    }

    @NotNull
    public static Set<CITTexturePropertyFile> of(@NotNull final PackMatchPredicate matchPredicate, @NotNull final Logger logger) {
        final Map<String, Set<PackPredicate>> textureFileMap = textureFileMap(matchPredicate);
        final Map<TextureType, List<ItemTextureMatch>> itemMatches = findMatches(textureFileMap, matchPredicate, logger);
        final Map<TextureType, List<ItemTextureMatch>> itemMatchesGrouped = groupItemTextureMatches(itemMatches);

        return itemMatchesGrouped.keySet()
                .stream()
                .map(t -> itemMatchesGrouped.get(t)
                        .stream()
                        .map(i -> new CITTexturePropertyFile(t, i.texture(), i, matchPredicate.matchPredicates(), matchPredicate.weight()))
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
            case ARMOR_L1 -> {
                final String armor = StringUtils.substringBefore(s, "_");
                yield "texture." + armor + "_layer_1" + "=" + value;
            }
            case ARMOR_L2 -> {
                final String armor = StringUtils.substringBefore(s, "_");
                yield "texture." + armor + "_layer_2" + "=" + value;
            }
            case ELYTRAS -> "texture.elytra" + "=" + value;
            case ITEMS -> "texture." + s + "=" + value;
        };
    }

    @NotNull
    private String textureOverrideAsString() {
        if (itemMatch.matchAll()) return "texture=" + textureFileNameNoPNG() + "\n";
        final List<String> overrides = itemMatch.items()
                .stream()
                .map(s -> {
                    assert itemMatch.type() != null;
                    return textureNamespacedKey(itemMatch.type(), s, textureFileNameNoPNG());
                })
                .distinct()
                .toList();

        return String.join("\n", overrides) + "\n";
    }

    @NotNull
    private String textureTypeAsString() {
        return "type=" + type + "\n";
    }

}
