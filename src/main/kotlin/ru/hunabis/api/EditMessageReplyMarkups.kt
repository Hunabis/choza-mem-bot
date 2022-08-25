import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

object EditMessageReplyMarkups {

	fun get(type: EEditMessageReplyMarkup, query: CallbackQuery, arguments: Array<String>): EditMessageReplyMarkup {
		val chatData = DatabaseManager.getChatData(query.message.chat);
		when(type){
			EEditMessageReplyMarkup.SELECT_LANGUAGE -> {
				return EditMessageReplyMarkup.builder()
					.chatId(query.message.chat.id)
					.messageId(query.message.messageId)
					.replyMarkup(InlineKeyboardMarkups.get(EInlineKeyboardMarkup.SELECT_LANGUAGE, chatData.languageCode))
				.build();
			}
			EEditMessageReplyMarkup.UPDATE_LANGUAGE -> {
				val newLanguageCode = arguments.get(0);
				return EditMessageReplyMarkup.builder()
					.chatId(query.message.chat.id)
					.messageId(query.message.messageId)
					.replyMarkup(InlineKeyboardMarkups.get(if(chatData.isGroup) EInlineKeyboardMarkup.INIT else EInlineKeyboardMarkup.START, newLanguageCode))
				.build()
			}
			EEditMessageReplyMarkup.GAME_JOIN -> {
				return EditMessageReplyMarkup.builder()
					.chatId(query.message.chat.id)
					.messageId(query.message.messageId)
					.replyMarkup(InlineKeyboardMarkups.get(EInlineKeyboardMarkup.GAME, chatData.languageCode))
				.build();
			}
			EEditMessageReplyMarkup.GAME_VOTE -> {
				return EditMessageReplyMarkup.builder()
					.chatId(query.message.chat.id)
					.messageId(query.message.messageId)
					.replyMarkup(InlineKeyboardMarkups.get(EInlineKeyboardMarkup.GAME_VOTE, chatData.languageCode, arrayOf(query.message.chat.id.toString())))
				.build();
			}
		}

		return EditMessageReplyMarkup.builder()
			.chatId(query.message.chat.id)
			.messageId(query.message.messageId)
			.replyMarkup(InlineKeyboardMarkups.ERROR)
		.build();
	}

	fun get(type: EEditMessageReplyMarkup, message: Message, arguements: Array<String>): EditMessageReplyMarkup {
		val chatData = DatabaseManager.getChatData(message.chat);
		when(type){
			EEditMessageReplyMarkup.GAME_JOIN -> {
				return EditMessageReplyMarkup.builder()
					.chatId(message.chat.id)
					.messageId(message.messageId)
					.replyMarkup(InlineKeyboardMarkups.get(EInlineKeyboardMarkup.GAME, chatData.languageCode))
				.build();
			}
		}

		return EditMessageReplyMarkup.builder()
			.chatId(message.chat.id)
			.messageId(message.messageId)
			.replyMarkup(InlineKeyboardMarkups.ERROR)
		.build();
	}

	fun get(type: EEditMessageReplyMarkup, message: Message): EditMessageReplyMarkup = get(type, message, arrayOf<String>());
	fun get(type: EEditMessageReplyMarkup, query: CallbackQuery): EditMessageReplyMarkup = get(type, query, arrayOf<String>());
}
