import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import com.google.common.io.Resources;

fun main(args: Array<String>) {
	TelegramBotsApi(DefaultBotSession::class.java).registerBot(ChozaMemBot());
}
