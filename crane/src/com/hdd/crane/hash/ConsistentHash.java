package com.hdd.crane.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author David Hu
 * @version 1.0.0
 * @since 2013-11-21
 */
public final class ConsistentHash {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final ReentrantLock lock = new ReentrantLock();

    private TreeMap<Long, String> consistentBuckets;
    private TreeMap<String, Node> nodes;
    private Integer totalWeight = 0;
    private MessageDigest MD5;

    public ConsistentHash() {
        consistentBuckets = new TreeMap<Long, String>();
        nodes = new TreeMap<String, Node>();
        try {
            MD5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // No MD5 algorithm is impossible.
        }

    }

    /**
     * Add a new node,use default weight-1.After <strong>addNode</strong>
     * called,the caller should call<strong>getNodeBackup</strong> to get the
     * backup node.
     * 
     * @param identity
     *            - unique mark of a node(ip:port)
     * @return the prev node's identity of the new node
     */
    public String addNode(String identity) {
        lock.lock();
        try {
            nodes.put(identity, new Node(identity));
            log.debug("A new node[" + identity + "] added.");
            populateBuckets();
            return getNodePrev(identity);
        } finally {
            lock.unlock();
        }

    }

    /**
     * Add a new node.After <strong>addNode</strong> called,the caller should
     * call<strong>getNodeBackup</strong> to get the backup node.
     * 
     * @param identity
     *            - unique mark of a node(ip:port)
     * @return the node's identity of the new node
     */
    public String addNode(String identity, int weight) {
        lock.lock();
        try {
            nodes.put(identity, new Node(identity, weight));
            log.debug("A new node[" + identity + "] added.");
            populateBuckets();
            return getNodePrev(identity);
        } finally {
            lock.unlock();
        }

    }

    public String getNode(String key) {
        lock.lock();
        try {
            return consistentBuckets.get(getBucketKey(key));
        } finally {
            lock.unlock();
        }
    }

    public String getNode(String key, Integer hashCode) {
        lock.lock();
        try {
            return consistentBuckets.get(getBucketKey(key, hashCode));
        } finally {
            lock.unlock();
        }
    }

    /**
     * The prev node is the greater of that is less than the given one.If the
     * given node is least,then prev node is the last node(as well as the
     * greatest node)
     * 
     * @param identity
     * @return
     */
    private String getNodePrev(String identity) {
        SortedMap<String, Node> lessMap = this.nodes.headMap(identity, false);
        return (lessMap.isEmpty()) ? this.nodes.lastKey() : lessMap.lastKey();
    }

    /**
     * The backup of a node is the less greater node than the given one.If the
     * given node is greatest,then backup node is the first node(as well as the
     * least node)
     * 
     * @param identity
     * @return
     */
    public String getNodeBackup(String identity) {
        lock.lock();
        try {
            SortedMap<String, Node> greaterSet = this.nodes.tailMap(identity, false);
            return (greaterSet.isEmpty()) ? this.nodes.firstKey() : greaterSet.firstKey();
        } finally {
            lock.unlock();
        }
    }

    private void populateBuckets() {
        this.totalWeight = 0;
        for (Node node : nodes.values()) {
            this.totalWeight += node.getWeight();
        }
        for (Node node : nodes.values()) {
            int thisWeight = node.getWeight();
            double factor = Math.floor(((double) (40 * this.nodes.size() * thisWeight)) / (double) this.totalWeight);
            for (long j = 0; j < factor; j++) {
                byte[] d = MD5.digest((node.getIdentity() + "-" + j).getBytes());
                for (int h = 0; h < 4; h++) {
                    Long k = ((long) (d[3 + h * 4] & 0xFF) << 24) | ((long) (d[2 + h * 4] & 0xFF) << 16) | ((long) (d[1 + h * 4] & 0xFF) << 8) | (d[0 + h * 4] & 0xFF);
                    consistentBuckets.put(k, node.getIdentity());
                }
            }
        }
    }

    private long getBucketKey(String key) {
        return getBucketKey(key, null);
    }

    private long getBucketKey(String key, Integer hashCode) {
        long hc = getHash(key, hashCode);
        return findFirstAvailableKey(hc);
    }

    private long getHash(String key, Integer hashCode) {
        if (hashCode != null) {
            return hashCode.longValue() & 0xffffffffL;
        } else {
            return md5Hash(key);
        }
    }

    /**
     * Gets the first available key equal or above the given one, if none found,
     * returns the first k in the bucket
     * 
     * @param k
     *            key
     * @return
     */
    private Long findFirstAvailableKey(Long hash) {
        SortedMap<Long, String> greaterMap = this.consistentBuckets.tailMap(hash);
        return (greaterMap.isEmpty()) ? this.consistentBuckets.firstKey() : greaterMap.firstKey();
    }

    /**
     * Internal private hashing method.
     * 
     * MD5 based hash algorithm for use in the consistent hashing approach.
     * 
     * @param key
     * @return
     */
    private long md5Hash(String key) {
        MD5.reset();
        MD5.update(key.getBytes());
        byte[] bKey = MD5.digest();
        long res = ((long) (bKey[3] & 0xFF) << 24) | ((long) (bKey[2] & 0xFF) << 16) | ((long) (bKey[1] & 0xFF) << 8) | bKey[0] & 0xFF;
        return res;
    }

    private class Node implements Comparable<Node> {
        public Node(String identity, int weight) {
            this.identity = identity;
            this.weight = weight;
        }

        public Node(String identity) {
            this.identity = identity;
            this.weight = 1;
        }

        private final String identity;
        private final int weight;

        public String getIdentity() {
            return identity;
        }

        public int getWeight() {
            return weight;
        }

        @Override
        public int compareTo(Node other) {
            return this.identity.hashCode() - other.identity.hashCode();
        }
    }

    public static void main(String[] args) {
        ConsistentHash hash = new ConsistentHash();
        hash.addNode("10.116.40.70:88", 4);
        hash.addNode("10.116.40.70:99", 4);
        hash.addNode("10.116.40.71:99", 3);
        hash.addNode("10.116.40.72:77", 6);

        System.out.println(hash.getBucketKey("tset"));
        System.out.println(hash.getNode("tset"));
        System.out.println(hash.getBucketKey("tset#backup"));
        System.out.println(hash.getNode("tset#backup"));

        System.out.println(hash.getNodeBackup("10.116.40.70:88"));
        System.out.println(hash.getNodePrev("10.116.40.70:88"));
        System.out.println(hash.getNodeBackup("10.116.40.72:77"));
        System.out.println(hash.getNodePrev("10.116.40.72:77"));

    }

}
