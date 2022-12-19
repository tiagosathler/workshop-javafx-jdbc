package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import db.DB;
import db.DbException;
import model.dao.DepartmentDao;
import model.entities.Department;

public class DepartmentDaoJDBC implements DepartmentDao {
	private Connection conn;
	private PreparedStatement ps;
	private ResultSet rs;

	private static final String DB_EXCEPTION_MSG = "Something was wrong: ";
	private static final String UNEXPECTED_ERROR = "Unexpected error! No rows affected!";

	public DepartmentDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Department department) {
		try {
			ps = conn.prepareStatement(""
					+ "INSERT INTO department "
					+ "(Name) "
					+ "VALUES "
					+ "(?);",
					Statement.RETURN_GENERATED_KEYS);

			ps.setString(1, department.getName());

			int affectedRows = ps.executeUpdate();

			if (affectedRows > 0) {
				rs = ps.getGeneratedKeys();
				if (rs.next()) {
					int id = rs.getInt(1);
					department.setId(id);
				}
				DB.closeResultSet(rs);
			} else {
				throw new DbException(UNEXPECTED_ERROR);
			}

		} catch (SQLException e) {
			throw new DbException(DB_EXCEPTION_MSG + e.getMessage());
		} finally {
			DB.closePreparedStatment(ps);
		}
	}

	@Override
	public Department findById(Integer id) {
		try {
			ps = conn.prepareStatement("SELECT * FROM department WHERE Id = ?;");

			ps.setInt(1, id);

			rs = ps.executeQuery();

			if (rs.next()) {
				return instantiateDepartment(rs);
			}

			return null;

		} catch (SQLException e) {
			throw new DbException(DB_EXCEPTION_MSG + e.getMessage());
		} finally {
			DB.closePreparedStatment(ps);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public List<Department> findAll() {
		try {
			ps = conn.prepareStatement("SELECT * FROM department ORDER BY name;");

			rs = ps.executeQuery();

			List<Department> departments = new ArrayList<>();

			while (rs.next()) {
				departments.add(instantiateDepartment(rs));
			}

			return departments;

		} catch (SQLException e) {
			throw new DbException(DB_EXCEPTION_MSG + e.getMessage());
		} finally {
			DB.closePreparedStatment(ps);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public void update(Department department) {
		try {
			ps = conn.prepareStatement(""
					+ "UPDATE department "
					+ "SET Name = ? "
					+ "WHERE Id = ?;");

			ps.setString(1, department.getName());
			ps.setInt(2, department.getId());

			int affectedRows = ps.executeUpdate();

			if (affectedRows == 0) {
				throw new DbException(UNEXPECTED_ERROR);
			}

		} catch (SQLException e) {
			throw new DbException(DB_EXCEPTION_MSG + e.getMessage());
		} finally {
			DB.closePreparedStatment(ps);
		}
	}

	@Override
	public void deleteById(Integer id) {
		try {
			ps = conn.prepareStatement("DELETE FROM department WHERE Id = ?;");

			ps.setInt(1, id);

			ps.executeUpdate();

		} catch (SQLException e) {
			throw new DbException(DB_EXCEPTION_MSG + e.getMessage());
		} finally {
			DB.closePreparedStatment(ps);
		}
	}

	private Department instantiateDepartment(ResultSet rs) throws SQLException {
		Department department = new Department();
		department.setId(rs.getInt("Id"));
		department.setName(rs.getString("Name"));
		return department;
	}

}
