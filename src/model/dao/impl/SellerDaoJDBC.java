package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import db.DB;
import db.DbException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

public class SellerDaoJDBC implements SellerDao {
	private Connection conn;
	private PreparedStatement ps;
	private ResultSet rs;
	
	private static final String DB_EXCEPTION_MSG = "Something was wrong: ";
	private static final String UNEXPECTED_ERROR = "Unexpected error! No rows affected!";

	public SellerDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Seller seller) {
		try {
			ps = conn.prepareStatement(""
					+ "INSERT INTO seller "
					+ "(Name, Email, BirthDate, BaseSalary, DepartmentId) "
					+ "VALUES "
					+ "(?, ?, ?, ?, ?);",
					Statement.RETURN_GENERATED_KEYS);

			ps.setString(1, seller.getName());
			ps.setString(2, seller.getEmail());
			ps.setDate(3, new java.sql.Date(seller.getBirthDate().getTime()));
			ps.setDouble(4, seller.getBaseSalary());
			ps.setInt(5, seller.getDepartment().getId());

			int affectedRows = ps.executeUpdate();

			if (affectedRows > 0) {
				rs = ps.getGeneratedKeys();
				if (rs.next()) {
					int id = rs.getInt(1);
					seller.setId(id);
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
	public Seller findById(Integer id) {
		try {
			ps = conn.prepareStatement(""
					+ "SELECT s.*, d.Name as DepName "
					+ "FROM seller AS s "
					+ "INNER JOIN department AS d "
					+ "ON s.DepartmentId = d.Id "
					+ "WHERE s.id = ?;");

			ps.setInt(1, id);

			rs = ps.executeQuery();

			if (rs.next()) {
				Department department = instantiateDepartment(rs);
				return instantiateSeller(rs, department);
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
	public List<Seller> findAll() {
		try {
			ps = conn.prepareStatement(""
					+ "SELECT s.*, d.Name AS DepName "
					+ "FROM seller AS s "
					+ "INNER JOIN department as d "
					+ "ON s.DepartmentId = d.Id "
					+ "ORDER BY Name;");

			rs = ps.executeQuery();

			List<Seller> sellers = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();

			while (rs.next()) {
				Integer departmentId = rs.getInt("DepartmentId");
				Department department = map.get(departmentId);

				if (department == null) {
					department = instantiateDepartment(rs);
					map.put(departmentId, department);
				}

				sellers.add(instantiateSeller(rs, department));
			}

			return sellers;

		} catch (SQLException e) {
			throw new DbException(DB_EXCEPTION_MSG + e.getMessage());
		} finally {
			DB.closePreparedStatment(ps);
			DB.closeResultSet(rs);
		}
	}

	@Override
	public void update(Seller seller) {
		try {
			ps = conn.prepareStatement(""
					+ "UPDATE seller "
					+ "SET "
					+ "Name = ?, "
					+ "Email = ?, "
					+ "BirthDate = ?, "
					+ "BaseSalary = ?, "
					+ "DepartmentId = ? "
					+ "WHERE Id = ?;");

			ps.setString(1, seller.getName());
			ps.setString(2, seller.getEmail());
			ps.setDate(3, new java.sql.Date(seller.getBirthDate().getTime()));
			ps.setDouble(4, seller.getBaseSalary());
			ps.setInt(5, seller.getDepartment().getId());
			ps.setInt(6, seller.getId());

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
			ps = conn.prepareStatement("DELETE FROM seller WHERE Id = ?;");

			ps.setInt(1, id);

			ps.executeUpdate();
		} catch (SQLException e) {
			throw new DbException(DB_EXCEPTION_MSG + e.getMessage());
		} finally {
			DB.closePreparedStatment(ps);
		}
	}

	public List<Seller> findByDepartment(Department department) {
		try {
			ps = conn.prepareStatement(""
					+ "SELECT s.*, d.Name as DepName "
					+ "FROM seller As s "
					+ "INNER JOIN department As d "
					+ "ON s.DepartmentId = d.Id "
					+ "WHERE s.DepartmentId = ? "
					+ "ORDER BY Name;");

			ps.setInt(1, department.getId());

			rs = ps.executeQuery();

			List<Seller> sellers = new ArrayList<>();
			Department foundDepartment = null;

			if (rs.next()) {
				foundDepartment = instantiateDepartment(rs);
				sellers.add(instantiateSeller(rs, foundDepartment));
			} else {
				return sellers;
			}

			while (rs.next()) {
				sellers.add(instantiateSeller(rs, foundDepartment));
			}

			return sellers;

		} catch (SQLException e) {
			throw new DbException(DB_EXCEPTION_MSG + e.getMessage());
		}
	}

	private Department instantiateDepartment(ResultSet rs) throws SQLException {
		Department department = new Department();
		department.setId(rs.getInt("DepartmentId"));
		department.setName(rs.getString("DepName"));
		return department;
	}

	private Seller instantiateSeller(ResultSet rs, Department department) throws SQLException {
		Seller seller = new Seller();
		seller.setId(rs.getInt("Id"));
		seller.setName(rs.getString("Name"));
		seller.setEmail(rs.getString("Email"));
		seller.setBaseSalary(rs.getDouble("BaseSalary"));
		seller.setBirthDate(rs.getDate("BirthDate"));
		seller.setDepartment(department);
		return seller;
	}
}
