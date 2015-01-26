package psz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import psz.Metric.MetricCalculator;
import psz.ProcessedSentence.RelationData;
import psz.ProcessedSentence.RelationData.WordData;
import psz.Util.Max;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;

public class Data {
	
	public enum ProcessType {CORE, NEW}

    public static ProcessedSentence processSentence(final StanfordCoreNLP pipeline, final String sentence, final ProcessType processType) {
                
        final Annotation annotation = new Annotation(sentence);
        pipeline.annotate(annotation);
        final CoreMap sentenceData = annotation.get(SentencesAnnotation.class).get(0);
        
        final ProcessedSentence processedSentence = new ProcessedSentence(sentence);
        
        if (processType == ProcessType.NEW) {
        	
        	for (CoreLabel token: sentenceData.get(TokensAnnotation.class)) {
	            
	            final String word = token.word();
	            final String pos = token.tag();
	            processedSentence.addWord(word, pos);
	        }
        	
        } else {
        
	        for (CoreLabel token: sentenceData.get(TokensAnnotation.class)) {
	            
	            final String word = token.word();
	            final String lemma = token.lemma();
	            final String pos = token.tag();
	            
	            processedSentence.incPOSCount(pos);
	            processedSentence.addPOSData(pos, lemma, word);
	        }
	        
	        SemanticGraph dependencies = sentenceData.get(CollapsedCCProcessedDependenciesAnnotation.class);
	        for (SemanticGraphEdge edge: dependencies.edgeIterable()) {
	            
	            final String relationName = edge.getRelation().toString();
	            final WordData governor = new WordData(edge.getGovernor().lemma(), edge.getGovernor().tag());
	            final WordData dependent = new WordData(edge.getDependent().lemma(), edge.getDependent().tag());
	            
	            processedSentence.addRelationData(relationName, new RelationData(governor, dependent));
	            processedSentence.incRelationCount(relationName);
	        }
        }
        
        return processedSentence;
    }
    
    public static double sameWords(final ProcessedSentence first, final ProcessedSentence second) {
    	
    	Map<String, Integer> firstWords = first.wordsData;
    	Map<String, Integer> secondWords = second.wordsData;
    	
    	double sameWords = 0;
    	
    	for (final String word: firstWords.keySet()) {
    		
    		if (secondWords.containsKey(word)) {
    			sameWords += Math.min(firstWords.get(word), secondWords.get(word));
    		}
    	}
    	
    	return sameWords/((first.wordCount + second.wordCount)/2.0);
    }
    
    public static int diffWords(final ProcessedSentence first, final ProcessedSentence second) {
        return Math.abs(first.wordCount - second.wordCount);
    }

    public static Map<String, Integer> diffPOS(final ProcessedSentence first, final ProcessedSentence second) {
        
        final Map<String, Integer> diff = new HashMap<>();
        
        for (String pos: POS.POSs) {
            int firstCount = first.getPOSCount(pos);
            int secondCount = second.getPOSCount(pos);
            diff.put(pos, new Integer(Math.abs(secondCount - firstCount)));
        }
        
        return diff;
    }
    
    public static int diffPOSTypes(Set<String> posTypes, final ProcessedSentence first, final ProcessedSentence second) {
        
        int firstCount = first.getPOSTypesCount(posTypes);
        int secondCount = second.getPOSTypesCount(posTypes);
        return Math.abs(secondCount - firstCount);
    }
    
    public static Map<String, Integer> diffRelations(final ProcessedSentence first, final ProcessedSentence second) {
        
        final Map<String, Integer> diff = new HashMap<>();
        
        final Set<String> relations = new HashSet<>();
        relations.addAll(first.relationCount.keySet());
        relations.addAll(second.relationCount.keySet());
        
        for (String pos: relations) {
            int firstCount = first.getRelationCount(pos);
            int secondCount = second.getRelationCount(pos);
            diff.put(pos, new Integer(Math.abs(secondCount - firstCount)));
        }
        
        return diff;
    }
    
    public static double calculatePOSMetric(final MetricCalculator calculator, final Set<String> firstLemmas, final Set<String> secondLemmas, final String pos) {

        final Set<String> allLemmas = new HashSet<>();
        allLemmas.addAll(firstLemmas);
        allLemmas.addAll(secondLemmas);
             
        final List<Double> metricsFirst = new ArrayList<>();
        final List<Double> metricsSecond = new ArrayList<>();
        
        for (String lemma: allLemmas) {
            double maxFirst = calculator.calculateMax(lemma, firstLemmas, pos);
            metricsFirst.add(maxFirst);
            
            double maxSecond = calculator.calculateMax(lemma, secondLemmas, pos);
            metricsSecond.add(maxSecond);
        }
        
        double firstNorm = Util.norm(metricsFirst);
        double secondNorm = Util.norm(metricsSecond);
        double finalMetric = 0.0;
        
        if (firstNorm != 0 && secondNorm != 0) {
            finalMetric = Util.dotProduct(metricsFirst, metricsSecond) / (firstNorm * secondNorm);
        }

        return finalMetric;
    }

    public static Map<String, Double> calculateMetricsByPOS(final MetricCalculator calculator, final ProcessedSentence first, final ProcessedSentence second) {
        
        final Map<String, Double> metrics = new HashMap<>();
        
        for (String pos: POS.POSs) {
    
            final Map<String, Set<String>> firstData = first.dataByPos.get(pos);
            final Map<String, Set<String>> secondData = second.dataByPos.get(pos);
            
            if (firstData == null || secondData == null) {
                metrics.put(pos, 0.0);
                continue;
            }
            
            double metric = calculatePOSMetric(calculator, firstData.keySet(), secondData.keySet(), pos);
            metrics.put(pos, metric);
        }
        
        return metrics;
    }
    
    public static double calculateMetricsForPOSType(final MetricCalculator calculator, final ProcessedSentence first, final ProcessedSentence second, Set<String> posTypes) {
        
        final Set<String> firstLemmas = new HashSet<>();
        final Set<String> secondLemmas = new HashSet<>();
        
        for (String pos: POS.POSs) {
            
            if (!posTypes.contains(pos)) {
                continue;
            }
            
            final Map<String, Set<String>> firstData = first.dataByPos.get(pos);
            final Map<String, Set<String>> secondData = second.dataByPos.get(pos);
            
            if (firstData == null || secondData == null) {
                continue;
            }

            firstLemmas.addAll(firstData.keySet());
            secondLemmas.addAll(secondData.keySet());
        }
        
        String pos;
        if (posTypes == POS.NOUNS) {
            pos = POS.NOUNS.iterator().next();
        } else if (posTypes == POS.VERBS) {
            pos = POS.VERBS.iterator().next();
        } else {
            throw new RuntimeException("Not implemeted for POS types other than Nouns and Verbs.");
        }
        
        return calculatePOSMetric(calculator, firstLemmas, secondLemmas, pos);
    }
    
    public static double calculateOverallLexicalSimilarity(final ProcessedSentence first, final ProcessedSentence second) {
        
        final Set<String> firstLemmas = new HashSet<>();
        final Set<String> secondLemmas = new HashSet<>();
        
        for (String pos: POS.POSs) {
            
            final Map<String, Set<String>> firstData = first.dataByPos.get(pos);
            final Map<String, Set<String>> secondData = second.dataByPos.get(pos);

            if (firstData != null) {
                firstLemmas.addAll(firstData.keySet());
            }
            
            if (secondData != null) {
                secondLemmas.addAll(secondData.keySet());
            }
        }
        
        // lexical similarity doesn't need pos
        return calculatePOSMetric(new Metric.LexicalSimilarity(), firstLemmas, secondLemmas, null);
    }
    
    public static double calculateRelationMetricHelper(final MetricCalculator calculator, final RelationData first, final RelationData second) {
        
        if (!first.governor.pos.equals(second.governor.pos) || !first.dependent.pos.equals(second.dependent.pos)) {
            return 0.0;
        }
            
        double base = calculator.calculate(first.governor.word, second.governor.word, first.governor.pos);
        double exp = calculator.calculate(first.dependent.word, second.dependent.word, first.dependent.pos);
        return base * Math.pow(2, exp - 1);
    }
    
    public static double calculateRelationMetric(final MetricCalculator calculator, final ProcessedSentence first, final ProcessedSentence second, final String relation) {
        
        final List<RelationData> firstRelations = first.dataByRelation.get(relation);
        final List<RelationData> secondRelations = second.dataByRelation.get(relation);
        
        if (firstRelations == null || secondRelations == null) {
            return 0.0;
        }
        
        int relationMatrixN = firstRelations.size();
        int relationMatrixM = secondRelations.size();
        double[][] relationMatrix = new double[relationMatrixN][relationMatrixM];
        
        for (int fi = 0; fi < relationMatrixN; fi++) {
            for (int si = 0; si < relationMatrixM; si++) {
                relationMatrix[fi][si] = calculateRelationMetricHelper(calculator, firstRelations.get(fi), secondRelations.get(si));
            }
        }
        
        final List<Double> beta = new ArrayList<>();
        
        Max max = Util.findMatrixMax(relationMatrix, relationMatrixN, relationMatrixM);
        while (max != null) {
            beta.add(max.value);
            Util.invalidateRowColon(relationMatrix, relationMatrixN, relationMatrixM, max);
            max = Util.findMatrixMax(relationMatrix, relationMatrixN, relationMatrixM);
        }
        
        double betaSum = 0.0;
        for (Double elem: beta) {
            betaSum += elem;
        }
        
        return betaSum * (double)(relationMatrixN + relationMatrixN) / (double)(2 * relationMatrixN * relationMatrixM);
    }
    
    public static Map<String, Double> calculateMetricByRelation(final MetricCalculator calculator, final ProcessedSentence first, final ProcessedSentence second) {
        
        final Map<String, Double> metrics = new HashMap<>();
        
        final Set<String> relations = new HashSet<>();
        relations.addAll(first.getRelations());
        relations.addAll(second.getRelations());
        
        for (String relation: relations) {
            double metric = calculateRelationMetric(new Metric.LexicalSimilarity(), first, second, relation);
            metrics.put(relation, metric);
        }
        
        return metrics;
    }
    
    public static class AllMetrics {
        
    	double sameWords;
        int diffWords;
        final Map<String, Integer> diffPos;
        int diffNouns;
        int diffVerbs;
        final Map<String, Integer> diffRel;
        double lexSim;
        final Map<String, Double> lexSimByPos;
        double lexSimNouns;
        double lexSimVerbs;
        final Map<String, Double> lexSimByRel;
        final Map<String, Double> semSimByPos;
        double semSimNouns;
        double semSimVerbs;
        final Map<String, Double> semSimByRel;
        
        public AllMetrics(double sameWords) {
        	this.sameWords = sameWords;
        	this.diffPos = null;
        	this.diffRel = null;
        	this.lexSimByPos = null;
        	this.lexSimByRel = null;
        	this.semSimByPos = null;
        	this.semSimByRel = null;
        }
        
        public AllMetrics(double sameWords, int diffWords, final Map<String, Integer> diffPos, final Map<String, Integer> diffRel, int diffNouns, int diffVerbs,
                double lexSim, final Map<String, Double> lexSimByPos, double lexSimNouns, double lexSimVerbs, final Map<String, Double> lexSimByRel,
                final Map<String, Double> semSimByPos, double semSimNouns, double semSimVerbs, final Map<String, Double> semSimByRel) {
            
        	this.sameWords = sameWords;
            this.diffWords = diffWords;
            this.diffPos = diffPos;
            this.diffRel = diffRel;
            this.diffNouns = diffNouns;
            this.diffVerbs = diffVerbs;
            this.lexSim = lexSim;
            this.lexSimByPos = lexSimByPos;
            this.lexSimNouns = lexSimNouns;
            this.lexSimVerbs = lexSimVerbs;
            this.lexSimByRel = lexSimByRel;
            this.semSimByPos = semSimByPos;
            this.semSimNouns = semSimNouns;
            this.semSimVerbs = semSimVerbs;
            this.semSimByRel = semSimByRel;
        }
        
        @Override
        public String toString() {
            
            final StringBuilder builder = new StringBuilder();
            for (Entry<String, Integer> entry: this.diffPos.entrySet()) {
                builder.append("POS: " + entry.getKey() + ", diff: " + entry.getValue() + "\n");
            }
            
            builder.append("Nouns diff: " + this.diffNouns + "\n");
            builder.append("Verbs diff: " + this.diffVerbs + "\n");
            
            for (Entry<String, Double> entry: this.lexSimByPos.entrySet()) {
                builder.append("POS: " + entry.getKey() + ", lex sim: " + entry.getValue() + "\n");
            }
            
            builder.append("Nouns lex sim: " + this.lexSimNouns + "\n");
            builder.append("Verbs lex sim: " + this.lexSimVerbs + "\n");
            
            for (Entry<String, Double> entry: this.semSimByPos.entrySet()) {
                builder.append("POS: " + entry.getKey() + ", sem sim: " + entry.getValue() + "\n");
            }
            
            builder.append("Nouns sem sim: " + this.semSimNouns + "\n");
            builder.append("Verbs sem sim: " + this.semSimVerbs + "\n");
            
            builder.append("Word diff: " + this.diffWords + "\n");
            builder.append("Overall lexical similarity: " + this.lexSim + "\n");
            
            for (Entry<String, Integer> entry: this.diffRel.entrySet()) {
                builder.append("Rel: " + entry.getKey() + ", diff: " + entry.getValue() + "\n");
            }
            
            for (Entry<String, Double> entry: this.lexSimByRel.entrySet()) {
                builder.append("Rel: " + entry.getKey() + ", lex sim: " + entry.getValue() + "\n");
            }
            
            for (Entry<String, Double> entry: this.semSimByRel.entrySet()) {
                builder.append("Rel: " + entry.getKey() + ", sem sim: " + entry.getValue() + "\n");
            }
            
            return builder.toString();
        }
    }
    
    public static AllMetrics calculateAllMetrics(final ProcessedSentence first, final ProcessedSentence second, final ProcessType procType) {
        
    	double sameWords;
    	if (procType == ProcessType.NEW) {
    		
    		sameWords = Data.sameWords(first, second);
    		return new AllMetrics(sameWords);
    		
    	} else {
    		
    		sameWords = 0;
	        Map<String, Integer> diffPos = Data.diffPOS(first, second);
	        int diffNouns = Data.diffPOSTypes(POS.NOUNS, first, second);
	        int diffVerbs = Data.diffPOSTypes(POS.VERBS, first, second);
	        
	        Map<String, Double> lexSimPos = Data.calculateMetricsByPOS(new Metric.LexicalSimilarity(), first, second);
	        
	        double lexSimNouns = Data.calculateMetricsForPOSType(new Metric.LexicalSimilarity(), first, second, POS.NOUNS);
	        double lexSimVerbs = Data.calculateMetricsForPOSType(new Metric.LexicalSimilarity(), first, second, POS.VERBS);
	        
	        Map<String, Double> semSimPos = Data.calculateMetricsByPOS(new Metric.SemanticSimilarity(), first, second);
	        
	        double semSimNouns = Data.calculateMetricsForPOSType(new Metric.SemanticSimilarity(), first, second, POS.NOUNS);
	        double semSimVerbs = Data.calculateMetricsForPOSType(new Metric.SemanticSimilarity(), first, second, POS.VERBS);
	        
	        int diffWords = Data.diffWords(first, second);
	        double lexSim = Data.calculateOverallLexicalSimilarity(first, second);
	        
	        Map<String, Integer> diffRel = Data.diffRelations(first, second);
	        Map<String, Double> lexSimRel = Data.calculateMetricByRelation(new Metric.LexicalSimilarity(), first, second);
	        Map<String, Double> semSimRel = Data.calculateMetricByRelation(new Metric.SemanticSimilarity(), first, second);
	        
	        return new AllMetrics(sameWords, diffWords, diffPos, diffRel, diffNouns, diffVerbs, lexSim, lexSimPos,
	                              lexSimNouns, lexSimVerbs, lexSimRel, semSimPos, semSimNouns, semSimVerbs, semSimRel);
    	}
    }
}
