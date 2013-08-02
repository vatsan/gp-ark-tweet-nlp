Parts-of-speech tagging for Twitter via SQL
=============================================

`gp-ark-tweet-nlp` is a PL/Java Wrapper for [`Ark-Tweet-NLP`](http://www.ark.cs.cmu.edu/TweetNLP/) - a state-of-the-art parts-of-speech tagger for Twitter.
This package enables you to perform part-of-speech tagging on Tweets, using SQL. If your environment is an MPP system like Pivotal's Greenplum Database
you can piggyback on the MPP architecture and achieve implicit parallelism in your part-of-speech tagging tasks.

The GitHub page can be found at: http://vatsan.github.io/gp-ark-tweet-nlp/

Installation
=============

To be able to use the PL/Java wrapper you'll need to ensure some pre-requisites are satisfied in your environment.


### 1. Ensure PL/Java is installed and it works
```SQL
--Install PL/Java
CREATE LANGUAGE pljava;

-- Define function mapping
DROP FUNCTION IF EXISTS getsysprop(varchar);
CREATE FUNCTION getsysprop(varchar) 
     RETURNS varchar
AS
'java.lang.System.getProperty'
LANGUAGE pljava;
				
--Invoke function
SELECT getsysprop('user.home');
```

### 2. Ensure `JAVA_HOME` is set on all hosts
As root run the following

1. gpssh into all hosts
```
[gpadmin@mdw ~]$ gpssh -f <hostfile> ;
```

2. In the gpssh prompt, run the following:
```
[gpadmin@mdw ~]$ echo "export JAVA_HOME=/usr/java/jdk1.6.0_24" >> /home/gpadmin/.bashrc
```

###3. Prepare the Greenplum/Postgres Environment to talk to our PL/Java wrapper
1. Copy the Libraries: The packaged jar file [ `gp-ark-tweet-nlp.jar`](https://github.com/vatsan/gp-ark-tweet-nlp/tree/master/build) that located in the folder `build/`
```
[gpadmin@mdw ~]$ gpscp -f <hostfile> gp-ark-tweet-nlp.jar =:/usr/local/greenplum-db/lib/postgresql/java/
[gpadmin@mdw ~]$ gpscp -f <hostfile> ark-tweet-nlp-0.3.2.jar =:/usr/local/greenplum-db/lib/postgresql/java/
```

2. Update the `CLASSPATH` for PL/Java so that it can find our libraries
```
[gpadmin@mdw ~]$ gpconfig -c pljava_classpath -v \'gp-ark-tweet-nlp.jar:ark-tweet-nlp-0.3.2.jar:examples.jar\'
```

3. To increase memory available to PL/Java run the following commands on the **Master Segment**
```
[gpadmin@mdw ~]$ gpconfig -c pljava_vmoptions -v \'-Xmx512M\' 
[gpadmin@mdw ~]$ gpstop -r
```

4. Ensure the options have taken effect 
```
[gpadmin@mdw ~]$ gpconfig --show pljava_vmoptions
Values on all segments are consistent
GUC          : pljava_vmoptions
Master  value: -Xmx512M
Segment value: -Xmx512M
[gpadmin@mdw ~]$ 
```

You are now set to run the part-of-speech tagger in SQL by defining a **User Defined Function** to invoke the PL/Java wrapper and a **User Defined Type**
to define the type of the returned result.

Usage
======

1. We'll first declare a **UDT** to define the type of the result return by our part-of-speech tagger
```SQL
-- Define a type to hold [tweet_id, token_index, token, tag] items
DROP TYPE IF EXISTS token_tag;
CREATE TYPE token_tag
AS
(
	indx int, 
	token text,
	tag text
);
```

2. We'll then declare a **UDF** to invoke the PL/Java wrapper
```SQL
DROP FUNCTION IF EXISTS posdemo.tag_pos(varchar);
CREATE FUNCTION posdemo.tag_pos(varchar)
	RETURNS SETOF token_tag
AS 
	'postagger.nlp.POSTagger.tagTweet'
IMMUTABLE LANGUAGE JAVAU;
```
Note the use of `javau` instead of just `java` in the **UDF**. This is because we are using the untrusted version of PL/Java as the part-of-speech tagger has to read a model file from within `gp-ark-tweet-nlp.jar` 
Without the untrusted language, we will encounter a `java.lang.SecurityException` when the code tries to read the model file embedded in `gp-ark-tweet-nlp.jar`.

3. Finally we can invoke the parts-of-speech tagger on a table containing a tweet column like so:

```SQL
vatsandb=# \d+ posdemo.training_data
                        Table "sentidemo.training_data"
   Column   |            Type             | Modifiers | Storage  | Description 
------------+-----------------------------+-----------+----------+-------------
 rating     | integer                     |           | plain    | 
 id         | bigint                      |           | plain    | 
 ts         | timestamp without time zone |           | plain    | 
 query      | text                        |           | extended | 
 poster     | text                        |           | extended | 
 tweet_body | text                        |           | extended | 
Has OIDs: no
Distributed by: (id)
```

Here are some sample rows from the table
```SQL
vatsandb=# select id, tweet_body from sentidemo.training_data limit 10;
```

```SQL
     id     |                                                                tweet_body                                                                 
------------+-------------------------------------------------------------------------------------------------------------------------------------------
 1467820906 | @localtweeps Wow, tons of replies from you, may have to unfollow so I can see my friends' tweets, you're scrolling the feed a lot. 
 1467862806 | @MySteezRadio I'm goin' to follow u, since u didn't  LOL  GO ANGELS!
 1467891880 | Argh! I was suuuper sleepy an hour ago, now I'm wide awake.  Hope I don't stay up all night. :-/
 1467896211 | michigan state you make me sad 
 1467911846 | @bananaface IM SORRY I GOT YOU SICK.  lol. going to bed too. NIGHT!
 1467962634 | Im in the mood for some chocolate. I want..... Miniature Reeses cups. Now 
 1467979881 | @redvinylgirl my mom has it. I wish you the best of luck 
 1467987712 | Change of plans, we ordered Macs instead. Time to hit the books! 
 1467996032 | Funny how the little things make me homesick, criminals breakn n2 a brownstone on LawNOrd CI made me misty 
 1468000208 | Man... taxes suck.  I'm horrified that i did something wrong on them.  TurboTax decided to keep around a lot of the stuff I turned off.  
(10 rows)

Time: 329.507 ms
```

Now we can perform parts-of-speech tagging of the tweets
```SQL
select id, 
       (t).indx, 
       (t).token, 
       (t).tag
from
(
    select id, 
           posdemo.tag_pos(tweet_body) as t
    from posdemo.training_data
) q
```

Which returns (limitng to 10 rows for sampel output)
```SQL
     id     | indx |  token   | tag 
------------+------+----------+-----
 1467810672 |    0 | is       | V
 1467810672 |    1 | upset    | A
 1467810672 |    2 | that     | P
 1467810672 |    3 | he       | O
 1467810672 |    4 | can't    | V
 1467810672 |    5 | update   | V
 1467810672 |    6 | his      | D
 1467810672 |    7 | Facebook | ^
 1467810672 |    8 | by       | P
 1467810672 |    9 | texting  | V
(10 rows)

```

Questions?
============

You can reach me at vatsan.cs@utexas.edu
