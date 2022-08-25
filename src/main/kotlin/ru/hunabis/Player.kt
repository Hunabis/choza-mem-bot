import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

class Player(val chatId: Long, val firstName: String){

	private var yourMemesMessage: Int? = null;
	private var memes = mutableMapOf<Int, String>();
	private var selectedMeme: Int? = null;
	private var votes = 0;
	private var score = 0;

	fun setYourMemesMessage(messageId: Int){
		this.yourMemesMessage = messageId;
	}

	fun getVotes(): Int {
		return this.votes;
	}

	fun getScore(): Int {
		return this.score;
	}

	fun setScore(score: Int){
		this.score = score;
	}

	fun addScore(){
		this.score++;
	}

	fun vote(){
		this.votes++;
	}

	fun setVotes(votes: Int){
		this.votes = votes;
	}

	fun getYourMemesMessage(): Int? {
		return this.yourMemesMessage;
	}

	fun selectMeme(messageId: Int?){
		this.selectedMeme = messageId;
	}

	fun getSelectedMeme(): Int? {
		return this.selectedMeme;
	}

	fun setMeme(messageId: Int, url: String){
		this.memes.put(messageId, url);
	}

	fun getMemes(): Map<Int, String> {
		return this.memes;
	}

	fun getMemeIds(): List<Int> {
		return this.memes.keys.toList();
	}

	fun getMemePicture(messageId: Int): String? {
		return this.memes.get(messageId);
	}

	fun getMemeAtIndex(index: Int): Pair<Int, String>? {
		return this.memes.entries.toList().get(index).toPair();
	}

	fun deleteMemes(absSender: AbsSender){
		this.memes.forEach {
			absSender.execute(DeleteMessage.builder()
				.chatId(chatId)
				.messageId(it.key)
			.build());
		}
	}

	fun deleteYourMemesMessage(absSender: AbsSender){
		absSender.execute(DeleteMessage.builder()
			.chatId(this.chatId)
			.messageId(this.yourMemesMessage ?: 0)
		.build());
	}

	override fun toString(): String {
		return "Player(yourMemesMessage=$yourMemesMessage, memes=${memes.toString()}, selectedMeme=$selectedMeme, votes=$votes, score=$score)"
	}
}
