package syncbfs;

public class MainApp {
    
    public static void main(String[] args) {
        Parser parser = new Parser();
        parser.parse(args[0]);
        int numProcesses = parser.getNumProcesses();
        int[] processIDs = parser.getProcessIDs();
        int rootIndex = parser.getRootIndex();
        boolean[][] connectivityMatrix = parser.getConnectivityMatrix();
        SyncBFS syncBFS = new SyncBFS.Builder()
                .withConnectivityMatrix(connectivityMatrix)
                .withNumProcesses(numProcesses)
                .withProcessIDs(processIDs)
                .withRootIndex(rootIndex - 1)
                .build();
        syncBFS.start();
    }
}
