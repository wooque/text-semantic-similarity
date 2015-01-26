package psz;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class Util {
    
    public static final class Max {
        
        public double value;
        public int row;
        public int col;
        
        public Max(double value, int row, int col) {
            
            this.value = value;
            this.row = row;
            this.col = col;
        }
    }

    public static void incMap(final Map<String, Integer> map, final String key) {
        
        if (map.containsKey(key)) {
            Integer count = map.get(key);
            count = new Integer(count.intValue() + 1);
        } else {
            map.put(key, 1);
        }
    }
    
    public static int getMapCount(final Map<String, Integer> map, final String key) {
        
        final Integer count = map.get(key);
        return (count == null)? 0: count.intValue();
    }
    
    public static int getOrZeroInt(final Map<String, Integer> map, final String key) {
        
        final Integer data = map.get(key);
        if (data != null) {
            return data;
        } else {
            return 0;
        }
    }
    
    public static double getOrZeroDouble(final Map<String, Double> map, final String key) {
        
        final Double data = map.get(key);
        if (data != null) {
            return data;
        } else {
            return 0.0;
        }
    }
    
    public static double dotProduct(final List<Double> first, final List<Double> second) {
        
        if (first.size() != second.size() || first.size() == 0) {
            return 0;
        }
        
        double product = 0;
        
        for (int i = 0; i < first.size(); i++) {
            double firstCoord = first.get(i);
            double secondCoord = second.get(i);
            product += firstCoord * secondCoord;
        }
        
        return product;
    }
    
    public static double norm(final List<Double> vector) {
        
        double norm = 0;
        
        for (double d: vector) {
            norm += Math.pow(d, 2);
        }
        
        return Math.sqrt(norm);
    }

    public static Max findMatrixMax(double[][] matrix, int n, int m) {
        
        double max = 0.0;
        int maxRow = -1;
        int maxCol = -1;
        
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < m; col++) {
                if (matrix[row][col] > max) {
                    max = matrix[row][col];
                    maxRow = row;
                    maxCol = col;
                }
            }
        }
        
        if (maxRow != -1 && maxCol != -1) {
            return new Max(max, maxRow, maxCol);
        } else {
            return null;
        }
    }

    public static void invalidateRowColon(double[][] matrix, int n, int m, final Max elem) {
        
        for (int row = 0; row < n; row++) {
            matrix[row][elem.col] = 0.0;
        }
        
        for (int col = 0; col < m; col++) {
            matrix[elem.row][col] = 0.0;
        }
    }
    
    public static final class LinePair {
        
        final String first;
        final String second;
        boolean similar;
        
        public LinePair(final String first, final String second, boolean similar) {
            this.first = first;
            this.second = second;
            this.similar = similar;
        }
    }
    
    public static LinePair readLinePair(BufferedReader reader) throws IOException {
        
        final String similarLine = reader.readLine();
        if (similarLine == null) {
            return null;
        }
        boolean similar = similarLine.equals("1")? true: false;
        
        final String first = reader.readLine();
        if (first == null) {
            return null;
        }
        
        final String second = reader.readLine();
        if (second == null) {
            return null;
        }
        
        return new LinePair(first, second, similar);
    }
}