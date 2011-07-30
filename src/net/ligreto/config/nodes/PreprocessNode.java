package net.ligreto.config.nodes;

import java.util.ArrayList;
import java.util.List;

public class PreprocessNode extends Node {

	protected List<SqlNode> sqlQueries = new ArrayList<SqlNode>();

	public PreprocessNode(LigretoNode aLigretoNode) {
		super(aLigretoNode);
	}

	public Iterable<SqlNode> sqlQueries() {
		return sqlQueries;
	}

	public void addSql(SqlNode sql) {
		sqlQueries.add(sql);
	}

}
