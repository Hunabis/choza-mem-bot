import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

abstract class CustomCommand(identifier: String, description: String): BotCommand(identifier, description){

	override fun processMessage(absSender: AbsSender, message: Message, arguments: Array<String>){
		if(absSender is ChozaMemBot) absSender.processCommand(this, message, arguments);
	}

	abstract fun execute(a: AbsSender, b: Message, c: Array<String>);

	override fun execute(a: AbsSender, b: User, c: Chat, d: Array<String>){}

}
