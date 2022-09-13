import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Types;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.Message;

object DatabaseManager {
	private val connection = ConectionDB();
	
	init {
		recreateTable();
	}

	fun recreateTable() {
		try {
			connection.initTransaction();
			createChatsTable();
			connection.commitTransaction();
		} catch (e: SQLException) {
			e.printStackTrace();
		}
	}

	fun createChatsTable(){
		connection.executeQuery("CREATE TABLE IF NOT EXISTS Chats (chatId BIGINT PRIMARY KEY, languageCode VARCHAR(11) NOT NULL, isGroup BOOLEAN NOT NULL, isAdmin BOOLEAN DEFAULT false, oldChatId BIGINT, registrationMessage INTEGER, registeredUsers BIGINT[] DEFAULT '{}');");
    }

    fun initChat(chat: Chat, languageCode: String = "en", user: User?): Boolean {
		var updatedRows = 0;
		try {
			val preparedStatement: PreparedStatement = connection.getPreparedStatement("INSERT INTO Chats (chatId, languageCode, isGroup, userId) VALUES (?, ?, ?, ?) ON CONFLICT (chatId) DO UPDATE SET languageCode = excluded.languageCode, isGroup = excluded.isGroup;");
			preparedStatement.setLong(1, chat.id);
			preparedStatement.setString(2, languageCode);
			preparedStatement.setBoolean(3, user == null);

			updatedRows = preparedStatement.executeUpdate();
		} catch (e: SQLException) {
			e.printStackTrace();
    	}

		//println("init chat: ${chat.id.toString()}, rows updated: $updatedRows");
		if(updatedRows > 0) {
			connection.commitTransaction();
			return true;
		}

		return false;
	}

	fun removeChat(chat: Chat): Boolean {
		var updatedRows = 0;
		try {
			val preparedStatement: PreparedStatement = connection.getPreparedStatement("DELETE FROM Chats WHERE chatId = ?;");
			preparedStatement.setLong(1, upgradeChatId(chat.id));

			updatedRows = preparedStatement.executeUpdate();
		} catch(e: SQLException){
			e.printStackTrace();
		}

		//println("remove chat: ${chat.id.toString()}, rows updated: $updatedRows");
		if(updatedRows > 0) {
			connection.commitTransaction();
			return true;
		}

		return false;
	}

	fun getGroupsCount(): Int {
		var result = 0;
		try {
			val preparedStatement: PreparedStatement = connection.getPreparedStatement("SELECT * FROM Chats");
			updatedRows = preparedStatement.executeUpdate();

			result = preparedStatement.getFetchSize();
        } catch (e: SQLException) {
			e.printStackTrace();
		}

		return result;
	}

	fun registerUser(chat: Chat, user: User): Boolean {
		var updatedRows = 0;
        try {
			val preparedStatement: PreparedStatement = connection.getPreparedStatement("UPDATE Chats SET registeredUsers = array_append(registeredUsers, ?) WHERE chatId = ?");
			preparedStatement.setLong(1, user.id);
			preparedStatement.setLong(2, upgradeChatId(chat.id));

			updatedRows = preparedStatement.executeUpdate();
        } catch (e: SQLException) {
			e.printStackTrace();
		}

		if(updatedRows > 0) {
			connection.commitTransaction();
			return true;
		}

		return false;
	}

	fun clearRegisteredUsers(chat: Chat): Boolean {
		var updatedRows = 0;
        try {
			val preparedStatement: PreparedStatement = connection.getPreparedStatement("UPDATE Chats SET registeredUsers = '{}' WHERE chatId = ?");
			preparedStatement.setLong(1, upgradeChatId(chat.id));

			updatedRows = preparedStatement.executeUpdate();
        } catch (e: SQLException) {
			e.printStackTrace();
		}

		if(updatedRows > 0) {
			connection.commitTransaction();
			return true;
		}

		return false;
	}

	fun getRegisteredUsers(chat: Chat): MutableList<Long> {
		var result = mutableListOf<Long>();
		try {
			val preparedStatement = connection.getPreparedStatement("SELECT registeredUsers FROM Chats WHERE chatId=?");
			preparedStatement.setLong(1, upgradeChatId(chat.id));
			val res = preparedStatement.executeQuery();
			if(res.next()) result = (res.getArray("registeredUsers").getArray() as Array<Long>).toMutableList();
			res.close();
        } catch (e: SQLException) {
			e.printStackTrace();
		}

		return result;
	}

    fun getChatData(chatId: Long): ChatData {
		var chatData = ChatData.EMPTY;
		try {
			val preparedStatement = connection.getPreparedStatement("SELECT * FROM Chats WHERE chatId = ?");
			preparedStatement.setLong(1, upgradeChatId(chatId));
			val result = preparedStatement.executeQuery();

			if(result.next()) chatData = ChatData(
				result.getString("languageCode"),
				result.getBoolean("isGroup"),
				result.getBoolean("isAdmin"),
				if(result.getInt("registrationMessage") == 0) null else result.getInt("registrationMessage")
			);

			result.close();
	  	} catch(e: SQLException){
			e.printStackTrace();
	  	}

		if(chatData == ChatData.EMPTY) {
			throw NullPointerException();
		}

		return chatData;
	}

	fun getChatData(chat: Chat): ChatData = this.getChatData(chat.id);

	fun updateAdminState(chat: Chat, isAdmin: Boolean = false): Boolean {
		var updatedRows = 0;
        try {
			val preparedStatement: PreparedStatement = connection.getPreparedStatement("UPDATE Chats SET isAdmin = ? WHERE chatId = ?");
			preparedStatement.setBoolean(1, isAdmin);
			preparedStatement.setLong(2, upgradeChatId(chat.id));

			updatedRows = preparedStatement.executeUpdate();
        } catch (e: SQLException) {
			e.printStackTrace();
		}

		if(updatedRows > 0) {
			connection.commitTransaction();
			return true;
		}

		return false;
	}

	fun updateChatId(oldId: Long, newId: Long): Boolean {
		var updatedRows = 0;
		try {
			val preparedStatement: PreparedStatement = connection.getPreparedStatement("UPDATE Chats SET chatId = ?, oldChatId = ? WHERE chatId = ?");
			preparedStatement.setLong(1, newId);
			preparedStatement.setLong(2, oldId);
			preparedStatement.setLong(3, oldId);

			updatedRows = preparedStatement.executeUpdate();
		} catch(e: SQLException){
			e.printStackTrace();
		}

		if(updatedRows > 0){
			connection.commitTransaction();
			return true;
		}

		return false;
	}

    fun setLanguageCode(chat: Chat, languageCode: String = "en"): Boolean {
		var updatedRows = 0;
        try {
			val preparedStatement: PreparedStatement = connection.getPreparedStatement("UPDATE Chats SET languageCode = ? WHERE chatId = ?");
			preparedStatement.setString(1, languageCode);
			preparedStatement.setLong(2, upgradeChatId(chat.id));

			updatedRows = preparedStatement.executeUpdate();
        } catch (e: SQLException) {
			e.printStackTrace();
		}

		if(updatedRows > 0) {
			connection.commitTransaction();
			return true;
		}

		return false;
	}

	fun setRegistrationMessage(chat: Chat, message: Message): Boolean {
		var updatedRows = 0;
		try {
			val preparedStatement: PreparedStatement = connection.getPreparedStatement("UPDATE Chats SET registrationMessage = ? WHERE chatId = ?");
			preparedStatement.setInt(1, message.messageId);
			preparedStatement.setLong(2, upgradeChatId(chat.id));

			updatedRows = preparedStatement.executeUpdate();
        } catch (e: SQLException) {
			e.printStackTrace();
		}

		if(updatedRows > 0) {
			connection.commitTransaction();
			return true;
		}

		return false;
	}

	fun clearRegistrationMessage(chat: Chat): Boolean {
		var updatedRows = 0;
        try {
			val preparedStatement: PreparedStatement = connection.getPreparedStatement("UPDATE Chats SET registrationMessage = NULL WHERE chatId = ?");
			preparedStatement.setLong(1, upgradeChatId(chat.id));

			updatedRows = preparedStatement.executeUpdate();
        } catch (e: SQLException) {
			e.printStackTrace();
		}

		if(updatedRows > 0) {
			connection.commitTransaction();
			return true;
		}

		return false;
	}

	fun containsChat(chat: Chat): Boolean {
		var result = false;
		try {
			val preparedStatement = connection.getPreparedStatement("SELECT * FROM Chats WHERE chatId=?");
			preparedStatement.setLong(1, chat.id);
			val res = preparedStatement.executeQuery();
			result = res.next();
			res.close();
        } catch (e: SQLException) {
			e.printStackTrace();
		}

		return result;
    }

	fun upgradeChatId(chatId: Long): Long {
		var result = chatId;
		try {
			val preparedStatement = connection.getPreparedStatement("SELECT chatId FROM Chats WHERE oldChatId=?");
			preparedStatement.setLong(1, chatId);
			val res = preparedStatement.executeQuery();
			if(res.next()) result = res.getLong("chatId") ?: chatId;
			res.close();
        } catch (e: SQLException) {
			e.printStackTrace();
		}

		//println("chatId: $chatId upgraded to $result");
		return result;
	}
}
