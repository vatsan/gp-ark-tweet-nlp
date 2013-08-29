A PL/Java Wrapper on Ark-Tweet-NLP (http://www.ark.cs.cmu.edu/TweetNLP/) - Twitter Parts-of-speech tagger in Postgres/Greenplum.
This package enables you to perform part-of-speech tagging on Tweets, using SQL. If your environment is an MPP system like Pivotal's Greenplum Database
you can piggyback on the MPP architecture and achieve implicit parallelism in your part-of-speech tagging tasks.

Getting Started
=================

The packaged jar file: gp-ark-tweet-nlp.jar is located in build/.
Follow the instructions to install the pre-requisites section of the sql/pos_tagger_invocation.sql file.
You're then set to run the part-of-speech tagger in SQL using steps described in the sql/pos_tagger_invocation.sql file.


Questions?
============

You can reach me at vatsan.cs@utexas.edu


