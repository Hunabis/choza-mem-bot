import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

object SendMessages {

	fun get(type: ESendMessage, message: Message, arguments: Array<String>): SendMessage {
		val chatData = DatabaseManager.getChatData(message.chat);
		when(type){
			ESendMessage.START -> {
				return SendMessage.builder()
					.chatId(message.chat.id)
					.text(TranslationHelper.translate(chatData.languageCode, "start"))
					.parseMode("MarkdownV2")
					.replyMarkup(InlineKeyboardMarkups.get(EInlineKeyboardMarkup.START, chatData.languageCode))
				.build();

			}
			ESendMessage.INIT -> {
				if(chatData.isGroup){
					return SendMessage.builder()
						.chatId(message.chat.id)
						.text(TranslationHelper.translate(chatData.languageCode, "init"))
						.parseMode("MarkdownV2")
						.replyMarkup(InlineKeyboardMarkups.get(EInlineKeyboardMarkup.INIT, chatData.languageCode))
					.build();
				}
			}
			ESendMessage.GAME -> {
				return SendMessage.builder()
					.chatId(message.chat.id)
					.text(TranslationHelper.translate(chatData.languageCode, "game.text"))
					.parseMode("MarkdownV2")
					.replyMarkup(InlineKeyboardMarkups.get(EInlineKeyboardMarkup.GAME, chatData.languageCode))
				.build();
			}
			ESendMessage.GAME_INVALID -> {
				return SendMessage.builder()
					.chatId(message.chat.id)
					.text(TranslationHelper.translate(chatData.languageCode,"game.invalid"))
					.parseMode("MarkdownV2")
				.build();
			}
			ESendMessage.REG_TIME -> {
				val totalSecs = arguments.get(0).toLong();
				val hours = (totalSecs / 3600).toShort();
				val minutes = ((totalSecs % 3600) / 60).toShort();
				val seconds = (totalSecs % 60).toShort();
				var time = "";
				if(hours > 0) time += "$hours " + TranslationHelper.translate(chatData.languageCode, "time.hour") + " ";
				if(minutes > 0) time += "$minutes " + TranslationHelper.translate(chatData.languageCode, "time.minute") + " ";
				if(seconds > 0) time += "$seconds " + TranslationHelper.translate(chatData.languageCode, "time.second");
				return SendMessage.builder()
					.chatId(message.chat.id)
					.parseMode("MarkdownV2")
					.replyMarkup(InlineKeyboardMarkups.get(EInlineKeyboardMarkup.GAME, chatData.languageCode))
					.replyToMessageId(message.messageId)
					.text(TranslationHelper.translate(chatData.languageCode, "reg.time").replace("{}", time))
				.build();
			}
			ESendMessage.EXTEND -> {
				val totalSecs = arguments.get(1).toLong();
				val hours = (totalSecs / 3600).toShort();
				val minutes = ((totalSecs % 3600) / 60).toShort();
				val seconds = (totalSecs % 60).toShort();
				var time = "";
				if(hours > 0) time += "$hours " + TranslationHelper.translate(chatData.languageCode, "time.hour") + " ";
				if(minutes > 0) time += "$minutes " + TranslationHelper.translate(chatData.languageCode, "time.minute") + " ";
				if(seconds > 0) time += "$seconds " + TranslationHelper.translate(chatData.languageCode, "time.second");
				return SendMessage.builder()
					.chatId(message.chat.id)
					.parseMode("MarkdownV2")
					.text(TranslationHelper.translate(chatData.languageCode, "extend").replaceFirst("{}", arguments.get(0)).replace("{}", time))
				.build();
			}
			ESendMessage.SHARE -> {
				return SendMessage.builder()
					.chatId(message.chat.id)
					.parseMode("MarkdownV2")
					.text(TranslationHelper.translate(chatData.languageCode, "share.text"))
					.replyMarkup(InlineKeyboardMarkups.get(EInlineKeyboardMarkup.SHARE, chatData.languageCode, arguments))
				.build();
			}
			ESendMessage.GAME_ADDITION -> {
				return SendMessage.builder()
					.chatId(message.chat.id)
					.parseMode("MarkdownV2")
					.text(TranslationHelper.translate(chatData.languageCode, "game.addition"))
				.build();
			}
			ESendMessage.GAME_QUESTION -> {
				return SendMessage.builder()
					.chatId(message.chat.id)
					.parseMode("MarkdownV2")
					.text("СУПЕР МЕГА ИНТЕРЕСНЫЙ ВАААПРОС?")
				.build();
			}
			ESendMessage.GAME_MEMES -> {
				return SendMessage.builder()
					.chatId(message.chat.id)
					.parseMode("MarkdownV2")
					.text(TranslationHelper.translate(chatData.languageCode, "game.memes.select"))
					.replyMarkup(InlineKeyboardMarkups.get(EInlineKeyboardMarkup.GAME_MEMES, chatData.languageCode, arguments))
					.replyToMessageId(message.chat.id)
				.build();
			}
			ESendMessage.GAME_VOTE -> {
				return SendMessage.builder()
					.chatId(arguments.get(0).toLong())
					.parseMode("MarkdownV2")
					.text(TranslationHelper.translate(DatabaseManager.getChatData(arguments.get(0).toLong()).languageCode, "game.vote"))
					.replyMarkup(InlineKeyboardMarkups.get(EInlineKeyboardMarkup.GAME_VOTE, chatData.languageCode, arguments))
				.build();
			}
			ESendMessage.GAME_ROUND -> {
				return SendMessage.builder()
					.chatId(message.chat.id)
					.parseMode("MarkdownV2")
					.text(TranslationHelper.translate(chatData.languageCode, "game.round").replace("{}", arguments.get(0)))
				.build();
			}
			ESendMessage.GAME_END -> {
				val kv = arguments.get(0).split(":");
				return SendMessage.builder()
					.chatId(message.chat.id)
					.parseMode("MarkdownV2")
					.text(TranslationHelper.translate(chatData.languageCode, "game.end").replaceFirst("{}", kv.get(0)).replace("{}", kv.get(1)))
				.build();
			}
		}

		return SendMessage.builder()
			.chatId(message.chat.id)
			.text("‼ Error ‼")
		.build();
	}

	fun get(type: ESendMessage, message: Message): SendMessage = get(type, message, arrayOf<String>());
}
