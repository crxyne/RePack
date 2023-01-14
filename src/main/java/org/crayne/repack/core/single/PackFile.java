package org.crayne.repack.core.single;

import org.crayne.repack.core.single.predicate.PackPredicate;
import org.crayne.repack.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PackFile {

    @NotNull
    private final List<PackVariable> variables;

    @NotNull
    private final List<PackPredicate> matches;

    public PackFile(@NotNull final Collection<PackVariable> variables, @NotNull final Collection<PackPredicate> matches) {
        this.variables = new ArrayList<>(variables);
        this.matches = new ArrayList<>(matches);
    }

    public PackFile() {
        this.variables = new ArrayList<>();
        this.matches = new ArrayList<>();
    }

    @NotNull
    public List<PackPredicate> matches() {
        return matches;
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
        matches.add(predicate);
    }

    @NotNull
    public String toString() {
        return "PackFile {\n" +
                ("variables = " + StringUtil.stringOf(variables) +
                ", \nmatches = " + StringUtil.stringOf(matches)).indent(3) +
                '}';
    }

}
