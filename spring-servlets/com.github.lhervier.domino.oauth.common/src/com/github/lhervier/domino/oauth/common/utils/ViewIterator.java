package com.github.lhervier.domino.oauth.common.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import lotus.domino.Base;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewNavigator;

/**
 * Un itérateur sur une vue ou sur une catégorie d'une vue.
 * @author Lionel HERVIER
 */
public class ViewIterator implements Iterable<ViewEntry>, Base {
	
	/**
	 * Pour recycler un objet, même s'il est null
	 * et sans lever d'exception
	 * @param o
	 */
	private static void recycleQuietly(Base o) {
		if( o == null )
			return;
		try {
			o.recycle();
		} catch(NotesException e) {
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * L'itérateur interne
	 * @author Lionel HERVIER
	 */
	private static class InternalIterator implements Iterator<ViewEntry>, Base {
	
		/**
		 * La vue
		 */
		private View v;
		
		/**
		 * Le navigateur
		 */
		private ViewNavigator nav;
		
		/**
		 * La prochaine entrée distribuée
		 */
		private ViewEntry nextEntry;
		
		/**
		 * L'entrée distribuée dernièrement
		 */
		private ViewEntry currEntry;
		
		/**
		 * Constructeur
		 * @param nav le navigator
		 * @throws NotesException en cas de pb
		 */
		private InternalIterator(ViewNavigator nav) throws NotesException {
			this.v = nav.getParentView();
			this.v.setAutoUpdate(false);
			this.nav = nav;
			
			this.nextEntry = this.nav.getFirst();
			this.currEntry = null;
		}
		
		/**
		 * @see lotus.domino.Base#recycle()
		 */
		public void recycle() throws NotesException {
			recycleQuietly(this.currEntry);
			recycleQuietly(this.nextEntry);
			recycleQuietly(this.nav);
			recycleQuietly(this.v);
		}
	
		/**
		 * @see lotus.domino.Base#recycle(java.util.Vector)
		 */
		@SuppressWarnings("unchecked")
		public void recycle(Vector arg0) throws NotesException {
			throw new UnsupportedOperationException();
		}
	
		/**
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return this.nextEntry != null;
		}
	
		/**
		 * @see java.util.Iterator#next()
		 */
		public ViewEntry next() {
			try {
				recycleQuietly(this.currEntry);
				this.currEntry = this.nextEntry;
				this.currEntry.setPreferJavaDates(true);
				this.nextEntry = this.nav.getNext();
				return this.currEntry;
			} catch(NotesException e) {
				throw new RuntimeException(e);
			}
		}
	
		/**
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException("Not implemented");
		}
	}

	/**
	 * La databse courante
	 */
	private Database database;
	
	/**
	 * Le nom de la vue sur laquelle on itère
	 */
	private String viewName;
	
	/**
	 * La colonne sur laquelle trier
	 */
	private String sortColumn;
	
	/**
	 * L'éventuelle catégorie
	 */
	private String category;
	
	/**
	 * La liste des itérateurs générés
	 */
	private List<InternalIterator> iterators;
	
	/**
	 * Pour construire un ViewIterator
	 */
	public static final ViewIterator create() {
		return new ViewIterator();
	}
	
	/**
	 * Constructeur vide
	 */
	private ViewIterator() {
		this.iterators = new ArrayList<InternalIterator>();
	}
	
	/**
	 * La base sur laquelle on cherche
	 * @param database la base
	 */
	public ViewIterator onDatabase(Database database) {
		this.database = database;
		return this;
	}
	
	/**
	 * La vue que laquelle on cherche
	 * @param viewName le nom de la vue
	 */
	public ViewIterator onView(String viewName) {
		this.viewName = viewName;
		return this;
	}
	
	/**
	 * La catégorie sur laquelle on cherche
	 * @param category la catégorie
	 */
	public ViewIterator onCategory(String category) {
		this.category = category;
		return this;
	}
	
	/**
	 * @param sortColumn the sortcolumn to set
	 */
	public ViewIterator sortOnColumn(String sortColumn) {
		this.sortColumn = sortColumn;
		return this;
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<ViewEntry> iterator() {
		try {
			View v = this.database.getView(this.viewName);
			v.setAutoUpdate(false);
			if( sortColumn != null )
				v.resortView(this.sortColumn);
			ViewNavigator nav;
			if( this.category == null )
				nav = v.createViewNav();
			else
				nav = v.createViewNavFromCategory(this.category);
			InternalIterator it = new InternalIterator(nav);
			this.iterators.add(it);
			return it;
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see lotus.domino.Base#recycle()
	 */
	public void recycle() throws NotesException {
		for( InternalIterator it : this.iterators )
			recycleQuietly(it);
	}

	/**
	 * @see lotus.domino.Base#recycle(java.util.Vector)
	 */
	@SuppressWarnings("unchecked")
	public void recycle(Vector arg0) throws NotesException {
		throw new UnsupportedOperationException("Not implemented");
	}
}
