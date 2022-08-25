import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

class StartCommand: CustomCommand(COMMAND_IDENTIFIER, COMMAND_DESCRIPTION){

	companion object {
		val COMMAND_IDENTIFIER = "start";
		val COMMAND_DESCRIPTION = "start.description"
	}

	override fun execute(absSender: AbsSender, message: Message, arguments: Array<String>){
		try {
			val chatData = DatabaseManager.getChatData(message.chat);
			if(chatData.isGroup){
				TimerTasker.setTaskTime("registration:${message.chat.id}", 0);
			} else {
				absSender.execute(SendMessages.get(ESendMessage.START, message));
			}
		} catch (e: Exception) {
			e.printStackTrace();
		}
	}
}
