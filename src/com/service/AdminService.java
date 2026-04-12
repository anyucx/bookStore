package com.service;

import java.sql.SQLException;

import com.dao.AdminDao;
import com.entity.Admin;

public class AdminService {
	private AdminDao adminDao = new AdminDao();
	
	/**
	 * µÇÂ¼¹¦ÄÜ
	 * @param admin
	 * @return
	 */
	public Admin login(Admin admin) {
		try {
			return adminDao.find(admin.getAdminname(), admin.getAdminpwd());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
