package psz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public final class ProcessedSentence {
    
    public static final class RelationData  {
        
        public static final class WordData {
            
            final String word;
            final String pos;
            
            public WordData(final String word, final String pos) {
                this.word = word;
                this.pos = pos;
            }
        }
        
        final WordData governor;
        final WordData dependent;
        
        public RelationData(final WordData governor, final WordData dependent) {
            this.governor = governor;
            this.dependent = dependent;
        }
    }
        
    public final String sentence;
    public final Map<String, Integer> wordsData;
    public int extendedWordCount;
    public int wordCount;
    public final Map<String, Integer> posCount;
    public final Map<String, Integer> relationCount;
    public final Map<String, Map<String, Set<String>>> dataByPos;
    public final Map<String, List<RelationData>> dataByRelation;
    
    public ProcessedSentence(final String sentence) {
        this.sentence = sentence;
        this.wordsData = new HashMap<>();
        this.posCount = new HashMap<>();
        this.relationCount = new HashMap<>();
        this.dataByPos = new HashMap<>();
        this.dataByRelation = new HashMap<>();
    }
    
    public String toString() {
        
        final StringBuilder string = new StringBuilder();
        
        for (Entry<String, Map<String, Set<String>>> pos: this.dataByPos.entrySet()) {
            
            string.append(pos.getKey() + ":\n");
            
            for (Entry<String, Set<String>> lemma: pos.getValue().entrySet()) {
                
                string.append("\t" + lemma.getKey() + ":\n");
                
                for (String word: lemma.getValue()) {
                    string.append("\t\t" + word + "\n");
                }
            }
        }
        
        for (Entry<String, List<RelationData>> rel: this.dataByRelation.entrySet()) {
            
            string.append(rel.getKey() + ":\n");
            
            for(RelationData data: rel.getValue()) {
                
                string.append("\t" + data.governor.word + " " + data.dependent.word + "\n");
            }
        }
        
        return string.toString();
    }
    
    public void addWord(final String word, final String pos) {
    	
        if (!POS.EXTENDED_POSs.contains(pos)) {
            return;
        }
    	
        this.extendedWordCount++;
    	Util.incMap(wordsData, word);
    }
    
    public void incPOSCount(final String pos) {
        
        if (!POS.POSs.contains(pos)) {
            return;
        }
        
        Util.incMap(this.posCount, pos);
    }
    
    public int getPOSCount(final String pos) {

        return Util.getMapCount(posCount, pos);
    }
    
    public void incRelationCount(final String relation) {
        
        Util.incMap(this.relationCount, relation);
    }
    
    public int getRelationCount(final String relation) {
        
        return Util.getMapCount(relationCount, relation);
    }
    
    public int getPOSTypesCount(Set<String> posTypes) {
        
        int count = 0;
        for (Entry<String, Integer> posData: this.posCount.entrySet()) {
            if (posTypes.contains(posData.getKey())) {
                count += posData.getValue();
            }
        }
        return count;
    }
    
    public void addPOSData(final String pos, final String lemma, final String word) {
        
        if (!POS.POSs.contains(pos)) {
            return;
        }
        
        this.wordCount++;
        
        if (this.dataByPos.containsKey(pos)) {
            
            final Map<String, Set<String>> data = this.dataByPos.get(pos);
            
            if (data.containsKey(lemma)) {
                
                final Set<String> words = data.get(lemma);
                words.add(word);
                
            } else {
                
                final Set<String> words = new HashSet<>();
                words.add(word);
                data.put(lemma, words);
            }
            
        } else {
            
            final Map<String, Set<String>> data = new HashMap<>();
            final Set<String> words = new HashSet<>();
            words.add(word);
            data.put(lemma, words);
            this.dataByPos.put(pos, data);
        }
    }
    
    public Set<String> getRelations() {
        return this.dataByRelation.keySet();
    }
    
    public void addRelationData(final String relation, final RelationData data ) {
        
        if (this.dataByRelation.containsKey(relation)) {
            
            List<RelationData> relData = this.dataByRelation.get(relation);
            relData.add(data);
            
        } else {
            
            List<RelationData> newRelData = new ArrayList<>();
            newRelData.add(data);
            this.dataByRelation.put(relation, newRelData);
        }
    }
}