package syncbfs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Phaser;

public class SyncBFS implements Runnable {

    private final int numProcesses;
    private final int rootIndex;
    private final int[] processIDs;
    private final boolean[][] connectivityMatrix;
    private final Thread thread;
    private final Phaser phaser;
    private final ArrayList<SyncProcess> processes = new ArrayList<>();
    
    public SyncBFS(final int numProcesses, 
                   final int rootIndex, 
                   final int[] processIDs, 
                   final boolean[][] connectivityMatrix) {
        this.numProcesses = numProcesses;
        this.rootIndex = rootIndex;
        this.processIDs = processIDs;
        this.connectivityMatrix = connectivityMatrix;
        phaser = new Phaser(numProcesses + 1);
        thread = new Thread(this);
    }

    @Override
    public void run() {
        printConfiguration();
        createProcesses();
        startProcesses();
        while (!phaser.isTerminated()) {
            phaser.arriveAndAwaitAdvance();
        }
        printTermination();
    }
    
    public void start() {
        thread.start();
    }
    
    private void startProcesses() {
        processes.forEach((p) -> {
            p.start();
        });
    }
    
    private void createProcesses() {
        int rootID = processIDs[rootIndex];
        
        ArrayList<Channel> receiveChannels = new ArrayList<>();
        for (int i = 0; i < numProcesses; i++) {
            receiveChannels.add(new Channel());
        }
        
        for (int i = 0; i < numProcesses; i++) {
            int processID = processIDs[i];
            HashMap<Integer, Channel> sendChannel = new HashMap<>();
            for (int j = 0; j < numProcesses; j++) {
                if (i != j && connectivityMatrix[i][j]) {
                    sendChannel.put(processIDs[j], receiveChannels.get(j));
                }
            }
            SyncProcess p = new SyncProcess.Builder()
                            .withPhaser(phaser)
                            .withProcessId(processID)
                            .withReceiveChannel(receiveChannels.get(i))
                            .withSendChannel(sendChannel)
                            .withRootId(rootID)
                            .build();
            processes.add(p);
        }
    }
    
    public void printConfiguration() {
        System.out.println("Config file:");
        System.out.println("# processes: " + numProcesses);
        System.out.println("Process IDs: ");
        for (int curr: processIDs) {
            System.out.print(curr + " ");
        }
        System.out.println("");
        System.out.println("Root index: " + rootIndex);
        System.out.println("Connectivity matrix: ");
        for (int i = 0; i < numProcesses; i++) {
            for (int j = 0; j < numProcesses; j++) {
                int x = connectivityMatrix[i][j] ? 1 : 0;
                System.out.print(x + " ");
            }
            System.out.println("");
        }
    }
    
    public void printTermination() {
        System.out.println("\nBFS tree: ");
        processes.forEach((p) -> {
            System.out.println("\n" + p.getProcessID() + ":");
            System.out.println("parent: " + p.getParentID());
            if (!p.getChildrenIDs().isEmpty()) {
                System.out.print("children:");
                p.getChildrenIDs().forEach((c) -> {
                    System.out.print(" " + c);
                });
                System.out.println("");
            }
        });
    }
    
    public static class Builder {
        
        private int numProcesses;
        private int rootIndex;
        private int[] processIDs;
        private boolean[][] connectivityMatrix;
        
        public Builder withNumProcesses(final int numProcesses) {
            this.numProcesses = numProcesses;
            return this;
        }
        
        public Builder withRootIndex(final int rootIndex) {
            this.rootIndex = rootIndex;
            return this;
        }
        
        public Builder withProcessIDs(final int[] processIDs) {
            this.processIDs = processIDs;
            return this;
        }
        
        public Builder withConnectivityMatrix(
                final boolean[][] connectivityMatrix) {
            this.connectivityMatrix = connectivityMatrix;
            return this;
        }
        
        public SyncBFS build() {
            return new SyncBFS(numProcesses, 
                               rootIndex, 
                               processIDs, 
                               connectivityMatrix);
        }
    }
}
