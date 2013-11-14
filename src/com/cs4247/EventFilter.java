package com.cs4247;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class EventFilter {
	private Context context;
	
	private ArrayList<String> messages;

	public EventFilter (Context context){
		this.context = context;
		messages = new ArrayList<String>();
	}

	public static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
	public static final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI, "inbox");

	public void extractAndIndexSMS(){
		boolean unreadOnly = false;
		String SMS_READ_COLUMN = "read";
		String WHERE_CONDITION = unreadOnly ? SMS_READ_COLUMN + " = 0" : null;
		String SORT_ORDER = "date DESC";
		int count = 0;

		//Log.v(WHERE_CONDITION);

		//         if (ignoreThreadId > 0) {
		//                 WHERE_CONDITION += " AND thread_id != " + ignoreThreadId;
		//         }

		Cursor cursor = context.getContentResolver().query(
				SMS_INBOX_CONTENT_URI,
				new String[] { "_id", "thread_id", "address", "person", "date", "body" },
				WHERE_CONDITION,
				null,
				SORT_ORDER);

		if (cursor != null) {
			try {
				count = cursor.getCount();
				while(cursor.moveToNext()){
//					cursor.moveToFirst();

//					long messageId = cursor.getLong(0);
//					long threadId = cursor.getLong(1);
//					String address = cursor.getString(2);
//					long contactId = cursor.getLong(3);
//					String contactId_string = String.valueOf(contactId);
//					long timestamp = cursor.getLong(4);

					String body = cursor.getString(5);
					messages.add(body);
//					System.out.println(body);

					if (!unreadOnly) {
						count = 0;
					}
				}
			} finally {
				cursor.close();
			}
		}    
	
		indexSMS();

	}

	private void indexSMS(){
		System.out.println("messages size = " + messages.size());
		File file = context.getFilesDir();
		Directory dir;
		IndexWriter writer;
		try {
			
			dir = FSDirectory.open(file);
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
		    IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_30, analyzer);
		    iwc.setOpenMode(OpenMode.CREATE);
		    writer = new IndexWriter(dir, iwc);
		    
			Document doc = new Document();
			
			String concatAll = "";
			for(String currMessage : messages){
				concatAll = concatAll.concat(currMessage);
			}
			doc.add(new Field("contents", concatAll, Field.Store.YES, Field.Index.ANALYZED));
			
			if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
	            // New index, so we just add the document (no old document can be there):
	            System.out.println("adding " + file);
	            writer.addDocument(doc);
	        } else {
	            // Existing index (an old copy of this document may have been indexed) so 
	            // we use updateDocument instead to replace the old one matching the exact 
	            // path, if present:
	            System.out.println("updating " + file);
	            writer.updateDocument(new Term("path", file.getPath()), doc);
	        }
			
			writer.commit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public float scoreQuery(String searchString){
		try {
			String field = "contents";
			
			Directory dir = FSDirectory.open(context.getFilesDir());
			
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
			org.apache.lucene.queryParser.QueryParser parser = new org.apache.lucene.queryParser.QueryParser(Version.LUCENE_30, field, analyzer);
			
			IndexSearcher searcher = new IndexSearcher(dir);
			Query query = parser.parse(searchString);
		    //System.out.println("Searching for: " + query.toString(field));
		    
		    TopDocs results = searcher.search(query, 5);
		    ScoreDoc[] hits = results.scoreDocs;
		    
		    //int numTotalHits = results.totalHits;
		    //System.out.println(numTotalHits + " total matching documents");
		    
		    searcher.close();
		    
		    if(hits.length > 0){
		    	//System.out.println("score = " + hits[0].score);
		    	return hits[0].score;
		    }
		    else{
		    	//System.out.println("hits = 0");
		    	return 0.0f;
		    }
		} catch (Exception e){
			// TODO
			e.printStackTrace();
		}
		
		return 0.0f;
	}
	
	public float scoreEvent(Event event){
		String desc = event.getDescription();
		
		String [] words = desc.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
		float totalScore = 0;
		for(int i = 0; i < words.length; i++){
			totalScore += scoreQuery(words[i]);
		}
		
		return totalScore / words.length;
	}
}
