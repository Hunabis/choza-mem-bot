import java.net.URLEncoder;

object InviteHelper {

	val whatsappLink = "https://api.whatsapp.com/send?text=";

	fun createWhatsapp(languageCode: String, inviteLink: String): String {
		val text = TranslationHelper.translate(languageCode, "share");
		return whatsappLink + URLEncoder.encode(text.replace("{}", inviteLink), "utf-8");
	}

}
