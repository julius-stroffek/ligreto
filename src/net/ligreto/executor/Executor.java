package net.ligreto.executor;

import net.ligreto.exceptions.LigretoException;

public abstract class Executor {
	public abstract void execute() throws LigretoException;
}
