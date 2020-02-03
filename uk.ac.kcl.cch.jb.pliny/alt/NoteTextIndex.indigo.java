/*******************************************************************************
 * Copyright (c) 2007 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/

package uk.ac.kcl.cch.jb.pliny.lucene;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.Note;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.NoteQuery;
import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;

/**
 * This is a singleton class that manages the interface between Lucene and Pliny to support word searching
 * in Pliny notes. (version for Eclipse: Indigo)
 * 
 * @see uk.ac.kcl.cch.jb.pliny.model.NoteLucened
 * @see uk.ac.kcl.cch.jb.pliny.views.NoteSearchView
 * 
 * @author John Bradley
 *
 */

public class NoteTextIndex {
        
        private IndexWriter indexWriter = null;
        private IndexReader indexReader = null;
        private Searcher searcher = null;
        
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
                return indexWriter != null;
        }
        
        /**
         * builds the Lucene index, and provides support for the display of a
         * progress monitor to the user.
         * 
         * @param monitor the monitor to be displayed to the user while s/he waits.
         * @throws IOException
         */
        
        public void buildIndex(IProgressMonitor monitor) throws IOException{
                if(indexWriter != null)return;
                
                indexWriter = new IndexWriter(luceneDir,  new StandardAnalyzer(), true);
                indexNotes(monitor);
                //System.out.println("after buildIndex: count: "+indexWriter.docCount());
        }

        private void indexNotes(IProgressMonitor monitor) {
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
                        addToIndex(note.getALID(), title, content);
                        if(monitor != null)monitor.worked(1);
                        }
                } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
        /*
                Connection con = PlinyPlugin.getDefault().getConnection();
            Statement stmt;
            try {
                        stmt = con.createStatement();
                        String query = "select Resource.resourceKey, Resource.fullName, Note.content "+
                           "from Resource, Note where Resource.resourceKey=Note.resourceKey";
                ResultSet rs = stmt.executeQuery(query);
                while(rs.next()){
                        int key = rs.getInt(1);
                        String title = rs.getString(2);
                        if(title == null)title = "";
                        String content = rs.getString(3);
                        if(content == null)content = "";
                        addToIndex(key, title, content);
                        if(monitor != null)monitor.worked(1);
                }
                stmt.close();
                } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                } finally {
                   PlinyPlugin.getDefault().returnConnection(con);
                   if(monitor != null)monitor.done();
                }
                */
                if(monitor != null)monitor.done();
        }

        private void addToIndex(int key, String title, String content) {
                if(key <= 0)return;
                if(title == null)title = "";
                if(content == null)content = "";
                if((title+content).trim().length() == 0)return;
                
            Document doc = new Document();
            String keyString = Integer.toString(key);
            // see deprecated update list at http://lucene.apache.org/java/1_9_0/api/deprecated-list.html
        //doc.add(Field.Keyword("key",keyString));
            doc.add(new Field("key",keyString,Field.Store.YES,Field.Index.UN_TOKENIZED));
        //Field titleField = Field.Text("title",title);
        //Field titleField = Field.UnStored("title",title);
            Field titleField = new Field("title",title,Field.Store.NO, Field.Index.TOKENIZED);
              titleField.setBoost(1.5f);
        doc.add(titleField);
        //doc.add(Field.Text("content", content));
        //doc.add(Field.UnStored("content", content));
        doc.add(new Field("content", content,Field.Store.NO, Field.Index.TOKENIZED));
        //doc.add(Field.UnStored("all",title+" "+content));
        doc.add(new Field("all",title+" "+content,Field.Store.NO, Field.Index.TOKENIZED));
                try {
                        indexWriter.addDocument(doc);
                } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
        }
        
        /**
         * adds information about a new NoteLucened to the Lucene index.
         * 
         * @param myNote the new note to be added.
         */
        
        public void addNoteToIndex(NoteLucened myNote){
                if(indexWriter == null) return;
                addToIndex(myNote.getALID(), myNote.getName(), myNote.getContent());
                searcher = null;
                //System.out.println("after addNoteToIndex: count: "+indexWriter.docCount());
        }
        
        /**
         * removes information about a new NoteLucened from the Lucene index.
         * Used when the note is being removed from the system.
         * 
         * @param myNote the new note to be removed from the Lucene index.
         */
        
        public void removeNoteFromIndex(NoteLucened myNote){
                if(indexWriter == null) return;
                if(myNote.getALID() == 0)return;
                //int numbDeleted = 0;
                
                try {
                        indexWriter.close();
                        indexReader = IndexReader.open(luceneDir);
                        String keyString = Integer.toString(myNote.getALID());
                        //numbDeleted = indexReader.delete(new Term("key", keyString));
                        // see deprecated list at http://lucene.apache.org/java/1_9_0/api/deprecated-list.html
                        //indexReader.delete(new Term("key", keyString));
                        indexReader.deleteDocuments(new Term("key", keyString));
                        indexReader.close();
                        indexReader = null;
                        indexWriter = new IndexWriter(luceneDir,  new StandardAnalyzer(), false);
                } catch (IOException e) {
                        e.printStackTrace();
                        return;
                }
                //System.out.println("after removeNoteToIndex: count: "+indexWriter.docCount()+", numbDeleted: "+numbDeleted);
        searcher = null;
        }
        
        /**
         * updates the name or content of a NoteLucened in the Lucene index.
         * Used when the user has changed the note's content.
         * 
         * @param myNote note with name of content that has changed
         */
        
        public void updateNoteInIndex(NoteLucened myNote){
                if(indexWriter == null) return;
                if(myNote.getALID() == 0)return;
                removeNoteFromIndex(myNote);
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
                if(indexWriter == null)return null;
                if(searcher == null)
                        try {
                                searcher = new IndexSearcher(luceneDir);
                        } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                                return null;
                        }
                //Query query = QueryParser.parse(queryString, "content", new StandardAnalyzer());
                // see deprecated data at http://lucene.apache.org/java/1_9_0/api/deprecated-list.html
                //Query query = QueryParser.parse(queryString, "all", new StandardAnalyzer());
                QueryParser qp = new QueryParser("all", new StandardAnalyzer());
                Query query = qp.parse(queryString);
                Vector rslt = new Vector();
                try {
                        Hits hits = searcher.search(query);
                        for(int i = 0; i < hits.length(); i++){
                                Document doc = hits.doc(i);
                                String id = doc.get("key");
                                if(id != null){
                                        int key = Integer.parseInt(id);
                                        if(key != 0)rslt.add(NoteLucened.getNoteLucenedItem(key));
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