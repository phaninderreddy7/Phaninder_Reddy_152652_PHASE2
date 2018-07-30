package com.cg.WalletApp.repo;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.TreeMap;

import com.cg.WalletApp.beans.Customer;
import com.cg.WalletApp.exception.WalletException;

public interface IWalletRepo {


	String addCustomer(Customer customer);

	Customer showBalance(String mobileNum, String password) throws SQLException;

	Customer findCustomer(String mobileNum, String password) throws SQLException;

	void deposit(Customer customer, BigDecimal amount) throws SQLException, ClassNotFoundException, WalletException;

	boolean withdraw(Customer customer, BigDecimal amount) throws ClassNotFoundException, SQLException, WalletException;

	boolean customerExists(String receiverMobile) throws SQLException;

	Customer transfer(String senderMobile, String receiverMobile, BigDecimal amount) throws ClassNotFoundException, SQLException, WalletException;

	String printTransactions(Customer customer) throws ClassNotFoundException, SQLException;

	

}
