import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

import kotlin.math.max;
import kotlin.math.min;

class GameCommand: CustomCommand(COMMAND_IDENTIFIER, COMMAND_DESCRIPTION){

	companion object {

		val COMMAND_IDENTIFIER = "game";
		val COMMAND_DESCRIPTION = "game.description";

	}

	override fun execute(absSender: AbsSender, message: Message, arguments: Array<String>){
		try {
			if(DatabaseManager.containsChat(message.chat)){
				val chatData = DatabaseManager.getChatData(message.chat);
				if(chatData.isGroup && chatData.isAdmin && chatData.registrationMessage == null && Game.get(message.chat.id) == null){
					val mes: Message? = absSender.execute(SendMessages.get(ESendMessage.GAME, message, arguments));
					if(mes != null){
						var initTime = min(max(try { arguments.get(0).toLong() } catch(e: Exception){ 90L }, 0), 14400);
						val task = object: CustomTimerTask("registration:${message.chat.id}", initTime){
							override fun onTick(){
								var time = 0;
								var secs = this.seconds.toInt();
								if(secs > 7200) time = secs % 3600;
								if(secs in 1800..3600) time = secs % 1800;
								if(secs in 600..1800) time = secs % 600;
								if(secs in 300..600) time = secs % 300;
								if(secs in 60..300) time = secs % 60;
								if(secs in 10..60) time = secs % 10;
								if(secs in 1..10) time = 0;
								if(time == 0) {
									val timeMes: Message? = absSender.execute(SendMessages.get(ESendMessage.REG_TIME, mes, arrayOf(this.seconds.toString())));
									if(timeMes != null){
										Game.get(timeMes.chat.id)?.addTimeMessage(timeMes.messageId);
									}
								}
							}
							override fun onFinish(){
								val count = DatabaseManager.getRegisteredUsers(mes.chat).size;
								val regStopMessage = absSender.execute(EditMessageTexts.get(EEditMessageText.REG_STOP, mes, arrayOf(count.toString())));
								val game = Game.get(mes.chat.id);
								game?.deleteTimeMessages(absSender);
								if(count < 1) return Unit;
								game?.setState(EGameState.PROCESS);
								val additionMessage = absSender.execute(SendMessages.get(ESendMessage.GAME_ADDITION, mes));
								if(additionMessage != null) TimerTasker.setTimeout({
									absSender.execute(DeleteMessage.builder()
										.chatId(additionMessage.chat.id)
										.messageId(additionMessage.messageId)
									.build());
									if(count > 0 && regStopMessage is Message){
										absSender.execute(DeleteMessage.builder()
											.chatId(additionMessage.chat.id)
											.messageId(regStopMessage.messageId)
										.build());
									}
								}, 5000);
								TimerTasker.setTimeout({
									DatabaseManager.getRegisteredUsers(mes.chat).forEach {
										val firstName = absSender.execute(GetChatMember.builder()
											.chatId(message.chat.id)
											.userId(it)
										.build()).getUser().firstName;
										game?.addPlayer(Player(it, firstName));
										val questionMessage = absSender.execute(SendMessages.get(ESendMessage.GAME_QUESTION, mes));
										if(questionMessage != null){
											game?.setQuestion(questionMessage.messageId);
											game?.cacheMessage(questionMessage.messageId);
										}

										val yourMemesMessage = absSender.execute(SendMessage.builder()
											.chatId(it)
											.parseMode("MarkdownV2")
											.text(TranslationHelper.translate(chatData.languageCode, "game.memes"))
										.build());

										val player = game?.getPlayer(it);
										if(yourMemesMessage != null){
											player?.setYourMemesMessage(yourMemesMessage.messageId);
											TimerTasker.setTimeout({
												println(1);
												try{absSender.execute(SendMessages.get(ESendMessage.GAME_MEMES, yourMemesMessage, arrayOf(mes.chat.id.toString())));}catch(e: Exception) { e.printStackTrace()};
												println(2);
											}, 6000);
										}
										for(i in 1..6){
											val url = randomPicture();
											try {
												val meme = absSender.execute(SendPhoto.builder()
													.chatId(it)
													.caption("$i.")
													.photo(InputFile(url))
												.build());

												if(meme != null) player?.setMeme(meme.messageId, url);
											} catch(e: Exception){
												e.printStackTrace();
											}
										}

										DatabaseManager.clearRegistrationMessage(mes.chat);
										DatabaseManager.clearRegisteredUsers(mes.chat);
									}
								}, 4000);
							}
						};

						Game(mes.chat);
						TimerTasker.addTask(task);
						DatabaseManager.setRegistrationMessage(message.chat, mes);
					}
				}
			}
		} catch (e: TelegramApiException){
			e.printStackTrace();
		}
	}

}
