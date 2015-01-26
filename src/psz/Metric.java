package psz;

import java.io.IOException;
import java.util.Set;

import edu.stanford.nlp.util.StringUtils;
import edu.sussex.nlp.jws.JWS;
import edu.sussex.nlp.jws.Lin;

public final class Metric {
    
    public static interface MetricCalculator {
        
        double calculate(final String lemma, final String otherLemma, final String pos);
        double calculateMax(final String lemma, final Set<String> lemmas, final String pos);
    }
    
    public static final class LexicalSimilarity implements MetricCalculator {
        
        public static double NMCLCSn(String s1, String s2) {
            if (s1 == null || s2 == null || s1.length() == 0 || s2.length() == 0) {
                return 0;
            }
            return Math.pow(StringUtils.longestCommonContiguousSubstring(s1, s2), 2) / (s1.length() * s2.length());
        }
    
        @Override
        public double calculateMax(final String lemma, final Set<String> lemmas, final String pos) {
            
            if (POS.IGNORED_POSs.contains(pos)) {
                return 0.0;
            }
                
            double maxLexicalSim = 0;
            
            for (String oneLemma: lemmas) {
                double lexicalSim = NMCLCSn(lemma, oneLemma);
                if (lexicalSim > maxLexicalSim) {
                    maxLexicalSim = lexicalSim;
                }
            }
            
            return maxLexicalSim;
        }

        @Override
        public double calculate(String lemma, String otherLemma, String pos) {
            
            if (POS.IGNORED_POSs.contains(pos)) {
                return 0.0;
            }
            
            return NMCLCSn(lemma, otherLemma);
        }
    }

    public static final class SemanticSimilarity implements MetricCalculator {
        
        private static final JWS WS;
        
        static {
            try {
                WS = new JWS("./deps/wordnet/", "3.0");
            } catch (IOException e) {
                throw new RuntimeException(e.toString());
            }
        }
    
        @Override
        public double calculateMax(final String lemma, final Set<String> lemmas, final String pos) {
            
            if (!POS.NOUNS.contains(pos) && !POS.VERBS.contains(pos)) {
                return 0.0;
            }
            
            final Lin lin = WS.getLin();
            double maxSemanticSim = 0;
            
            for (String oneLemma: lemmas) {
                double semanticSim = lin.max(lemma, oneLemma, pos.substring(0, 1).toLowerCase());
                if (semanticSim > maxSemanticSim) {
                    maxSemanticSim = semanticSim;
                }
            }
            
            return maxSemanticSim;
        }

        @Override
        public double calculate(String lemma, String otherLemma, String pos) {
            
            if (!POS.NOUNS.contains(pos) && !POS.VERBS.contains(pos)) {
                return 0.0;
            }
            
            final Lin lin = WS.getLin();
            return lin.max(lemma, otherLemma, pos.substring(0, 1).toLowerCase());
        }
    }
}