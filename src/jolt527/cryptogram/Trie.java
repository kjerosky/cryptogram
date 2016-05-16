package jolt527.cryptogram;

import java.util.LinkedList;
import java.util.List;

/**
 * A Trie data structure for fast insertion and lookup of lowercase words (matches "[a-z]+").
 *
 * See this wiki page for more details:
 * https://en.wikipedia.org/wiki/Trie
 *
 * Created by Keith on 5/15/2016.
 */
public class Trie {

    private static final char WILDCARD_CHAR = '*';

    // the root node of the trie
    private TrieNode root = new TrieNode();


    /**
     * Private inner class for a node in the trie.
     */
    private class TrieNode {

        boolean wordHere;       // does the path traversed to this node indicate a word?
        TrieNode[] nodeLinks;   // links to the other trie nodes

        /**
         * By default, no word is at this node, and its links shouldn't be connected to anywhere.
         */
        TrieNode() {
            wordHere = false;
            nodeLinks = new TrieNode[26];
            for (int i = 0; i < 26; i++) {
                nodeLinks[i] = null;
            }
        }

    }

    /**
     * Inserts a word into the trie.  This is the public storefront method that calls
     * the private recursive version.
     *
     * IMPORTANT: The word must consist of only lowercase a-z characters!
     *
     * @param word    The word to insert into the trie, consisting of only lowercase a-z characters.
     */
    public void insertWord(String word) {
        insertWord(word, root);
    }

    /**
     * Inserts a word into the trie, recursively.
     *
     * @param word           The word to insert into the trie.
     * @param currentNode    Reference to the current node in the trie we've traversed to.
     */
    private void insertWord(String word, TrieNode currentNode) {
        // if the word at this point is empty, then we place a marker here to signal
        // that the word exists at this point, and we're done
        if (word.isEmpty()) {
            currentNode.wordHere = true;
            return;
        }

        // get the node for the current character (the first one in the string right now),
        // or create the node if it doesn't exist yet
        char currentChar = word.charAt(0);
        TrieNode nextNode = currentNode.nodeLinks[currentChar - 'a'];
        if (null == nextNode) {
            nextNode = new TrieNode();
            currentNode.nodeLinks[currentChar - 'a'] = nextNode;
        }

        // process the rest of the word at the next node
        insertWord(word.substring(1), nextNode);
    }

    /**
     * Determines if a word exists in the trie.  This is the public storefront method
     * that calls the private recursive version.
     *
     * @param word    The word to search for.
     * @return  True if the word exists in the trie, false otherwise.
     */
    public boolean doesWordExist(String word) {
        return doesWordExist(word, root);
    }

    /**
     * Determines if a word exists in the trie, recursively.
     *
     * @param word           The word to search for.
     * @param currentNode    Reference to the current node in the trie we've traversed to.
     * @return  True if the word exists in the trie, false otherwise.
     */
    private boolean doesWordExist(String word, TrieNode currentNode) {
        // if the word at this point is empty, then the word exists in the trie if a
        // marker exists for a word being at this node
        if (word.isEmpty()) {
            return currentNode.wordHere;
        }

        // get the node for the current character (the first one in the string right now)
        char currentChar = word.charAt(0);
        TrieNode nextNode = currentNode.nodeLinks[currentChar - 'a'];

        // if the node doesn't exist, then there's no path to the word, so we're done
        if (null == nextNode) {
            return false;
        }

        // process the rest of the word at the next node
        return doesWordExist(word.substring(1), nextNode);
    }

    /**
     * Finds the words in the trie that match a specified pattern, and returns them in a list.
     * This is the public storefront method that calls the private recursive version.
     *
     * A pattern can consist of any number of lowercase letters and asterisks ('*').  An asterisk is a
     * wildcard character that will match any lowercase letter.
     *
     * Example:
     * If "cat", "cot", and "cab" are in the trie, using the pattern "c*t" will return a list with
     * "cat" and "cot".
     *
     * @param pattern    The pattern to match the trie's words against (lowercase letters or asterisks only).
     * @return  The list of words in the trie that match against the input pattern.
     */
    public List<String> findWordsByPattern(String pattern) {
        List<String> wordList = new LinkedList<>();
        findWordsByPattern(pattern, wordList, root, "");
        return wordList;
    }

    /**
     * Finds the words in the trie that match a specified pattern, and adds them to an input list.
     *
     * @param pattern        The pattern to match the trie's words against.
     * @param wordList       The list of matched words so far.
     * @param currentNode    Reference to the current node in the trie we've traversed to.
     * @param wordSoFar      The string that represents the word built so far, using the path we've traversed so far.
     */
    private void findWordsByPattern(String pattern, List<String> wordList, TrieNode currentNode, String wordSoFar) {
        // if the pattern is empty, we're done here, but first check if we're at a word before quitting
        if (pattern.isEmpty()) {
            if (currentNode.wordHere) {
                wordList.add(wordSoFar);
            }
            return;
        }

        // determine the next nodes to visit - for wildcard characters, we want to visit all possible paths
        char currentChar = pattern.charAt(0);
        int currentCharIndex = currentChar - 'a';
        List<Integer> indicesToVisit = new LinkedList<>();
        if (WILDCARD_CHAR == currentChar) {

            // visit all nodes linked to by the current node
            for (int i = 0; i < 26; i++) {
                if (null != currentNode.nodeLinks[i]) {
                    indicesToVisit.add(i);
                }
            }

        }

        // for non-wildcard characters, we want to visit the node associated with those characters (if they exist)
        else if (null != currentNode.nodeLinks[currentCharIndex]) {
            indicesToVisit.add(currentCharIndex);
        }

        // visit those nodes recursively
        for (int linkIndex : indicesToVisit) {
            findWordsByPattern(
                    pattern.substring(1),
                    wordList, currentNode.nodeLinks[linkIndex],
                    wordSoFar + Character.toString((char)('a' + linkIndex)));
        }
    }

}
