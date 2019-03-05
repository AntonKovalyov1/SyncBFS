package syncbfs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
    
    private int numProcesses;
    private int[] processIDs;
    private int rootIndex;
    private boolean[][] connectivityMatrix;
    
    public void parse(String path) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(
                    new File(path)));
            numProcesses = getNumber(br);
            processIDs = getIntArray(br, numProcesses);
            rootIndex = getNumber(br);
            connectivityMatrix = getBooleanSquareMatrix(br, numProcesses);
            br.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private String readLine(final BufferedReader br) {
        String s = null;
        try {
        s = br.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return s;
    }
    
    private int getNumber(final BufferedReader br) {
        return Integer.parseInt(readLine(br));
    }
    
    private int[] getIntArray(final BufferedReader br, final int n) {
        int[] array = new int[n];
        String s = readLine(br);
        String[] tokens = s.trim().split("\\s+");
        for (int i = 0; i < n; i++) {
            array[i] = Integer.parseInt(tokens[i]);
        }
        return array;
    }
    
    private boolean[][] getBooleanSquareMatrix(final BufferedReader br, 
                                               final int n) {
        boolean[][] matrix = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            String s = readLine(br);
            String[] tokens = s.trim().split("\\s+");
            for (int j = 0; j < n; j++) {
                if (Integer.parseInt(tokens[j]) == 1) {
                    matrix[i][j] = true;
                }
            }
        }
        return matrix;
    }

    public int getNumProcesses() {
        return numProcesses;
    }

    public int[] getProcessIDs() {
        return processIDs;
    }

    public int getRootIndex() {
        return rootIndex;
    }

    public boolean[][] getConnectivityMatrix() {
        return connectivityMatrix;
    }
}
