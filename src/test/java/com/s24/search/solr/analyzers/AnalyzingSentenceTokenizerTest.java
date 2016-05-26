package com.s24.search.solr.analyzers;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.junit.Before;
import org.junit.Test;

import com.s24.util.lucene.test.AbstractTokenFilterTest;

public class AnalyzingSentenceTokenizerTest extends AbstractTokenFilterTest {

   private AnalyzingSentenceTokenizer tokenizer;
   private static final CharArraySet STOPWORDS = new CharArraySet(Arrays.asList("stopword", "ignore", "this", "word"),
         true);

   @Before
   public void setUp() throws Exception {

      // build tokenizer
      tokenizer = new AnalyzingSentenceTokenizer(
            TokenStream.DEFAULT_TOKEN_ATTRIBUTE_FACTORY,
            true,
            STOPWORDS,
            AnalyzingSentenceTokenizerFactory.DEFAULT_COMMA_WORD_THRESHOLD,
            AnalyzingSentenceTokenizerFactory.DEFAULT_MAX_STOPWORD_RATIO,
            AnalyzingSentenceTokenizerFactory.DEFAULT_MIN_SENTENCE_LENGTH);
   }

   static TokenStream tokenize(String input, Tokenizer tokenizer) throws IOException {
      tokenizer.close();
      tokenizer.end();
      Reader reader = new StringReader(input);
      tokenizer.setReader(reader);
      tokenizer.reset();
      return tokenizer;
   }

   @Test
   public void testSentenceWithoutStopwordsIsEmittedAsToken() throws Exception {
      assertTokenStream(tokenize("foo", tokenizer),
            token(term("foo"), offsets(0, 3)));
      assertTokenStream(tokenize("A simple sentence without stopwords.", tokenizer),
            token(term("A simple sentence without stopwords."), offsets(0, 36)));
   }

   @Test
   public void testTwoSentencesWithoutStopwords() throws Exception {
      assertTokenStream(tokenize("First sentence. Second sentence.", tokenizer),
            token(term("First sentence. "), offsets(0, 16)),
            token(term("Second sentence."), offsets(16, 32)));

      // sentences with few words will always be emitted, so we test it with a longer sentence.
      assertTokenStream(tokenize("First sentence with some more words. Second sentence.", tokenizer),
            token(term("First sentence with some more words. "), offsets(0, 37)),
            token(term("Second sentence."), offsets(37, 53)));
   }

   @Test
   public void testSentenceWithTooManyStopwordsIsRemoved() throws Exception {
      assertTokenStream(tokenize(
            "First sentence. Should ignore this sentence ignore. Another sentence with some more words.", tokenizer),
            token(term("First sentence. "), offsets(0, 16)),
            token(term("Another sentence with some more words."), offsets(52, 90)));
   }

   @Test
   public void testSplitSentenceWithManyCommas() throws Exception {
      assertTokenStream(tokenize("90% cotton, 10% wool, size 42, lets take a look at this.", tokenizer),
            token(term("90% cotton,"), offsets(0, 11)),
            token(term(" 10% wool,"), offsets(11, 21)),
            token(term(" size 42,"), offsets(21, 30)),
            token(term(" lets take a look at this."), offsets(30, 56)));
   }

   @Test
   public void testOnlySentenceIsPassedThrough() throws Exception {
      // If the input has only a single sentence that is not split, it should be emitted as a token
      assertTokenStream(tokenize("ignore this word ignore this.", tokenizer),
            token(term("ignore this word ignore this."), offsets(0, 29)));
   }

   @Test
   public void testSentenceWithSomeIgnoredSubsentences() throws Exception {
      assertTokenStream(tokenize("90% cotton, 10% ignore this with too much stopword, size 42.", tokenizer),
            token(term("90% cotton,"), offsets(0, 11)),
            token(term(" size 42."), offsets(51, 60)));
   }

   @Test
   public void testSentenceWithSomeIgnoredSubsentencesFollowedByAcceptedSentence() throws Exception {
      assertTokenStream(tokenize("90% cotton, 10% ignore this ignore this, size 42. Another sentence.", tokenizer),
            token(term("90% cotton,"), offsets(0, 11)),
            token(term(" size 42. "), offsets(40, 50)),
            token(term("Another sentence."), offsets(50, 67)));
   }

}
