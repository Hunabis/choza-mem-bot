data class ChatData(val languageCode: String, val isGroup: Boolean, val isAdmin: Boolean, val registrationMessage: Int?){
	companion object {
		val EMPTY = ChatData("", false, false, null);
	}
}
