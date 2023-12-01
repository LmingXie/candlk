package com.candlk.common.gencode.tool;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBGen {

	public static void main(String[] args) {
		getcolumn("dt_user");
	}

	public static void getcolumn(String tableName) {
		final ResourceBundle rb = ResourceBundle.getBundle("Mybatis-Plus");
		Connection conn = null;
		try {
			Class.forName(rb.getString("driverClassName")).newInstance();
			conn = java.sql.DriverManager.getConnection(
					rb.getString("url"),
					rb.getString("userName"),
					rb.getString("password"));
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		// 1、获取数据库所有表
		StringBuilder sbTables = new StringBuilder();
		sbTables.append("-------------- 数据库中有下列的表 ----------<br/>");
		// 2、遍历数据库表，获取各表的字段等信息
		try {
			final String sql = "select * from " + tableName + " limit 1";
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();
			int columeCount = meta.getColumnCount();
			System.out.println("表 " + tableName + "共有 " + columeCount + " 个字段。字段信息如下：");
			int outCount = 7;
			Map<Integer, StringBuilder> sbMap = new LinkedHashMap<>(outCount);
			for (int i = 1; i < columeCount + 1; i++) {
				for (int j = 0; j < outCount; j++) {
					StringBuilder sb = sbMap.computeIfAbsent(j, StringBuilder::new);
					switch (j) {
						case 0: // js_user_sign_config.id,js_user_sign_config.sign_day
							// sbCloumns.append("字段名："+meta.getColumnName(i)+"<br/>");
							sb.append(tableName).append(".").append(meta.getColumnName(i)).append(",");
							// sbCloumns.append("r."+meta.getColumnName(i)+",");
							// sbCloumns.append("#{"+meta.getColumnName(i)+"},");
							break;
						case 1: // d,sign_day,gift_id,gift_name,gift_num
							sb.append(meta.getColumnName(i)).append(",");
							break;
						case 2: // #{id},#{signDay},#{giftId},#{giftName}
							sb.append("#{").append(lineToHump(meta.getColumnName(i))).append("},");
							break;
						case 3: // id=#{id},sign_day=#{signDay},gift_id=#{giftId}
							sb.append(meta.getColumnName(i)).append("=#{").append(lineToHump(meta.getColumnName(i))).append("},");
							break;
						case 4: // id AS id ,sign_day AS signDay ,gift_id AS giftId
							sb.append(meta.getColumnName(i)).append(" AS ").append(lineToHump(meta.getColumnName(i))).append(" ,");
							break;
						case 5: // js_user_sign_config.id AS id ,js_user_sign_config.sign_day AS signDay
							sb.append(tableName).append(".").append(meta.getColumnName(i)).append(" AS ").append(lineToHump(meta.getColumnName(i))).append(" ,");
							break;
						case 6: // 'js_user_sign_config.id' ,'js_user_sign_config.sign_day' ,'js_user_sign_config.gift_id'
							sb.append("'").append(tableName).append(".").append(meta.getColumnName(i)).append("' ,");
							break;
					}
				}
			}
			for (StringBuilder value : sbMap.values()) {
				System.out.println(value.toString());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static Pattern linePattern = Pattern.compile("_(\\w)");

	public static String lineToHump(String str) {
		str = str.toLowerCase();
		Matcher matcher = linePattern.matcher(str);
		StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

}
