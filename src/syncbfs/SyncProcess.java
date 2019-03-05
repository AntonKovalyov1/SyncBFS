package syncbfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Phaser;

public class SyncProcess implements Runnable {
    
    private final Phaser phaser;
    private final HashMap<Integer, Channel> sendChannel;
    private final Channel receiveChannel;
    private final int processID;
    private final int rootID;
    private final Thread thread;
    
    private int round = 0;
    private boolean hasParent = false;
    private boolean receivedMessages = false;
    private boolean firstMessage = true;
    private int parentID = Integer.MAX_VALUE;
    private final Set<Integer> childrenIDs = new HashSet<>();
    private int ackCount = 0;
    private int nackCount = 0;
    private boolean terminated = false;
    private final HashMap<Integer, Message> pending = new HashMap<>();
    
    public SyncProcess(final Phaser phaser,
                       final HashMap<Integer, Channel> sendChannels,
                       final Channel receiveChannel,
                       final int processID,
                       final int rootID) {
        this.phaser = phaser;
        this.sendChannel = sendChannels;
        this.receiveChannel = receiveChannel;
        this.processID = processID;
        this.rootID = rootID;
        thread = new Thread(this);
    }
    
    @Override
    public void run() {
        if (processID == rootID) {
            hasParent = true;
            parentID = processID;
        }
        while (!terminated) {
            nextRound();
        }
        if (processID == rootID) {
            phaser.forceTermination();
        } else {
            phaser.arriveAndDeregister();
        }
    }
    
    public void start() {
        thread.start();
    }
    
    public void nextRound() {
        phaser.arriveAndAwaitAdvance();
        sendMessages();
        phaser.arriveAndAwaitAdvance();
        receiveMessages();
        round++;
    }
    
    public void sendMessages() {
        if (round == 0) {
            if (processID == rootID) {
                broadcast(Message.HELLO_TYPE);
            }
        } else if (receivedMessages) {
            if (!hasParent) {
                hasParent = true;
                broadcast(Message.HELLO_TYPE);
            } else {
                if (ackCount + nackCount == getNeighborsNum()) {
                    terminated = true;
                    if (processID != rootID) {
                        sendMessage(Message.ACK_TYPE, parentID);
                    }
                }
            }
            receivedMessages = false;
            sendPending();
        }
    }
    
    public void receiveMessages() {
        if (receiveChannel.isEmpty()) {
            return;
        }
        receivedMessages = true;
        while(!receiveChannel.isEmpty()) {
            Message msg = receiveChannel.poll();
            int senderID = msg.senderID;
            int type = msg.type;
            switch (type) {
                case Message.HELLO_TYPE:
                    handleHelloMessage(senderID);
                    break;
                case Message.ACK_TYPE:
                    handleACKMessage(senderID);
                    break;
                case Message.NACK_TYPE:
                    handleNACKMessage();
                    break;
            }
        }
    }
    
    public void sendMessage(int type, int neighborID) {
        sendChannel.get(neighborID).add(buildMessage(type));
    }
    
    public void broadcast(int type) {
        Iterator<Channel> iterator = sendChannel.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().add(new Message(type, processID));
        }
    }
    
    public void handleHelloMessage(int senderID) {
        if (!hasParent) {
            if (firstMessage) {
                parentID = senderID;
                firstMessage = false;
                return;
            }
            else if (senderID < parentID) {
                pending.put(parentID, buildMessage(Message.NACK_TYPE));
                parentID = senderID;
                return;
            }
        }
        pending.put(senderID, buildMessage(Message.NACK_TYPE));
    }
    
    private void handleACKMessage(int senderID) {
        ackCount++;
        childrenIDs.add(senderID);
    }
    
    private void handleNACKMessage() {
        nackCount++;
    }
    
    private void sendPending() {
        pending.entrySet().forEach((curr) -> {
            sendChannel.get(curr.getKey()).add(curr.getValue());
        });
        pending.clear();
    }
    
    private int getNeighborsNum() {
        return sendChannel.size();
    }
    
    private Message buildMessage(int type) {
        return new Message(type, processID);
    }
    
    public boolean isTerminated() {
        return terminated;
    }

    public int getParentID() {
        return parentID;
    } 
    
    public int getProcessID() {
        return processID;
    }
    
    public Collection<Integer> getChildrenIDs() {
        return childrenIDs;
    }
    
    public static class Builder {
        
        private Phaser phaser;
        private HashMap<Integer, Channel> sendChannel;
        private Channel receiveChannel;
        private int processID;
        private int rootID; 
        
        public Builder withPhaser(final Phaser phaser) {
            this.phaser = phaser;
            return this;
        }
        
        public Builder withSendChannel(final HashMap<Integer, Channel> 
                sendMessages) {
            this.sendChannel = sendMessages;
            return this;
        }
                
        public Builder withReceiveChannel(final Channel receiveChannel) {
            this.receiveChannel = receiveChannel;
            return this;
        }
                        
        public Builder withProcessId(final int processId) {
            this.processID = processId;
            return this;
        }
                                
        public Builder withRootId(final int rootID) {
            this.rootID = rootID;
            return this;
        }
        
        public SyncProcess build() {
            return new SyncProcess(phaser,
                                   sendChannel, 
                                   receiveChannel, 
                                   processID, 
                                   rootID);
        }
    }
}
