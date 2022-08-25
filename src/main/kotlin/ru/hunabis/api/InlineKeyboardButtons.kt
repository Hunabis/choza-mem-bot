import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.*;

object InlineKeyboardButtons {

	val ERROR = InlineKeyboardButton("‼ Error ‼").apply {
		callbackData = "error"
	};

	fun get(type: EInlineKeyboardButton, languageCode: String, arguments: Array<String>): InlineKeyboardButton {
		when(type){
			EInlineKeyboardButton.INVITE -> {
				return InlineKeyboardButton("➕ " + TranslationHelper.translate(languageCode, "start.invite")).apply {
					url = "https://t.me/ChozaMemBot?startgroup";
				}
			}
			EInlineKeyboardButton.SELECT_LANGUAGE -> {
				return InlineKeyboardButton(TranslationHelper.getEmojiByLanguage(languageCode) + " " + TranslationHelper.translate(languageCode, "start.select_language") + if(languageCode == "en") "" else " / " + TranslationHelper.translate("en", "start.select_language")).apply {
					callbackData = "start.select_language";
				}
			}
			EInlineKeyboardButton.NEWS -> {
				return InlineKeyboardButton("📰 " + TranslationHelper.translate(languageCode, "start.news")).apply {
					url = "https://t.me/+ra5ba9O_k7cxMzAy";
				}
			}
			EInlineKeyboardButton.HELP -> {
				return InlineKeyboardButton("💭 " + TranslationHelper.translate(languageCode, "start.help")).apply {
					url = "https://t.me/+DDKd_458Pr41OTBi";
				}
			}
			EInlineKeyboardButton.LANGUAGE -> {
				return InlineKeyboardButton(TranslationHelper.getEmojiByLanguage(languageCode) + " " + TranslationHelper.translate(languageCode)).apply {
					callbackData = "start.select_language." + languageCode;
				}
			}
			EInlineKeyboardButton.GAME -> {
				return InlineKeyboardButton(TranslationHelper.translate(languageCode, "game")).apply {
					callbackData = "game.join";
				}
			}
			EInlineKeyboardButton.SHARE_WHATSAPP -> {
				return InlineKeyboardButton("WhatsApp").apply {
					url = InviteHelper.createWhatsapp(languageCode, arguments.get(0));
				}
			}
		}

		return ERROR;
	}

	fun get(type: EInlineKeyboardButton, languageCode: String): InlineKeyboardButton = get(type, languageCode, arrayOf<String>());

}
