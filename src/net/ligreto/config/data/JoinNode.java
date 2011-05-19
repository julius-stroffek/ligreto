/**
 * 
 */
package net.ligreto.config.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Julius Stroffek
 *
 */
public class JoinNode {
	protected List<SqlNode> sqlQueries = new ArrayList<SqlNode>();
	
	public void addSql(SqlNode sql) {
		sqlQueries.add(sql);
	}
}
