package com.mamba.framework.context.date.provider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class DatabaseSystemDateProvider implements SystemDateProvider {
	@Autowired
	private DataSource dataSource;

	@Override
	public final Date now() {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Timestamp timestamp = null;
		try {
			connection = dataSource.getConnection();
			preparedStatement = connection.prepareStatement(getNowDateSQL());
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				timestamp = resultSet.getTimestamp("NOW");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != resultSet) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (null != preparedStatement) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		Date now = null;
		if (null != timestamp) {
			now = new Date(timestamp.getTime());
		} else {
			now = new Date();
		}
		return now;
	}

	protected abstract String getNowDateSQL();
}
