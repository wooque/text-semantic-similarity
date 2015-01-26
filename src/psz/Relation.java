package psz;

import java.util.HashSet;
import java.util.Set;

public class Relation {
    public static final Set<String> RELATIONS;
     
    static {
        
        String[] RelationStrings = {"acomp", "advcl", "advmod", "agent", "amod", "appos", "aux", "auxpass", "cc", "ccomp", "conj",
                                    "cop", "csubj", "csubjpass", "dep", "det", "discourse", "dobj", "expl", "goeswith", "iobj",
                                    "mark", "mwe", "neg", "nn", "npadvmod", "nsubj", "nsubjpass", "num", "number", "parataxis",
                                    "pcomp", "pobj", "poss", "possessive", "preconj", "predet", "prep", "prepc", "prt", "punct",
                                    "quantmod", "ref", "root", "tmod", "vmod", "xcomp", "xsubj"};
        
        RELATIONS = new HashSet<>();
        
        for (String relation: RelationStrings) {
            RELATIONS.add(relation);
        }
    }
}
