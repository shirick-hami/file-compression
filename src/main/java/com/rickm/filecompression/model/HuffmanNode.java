package com.rickm.filecompression.model;

import java.io.Serializable;

/**
 * Represents a node in the Huffman tree.
 * Each node contains a byte value, frequency, and references to left and right children.
 */
public class HuffmanNode implements Comparable<HuffmanNode>, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final byte data;
    private final int frequency;
    private final HuffmanNode left;
    private final HuffmanNode right;
    
    /**
     * Creates a leaf node with the given byte value and frequency.
     */
    public HuffmanNode(byte data, int frequency) {
        this.data = data;
        this.frequency = frequency;
        this.left = null;
        this.right = null;
    }
    
    /**
     * Creates an internal node by combining two child nodes.
     */
    public HuffmanNode(HuffmanNode left, HuffmanNode right) {
        this.data = 0;
        this.frequency = left.frequency + right.frequency;
        this.left = left;
        this.right = right;
    }
    
    public byte getData() {
        return data;
    }
    
    public int getFrequency() {
        return frequency;
    }
    
    public HuffmanNode getLeft() {
        return left;
    }
    
    public HuffmanNode getRight() {
        return right;
    }
    
    public boolean isLeaf() {
        return left == null && right == null;
    }
    
    @Override
    public int compareTo(HuffmanNode other) {
        int freqCompare = Integer.compare(this.frequency, other.frequency);
        if (freqCompare != 0) {
            return freqCompare;
        }
        // Secondary comparison by data for deterministic tree building
        return Byte.compare(this.data, other.data);
    }
    
    @Override
    public String toString() {
        if (isLeaf()) {
            return String.format("Leaf[data=%d, freq=%d]", data & 0xFF, frequency);
        }
        return String.format("Internal[freq=%d]", frequency);
    }
}
