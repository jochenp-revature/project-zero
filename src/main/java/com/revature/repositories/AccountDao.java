package com.revature.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import com.revature.models.Account;
import com.revature.util.ConnectionUtil;

public class AccountDao implements IAccountDao {

	private static Logger log = Logger.getLogger(AccountDao.class);

	@Override
	public int insert(Account a) {
		try {
			Connection conn = ConnectionUtil.getConnection();
			String sql = "INSERT INTO accounts (id, balance) VALUES (?, ?) RETURNING id;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, a.getId());
			stmt.setDouble(2, a.getBalance());
			ResultSet rs;
			if ((rs = stmt.executeQuery()) != null) {
				rs.next();
				int id = rs.getInt("id");
				log.info("Successfully inserted account with id: " + id);
				return id;
			}
		} catch (SQLException e) {
			log.error("Unable to create account.");
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public List<Account> findAll() { // here we have to make an actual query
		List<Account> accList = new ArrayList<Account>();
		try (Connection conn = ConnectionUtil.getConnection()) {
			Statement stmt = conn.createStatement();
			String sql = "SELECT * FROM jochenp.accounts";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int id = rs.getInt("id");
				double balance = rs.getDouble("balance");
				Account a = new Account(id, balance);
				accList.add(a);
			}
		} catch (SQLException e) {
			log.warn("A SQL Exception occurred when querying all accounts");
			e.printStackTrace();
		}
		return accList;
	}

	@Override
	public Account findById(int id) {
		Account a = new Account();
		try (Connection conn = ConnectionUtil.getConnection()) {
			String sql = "SELECT FROM accounts WHERE id = ?;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			int returnedId = rs.getInt("id");
			double balance = rs.getDouble("balance");
			a.setId(returnedId);
			a.setBalance(balance);
		} catch (SQLException e) {
			log.warn("Failed to retrieve account with id " + id);
			e.printStackTrace();
		}
		return a;
	}

	@Override
	public List<Account> findByOwner(int userId) {
		List<Account> ownedAccounts = new ArrayList<Account>();
		try (Connection conn = ConnectionUtil.getConnection()) {
			String sql = "SELECT jochenp.accounts.id, jochenp.accounts.balance FROM jochenp.accounts\r\n"
					+ "	INNER JOIN jochenp.users_accounts_jt \r\n"
					+ "		ON jochenp.accounts.id = jochenp.users_accounts_jt.account	\r\n"
					+ "			WHERE jochenp.users_accounts_jt.acc_owner = ?;"; // this will have to be a joins
																					// statement
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userId); // how do we set the ?
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("id");
				double balance = rs.getDouble("balance");
				Account a = new Account(id, balance);
				// in the case that there are duplicates, DON'T add them to the arraylist
				if (!ownedAccounts.contains(a)) {
					ownedAccounts.add(a);
				}
			}
		} catch (SQLException e) {
			log.error("Failed to retrieve all accounts owned by user with id " + userId);
			e.printStackTrace();
		}
		return ownedAccounts;
	}

	public double getBalance(int accId) {
		double balance = 0;
		try (Connection conn = ConnectionUtil.getConnection()) {
			String sql = "SELECT balance FROM jochenp.accounts WHERE id = ?;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, accId);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			balance = rs.getDouble("balance");
		} catch (SQLException e) {
			log.warn("Failed to retrieve balance of account id " + accId);
			e.printStackTrace();
		}
		return balance;
	}

	@Override
	public void updateBalance(int accId, double balance) {
		try (Connection conn = ConnectionUtil.getConnection()) {
			String sql = "UPDATE accounts SET balance = ? WHERE id = ? RETURNING balance;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setDouble(1, balance);
			stmt.setInt(2, accId);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			if (rs.getDouble("balance") == balance) {
				log.info("Successfully updated balance of account " + accId + " to " + balance);
			}
		} catch (SQLException e) {
			log.error("Failed to update account with id " + accId);
			e.printStackTrace();
		}
	}

	@Override
	public boolean delete(int id) {
		try (Connection conn = ConnectionUtil.getConnection()) {
			String sql = "DELETE FROM accounts WHERE id = ?;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			stmt.executeQuery();
			log.info("Successfully deleted account " + id);
		} catch (SQLException e) {
			log.error("Failed to delete account with id " + id);
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
