/*******************************************************************************
 * Copyright (c) 2007, 2013 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/

package uk.ac.kcl.cch.jb.pliny.lucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
//import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
//import org.apache.lucene.queryParser.ParseException;
//import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
//import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
//import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.figures.MapContentFigure;
import uk.ac.kcl.cch.jb.pliny.figures.TextContentFigure;
import uk.ac.kcl.cch.jb.pliny.figures.TopPanel;
import uk.ac.kcl.cch.jb.pliny.model.Note;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.NoteQuery;
import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;

/**
 * This is a singleton class that manages the interface between Lucene and Pliny to support word searching
 * in Pliny notes. It appears to the author of this code that lucene developers are constantly reinventing
 * the API, requiring substantial revision of this code each time the lucene bundle is updated in Eclipse.  
 * The code here is constructed for Lucene version 8.0.0, which ships with Eclipse version 2019-09 R (4.13.0).
 * 
 * <p>Much of this code is inspired by online examples, in particular
 * https://lucene.apache.org/core/8_0_0/core/overview-summary.html#overview.description
 * 
 * @see uk.ac.kcl.cch.jb.pliny.model.NoteLucened
 * @see uk.ac.kcl.cch.jb.pliny.views.NoteSearchView
 * 
 * @author John Bradley
 *
 */

public class NoteTextIndex {
	
	IndexWriter indexWriter = null;
	private Directory fsDir = null;
	private IndexWriterConfig writerConfig = null;
	private IndexReader indexReader = null;
	//private Searcher searcher = null;
	private boolean indexingDone = false;
	
	private static NoteTextIndex instance = null;
	private static String luceneDir = PlinyPlugin.getDefault().getStateLocationUrl("lucene");

	private NoteTextIndex() {
		super();
		// TODO Auto-generated constructor stub
	}
	/**
	 * returns the singleton instance of this class.
	 * 
	 * @return the instance of NoteTextIndex
	 */
	
	public static NoteTextIndex getInstance(){
		if(instance == null)instance = new NoteTextIndex();
		return instance;
	}
	
	/**
	 * returns true of the index has been set up.
	 * 
	 * @return <code>true</code> if index has been set up.
	 */
	
	public boolean isIndexOn(){
		return indexingDone;
	}
	
	private Directory getFSDirectory() {
		try {
			if(fsDir == null)
				fsDir = FSDirectory.open(Paths.get(luceneDir));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fsDir;
	}
	
	private IndexWriter getIndexWriter(){
		if(indexWriter == null){
			try {
				Directory myDir = getFSDirectory();
				// writerConfig = new IndexWriterConfig(Version.LUCENE_CURRENT,new ClassicAnalyzer(Version.LUCENE_CURRENT));
				writerConfig = new IndexWriterConfig(new StandardAnalyzer());
				indexWriter = new IndexWriter(myDir,  writerConfig);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return indexWriter;
	}
	
	/**
	 * builds the Lucene index, and provides support for the display of a
	 * progress monitor to the user.
	 * 
	 * @param monitor the monitor to be displayed to the user while s/he waits.
	 * @throws IOException
	 */
	
	public void buildIndex(IProgressMonitor monitor) throws IOException{
		if(indexingDone)return;
		
		//indexWriter = new IndexWriter(luceneDir,  new StandardAnalyzer(), true);
		// from http://alias-i.com/lingpipe-book/lucene-3-tutorial-0.5.pdf
		
		IndexWriter indexWriter = getIndexWriter();
		if(indexWriter == null)return;
		indexNotes(indexWriter, monitor);
		//System.out.println("after buildIndex: count: "+indexWriter.docCount());
		//indexWriter.close();
		indexWriter.commit();
		indexingDone = true;
	}

	private void indexNotes(IndexWriter indexWriter, IProgressMonitor monitor) {
		NoteQuery q = new NoteQuery();
		int numbNotes = q.executeCount();
		if(numbNotes <= 0)return;
		if(monitor != null)
		   monitor.beginTask("Building Index", numbNotes);

        ResultSet rs = Rdb2javaPlugin.getDataServer().prepareFullValueResultSet(q);
        try {
			while(rs.next()){
				Note note = new Note(true);
				note.loadFromResultSet(rs);
	        	String title = note.getName();
	        	if(title == null)title = "";
	        	String content = note.getContent();
	        	if(content == null)content = "";
	        	addToIndex(indexWriter, note.getALID(), title, content);
	        	if(monitor != null)monitor.worked(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if(monitor != null)monitor.done();
	}

	private void addToIndex(IndexWriter indexWriter, int key, String title, String content) {
		if(key <= 0)return;
		if(title == null)title = "";
		if(content == null)content = "";
		if((title+content).trim().length() == 0)return;
		
	    Document doc = new Document();
	    String keyString = Integer.toString(key);
	    doc.add(new Field("key",keyString,StringField.TYPE_STORED));
      	Field titleField = new Field("title",title,TextField.TYPE_NOT_STORED);
        //    titleField.setBoost(1.5f);
      	// https://community.neo4j.com/t/how-to-boost-lucene-queries-with-field-values/9975
        doc.add(titleField);
        doc.add(new Field("content", content,TextField.TYPE_NOT_STORED));
        doc.add(new Field("all",title+" "+content,TextField.TYPE_NOT_STORED));
		try {
			indexWriter.addDocument(doc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//private IndexReader getReader() {
	//	try {
	//		if(indexReader == null){
	//			indexReader = new IndexReader();
	//		}
	//		
	//	} catch (IOException e) {
	//		e.printStackTrace();
	//	}
	//}
	
	private IndexSearcher getSearcher(){
		try {
			DirectoryReader ireader = DirectoryReader.open(getFSDirectory());
			return new IndexSearcher(ireader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	private void finishWriter(){
		if(indexWriter == null)return;
		// indexWriter.optimize();
		try {
			indexWriter.close();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		indexWriter = null;
	}
	
	/**
	 * adds information about a new NoteLucened to the Lucene index.
	 * 
	 * @param myNote the new note to be added.
	 */
	
	public void addNoteToIndex(NoteLucened myNote){
		if(!indexingDone)return;
		IndexWriter indexWriter = getIndexWriter();
		if(indexWriter == null)return;
		addToIndex(indexWriter, myNote.getALID(), myNote.getName(), myNote.getContent());
		try {
			indexWriter.commit();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("after addNoteToIndex: count: "+indexWriter.docCount());
	}
	
	/**
	 * removes information about a new NoteLucened from the Lucene index.
	 * Used when the note is being removed from the system.
	 * 
	 * @param myNote the new note to be removed from the Lucene index.
	 */
	
	public void removeNoteFromIndex(NoteLucened myNote){
		doRealremoveNoteFromIndex(myNote);
		if(indexWriter != null)
			try {
				indexWriter.commit();
			} catch (CorruptIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	// https://stackoverflow.com/questions/39292911/how-to-delete-or-update-the-documents-in-apache-lucene
	
	private void doRealremoveNoteFromIndex(NoteLucened myNote){
		if(!indexingDone)return;
		if(myNote.getALID() == 0)return;
		//int numbDeleted = 0;
		
		try {
			IndexWriter writer = getIndexWriter();
			String keyString = Integer.toString(myNote.getALID());
			indexWriter.deleteDocuments(new Term("key", keyString));
			indexWriter.commit();
			finishWriter();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * updates the name or content of a NoteLucened in the Lucene index.
	 * Used when the user has changed the note's content.
	 * 
	 * @param myNote note with name or content that has changed
	 */
	
	public void updateNoteInIndex(NoteLucened myNote){
		if(!indexingDone)return;
		if(myNote.getALID() == 0)return;
		doRealremoveNoteFromIndex(myNote);
		addNoteToIndex(myNote);
	}
	
	/**
	 * passes the query string to Lucene, takes results, and locates selected
	 * NoteLuucened which are then assembled into a Vector and returned.
	 * 
	 * @param queryString the Lucene query string.
	 * @return Vector containing NoteLucene items that Lucene selected.
	 * @throws ParseException
	 */
	
	public Vector search(String queryString) throws ParseException{
		if(!indexingDone)return null;
		IndexSearcher searcher = getSearcher();
		if(searcher == null)return null;
		//Query query = QueryParser.parse(queryString, "content", new StandardAnalyzer());
		// see deprecated data at http://lucene.apache.org/java/1_9_0/api/deprecated-list.html
		//Query query = QueryParser.parse(queryString, "all", new StandardAnalyzer());
		//QueryParser qp = new QueryParser("all", new StandardAnalyzer());
		// code from http://alias-i.com/lingpipe-book/lucene-3-tutorial-0.5.pdf
		Analyzer stdAn = // new ClassicAnalyzer(Version.LUCENE_CURRENT);
		  // = new StandardAnalyzer(Version.LUCENE_CURRENT);
		  new StandardAnalyzer();
		//QueryParser qp = new QueryParser(Version.LUCENE_CURRENT,"all",stdAn);
		QueryParser qp = new QueryParser("all",stdAn);
		Query query = qp.parse(queryString);
		Vector rslt = new Vector();
		try {
			//Hits hits = searcher.search(query);
			TopDocs hits = searcher.search(query, 1000);
			ScoreDoc[] scoreDocs = hits.scoreDocs;
			for(int i = 0; i < scoreDocs.length; i++){
				//Document doc = hits.doc(i);
				ScoreDoc sd = scoreDocs[i];
				float score = sd.score;
				int docId = sd.doc;
				Document doc = searcher.doc(docId);
				String id = doc.get("key");
				if(id != null){
					int key = Integer.parseInt(id);
					// System.out.println("Item id:"+id+", key="+key);
					if(key != 0){
						NoteLucened theNote = NoteLucened.getNoteLucenedItem(key);
						if(theNote != null  && !rslt.contains(theNote))
						   rslt.add(theNote);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
        return rslt;
	}
}
