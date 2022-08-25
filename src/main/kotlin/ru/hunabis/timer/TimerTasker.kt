import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

object TimerTasker {

	val executorService = Executors.newScheduledThreadPool(1);
	val tasks = mutableMapOf<String, CustomTimerTask>();

	fun addTask(task: CustomTimerTask){
		tasks.put(task.taskName, task);
	}

	fun removeTask(name: String): Boolean {
		return tasks.remove(name) != null;
	}

	fun getTask(name: String): CustomTimerTask? {
		return tasks.get(name);
	}

	fun hasTask(name: String): Boolean = tasks.get(name) != null;

	fun setTaskTime(name: String, newTime: Long){
		this.getTask(name)?.seconds = newTime;
	}

	fun addTaskTime(name: String, addTime: Long): Long {
		val task = this.getTask(name);
		if(task != null){
			task.seconds += addTime;
			return task.seconds;
		}

		return 0L;
	}

	fun setTimeout(task: () -> Unit, millis: Long){
		executorService.schedule(task, millis, TimeUnit.MILLISECONDS)
	}

	init {
		_init();
	}

	private fun _init(){
		var taskWrapper: () -> Unit = {
			try {
				for(task in tasks.values)
					if(task.seconds + 1 > 0) task.tick();
					else this.removeTask(task.taskName);
				this._init();
			} catch (e: Exception) {
				e.printStackTrace();
			}
		};

		executorService.schedule(taskWrapper, 1, TimeUnit.SECONDS);
	}

}
