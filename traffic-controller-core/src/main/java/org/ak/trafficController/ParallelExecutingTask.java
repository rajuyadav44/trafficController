package org.ak.trafficController;

import org.ak.trafficController.pool.ObjectPoolManager;


public class ParallelExecutingTask<T> extends ParallelTask<T> {

	static ParallelExecutingTask getFromPool(int unique, TaskType taskType, RunnableToBeExecuted... runnables) {
		ParallelExecutingTask et = ObjectPoolManager.getInstance().getFromPool(ParallelExecutingTask.class, ()->new ParallelExecutingTask(unique, taskType, runnables));
		et.tasks.clear();
		et.taskType = taskType;
		et.uniqueNumber = unique;
		if (taskType == TaskType.NORMAL) et.addRunnables(runnables);
		if (taskType == TaskType.SLOW) et.addSlowRunnables(runnables);
		et.startingTask = et;
		return et;
	}
	
	public ParallelExecutingTask(int unique, TaskType taskType, RunnableToBeExecuted... runnables) {
		super(unique, taskType);
		addRunnables(runnables);
	}
	
	public ParallelExecutingTask and(RunnableToBeExecuted... runnables) {
		addRunnables(runnables);
		return this;
	}
}
