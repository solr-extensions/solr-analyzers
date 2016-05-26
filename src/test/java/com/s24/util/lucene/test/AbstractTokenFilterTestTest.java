package com.s24.util.lucene.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class AbstractTokenFilterTestTest {

   private AbstractTokenFilterTest test;

   private TokenStream stream;
   private Matcher<TokenStream> matcherTrue;
   private Matcher<TokenStream> matcherFalse;

   @Before
   public void setUp() throws Exception {
      test = new AbstractTokenFilterTest();

      stream = new TokenStream() {
         private int i = 0;

         @Override
         public boolean incrementToken() throws IOException {
            return i++ < 1;
         }
      };

      matcherTrue = mock(Matcher.class);
      when(matcherTrue.matches(any())).thenReturn(true);
      matcherFalse = mock(Matcher.class);
      when(matcherFalse.matches(any())).thenReturn(false);
   }

   @Test
   public void testTokenMatcherWithOneMatcher() throws Exception {
      Matcher<TokenStream> matcher = test.token(matcherTrue);

      assertTrue(matcher.matches(stream));
      verify(matcherTrue).matches(stream);
   }

   @Test
   public void testTokenMatcherMatchesIfAllNestedMatchersMatch() throws Exception {
      Matcher<TokenStream> matcher = test.token(matcherTrue, matcherTrue, matcherTrue);
      assertTrue(matcher.matches(stream));
   }

   @Test
   public void testTokenMatcherDoesNotMatchIfOneNestedMatcherDoesNotMatch() throws Exception {
      Matcher<TokenStream> matcher = test.token(matcherTrue, matcherTrue, matcherFalse);
      assertFalse(matcher.matches(stream));

      Description d = new StringDescription();
      matcher.describeMismatch(stream, d);
      // Mismatch description must have been requested from the failed match:
      verify(matcherFalse).describeMismatch(stream, d);
   }

   @Test
   public void testTokenMatcherDescriptionBasedOnDescriptionOfNestedMatchers() throws Exception {
      Matcher<TokenStream> matcher = test.token(matcherTrue, matcherFalse);
      Description d = new StringDescription();
      matcher.describeTo(d);

      verify(matcherTrue).describeTo(d);
      verify(matcherFalse).describeTo(d);
   }

   @Test
   public void testTerm() throws Exception {
      Description description = mock(Description.class);
      when(description.appendText(anyString())).thenReturn(description);

      Matcher<TokenStream> term = test.term("expected");

      term.describeTo(description);
      verify(description, times(1)).appendText("term=");
      verify(description, times(1)).appendValue("expected");

      // Not a real test, but good for coverage
      assertFalse(test.term("foo").matches(stream));
      test.term("foo").describeMismatch(stream, new StringDescription());
   }

   @Test
   public void testOffset() throws Exception {
      Description description = mock(Description.class);
      when(description.appendText(anyString())).thenReturn(description);
      when(description.appendValue(anyInt())).thenReturn(description);

      Matcher<TokenStream> offsetsMatcher = test.offsets(0, 1);
      offsetsMatcher.describeTo(description);

      verify(description, times(1)).appendText("startOffset=");
      verify(description, times(1)).appendValue(0);
      verify(description, times(1)).appendText(",endOffset=");
      verify(description, times(1)).appendValue(1);

      // Not a real test, but good for coverage
      assertFalse(test.offsets(42, 4711).matches(stream));
      test.offsets(42, 4711).describeMismatch(stream, new StringDescription());
   }

   @Test
   public void testPositionIncrement() throws Exception {
      Description description = mock(Description.class);
      when(description.appendText(anyString())).thenReturn(description);
      when(description.appendValue(anyInt())).thenReturn(description);

      Matcher<TokenStream> positionMatcher = test.positionIncrement(1);
      positionMatcher.describeTo(description);

      verify(description, times(1)).appendText("positionIncrement=");
      verify(description, times(1)).appendValue(1);

      // Not a real test, but good for coverage
      assertFalse(test.positionIncrement(23).matches(stream));
      test.positionIncrement(23).describeMismatch(stream, new StringDescription());
   }

   @Test
   public void testAssertTokenStreamTrue() throws Exception {
      test.assertTokenStream(stream, matcherTrue);
   }

   @Test
   public void testAssertTokenStreamFalse() throws Exception {
      boolean failed = false;
      try {
         test.assertTokenStream(stream, matcherFalse);
      } catch (AssertionError e) {
         failed = true;
      }
      if (!failed) {
         fail("assertTokenStream did not fail although matcher returned false");
      }
   }

}
