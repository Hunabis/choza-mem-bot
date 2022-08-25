abstract class CustomTimerTask(var taskName: String, var seconds: Long){

	var stopped = false;

	fun tick() {
		if (this.seconds > 0){
			try {
				this.onTick();
			} catch(e: Exception){
				e.printStackTrace();
			}

			this.seconds--;
		}
		else this.finish();
	}

	fun finish(){
		if(!this.stopped) {
			this.stopped = true;
			try {
				this.onFinish();
			} catch(e: Exception){
				e.printStackTrace();
			}
		}
	}

	abstract fun onTick();
    abstract fun onFinish();

}
