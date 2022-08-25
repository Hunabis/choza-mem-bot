import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

import kotlin.math.max;
import kotlin.math.min;

class ExtendCommand: CustomCommand(COMMAND_IDENTIFIER, COMMAND_DESCRIPTION){

	companion object {
		val COMMAND_IDENTIFIER = "extend";
		val COMMAND_DESCRIPTION = "extend.description";
	}

	override fun execute(absSender: AbsSender, message: Message, arguments: Array<String>){
		try {
			val task = TimerTasker.getTask("registration:${message.chat.id}");
			if(task != null){
				val time: Long = try { max(min(arguments.get(0).toLong(), 14400L - task.seconds), 1L) } catch(e: Exception) { 30L };
				val newTime = TimerTasker.addTaskTime("registration:${message.chat.id}", time);
				val mes: Message? = absSender.execute(SendMessages.get(ESendMessage.EXTEND, message, arrayOf(time.toString(), newTime.toString())));
				if(mes != null){
					val task = object: CustomTimerTask("extend:${message.chat.id}:${mes.messageId}", 10){
						override fun onTick(){}
						override fun onFinish(){
							absSender.execute(DeleteMessage.builder()
								.chatId(mes.chat.id)
								.messageId(mes.messageId)
							.build());
						}
					};

					TimerTasker.addTask(task);
				}
			}
		} catch(e: Exception){
			e.printStackTrace();
		}
	}
}

