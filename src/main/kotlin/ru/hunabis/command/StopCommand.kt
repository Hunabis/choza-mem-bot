import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

class StopCommand: CustomCommand(COMMAND_IDENTIFIER, COMMAND_DESCRIPTION){

	companion object {
		val COMMAND_IDENTIFIER = "stop"
		val COMMAND_DESCRIPTION = "stop.description"
	}

	override fun execute(absSender: AbsSender, message: Message, arguments: Array<String>){
		val chatData = DatabaseManager.getChatData(message.chat);
		if(chatData.registrationMessage != null && Game.get(message.chat.id)?.getState() == EGameState.REGISTRATION){
			if(DatabaseManager.clearRegisteredUsers(message.chat)){
				absSender.execute(EditMessageTexts.get(EEditMessageText.STOP, message, arguments));
				DatabaseManager.clearRegistrationMessage(message.chat);
				Game.get(message.chat.id)?.deleteTimeMessages(absSender);
				TimerTasker.removeTask("registration:${message.chat.id}");
				Game.remove(message.chat.id);
			}
		}
	}
}
