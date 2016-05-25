package me.thrusmyster.tevobungee.SQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import me.thrusmyster.tevobungee.TevoBungee;

public class SQLManager extends SQLOperations implements Runnable{

	TevoBungee main = TevoBungee.getInstance();
	
	private Connection connection;
	
	private String host = "jdbc:mysql://sql.tevonetwork.com:3306/";
	private String database;
	private String username = "tevoserv_Server";
	private String password = "INM(UQfo*9cA";
	
	public void refreshConnection()
	{
		try
		{
			if ((this.connection == null) || (this.connection.isClosed()))
			{
				initialize(this.database);
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void closeConnection()
	{
		try
		{
			this.connection.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	
	public boolean initialize(String database) {
		try
		{
			this.database = database;
			Class.forName("com.mysql.jdbc.Driver");
			this.connection = DriverManager.getConnection(this.host + this.database + "?autoReconnect=true&failOverReadOnly=false&maxReconnects=10", this.username, this.password);
			return true;
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
			return false;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * Any query which does not return a ResultSet object. Such as : INSERT,
	 * UPDATE, CREATE TABLE...
	 * 
	 * @param query
	 */
	public void standardQuery(String query) throws SQLException {
		this.refreshConnection();
		super.standardQuery(query, this.connection);
	}

	/**
	 * Check whether a field/entry exists in a database.
	 * @param query
	 * @return Whether or not a result has been found in the query.
	 * @throws java.sql.SQLException
	 */
	public boolean existanceQuery(String query) throws SQLException {
		this.refreshConnection();
		return super.sqlQuery(query, this.connection).next();
	}

	/**
	 * Any query which returns a ResultSet object. Such as : SELECT Remember to
	 * close the ResultSet object after you are done with it to free up
	 * resources immediately. ----- ResultSet set =
	 * sqlQuery("SELECT * FROM sometable;"); set.doSomething(); set.close();
	 * -----
	 * 
	 * @param query
	 * @return ResultSet
	 */
	public ResultSet sqlQuery(String query) throws SQLException{
		this.refreshConnection();
		return super.sqlQuery(query, this.connection);
	}

	/**
	 * Check whether the table name exists.
	 * 
	 * @param table
	 * @return
	 */
	public boolean doesTableExist(String table) throws SQLException{
		this.refreshConnection();
		return super.checkTable(table, this.connection);
	}

	@Override
	public void run() 
	{
		try
		{
			existanceQuery("SELECT UUID FROM OPS LIMIT 0, 1;");
		}
		catch(SQLException e)
		{
			main.getUtilLogger().warning("SQL> Failed to keep connection non-stale!");
			e.printStackTrace();
		}
		
	}
	
	
}
