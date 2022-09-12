import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.adminrights.SetMyDefaultAdministratorRights;
import org.telegram.telegrambots.meta.api.objects.adminrights.ChatAdministratorRights;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;

class ChozaMemBot: TelegramLongPollingCommandBot(){
	
	init {

		register(StartCommand());
		register(GameCommand());
		register(StopCommand());
		register(ExtendCommand());
		register(HelpCommand());
		register(ShareCommand());

		execute(
			SetMyDefaultAdministratorRights(
				ChatAdministratorRights.builder()
					.canDeleteMessages(true)
					.canRestrictMembers(true)
					.canPinMessages(true)
				.build(),
			false)
		);

	}

	fun processCommand(command: CustomCommand, message: Message, arguments: Array<String>){
		if(!DatabaseManager.containsChat(message.chat)){
			DatabaseManager.initChat(message.chat, message.from.languageCode, if(message.chat.type != "private") null else message.from);
		}

		var chatData = DatabaseManager.getChatData(message.chat);
		if(chatData.isGroup && !chatData.isAdmin){
			execute(GetChatAdministrators(message.chat.id.toString()))?.forEach {
				if(it.getUser().userName == getBotUsername()){
					val admin = it as ChatMemberAdministrator;
					val cdm = admin.canDeleteMessages ?: false;
					val crm = admin.canRestrictMembers ?: false;
					val cpm = admin.canPinMessages ?: false;
					DatabaseManager.updateAdminState(message.chat, cdm && crm && cpm);

					if(cdm && crm && cpm){
						chatData = ChatData(chatData.languageCode, chatData.isGroup, true, chatData.registrationMessage);
						createGroupCommandMenu(message.chat);
					} else deleteGroupCommandMenu(message.chat);
				}
			}
		}

		if((chatData.isGroup && chatData.isAdmin) || !chatData.isGroup){
			execute(DeleteMessage.builder()
				.chatId(message.chat.id)
				.messageId(message.messageId)
			.build());
		} else {
			execute(SendMessages.get(ESendMessage.GAME_INVALID, message, arguments));
			return Unit;
		}

		command.execute(this, message, arguments);
	}

	override fun processNonCommandUpdate(update: Update){
		if(update.hasMyChatMember()){
			if(update.myChatMember.newChatMember.user.userName == getBotUsername()){
				if(update.myChatMember.chat.type == "private"){
					when(update.myChatMember.newChatMember.status){
						"kicked" -> {
							DatabaseManager.removeChat(update.myChatMember.chat);
							Game.remove(update.myChatMember.chat.id);
						}
					}
				} else {
					when(update.myChatMember.newChatMember.status){
						"administrator" -> {
							val admin = update.myChatMember.newChatMember as ChatMemberAdministrator;
							val cdm = admin.canDeleteMessages ?: false;
							val crm = admin.canRestrictMembers ?: false;
							val cpm = admin.canPinMessages ?: false;
							DatabaseManager.updateAdminState(update.myChatMember.chat, cdm && crm && cpm);

							if(cdm && crm && cpm) createGroupCommandMenu(update.myChatMember.chat);
							else deleteGroupCommandMenu(update.myChatMember.chat);

							when(update.myChatMember.oldChatMember.status){
								"kicked" -> {
									
								}
								"member" -> {
									
								}
								"left" -> {
									
								}
								"registered" -> {
									
								}
								"creator" -> {
									
								}
							}
						}
						"kicked" -> {
							if(DatabaseManager.containsChat(update.myChatMember.chat)){
								DatabaseManager.removeChat(update.myChatMember.chat);
								Game.remove(update.myChatMember.chat.id);
							}
							when(update.myChatMember.oldChatMember.status){
								"administrator" -> {
									DatabaseManager.updateAdminState(update.myChatMember.chat, false);
								}
								"member" -> {
								
								}
								"left" -> {
									
								}
								"registered" -> {
								
								}
								"creator" -> {
									
								}
							}
						}
						"member" -> {
							when(update.myChatMember.oldChatMember.status){
								"administrator" -> {
									DatabaseManager.updateAdminState(update.myChatMember.chat, false);
									deleteGroupCommandMenu(update.myChatMember.chat);
								}
								"kicked" -> {
								
								}
								"left" -> {
									deleteGroupCommandMenu(update.myChatMember.chat);
								}
								"registered" -> {
									
								}
								"creator" -> {
									
								}
							}
						}
						"left" -> {
							if(DatabaseManager.containsChat(update.myChatMember.chat)){
								DatabaseManager.removeChat(update.myChatMember.chat);
								Game.remove(update.myChatMember.chat.id);
							}

							when(update.myChatMember.oldChatMember.status){
								"administrator" -> {
									DatabaseManager.updateAdminState(update.myChatMember.chat, false);
								}
								"kicked" -> {
									
								}
								"member" -> {
									
								}
								"registered" -> {
									
								}
								"creator" -> {
									
								}
							}
						}
						"registered" -> {
							when(update.myChatMember.oldChatMember.status){
								"administrator" -> {
									DatabaseManager.updateAdminState(update.myChatMember.chat, false);
								}
								"kicked" -> {
									
								}
								"member" -> {
									
								}
								"left" -> {
									
								}
								"creator" -> {
									
								}
							}
						}
						"creator" -> {
							println("GGGGGGGGGGGGGGGGGG");
							when(update.myChatMember.oldChatMember.status){
								"administrator" -> {
									
								}
								"kicked" -> {
									
								}
								"member" -> {
									
								}
								"left" -> {
									
								}
								"registered" -> {
									
								}
							}
						}
					}
				}
			}
		} else if(update.hasMessage()){
			if(update.message.chat.type == "private"){
				if(!DatabaseManager.containsChat(update.message.chat)){
					DatabaseManager.initChat(update.message.chat, update.message.from.languageCode, update.message.from);
				}
			} else {
				if(update.message.migrateToChatId != null){
					DatabaseManager.updateChatId(update.message.chat.id, update.message.migrateToChatId);
				} else if(update.message.groupchatCreated != null){
					if(DatabaseManager.initChat(update.message.chat, update.message.from.languageCode, null)){
						execute(SendMessages.get(ESendMessage.INIT, update.message));
					}
				} else {
					for(member in update.message.newChatMembers){
						if(member.userName == getBotUsername()){
							if(DatabaseManager.initChat(update.message.chat, update.message.from.languageCode, null)){
								execute(SendMessages.get(ESendMessage.INIT, update.message));
								break;
							}
						}
					}
				}
			}
		}

		if(update.hasCallbackQuery()){
			val query = update.callbackQuery;
			//val userIsAdmin = execute(GetChatAdministrators(query.message.chat.id.toString())).map{it.getUser().id}.contains(query.message.from.id);
			if(query.data != null && query.data.length > 0){
				when(query.data){
					"start.select_language" -> {
						execute(EditMessageTexts.get(EEditMessageText.SELECT_LANGUAGE, query));
						execute(EditMessageReplyMarkups.get(EEditMessageReplyMarkup.SELECT_LANGUAGE, query));
					}
					"game.join" -> {
						try {
							val users = DatabaseManager.getRegisteredUsers(query.message.chat);
							if(!users.contains(query.from.id) && DatabaseManager.registerUser(query.message.chat, query.from)){
								users.add(query.from.id);
								var str = "";
								users.forEach {
									val firstName = execute(
										GetChatMember.builder()
											.chatId(query.message.chat.id)
											.userId(it)
										.build()
									).getUser().firstName;

									str += "\n[$firstName]({})".replaceFirst("{}", fixMarkdown("tg://user?id=$it"));
								}

								execute(EditMessageTexts.get(EEditMessageText.GAME_JOIN, query, arrayOf(str, "${users.size}")));
								execute(EditMessageReplyMarkups.get(EEditMessageReplyMarkup.GAME_JOIN, query));
							}
						} catch(e: Exception){
							e.printStackTrace();
						}
					}
				}

				if(query.data.indexOf("start.select_language.") != -1){
					val chatData = DatabaseManager.getChatData(query.message.chat);
					var newLanguageCode = chatData.languageCode;
					if(query.data != "start.select_language.back") {
						newLanguageCode = query.data.split("start.select_language.").get(1);
						DatabaseManager.setLanguageCode(query.message.chat, newLanguageCode);
					}

					execute(EditMessageTexts.get(EEditMessageText.UPDATE_LANGUAGE, query, arrayOf(newLanguageCode)));
					execute(EditMessageReplyMarkups.get(EEditMessageReplyMarkup.UPDATE_LANGUAGE, query, arrayOf(newLanguageCode)));
				}

				if(query.data.indexOf("game.memes.select.") != -1){
					execute(DeleteMessage.builder()
						.chatId(query.message.chat.id)
						.messageId(query.message.messageId)
					.build());
					val gameData = query.data.split("game.memes.select.").get(1).split(":");
					val chatId = gameData.get(1).toLong();
					val selectedMeme = gameData.get(0).toInt() - 1;
					val game = Game.get(chatId);
					val player = game?.getPlayer(query.message.chat.id);
					if(player != null){
						val meme = player.getMemeAtIndex(selectedMeme);
						if(meme != null) {
							player.selectMeme(meme.first);
							val memePicture = try{execute(SendPhoto.builder()
								.chatId(chatId)
								.photo(InputFile(meme.second))
								.replyToMessageId(game?.getQuestion() ?: -1)
								.caption("[${player.firstName}]({})".replaceFirst("{}", fixMarkdown("tg://user?id=${query.from.id}")))
								.parseMode("MarkdownV2")
							.build());}catch(e: Exception){null}

							if(memePicture != null){
								game.cacheMessage(memePicture.messageId);
							}
						}
					}

					if(game != null && game.allPlayersSelectedMemes()){
						val voteMessage = execute(SendMessages.get(ESendMessage.GAME_VOTE, query.message, arrayOf(chatId.toString())));
						if(voteMessage != null){
							game.setVoteMessage(voteMessage.messageId);
							TimerTasker.setTimeout({
								if(game.getRound() < 5){
									//game.deleteMemePictures(this);
									//game.deleteQuestionMessage(this);
									game.deleteVoteMessage(this);
									game.clearVotedUsers();
									game.addRound();

									val votes = mutableMapOf<Long, Int>();
									for(player in game.getPlayers()){
										votes.put(player.chatId, player.getVotes());
										player.setVotes(0);
										val newPicture = randomPicture();
										val selectedMeme2 = player.getSelectedMeme();
										player.selectMeme(null);
										if(selectedMeme2 != null){
											player.setMeme(selectedMeme2, newPicture);
											execute(EditMessageMedia.builder()
												.chatId(player.chatId)
												.messageId(selectedMeme2)
												.media(InputMediaPhoto.builder()
													.media(newPicture)
													.caption("${player.getMemes().keys.indexOf(selectedMeme2) + 1}.")
												.build())
											.build());
										}

										TimerTasker.setTimeout({
											execute(SendMessages.get(ESendMessage.GAME_MEMES, query.message, arrayOf(player.chatId.toString(), player.getYourMemesMessage().toString() ?: "", voteMessage.chat.id.toString())));
										}, 6000);
									}

									val maxVotes = votes.maxBy { it.value }?.value ?: 0;
									val winners = votes.filterValues { it == maxVotes };
									if(winners != null) for(chatId in winners.keys){
										game.getPlayer(chatId)?.addScore();
									}

									var str = "";
									for(chatId in winners.keys){
										val player = game.getPlayer(chatId.toLong());
										if(player != null){
											str += "\n[${player.firstName}]({})".replaceFirst("{}", fixMarkdown("tg://user?id=${player.chatId}"));
										}
									}

									val roundMessage = execute(SendMessages.get(ESendMessage.GAME_ROUND, voteMessage, arrayOf(str)));
									if(roundMessage != null) game.cacheMessage(roundMessage.messageId);

									TimerTasker.setTimeout({
										val questionMessage = execute(SendMessages.get(ESendMessage.GAME_QUESTION, voteMessage));
										if(questionMessage != null){
											game.setQuestion(questionMessage.messageId);
											game.cacheMessage(questionMessage.messageId);
										}
									}, 6000);
									println(game.toString());
								} else {
									Game.remove(game.chat.id);
									game.deleteCachedMessages(this);
									game.deleteVoteMessage(this);
									val sorted = game.getPlayers().sortedBy { it.getScore() };
									var str = ""; 
									var i = 0;
									for(player in sorted){
										str += "\n\t[${player.firstName}]({})".replaceFirst("{}", fixMarkdown("tg://user?id=${player.chatId}")) + "  —  ${player.getScore()}" + if(i == 2) ":" else "";
										execute(SendMessages.get(ESendMessage.GAME_END, voteMessage, arrayOf(str)));
										player.deleteMemes(this);
										player.deleteYourMemesMessage(this);
										i++;
									}
								}
							}, 25000);
						}
					}
				}

				if(query.data.indexOf("game.vote.") != -1){
					val game = Game.get(query.message.chat.id);
					if(game != null && game.vote(query.from.id, query.data.split("game.vote.").get(1).toLong())){
						execute(EditMessageReplyMarkups.get(EEditMessageReplyMarkup.GAME_VOTE, query));
					}
				}
			}
		}
    }

	fun createGroupCommandMenu(chat: Chat){ 
		for(languageCode in TranslationHelper.getSupportedLanguages()){
			val menu = mutableListOf<BotCommand>();
			for(command in getRegisteredCommands()) menu.add(BotCommand(command.getCommandIdentifier(), TranslationHelper.translate(languageCode, command.getCommandIdentifier() + ".description.group")));

			execute(
				SetMyCommands.builder()
					.commands(menu)
					.languageCode(languageCode)
					.scope(BotCommandScopeChatAdministrators(chat.id.toString()))
				.build()
			)
		}
	}

	fun deleteGroupCommandMenu(chat: Chat){
		for(languageCode in TranslationHelper.getSupportedLanguages()){
			execute(
				DeleteMyCommands.builder()
					.languageCode(languageCode)
					.scope(BotCommandScopeChatAdministrators(chat.id.toString()))
				.build()
			)
		}
	}

    override fun getBotToken() = System.getenv("TOKEN");
    
    override fun getBotUsername() = "ChozaMemBot";
}
