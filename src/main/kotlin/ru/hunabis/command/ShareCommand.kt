import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink

class ShareCommand: CustomCommand(COMMAND_IDENTIFIER, COMMAND_DESCRIPTION){

	companion object {

		val COMMAND_IDENTIFIER = "share";
		val COMMAND_DESCRIPTION = "share.description";

	}

	override fun execute(absSender: AbsSender, message: Message, arguments: Array<String>){
		try {
			val chatData = DatabaseManager.getChatData(message.chat);
			if(chatData.isGroup){
				val link = absSender.execute(CreateChatInviteLink.builder().chatId(message.chat.id).memberLimit(1).build());
				if(link != null) absSender.execute(SendMessages.get(ESendMessage.SHARE, message, arrayOf(link.inviteLink)));
			}
		} catch(e: Exception){
			e.printStackTrace();
		}
	}

}
