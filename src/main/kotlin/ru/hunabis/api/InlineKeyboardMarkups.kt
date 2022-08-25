import org.telegram.telegrambots.meta.api.objects.replykeyboard.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.*;

object InlineKeyboardMarkups {

	val ERROR = InlineKeyboardMarkup().apply { 
		keyboard = listOf(listOf(InlineKeyboardButtons.ERROR));
	}

	fun get(type: EInlineKeyboardMarkup, languageCode: String, arguments: Array<String>): InlineKeyboardMarkup{
		when(type){
			EInlineKeyboardMarkup.START -> return InlineKeyboardMarkup().apply {
				keyboard = listOf(
					listOf(InlineKeyboardButtons.get(EInlineKeyboardButton.INVITE, languageCode)),
					listOf(InlineKeyboardButtons.get(EInlineKeyboardButton.SELECT_LANGUAGE, languageCode)),
					listOf(
						InlineKeyboardButtons.get(EInlineKeyboardButton.NEWS, languageCode),
						InlineKeyboardButtons.get(EInlineKeyboardButton.HELP, languageCode)
					)
				);
			};
			EInlineKeyboardMarkup.SELECT_LANGUAGE -> {
				val languageCodes = TranslationHelper.getSupportedLanguages();
				val buttons = mutableListOf<MutableList<InlineKeyboardButton>>();
				for(i in languageCodes.indices){
					val ii = (i.toFloat() / 2.0).toInt();
					val button = InlineKeyboardButtons.get(EInlineKeyboardButton.LANGUAGE, languageCodes.get(i));

					try {
						buttons.get(ii).add(button);
					} catch(e: Exception){
						buttons.add(mutableListOf<InlineKeyboardButton>());
						buttons.get(ii).add(button);
					}
				}

				buttons.add(mutableListOf(InlineKeyboardButton(TranslationHelper.translate(languageCode, "start.select_language.back")).apply {
					callbackData = "start.select_language.back";
				}));

				return InlineKeyboardMarkup().apply {
					keyboard = buttons;
				}
			}
			EInlineKeyboardMarkup.INIT -> {
				return InlineKeyboardMarkup().apply {
					keyboard = listOf(
						listOf(
							InlineKeyboardButtons.get(EInlineKeyboardButton.SELECT_LANGUAGE, languageCode),
							InlineKeyboardButtons.get(EInlineKeyboardButton.HELP, languageCode)
						)
					)
				}
			}
			EInlineKeyboardMarkup.GAME -> {
				return InlineKeyboardMarkup().apply {
					keyboard = listOf(
						listOf(
							InlineKeyboardButtons.get(EInlineKeyboardButton.GAME, languageCode)
						)
					)
				}
			}
			EInlineKeyboardMarkup.SHARE -> {
				return InlineKeyboardMarkup().apply {
					keyboard = listOf(
						listOf(
							InlineKeyboardButtons.get(EInlineKeyboardButton.SHARE_WHATSAPP, languageCode, arguments)
						)
					)
				}
			}
			EInlineKeyboardMarkup.GAME_MEMES -> {
				val buttons = mutableListOf<List<InlineKeyboardButton>>();
				for(i in 1..6) buttons.add(listOf(InlineKeyboardButton("$i").apply {
					callbackData = "game.memes.select.$i:${arguments.get(2)}";
				}));

				return InlineKeyboardMarkup().apply {
					keyboard = buttons;
				}
			}
			EInlineKeyboardMarkup.GAME_VOTE -> {
				val buttons = mutableListOf<List<InlineKeyboardButton>>();
				val game = Game.get(arguments.get(0).toLong());
				if(game != null) for(player in game.getPlayers()){
					buttons.add(listOf(InlineKeyboardButton("${player.firstName}  —  ${player.getVotes()}🔥").apply {
						callbackData = "game.vote.${player.chatId}";
					}))
				}

				return InlineKeyboardMarkup().apply {
					keyboard = buttons;
				}
			}
		}

		return ERROR;
	}

	fun get(type: EInlineKeyboardMarkup, languageCode: String): InlineKeyboardMarkup = get(type, languageCode, arrayOf<String>());

}
