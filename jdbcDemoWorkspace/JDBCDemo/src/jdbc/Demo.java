package jdbc;

public class Demo {

	public static void main(String[] args) {
		JDBCImpl.getInstance().init("db_user","emp");
	/*	
		ContentValues values = new ContentValues();
		values.put("sid", "S_1017");
		values.put("sname", "pansihua");
		values.put("age", "25");
		values.put("gender", "female");
		
		boolean isSuccess = JDBCImpl.getInstance().insert(values);
		if(isSuccess) 
			System.out.println("插入记录成功！");
		else
			System.out.println("插入记录失败！");*/
	/*	
		ContentValues values = new ContentValues(); 
		values.put("age", "120");
		
		ContentValues condition = new ContentValues(); 
		condition.put("sname", "zhaoLiu");
		
		if(JDBCImpl.getInstance().update(values, condition))
			System.out.println("修改记录成功");
		else
			System.out.println("修改记录失败");*/
/*		
		
		ContentValues values = new ContentValues(); 
		values.put("sname", "HHZZOO");
		values.put("age", "25");
		values.put("gender", "male");
		
		JDBCImpl.getInstance().delete(values);*/
		
		System.out.println(JDBCImpl.getInstance().queryAll());
	}
}
