package postagger.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.postgresql.pljava.ResultSetProvider;

/**
 * Class to handle the return of complex type
 * @author Srivatsan Ramanujam<vatsan.cs@utexas.edu>
 *
 */
public class TaggedResultProvider implements ResultSetProvider{
	private List<TaggedResult> taggedItems;
	private final Iterator<TaggedResult> itemIter;
	
	public TaggedResultProvider() {
		taggedItems = new ArrayList<TaggedResult>();
		itemIter = taggedItems.iterator();
	}
	
	public TaggedResultProvider(List<TaggedResult> taggedResult) {
		taggedItems = taggedResult;
		itemIter = taggedItems.iterator();		
	}
	@Override
	public boolean assignRowValues(ResultSet receiver, int currRow)
			throws SQLException {
		
	     if (!itemIter.hasNext()) {
             return false;
         }
	     
	     TaggedResult item = itemIter.next();
	     //The strings "indx", "token" and "tag" are the column names of the composite type that will be defined in postgresql
	     receiver.updateInt("indx", item.getIndex());
	     receiver.updateString("token", item.getToken());
	     receiver.updateString("tag", item.getTag());
	     return true;
	}

	@Override
	public void close() throws SQLException {
		
	}	
}
