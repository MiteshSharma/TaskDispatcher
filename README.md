# TaskDispatcher
Task dispatcher is used to dispatch any task which needs to run in background thread. We can simply send tasks to task dispatcher and then it keeps executing them one by one using executors.

Any task which needs to be run by TaskDispatcher must extend ITask. We are also considering priorities of tasks and running them based on their priority.

