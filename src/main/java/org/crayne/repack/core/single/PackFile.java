package org.crayne.repack.core.single;

import org.crayne.repack.core.single.predicate.PackPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PackFile {

    @NotNull
    private final List<PackVariable> variables;

    @NotNull
    private final List<PackPredicate> predicates;

    public PackFile(@NotNull final Collection<PackVariable> variables, @NotNull final Collection<PackPredicate> predicates) {
        this.variables = new ArrayList<>(variables);
        this.predicates = new ArrayList<>(predicates);
    }

    public PackFile() {
        this.variables = new ArrayList<>();
        this.predicates = new ArrayList<>();
    }

    @NotNull
    public List<PackPredicate> predicates() {
        return predicates;
    }

    @NotNull
    public List<PackVariable> variables() {
        return variables;
    }

    public void defineVariable(@NotNull final PackVariable variable) {
        variables.add(variable);
    }

    public boolean variableDefined(@NotNull final String name) {
        return variables.stream().anyMatch(v -> v.name().equals(name));
    }

    public void definePredicate(@NotNull final PackPredicate predicate) {
        predicates.add(predicate);
    }

    @NotNull
    public String toString() {
        return "PackFile{" +
                "variables=" + variables +
                ", predicates=" + predicates +
                '}';
    }

}
