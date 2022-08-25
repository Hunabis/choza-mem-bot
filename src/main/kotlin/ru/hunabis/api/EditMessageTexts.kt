import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.Chat;

object EditMessageTexts {

	fun get(type: EEditMessageText, query: CallbackQuery, arguments: Array<String>): EditMessageText {
		val chatData = DatabaseManager.getChatData(query.message.chat);
		when(type){
			EEditMessageText.SELECT_LANGUAGE -> {
				return EditMessageText.builder()
					.chatId(query.message.chat.id)
					.messageId(query.message.messageId)
					.text(TranslationHelper.translate(chatData.languageCode, "start.select_language.text") + if(chatData.languageCode != "en") (" / " + TranslationHelper.translate("en", "start.select_language.text")) else "")
				.build();
			}
			EEditMessageText.UPDATE_LANGUAGE -> {
				val newLanguageCode = arguments.get(0);
				return EditMessageText.builder()
					.chatId(query.message.chat.id)
					.messageId(query.message.messageId)
					.parseMode("MarkdownV2")
					.text(TranslationHelper.translate(newLanguageCode, if(chatData.isGroup) "init" else "start"))
				.build()
			}
			EEditMessageText.GAME_JOIN -> {
				var str = arguments.get(0) + "\n\n" + TranslationHelper.translate(chatData.languageCode, "game.count").replace("{}", arguments.get(1));
				return EditMessageText.builder()
					.chatId(query.message.chat.id)
					.messageId(query.message.messageId)
					.parseMode("MarkdownV2")
					.disableWebPagePreview(true)
					.text(TranslationHelper.translate(chatData.languageCode, "game.text") + "\n\n" + TranslationHelper.translate(chatData.languageCode, "game.join") + str)
				.build();
			}
		}

		return EditMessageText.builder()
			.chatId(query.message.chat.id)
			.messageId(query.message.messageId)
			.text("‼️ Error ‼")
		.build();
	}

	fun get(type: EEditMessageText, message: Message, arguments: Array<String>): EditMessageText {
		val chatData = DatabaseManager.getChatData(message.chat);
		when(type){
			EEditMessageText.STOP -> {
				if(chatData.registrationMessage != null){
					return EditMessageText.builder()
						.chatId(message.chat.id)
						.messageId(chatData.registrationMessage)
						.parseMode("MarkdownV2")
						.text(TranslationHelper.translate(chatData.languageCode, "stop.text"))
					.build();
				}
			}
			EEditMessageText.REG_STOP -> {
				return EditMessageText.builder()
					.chatId(message.chat.id)
					.messageId(message.messageId)
					.parseMode("MarkdownV2")
					.text(TranslationHelper.translate(chatData.languageCode, if(arguments.get(0).toInt() > 0) "reg.stop.valid_count" else "reg.stop.invalid_count"))
				.build();
			}
		}

		return EditMessageText.builder()
			.chatId(message.chat.id)
			.messageId(message.messageId)
			.text("‼️ Error ‼️")
		.build();
	}

	fun get(type: EEditMessageText, message: Message): EditMessageText = get(type, message, arrayOf<String>());
	fun get(type: EEditMessageText, query: CallbackQuery): EditMessageText = get(type, query, arrayOf<String>());
}
