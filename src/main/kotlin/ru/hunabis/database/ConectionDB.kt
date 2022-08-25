import java.sql.*;
import java.lang.Class;
import java.lang.System;
import java.net.URISyntaxException;
import java.net.URI;

class ConectionDB {
	val currentConection: Connection;

    init {
        this.currentConection = openConexion();
    } 

	@Throws(URISyntaxException::class, SQLException::class)
	fun openConexion(): Connection {
		val dbUri = URI(System.getenv("DATABASE_URL"));

		val username = dbUri.getUserInfo().split(":")[0];
		val password = dbUri.getUserInfo().split(":")[1];
	    val dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() + dbUri.getPath();
		return DriverManager.getConnection(dbUrl, username, password);
	}

    /*fun openConexion(): Connection? {
        var connection: Connection? = null;
        try {
            Class.forName("").newInstance();
            connection = DriverManager.getConnection("", "", "");
        } catch (e: Exception) {
            e.printStackTrace();
        }

        return connection;
    }*/

    fun closeConexion() {
        try {
            this.currentConection.close();
        } catch (e: SQLException) {
            e.printStackTrace();
        }
    }

	@Throws(SQLException::class)
    fun runSqlQuery(query: String): ResultSet {
        val statement = this.currentConection.createStatement();
        return statement.executeQuery(query);
    }

	@Throws(SQLException::class)
    fun executeQuery(query: String): Boolean{
        val statement = this.currentConection.createStatement();
        return statement.execute(query);
    }

	@Throws(SQLException::class)
    fun getPreparedStatement(query: String): PreparedStatement {
        return this.currentConection.prepareStatement(query);
    }

	@Throws(SQLException::class)
    fun getPreparedStatement(query: String, flags: Int): PreparedStatement {
        return this.currentConection.prepareStatement(query, flags);
    }

    fun checkVersion(): Int {
        var max = 0;
        try {
            val metaData = this.currentConection.getMetaData();
            val res = metaData.getTables(null, null, "", arrayOf("TABLE"));
            while (res.next()) {
                if (res.getString("TABLE_NAME").compareTo("Versions") == 0) {
                    val result = runSqlQuery("SELECT Max(Version) FROM Versions");
                    while (result.next()) {
                        max = if(max > result.getInt(1)) max else result.getInt(1);
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace();
        }
        return max;
    }

	@Throws(SQLException::class)
    fun initTransaction() {
        this.currentConection.setAutoCommit(false);
    }

	@Throws(SQLException::class)
    fun commitTransaction() {
        try {
            this.currentConection.commit();
        } catch (e: SQLException) {
            if (this.currentConection != null) {
                this.currentConection.rollback();
            }
        } finally {
            this.currentConection.setAutoCommit(false);
        }
    }
}
