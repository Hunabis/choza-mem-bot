import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

class HelpCommand: CustomCommand(COMMAND_IDENTIFIER, COMMAND_DESCRIPTION){

	companion object {
		val COMMAND_IDENTIFIER = "help";
		val COMMAND_DESCRIPTION = "help.description";
	}

	override fun execute(absSender: AbsSender, message: Message, arguments: Array<String>){
		try {
			var chatData = DatabaseManager.getChatData(message.chat);
			if(chatData.isGroup){
				var chatId = message.from.id;
				chatData = DatabaseManager.getChatData(absSender.execute(GetChat(chatId.toString())));
				absSender.execute(SendMessage.builder()
					.chatId(chatId)
					.text(TranslationHelper.translate(chatData.languageCode, "start"))
					.parseMode("MarkdownV2")
					.replyMarkup(InlineKeyboardMarkups.get(EInlineKeyboardMarkup.START, chatData.languageCode))
				.build());
			}
		} catch(e: Exception){
			e.printStackTrace();
		}
	}
}
