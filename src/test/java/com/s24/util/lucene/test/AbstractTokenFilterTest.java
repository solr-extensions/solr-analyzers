package com.s24.util.lucene.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.hamcrest.TypeSafeMatcher;

import com.s24.search.solr.analyzers.StemmingBufferAttribute;

/**
 * Base class for token filter tests.
 */
public class AbstractTokenFilterTest {

   public AbstractTokenFilterTest() {
      super();
   }

   /**
    * Matches the current token of the token stream if the token matches all of the given matches.
    * 
    * @param tokenMatchers
    *           the matchers the token must match against.
    */
   @SafeVarargs
   protected final Matcher<TokenStream> token(final Matcher<TokenStream>... tokenMatchers) {
      return new TypeSafeMatcher<TokenStream>() {
         @Override
         public void describeTo(Description description) {
            description.appendText("a token ").appendList("with ", " and ", "", Arrays.asList(tokenMatchers));
         }

         @Override
         protected void describeMismatchSafely(TokenStream stream, Description mismatchDescription) {
            for (Matcher<TokenStream> matcher : tokenMatchers) {
               if (!matcher.matches(stream)) {
                  matcher.describeMismatch(stream, mismatchDescription);
               }
            }
         }

         @Override
         protected boolean matchesSafely(TokenStream stream) {
            for (Matcher<TokenStream> matcher : tokenMatchers) {
               if (!matcher.matches(stream)) {
                  return false;
               }
            }
            return true;
         }
      };
   }

   /**
    * Matches the current token of the token stream if its {@link CharTermAttribute} matches the given string.
    * 
    * @param expectedTerm
    *           the expected string.
    */
   protected Matcher<TokenStream> term(final String expectedTerm) {
      return new TypeSafeMatcher<TokenStream>() {
         @Override
         public void describeTo(Description description) {
            description.appendText("term=").appendValue(expectedTerm);
         }

         @Override
         protected void describeMismatchSafely(TokenStream stream, Description mismatchDescription) {
            CharTermAttribute termAttr = stream.addAttribute(CharTermAttribute.class);
            mismatchDescription.appendText("Expected term=").appendValue(expectedTerm)
                  .appendText(", but was term=").appendValue(termAttr.toString()).appendText("\n");
         }

         @Override
         protected boolean matchesSafely(TokenStream stream) {
            CharTermAttribute termAttr = stream.addAttribute(CharTermAttribute.class);
            return termAttr.toString().equals(expectedTerm);
         }
      };
   }

   /**
    * Matches the current token of the token stream if its {@link OffsetAttribute} has the given start and end offsets.
    * 
    * @param expectedStart
    *           the expected start offset.
    * @param expectedEnd
    *           the expected end offset.
    */
   protected Matcher<TokenStream> offsets(final int expectedStart, final int expectedEnd) {
      return new TypeSafeMatcher<TokenStream>() {
         @Override
         public void describeTo(Description description) {
            description.appendText("startOffset=").appendValue(expectedStart).appendText(",endOffset=")
                  .appendValue(expectedEnd);
         }

         @Override
         protected void describeMismatchSafely(TokenStream stream, Description mismatchDescription) {
            OffsetAttribute attr = stream.addAttribute(OffsetAttribute.class);
            mismatchDescription
                  .appendText("Expected offsets=").appendValue(expectedStart).appendText(",").appendValue(expectedEnd)
                  .appendText(", but was offsets=").appendValue(attr.startOffset()).appendText(",")
                  .appendValue(attr.endOffset()).appendText("\n");
         }

         @Override
         protected boolean matchesSafely(TokenStream stream) {
            OffsetAttribute attr = stream.addAttribute(OffsetAttribute.class);
            return attr.startOffset() == expectedStart && attr.endOffset() == expectedEnd;
         }
      };
   }

   /**
    * Matches the current token of the token stream if its {@link PositionIncrementAttribute} has the given increment.
    * 
    * @param expectedIncrement
    *           the expected position increment.
    */
   protected Matcher<TokenStream> positionIncrement(final int expectedIncrement) {
      return new TypeSafeMatcher<TokenStream>() {
         @Override
         public void describeTo(Description description) {
            description.appendText("positionIncrement=").appendValue(expectedIncrement);
         }

         @Override
         protected void describeMismatchSafely(TokenStream stream, Description mismatchDescription) {
            PositionIncrementAttribute attr = stream.addAttribute(PositionIncrementAttribute.class);
            mismatchDescription.appendText("Expected positionIncrement=").appendValue(expectedIncrement)
                  .appendText(", but was positionIncrement=").appendValue(attr.getPositionIncrement()).appendText("\n");
         }

         @Override
         protected boolean matchesSafely(TokenStream stream) {
            PositionIncrementAttribute attr = stream.addAttribute(PositionIncrementAttribute.class);
            return attr.getPositionIncrement() == expectedIncrement;
         }
      };
   }

   /**
    * Matches that the given <code>TokenStream</code> produces the expected sequence of tokens. Fails if a token does
    * not match its respective matcher, or if the number of tokens in the token stream does not match the number of
    * given matchers.
    * 
    * @param stream
    *           the token stream.
    * @param expectedTokens
    *           the matchers for the expected tokens.
    */
   @SafeVarargs
   protected final void assertTokenStream(TokenStream stream, Matcher<TokenStream>... expectedTokens) throws Exception {
      final int expectedTokenCount = expectedTokens.length;
      int tokenCount = 0;
      while (stream.incrementToken()) {
         assertTrue("Too many tokens", tokenCount < expectedTokens.length);
         Matcher<TokenStream> tokenMatcher = expectedTokens[tokenCount];
         boolean matches = tokenMatcher.matches(stream);
         if (!matches) {
            Description description = new StringDescription();
            description.appendText("Unexpected token at position ").appendValue(tokenCount).appendText("\n");
            tokenMatcher.describeMismatch(stream, description);
            fail(description.toString());
         }
         tokenCount++;
      }
      assertEquals("Unexpected number of tokens", expectedTokenCount, tokenCount);
   }

   protected Matcher<TokenStream> positionLength(final int expectedLength) {
      return new TypeSafeMatcher<TokenStream>() {
         @Override
         public void describeTo(Description description) {
            description.appendText("positionLength=").appendValue(expectedLength);
         }

         @Override
         protected boolean matchesSafely(TokenStream stream) {
            PositionLengthAttribute attr = stream.addAttribute(PositionLengthAttribute.class);
            return attr.getPositionLength() == expectedLength;
         }
      };
   }

   /**
    * Matches the current token of the token stream if its {@link StemmingBufferAttribute} matches the given string.
    * 
    * @param expectedStemmedToken
    *           the expected string.
    */
   protected Matcher<TokenStream> stemmingBuffer(final String expectedOriginalToken,
         final String expectedStemmedToken) {
      return new TypeSafeMatcher<TokenStream>() {
         @Override
         public void describeTo(Description description) {
            description.appendText("originalToken=").appendValue(expectedOriginalToken);
            description.appendText(" and stemmedToken=").appendValue(expectedStemmedToken);
         }

         @Override
         protected boolean matchesSafely(TokenStream stream) {
            StemmingBufferAttribute termAttr = stream.addAttribute(StemmingBufferAttribute.class);
            // if one of the token (stemmed or original) is null, the expected must be null too
            if (termAttr == null) {
               return (expectedStemmedToken == null && expectedOriginalToken == null);
            } else {
               boolean isEqual;
               if (expectedOriginalToken == null) {
                  isEqual = (termAttr.getOriginalToken() == null || termAttr.getOriginalTokenLength() == 0);
               } else if (expectedStemmedToken == null) {
                  isEqual = (termAttr.getStemmedToken() == null || termAttr.getStemmedTokenLength() == 0);
               } else {
                  // if we compare the values, we have to create char arrays with the real length, because termAttribute
                  // arrays are initialized with 16
                  char[] originalToken = Arrays.copyOf(termAttr.getOriginalToken(), termAttr.getOriginalTokenLength());
                  char[] charArray = expectedOriginalToken.toCharArray();
                  char[] stemmedToken = Arrays.copyOf(termAttr.getStemmedToken(), termAttr.getStemmedTokenLength());
                  char[] charArray2 = expectedStemmedToken.toCharArray();

                  isEqual = (Arrays.equals(originalToken, charArray) && Arrays
                        .equals(stemmedToken, charArray2));
               }

               return isEqual;
            }
         }
      };
   }


}
