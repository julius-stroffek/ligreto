package net.ligreto.executor;

import net.ligreto.ResultStatus;
import net.ligreto.exceptions.LigretoException;

public abstract class Executor {
	
	/**
	 * Execute the operation.
	 * 
	 * @return The number of rows returned.
	 * @throws LigretoException
	 */
	public abstract ResultStatus execute() throws LigretoException;
}
