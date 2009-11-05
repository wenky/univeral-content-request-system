import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mueller.booklibrary.Techbook;

public class hibernatetest {
	public static void main (String[] args) throws Exception
	{
		try {
	        Session s = BookLibraryHibernateSessionFactory.currentSession();
	        
	        Transaction t = s.beginTransaction();
	        
	        Techbook book = new Techbook();
	        book.setKeypath("nowhere");
	        book.setCategories("a|bunch|of|categories|for|this|book");
	        book.setLink("linking nowhere");
	        book.setTitle("gulliver's travels");
	        
	        s.save(book);
	        
	        t.commit();
	        
	        BookLibraryHibernateSessionFactory.closeSession();
		} catch (Exception e) {
			int a=1;
			a= a+1;
			throw e;
		}
        
        System.exit(0);    
	}

}
