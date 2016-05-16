package jolt527.cryptogram;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * This is a class that represents a dictionary of English language words.  It reads in a file full of words
 * (one word per line), converts each one to lowercase, and inserts them in a trie for fast insertion and
 * lookup.  This class abstracts away the trie in favor of a more natural interface.
 *
 * Created by Keith on 5/14/2016.
 */
public class Dictionary {

    private static final String DICTIONARY_FILENAME = "resources\\linuxwords.txt";

    private static Trie wordTrie = null;

    /**
     * Constructor that creates the trie and inserts English language words from a file into it.
     * Also outputs metrics on load times and dictionary word size counts to standard output.
     */
    public Dictionary() {
        // if the trie already exists, we're done - the trie is actually fairly big in memory, so we don't
        // want too many of them around!
        if (null != wordTrie) {
            return;
        }

        wordTrie = new Trie();

        Map<Integer, Integer> wordCounts = new TreeMap<>();

        // fancy java 8 streams and lambdas for reading a file in line-by-line
        long startTime = System.currentTimeMillis();
        try (Stream<String> stream = Files.lines(Paths.get(DICTIONARY_FILENAME))) {
            stream.forEach( (originalWord) -> {
                // only insert words that are only a-z, and make them lowercase before doing so
                // (the trie requires this)
                String word = originalWord.toLowerCase();
                if (word.matches("[a-z]+")) {
                    wordTrie.insertWord(word);

                    int currentCount = null != wordCounts.get(word.length()) ? wordCounts.get(word.length()) + 1 : 1;
                    wordCounts.put(word.length(), currentCount);
                }
            } );
        }
        catch (IOException e) {
            System.out.println(e);
        }

        // output some metrics on processing time and dictionary word size counts (for fun!)
        System.out.println(String.format("Dictionary words list processed in %s ms.", System.currentTimeMillis() - startTime));
        System.out.println("-----------------------------------");
        System.out.println("DICTIONARY WORD SIZE COUNTS:");
        for (Map.Entry<Integer, Integer> currentEntry : wordCounts.entrySet()) {
            System.out.println(String.format("%s => %s", currentEntry.getKey(), currentEntry.getValue()));
        }
        System.out.println("-----------------------------------");
    }

    /**
     * Tests whether a word exists in the dictionary.
     *
     * @param word    The word to search for.
     * @return  True is the word exists in the dictionary, false otherwise.
     */
    public boolean doesWordExist(String word) {
        return wordTrie.doesWordExist(word);
    }

    /**
     * Finds all words in the dictionary that match a specified pattern, and returns them in a list.
     *
     * A pattern can consist of any number of lowercase letters and asterisks ('*').  An asterisk is a
     * wildcard character that will match any lowercase letter.
     *
     * Example:
     * If "cat", "cot", and "cab" are in the dictionary, using the pattern "c*t" will return a list with
     * "cat" and "cot".
     *
     * @param pattern    The pattern to match the dictionary's words against (lowercase letters or asterisks only).
     * @return  The list of words in the dictionary that match against the input pattern.
     */
    public List<String> findWordsByPattern(String pattern) {
        return wordTrie.findWordsByPattern(pattern);
    }
}
