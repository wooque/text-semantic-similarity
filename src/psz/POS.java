package psz;

import java.util.HashSet;
import java.util.Set;

public final class POS {

    public static final Set<String> POSs;
    public static final Set<String> EXTENDED_POSs;
    public static final Set<String> NOUNS;
    public static final Set<String> VERBS;
    public static final Set<String> IGNORED_POSs;
     
    static {
        
        String[] NounsStrings = {"NN", "NNS", "NNP", "NNPS"};
        String[] VerbsStrings = {"VB", "VBD", "VBG", "VBN", "VBP", "VBZ"};
        String[] IgnoredPosStrings = {"CD", "UNKNOWN"};
        String[] OtherPosStrings = 
            {"CC", "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS",
             "LS", "MD", "PDT", "POS", "PRP", "PRP$", "RB", "RBR",
             "RBS", "RP", "SYM", "TO", "UH", "WDT", "WP$", "WRB"};
        
        POSs = new HashSet<>();
        EXTENDED_POSs = new HashSet<>();
        NOUNS = new HashSet<>();
        VERBS = new HashSet<>();
        IGNORED_POSs = new HashSet<>();
        
        for (String noun: NounsStrings) {
            NOUNS.add(noun);
            POSs.add(noun);
        }
        
        for (String verb: VerbsStrings) {
            VERBS.add(verb);
            POSs.add(verb);
        }
        
        for (String ignored: IgnoredPosStrings) {
            IGNORED_POSs.add(ignored);
            POSs.add(ignored);
        }
        
        for (String other: OtherPosStrings) {
            POSs.add(other);
        }
        
        EXTENDED_POSs.addAll(POSs);
        EXTENDED_POSs.add("CD");
    }
}