A repository for all open source tokenizers and filters
==================

[![travis ci build status](https://travis-ci.org/solr-cool/solr-analyzers.png)](https://travis-ci.org/github/solr-cool/solr-analyzers)
[![Maven Central](https://img.shields.io/maven-central/v/cool.solr/solr-analyzers)](https://search.maven.org/artifact/cool.solr/solr-analyzers/)

> ♻️ this is the official and maintained fork of the original [@shopping24](https://github.com/shopping24) repository maintained by [solr.cool](https://solr.cool).

## AnalyzingSentenceTokenizer

This analyzer will filter sentences from text in a efficient way that contains a lot (defined by a threshold) of stopwords. Could be used as a filter for SEO-text from product descriptions.

Example usage in your field types after you put the jar (`solr-analyzers-<VERSION>-jar-with-dependencies.jar`) into your solr `lib` dir:

     <!-- Use the sentence tokenizer, which removes "noise" sentences and keeps only "signal" -->
     <tokenizer class="com.s24.search.solr.analyzers.AnalyzingSentenceTokenizerFactory"
                stopwordfile="list_of_stopwords.txt"
                filter="true" />

Arguments:
* `stopwordfile (required)`: List of stopwords.
* `filter`: Set to true if the sentences should be filtered out.
* `commaWordThreshold`: Threshold that defines the "comma density" that, if exceeded, causes a sentence to be split into sub-sentences that are analyzed individually.
* `maxStopwordRatio`: Ratio of stopwords exceeds this threshold, the sentence is filtered out.
* `minSentenceLength`: Sentence must contain at least this many words, otherwise it is not analyzed and always emitted.


## Building the project

This should install the current version into your local repository

    $ mvn clean install

## License

This project is licensed under the [Apache License, Version 2](http://www.apache.org/licenses/LICENSE-2.0.html).
