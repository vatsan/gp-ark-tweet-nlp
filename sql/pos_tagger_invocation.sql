------------------------------------------------------------------------------------------------------------------------------
--------- Invoke the gp-ark-tweet-nlp part-of-speech tagger for Twitter as a Greenplum/Postgres User Defined Function     ----
---------                                       Srivatsan Ramanujam<vatsan.cs@utexas.edu>                                -----
------------------------------------------------------------------------------------------------------------------------------


-------------------------------------------------------------------------------------------------------------------------------------
--                                      Pre-Requisites
-------------------------------------------------------------------------------------------------------------------------------------
		/*
		1) Ensure PL/Java is installed and it works:
		
		        -- Create Language
				create language pljava;
				
				-- Define function mapping
				drop function if exists getsysprop(varchar);
				create function getsysprop(varchar) 
				   returns varchar
				as 'java.lang.System.getProperty'
				language pljava;
				
				--Invoke function
				select getsysprop('user.home');
		
		2) Ensure JAVA_HOME is set on all hosts 
				As root run the following: 
				a) gpssh into all hosts
				   root$ gpssh -f <hostfile> ; 
				b) In the gpssh prompt, run the following:
				   root$ echo "export JAVA_HOME=/usr/java/jdk1.6.0_24" >> /home/gpadmin/.bashrc)
		
		3) Prepare the Greenplum/Postgres Environment to talk to our PL/Java wrapper
		
		        --a) Copy the Libraries
				gpscp -f <hostfile> gp-ark-tweet-nlp.jar =:/usr/local/greenplum-db/lib/postgresql/java/
				gpscp -f <hostfile> ark-tweet-nlp-0.3.2.jar =:/usr/local/greenplum-db/lib/postgresql/java/
				
				--b) Update the CLASSPATH for PL/Java so that it can find our libraries
				gpconfig -c pljava_classpath -v \'gp-ark-tweet-nlp.jar:ark-tweet-nlp-0.3.2.jar:examples.jar\'
				
				--c) Increase memory available to PL/Java
				Run the following commands on the Master Segment
					1) gpconfig -c pljava_vmoptions -v \'-Xmx512M\' 
					2) gpstop -r 
					3) Ensure the options have taken effect : 
					   gpconfig --show pljava_vmoptions
						
						Values on all segments are consistent
						GUC          : pljava_vmoptions
						Master  value: -Xmx512M
						Segment value: -Xmx512M
						
		*/	


-------------------------------------------------------------------------------------------------------------------------------------
-------                                    Part-of-speech Tagging Demo                                                         ------
-------------------------------------------------------------------------------------------------------------------------------------
 
	--1) Define UDFs and UDTs to invoke the gp-ark-tweet-nlp tokenizer and part-of-speech tagger
	
			-- Define a type to hold [tweet_id, token_index, token, tag] items
			drop type if exists token_tag;
			create type token_tag
			as
			(
				indx int, 
				token text,
				tag text
			);
			
			-- Define function to invoke Part-of-speech tagger and return the result as a set of composite type we defined above
			-- Note the use of 'javau' instead of 'java'. This is coz we are using the untrusted version (the POSTagger has to read a model file)
			-- without the untrusted language, we will get a java.lang.SecurityException when the code tries to read a file in its own jar).
			drop function if exists posdemo.tag_pos(varchar);
			create function posdemo.tag_pos(varchar)
			returns setof token_tag
			as 
				'postagger.nlp.POSTagger.tagTweet'
			immutable language javau;
	
	--2) Part-of-speech Tagging Demo - SQL
	
			-- Tag tweets
			drop table if exists posdemo.training_data_pos_tagged;
			create table posdemo.training_data_pos_tagged 
			as
			(
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
			) distributed by (id);
		