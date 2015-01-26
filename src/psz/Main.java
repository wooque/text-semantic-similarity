package psz;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import psz.Data.AllMetrics;
import psz.Data.ProcessType;
import psz.Util.LinePair;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class Main {
    
    public static void main(String[] args) throws IOException {
    	
    	final ProcessType procType;
        if (args.length > 1 && args[1].equals("new")) {
        	procType = ProcessType.NEW;
        } else {
        	procType = ProcessType.CORE;
        }
        
        final Properties props = new Properties();
        final String anns;
        
        if (procType == ProcessType.NEW) {
        	anns = "tokenize, ssplit, pos";
        } else {
        	anns = "tokenize, ssplit, pos, lemma, parse";
        }
        
        props.put("annotators", anns);
        final StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        
        final String inputFile = "./data/" + args[0];
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        
        final String path = args[0].split("\\.")[0];
        final String arffFile;
        
        if (procType == ProcessType.NEW) {
        	arffFile = "./data/" + path + "_new.arff";
        } else {
        	arffFile = "./data/" + path + ".arff";
        }
        
        final ARFF arrf = new ARFF(new PrintStream(new FileOutputStream(arffFile), true), procType);
        
        final long start = System.currentTimeMillis();
        while (true) {
            
            final LinePair pair = Util.readLinePair(reader);
            
            if (pair == null) {
                break;
            }
            
            final ProcessedSentence first;
            final ProcessedSentence second;
            
        	first = Data.processSentence(pipeline, pair.first, procType);
        	second = Data.processSentence(pipeline, pair.second, procType);
            
            final AllMetrics metrics = Data.calculateAllMetrics(first, second, procType);
            arrf.writeMetric(metrics, pair.similar);
        }
        final long total = System.currentTimeMillis() - start;
        System.out.println("Time processing: " + total/1000.0 + " s");
    }
}
