package org.crayne.repack.core.single.predicate;

import java.util.HashSet;

public class PackAnyPredicate extends PackMatchPredicate {

    public PackAnyPredicate() {
        super(new HashSet<>());
    }

}
