import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;

object TranslationHelper {
	private val langPath = "/assets/lang";
	private val supportedLanguages = mutableListOf<String>();
	private val translations = mutableMapOf<String, MutableMap<Int, String>>();
	private val languageCodeLocalizations = mutableMapOf<String, String>();

	fun getEmojiByLanguage(languageCode: String): String {
		when(languageCode){
			"ru" -> return "🇷🇺";
			"en" -> return "🇺🇲";
			"ko" -> return "🇰🇷";
		}

		return "";
	}

	fun getSupportedLanguages(): MutableList<String> = supportedLanguages;

	fun isLegalLanguage(languageCode: String): Boolean = supportedLanguages.contains(languageCode);

	fun getTranslationMap(languageCode: String): MutableMap<Int, String> {
        var map = translations.get(languageCode);
		if(map == null) map = mutableMapOf<Int, String>();
        translations.put(languageCode, map);
        return map;
    }

    fun toShortName(languageCode: String): String {
		val index = languageCode.indexOf('_');
        if (index == -1) {
            return languageCode;
        }

        return languageCode.substring(0, index);
    }

	fun addSingleTranslation(languageCode: String, origin: String, translation: String) {
        getTranslationMap(toShortName(languageCode)).put(origin.hashCode(), translation);
    }

	fun addTranslation(origin: String, translationsMap: Map<String, String>) {
        for (languageCode in translationsMap.keys){
            val translation = translationsMap.get(languageCode);
            if(translation != null) addSingleTranslation(languageCode, origin, translation);
        }
    }

	fun addTranslation(command: IBotCommand, translationsMap: Map<String, String>){
		addTranslation("command." + command.getCommandIdentifier(), translationsMap);
	}

	fun addTranslation(languageCode: String, languageCodeLocalization: String){
		if(!supportedLanguages.contains(languageCode)) supportedLanguages.add(languageCode);
		languageCodeLocalizations.put(languageCode, languageCodeLocalization);
	}

	fun translate(languageCode: String, str: String): String {
        var map = getTranslationMap(languageCode);
        var str2 = map.get(str.hashCode());
        if(str2 != null) return str2;
        return str;
    }

    fun translate(languageCode: String): String {
		val str = languageCodeLocalizations.get(languageCode);
		if(str != null) return str;
		return languageCode;
	}

	/*fun readFile(map: MutableMap<String, MutableMap<String, String>>, name: String): {
		
	}

	fun initLangs(){
		val allTranslationKeys = mutableMapOf<String, MutableMap<String, String>>();
		FileTools.GetListOfFiles(__dir__ + "/assets/lang", "lang")
			.forEach({ file -> readFile(allTranslationKeys, file.getName().)});
		for (key in allTranslationKeys) {
			Translation.addTranslation(key, allTranslationKeys.get(key));
		}

	}*/

	init {
	
		addTranslation("ru", "Русский");
		addTranslation("en", "English");
		addTranslation("ko", "한국어");

		ResourceReader.getAllFilesInResources(langPath).forEach { fileName ->
			ResourceReader.readTextResource("$langPath/$fileName").split("\n").filter {
				it.length > 0 && !it.startsWith("#");
			}.forEach {
				val kv = it.split("=");
				var str = if(kv.get(1).startsWith("&")) fixChototam(kv.get(1).replaceFirst("&", "")) else fixMarkdown(kv.get(1));
				TranslationHelper.addSingleTranslation(fileName.split(".").get(0), kv.get(0), str);
			}
		}
	}
}

fun fixMarkdown(str: String): String {
	var result = str;

	result = result.replace("!", "\\!");
	result = result.replace("-", "\\-");
	result = result.replace(".", "\\.");
	result = result.replace("+", "\\+");
	result = result.replace("=", "\\=");
	result = result.replace(")", "\\)");

	return fixChototam(result);
}

fun fixChototam(str: String): String {
	var result = str;

	result = result.replace("%n", "\n");

	return result;
}
