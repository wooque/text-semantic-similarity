package psz;

import java.io.PrintStream;

import psz.Data.AllMetrics;
import psz.Data.ProcessType;

public class ARFF {
    
    public final PrintStream printer;
    public final ProcessType procType;
    
    public ARFF(final PrintStream printer, final ProcessType procType) {
        
        if (printer == null) {
            this.printer = System.out;
        } else {
            this.printer = printer;
        }
        
        this.procType = procType;
        
        this.printer.println("@RELATION similarity");
        this.printer.println();
        
        if (procType == ProcessType.NEW) {
            
        	writeNumericAttribute("sameWords");
            
        } else {
        	
	        for (String pos: POS.POSs) {
	            writeNumericAttribute("diff_Tag_" + pos);
	        }
	        writeNumericAttribute("diffNouns");
	        writeNumericAttribute("diffVerbs");
	        for (String rel: Relation.RELATIONS) {
	            writeNumericAttribute("diff_Dep_" + rel);
	        }
	        writeNumericAttribute("diff_All");
	        
	        writeNumericAttribute("overallLexsim");
	        for (String pos: POS.POSs) {
	            writeNumericAttribute("lexSim_Tag_" + pos);
	        }
	        writeNumericAttribute("lexSimNouns");
	        writeNumericAttribute("lexSimVerbs");
	        for (String rel: Relation.RELATIONS) {
	            writeNumericAttribute("lexSim_Dep_" + rel);
	        }
	        
	        for (String pos: POS.POSs) {
	            writeNumericAttribute("semSim_Tag_" + pos);
	        }
	        writeNumericAttribute("semSimNouns");
	        writeNumericAttribute("semSimVerbs");
	        for (String rel: Relation.RELATIONS) {
	            writeNumericAttribute("semSim_Dep_" + rel);
	        }
        }
        
        this.printer.print("@ATTRIBUTE ");
        this.printer.printf("%-25s", "class");
        this.printer.println(" {similar,different}");
        this.printer.println();
        
        this.printer.println("@DATA");
    }
    
    public void writeNumericAttribute(final String attribute) {
        this.printer.print("@ATTRIBUTE ");
        this.printer.printf("%-25s", attribute);
        this.printer.println(" NUMERIC");
    }

    public void writeMetric(final AllMetrics metrics, boolean similar) {
        
        final StringBuilder builder = new StringBuilder();
        
        if (procType == ProcessType.NEW) {
        	
        	builder.append(metrics.sameWords + ",");
        	
        } else {
        	
        	builder.append(metrics.diffWords + ",");

	        for (String pos: POS.POSs) {
	            builder.append(Util.getOrZeroInt(metrics.diffPos, pos) + ",");
	        }
	        builder.append(metrics.diffNouns + ",");
	        builder.append(metrics.diffVerbs + ",");
	        for (String rel: Relation.RELATIONS) {
	            builder.append(Util.getOrZeroInt(metrics.diffRel, rel) + ",");
	        }
	        
	        builder.append(metrics.lexSim + ",");
	        for (String pos: POS.POSs) {
	            builder.append(Util.getOrZeroDouble(metrics.lexSimByPos, pos) + ",");
	        }
	        builder.append(metrics.lexSimNouns + ",");
	        builder.append(metrics.lexSimVerbs + ",");
	        for (String rel: Relation.RELATIONS) {
	            builder.append(Util.getOrZeroDouble(metrics.lexSimByRel, rel) + ",");
	        }
	        
	        for (String pos: POS.POSs) {
	            builder.append(Util.getOrZeroDouble(metrics.semSimByPos, pos) + ",");
	        }
	        builder.append(metrics.semSimNouns + ",");
	        builder.append(metrics.semSimVerbs + ",");
	        for (String rel: Relation.RELATIONS) {
	            builder.append(Util.getOrZeroDouble(metrics.semSimByRel, rel) + ",");
	        }
        }

        builder.append(similar? "similar": "different");
        
        this.printer.println(builder.toString());
    }
}
