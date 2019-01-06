package jdbc;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JDBCImpl {
	
	private static final String USER = "user";
	private static final String PASSWORD = "password";
	private static final String URL = "url";
	
	private Connection mConn = null ;
	private String mDatabaseName = "";
	private String mTableName = "";
	
	private static final JDBCImpl sInstance = new JDBCImpl() ;
	private ResultSet mQuery;
	private Statement mStatement ;
	
	private boolean isInit = false ;
	
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch(Exception e) {
			e.printStackTrace(); 
		}
	}
	
	private JDBCImpl() {}
	
	public static JDBCImpl getInstance() {
		return sInstance ;
	}
	
	public synchronized void init(String dbName,String tableName) {
		
		if(isInit)
			return ;
		
		mDatabaseName = dbName; 
		mTableName = tableName ;
		Map<String, String> res = getConfig(); 
		try {
			mConn = DriverManager.getConnection(res.get(URL) + mDatabaseName ,
										res.get(USER),
										res.get(PASSWORD));
			isInit = true ;
		} catch (Exception e) {
			e.printStackTrace();
			isInit = false ;
		}
	}
	
	public synchronized void closeAll() {
		try{
			if(mQuery != null)
				mQuery.close() ;
			
			if(mStatement != null)
				mStatement.close();
			
			if(mConn != null) 
				mConn.close() ;
			
			mConn = null ;
			mQuery = null ;
			mStatement = null ;
		} catch (Exception e) {
			e.printStackTrace() ;
		}
	}
	
	/**
	 * 往数据库指定的表插入一条数据。
	 * @param values
	 * @return
	 */
	public synchronized boolean insert(ContentValues values) {
		StringBuilder sbKey = new StringBuilder() ;
		StringBuilder sbValues = new StringBuilder() ;
		
		Set<Entry<String,String>> entrySet = values.entrySet();
		Iterator<Entry<String, String>> iter = entrySet.iterator();
		while(iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			sbKey.append(entry.getKey() + ",");
			sbValues.append("'"+entry.getValue() + "',");
		}
		
		sbKey.delete(sbKey.length()-1, sbKey.length());
		sbValues.delete(sbValues.length()-1, sbValues.length());
		
		StringBuilder sb = new StringBuilder() ;
		sb.append("insert into ")
			.append(mTableName)
			.append("(")
			.append(sbKey)
			.append(")")
			.append(" values(")
			.append(sbValues)
			.append(");") ;
		
		String sql = sb.toString(); 
		System.out.println(sql);
		
		try {
			mStatement = mConn.createStatement();
			mStatement.execute(sql);
		} catch(Exception e) {
			e.printStackTrace();
			return false ;
		} finally {
			closeAll();			
		}
		return true ;
	}
	
	/**
	 * 更新数据库中指定的表中的某个记录。
	 * @param values 更新的值。
	 * @param condition 更新条件。
	 * @return
	 */
	public synchronized boolean update(ContentValues values,ContentValues condition) {
		
		StringBuilder sbKey = new StringBuilder() ;
		StringBuilder sbCondition = new StringBuilder() ;
		
		for(Entry<String, String> entry : values.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue() ;
			sbKey.append(key)
				.append("='")
				.append(value)
				.append("'")
				.append(",");
		}
		int index = sbKey.lastIndexOf(",");
		sbKey.delete(index, sbKey.length()) ;
		
		for(Entry<String, String> entry : condition.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue() ;
			sbCondition.append(key)
				.append("='")
				.append(value)
				.append("'")
				.append(" AND ");
		}
	
		index = sbCondition.lastIndexOf(" AND ");
		sbCondition.delete(index, sbCondition.length()) ;
		
		String sql = "update " + mTableName + " set " + sbKey + " where " + sbCondition + " ;";
		System.out.println(sql);
		try {
			mStatement = mConn.createStatement();
			mStatement.executeUpdate(sql);
		} catch(Exception e) {
			e.printStackTrace() ;
			return false ;
		} finally {
			closeAll();
		}
		return true ;
	}
	
	public synchronized boolean delete(ContentValues values) {

		StringBuilder sbCondition = new StringBuilder() ;
		
		for(Entry<String,String> entry : values.entrySet()) {
			String key = entry.getKey() ;
			String value = entry.getValue() ;
			
			sbCondition.append(key)
			.append("='")
			.append(value)
			.append("'")
			.append(" AND ");
		}
		
		int index = sbCondition.lastIndexOf(" AND ");
		sbCondition.delete(index, sbCondition.length()) ;
		
		String sql = "delete from " + mTableName + " where " + sbCondition + " ;";
		System.out.println(sql);
		
		try {
			mStatement = mConn.createStatement();
			mStatement.execute(sql);
		} catch(Exception e) {
			e.printStackTrace();
			return false; 
		} finally {
			closeAll();
		}
		return true ;
	}
	
	/**
	 * 查询数据库指定表的所有数据 ;
	 * @return
	 */
	public synchronized JSONObject queryAll() {

		JSONObject resObj = new JSONObject() ;
		JSONArray jsonArr = new JSONArray() ;
		
		String sql = "select * from " + mTableName +" ;";
		mQuery = null;
		try {
			mQuery = mConn.createStatement().executeQuery(sql);
			ResultSetMetaData metaData = mQuery.getMetaData();
			int column = metaData.getColumnCount(); // 总列数。
			int index = 0; 
			while(mQuery.next()) {
				JSONObject jsonObj = new JSONObject(); 
				for(int i = 1 ; i <= column ; i++) {
					String key = metaData.getColumnName(i); //字段名称
					String field = mQuery.getString(i);   //值
					jsonObj.put(key, field);
				}
				jsonArr.add(index++, jsonObj);
			}
			resObj.put("queryResult", jsonArr); 
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			closeAll();
		}
		
		return resObj ;
	}
	
	private Map<String,String> getConfig() {
		
		HashMap<String, String> map = new HashMap<String, String>() ;
		
		try{
			String basePath = Thread
								.currentThread()
								.getContextClassLoader()
								.getResource("")
								.getPath();      //bin目录。
			basePath = new File(basePath).getParent() ;
			
			Properties pro = new Properties();
			pro.load(new FileInputStream(
					new File(basePath,"/assets/config.properties")));
			
			map.put(USER, pro.getProperty(USER));
			map.put(PASSWORD, pro.getProperty(PASSWORD));
			map.put(URL, pro.getProperty(URL));
	
			return map ;
		} catch(Exception e) {
			e.printStackTrace() ;
		}
		return map ;
	}
}
