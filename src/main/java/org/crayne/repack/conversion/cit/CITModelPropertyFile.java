package org.crayne.repack.conversion.cit;

import org.apache.commons.lang3.StringUtils;
import org.crayne.repack.conversion.match.ItemModelMatch;
import org.crayne.repack.conversion.util.TextureType;
import org.crayne.repack.core.single.PredicateType;
import org.crayne.repack.core.single.predicate.PackItemModelPredicate;
import org.crayne.repack.core.single.predicate.PackMatchPredicate;
import org.crayne.repack.core.single.predicate.PackPredicate;
import org.crayne.repack.core.single.predicate.PackSimplePredicate;
import org.crayne.repack.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class CITModelPropertyFile implements CITPropertyFile {

    @NotNull
    private final String filePath;

    @NotNull
    private final ItemModelMatch itemMatch;

    @NotNull
    private final Set<PackSimplePredicate> predicates;

    private final int weight;

    @NotNull
    private final PredicateType type;

    public CITModelPropertyFile(@NotNull final PredicateType type, @NotNull final String filePath,
                                @NotNull final ItemModelMatch itemMatch, @NotNull final Collection<PackSimplePredicate> predicates,
                                final int weight) {
        this.type = type;
        this.filePath = filePath;
        this.itemMatch = itemMatch;
        this.predicates = new HashSet<>(predicates);
        this.weight = weight;
    }

    @NotNull
    private String modelJsonAsString() {
        return "model=" + itemMatch.json() + "\n";
    }

    @NotNull
    public String filePath() {
        return filePath;
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
    public ItemModelMatch itemMatch() {
        return itemMatch;
    }

    @NotNull
    public Set<PackSimplePredicate> predicates() {
        return predicates;
    }

    public int weight() {
        return weight;
    }

    @NotNull
    public PredicateType type() {
        return type;
    }

    @NotNull
    private String modelTypeAsString() {
        return "type=" + Objects.requireNonNull(TextureType.of(type)) + "\n";
    }

    @NotNull
    public String compile() {
        return modelTypeAsString()
                + itemMatchAsString()
                + modelJsonAsString()
                + nbtMatchAsString()
                + weightAsString();
    }

    @NotNull
    private static Map<String, Set<PackPredicate>> jsonFileMap(@NotNull final PackMatchPredicate matchPredicate) {
        final Map<String, Set<PackPredicate>> textureFileMap = new HashMap<>();
        matchPredicate.predicates()
                .stream()
                .filter(p -> p instanceof PackItemModelPredicate)
                .map(p -> (PackItemModelPredicate) p)
                .forEach(p -> {
                    textureFileMap.putIfAbsent(p.json(), new HashSet<>());
                    textureFileMap.get(p.json()).add(p);
                });
        return textureFileMap;
    }

    @NotNull
    private static Set<ItemModelMatch> matchAll(@NotNull final Collection<PackPredicate> predicates) {
        return predicates.stream()
                .filter(p -> p instanceof PackItemModelPredicate)
                .map(p -> (PackItemModelPredicate) p)
                .map(p -> new ItemModelMatch(p.json(), p.type(), new HashSet<>(), true))
                .collect(Collectors.toSet());
    }

    @NotNull
    private static ItemModelMatch findItemMatches(@NotNull final PackPredicate p, @NotNull final Logger logger) {
        final PackItemModelPredicate modelPredicate = (PackItemModelPredicate) p;
        return new ItemModelMatch(modelPredicate.json(), modelPredicate.type(), new ArrayList<>(CITPropertyFile.findItemsMatchingPredicate(p, logger)), false);
    }

    @NotNull
    private static Set<ItemModelMatch> findMatches(@NotNull final Collection<PackPredicate> predicates, @NotNull final Logger logger) {
        return predicates.stream()
                .map(p -> findItemMatches(p, logger))
                .collect(Collectors.toSet());
    }

    @NotNull
    private static Map<PredicateType, List<ItemModelMatch>> findMatches(@NotNull final Map<String, Set<PackPredicate>> jsonFileMap,
                                                                        @NotNull final PackMatchPredicate matchPredicate, @NotNull final Logger logger) {
        return jsonFileMap.keySet().stream().map(s -> {
                    final Collection<PackPredicate> packPredicates = jsonFileMap.get(s);
                    final boolean setall = CITPropertyFile.isSettingAll(packPredicates, matchPredicate);
                    return setall ? matchAll(packPredicates) : findMatches(packPredicates, logger);
                })
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(ItemModelMatch::type));
    }

    @NotNull
    public static Set<CITModelPropertyFile> of(@NotNull final PackMatchPredicate matchPredicate, @NotNull final Logger logger) {
        final Map<String, Set<PackPredicate>> jsonFileMap = jsonFileMap(matchPredicate);
        final Map<PredicateType, List<ItemModelMatch>> itemMatches = findMatches(jsonFileMap, matchPredicate, logger);

        return itemMatches.keySet()
                .stream()
                .map(t -> itemMatches.get(t)
                        .stream()
                        .map(i -> new CITModelPropertyFile(t, i.json(), i, matchPredicate.matchPredicates(), matchPredicate.weight()))
                        .collect(Collectors.toSet()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @NotNull
    public String toString() {
        return "CITModelPropertyFile {\n" + compile().indent(3) + "}";
    }

}