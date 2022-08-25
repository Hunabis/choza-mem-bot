import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

import kotlin.random.Random;

class Game(val chat: Chat){

	private var state = EGameState.REGISTRATION;
	private var round = 1;
	private val timeMessages = mutableListOf<Int>();
	private val players = mutableMapOf<Long, Player>();
	private var questionMessage: Int? = null;
	private var voteMessage: Int? = null;
	private var votedUsers = mutableListOf<Long>();
	private val messageCache = mutableListOf<Int>();

	companion object {

		val games = mutableMapOf<Long, Game>();

		fun get(chatId: Long): Game? = games.get(chatId);
		fun remove(chatId: Long): Game? = games.remove(chatId);
	}

	init {

		games.put(chat.id, this);

	}

	fun vote(userId: Long, playerChatId: Long): Boolean {
		if(!this.votedUsers.contains(userId)){
			this.votedUsers.add(userId);
			this.getPlayer(playerChatId)?.vote();
			return true;
		}

		return false;
	}

	fun clearVotedUsers(){
		this.votedUsers = mutableListOf<Long>();
	}

	fun cacheMessage(messageId: Int){
		this.messageCache.add(messageId);
	}

	fun deleteCachedMessages(absSender: AbsSender){
		this.messageCache.forEach {
			absSender.execute(DeleteMessage.builder()
				.chatId(this.chat.id)
				.messageId(it)
			.build());
		}
	}

	fun deleteVoteMessage(absSender: AbsSender){
		absSender.execute(DeleteMessage.builder()
			.chatId(this.chat.id)
			.messageId(this.voteMessage ?: 0)
		.build());
	}

	fun getRound(): Int {
		return this.round;
	}

	fun setRound(round: Int){
		this.round = round; 
	}

	fun addRound(){
		this.round++;
	}

	fun setVoteMessage(messageId: Int){
		this.voteMessage = messageId;
	}

	fun getVoteMessage(): Int? {
		return this.voteMessage;
	}

	fun addPlayer(player: Player){
		this.players.put(player.chatId, player);
	}

	fun getPlayers(): List<Player> {
		return this.players.values.toList();
	}

	fun getQuestion(): Int? {
		return this.questionMessage;
	}

	fun setQuestion(messageId: Int){
		this.questionMessage = messageId;
	}

	fun getPlayer(chatId: Long): Player? {
		return this.players.get(chatId);
	}

	fun setState(state: EGameState){
		this.state = state;
	}

	fun getState(): EGameState {
		return this.state;
	}

	fun allPlayersSelectedMemes(): Boolean {
		val size = this.players.size;
		var count = 0;
		for(player in this.players.values) if(player.getSelectedMeme() != null) count++;
		return size == count;
	}

	fun deleteTimeMessages(absSender: AbsSender){
		timeMessages.forEach {
			absSender.execute(DeleteMessage.builder()
				.chatId(chat.id)
				.messageId(it)
			.build());
		}
	}
	
	fun addTimeMessage(messageId: Int){
		timeMessages.add(messageId);
	}

	override fun toString(): String {
		return "Game(round=$round, questionMessage=$questionMessage, voteMessage=$voteMessage, votedUsers=${votedUsers.toString()}, messageCache=${messageCache.toString()}, state=$state, players=${players.toString()}, timeMessages=${timeMessages.toString()})"
	}

}

fun randomPicture(): String {
	val chars = "01234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghiklmnopqrstuvwxyz";
	var str = "";
	for(i in 0..4) str += chars.random();
	return "https://m.imgur.com/$str.png";
}
