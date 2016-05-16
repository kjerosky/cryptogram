package jolt527.cryptogram;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the Trie class.
 *
 * Created by Keith on 5/15/2016.
 */
public class TrieTest {

    private static final String TEST_WORD = "cat";
    private static final String SIMILAR_TEST_WORD = "cot";
    private static final String OTHER_WORD = "dog";
    private static final String PATTERN_FOR_TEST_WORD_AND_SIMILAR_ONE = "c*t";

    private static Trie trie;

    /**
     * Every test needs a blank trie.
     */
    @Before
    public void setup() {
        trie = new Trie();
    }

    /**
     * Ensure a word inserted into the trie is found.
     */
    @Test
    public void test_insertWordAndFindIt() {
        trie.insertWord(TEST_WORD);
        assertTrue(trie.doesWordExist(TEST_WORD));
    }

    /**
     * Ensure a word not inserted into the trie is not found.
     */
    @Test
    public void test_insertWordAndTryToFindAnotherWord() {
        trie.insertWord(TEST_WORD);
        assertFalse(trie.doesWordExist(OTHER_WORD));
    }

    /**
     * Ensure that finding words with only non-wildcard characters in a pattern works correctly.
     */
    @Test
    public void test_findWordsWithOnlyNonWildcardCharactersInPattern() {
        trie.insertWord(TEST_WORD);
        trie.insertWord(SIMILAR_TEST_WORD);

        List<String> foundWordsList = trie.findWordsByPattern(TEST_WORD);
        assertTrue(1 == foundWordsList.size());
        assertTrue(foundWordsList.contains(TEST_WORD));
    }

    /**
     * Ensure that finding words with wildcard characters in a pattern works correctly.
     */
    @Test
    public void test_findWordsWithWildcardCharactersInPattern() {
        trie.insertWord(TEST_WORD);
        trie.insertWord(SIMILAR_TEST_WORD);
        trie.insertWord(OTHER_WORD);

        List<String> foundWordsList = trie.findWordsByPattern(PATTERN_FOR_TEST_WORD_AND_SIMILAR_ONE);
        assertTrue(2 == foundWordsList.size());
        assertTrue(foundWordsList.contains(TEST_WORD));
        assertTrue(foundWordsList.contains(SIMILAR_TEST_WORD));
    }

}
