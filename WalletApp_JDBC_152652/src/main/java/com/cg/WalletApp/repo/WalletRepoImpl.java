package com.cg.WalletApp.repo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.cg.WalletApp.exception.WalletException;
import com.cg.WalletApp.exception.IWalletException;
import com.cg.WalletApp.beans.Customer;
import com.cg.WalletApplication.dbutil.DButil;


public class WalletRepoImpl implements IWalletRepo{
	Connection con=null;
	public WalletRepoImpl() {
		con=DButil.getConnection();
	}



public String addCustomer(Customer customer) {
	String result=null; int i=0;
	try {
		String sql="insert into customer_wallet values (?,?,?,?,?)";
		PreparedStatement ptst=con.prepareStatement(sql);
		ptst.setString(1, customer.getMobileNumber());
		ptst.setString(2,customer.getEmailId() );
		ptst.setString(3, customer.getName() );
		ptst.setString(4,customer.getPassword());
		ptst.setBigDecimal(5, customer.getWallet().getBalance());
		 i=ptst.executeUpdate();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	if(i==1)
		result=customer.getMobileNumber();
	return result;	
}

public Customer showBalance(String mobileNum, String password) throws SQLException {
	Customer result=null; 
		String sql="select * from customer_wallet where mobile_no = '"+mobileNum +"'";
		PreparedStatement ptst=con.prepareStatement(sql);
		ResultSet rs=ptst.executeQuery();
		if(rs.next()) {
		String actualPassword= rs.getString("CUSTOMER_PASSWORD");
		if(actualPassword.equals(password))
		{
		
	            result = new Customer();
				result.getWallet().setBalance(rs.getBigDecimal("balance"));
			
		}
	 
		}
	return result;	
	
	
}
public Customer findCustomer(String mobileNum, String password) throws SQLException {
	
	Customer customer = null;

		String sql="select * from customer_wallet where mobile_no = '"+mobileNum +"'";
		PreparedStatement ptst=con.prepareStatement(sql);
		ResultSet rs=ptst.executeQuery();
		if(rs.next()) {
		String actualPassword= rs.getString("CUSTOMER_PASSWORD");
		if(actualPassword.equals(password))
		{
			customer = new Customer();
			customer.setMobileNumber(rs.getString("MOBILE_NO"));
			customer.setName(rs.getString("CUSTOMER_NAME"));
			customer.setEmailId(rs.getString("EMAIL_ID"));
			customer.setPassword(rs.getString("CUSTOMER_PASSWORD"));
			customer.getWallet().setBalance(rs.getBigDecimal("BALANCE"));
		}
		}
 
	return customer;
}
public void deposit(Customer customer, BigDecimal amount) throws SQLException, ClassNotFoundException, WalletException {
	String sql="select * from customer_wallet where mobile_no = '"+customer.getMobileNumber() +"'";
	PreparedStatement ptst=con.prepareStatement(sql);
	ResultSet rs=ptst.executeQuery();
    BigDecimal existingBal = BigDecimal.valueOf(0.0);
    if(rs.next())
    	existingBal=rs.getBigDecimal("BALANCE");
	BigDecimal newBal = existingBal.add(amount);
	String updateQuery = "update customer_wallet set balance='"+newBal+"' where mobile_no = '"+customer.getMobileNumber() +"'";
    ptst=con.prepareStatement(updateQuery);
    int i=ptst.executeUpdate();
    if(i==1) {
    String query = "Insert Into Transactions VALUES (?,?,?,?,?)";
    PreparedStatement pstmt= con.prepareStatement(query);
    pstmt.setInt(1, getTransactionId());
    pstmt.setString(2,customer.getMobileNumber());
    java.util.Date today = new java.util.Date();
    pstmt.setTimestamp(3,new java.sql.Timestamp(today.getTime()) );
    pstmt.setString(4, "Credited");
    pstmt.setBigDecimal(5, amount);
    pstmt.executeUpdate();
    }
    else
    {
    	throw new WalletException(IWalletException.transFailed);
    }
	
}
private int getTransactionId() throws ClassNotFoundException, WalletException {
	// TODO Auto-generated method stub
	int empId = 0;
	String sql = "SELECT transaction_sequence.NEXTVAL FROM DUAL";
	try {
		Class.forName("oracle.jdbc.driver.OracleDriver");
		Connection con=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe","system","Capgemini123");
		PreparedStatement pstmt = con.prepareStatement(sql);
		ResultSet res = pstmt.executeQuery();
		if (res.next()) {
			empId = res.getInt(1);
		}
	} catch (SQLException e) {
         throw new WalletException(IWalletException.sqlException);
	}
	return empId;
}
public boolean withdraw(Customer customer, BigDecimal amount) throws ClassNotFoundException, SQLException, WalletException {
	boolean result=false;
	String sql="select * from customer_wallet where mobile_no = '"+customer.getMobileNumber() +"'";
	PreparedStatement ptst=con.prepareStatement(sql);
	ResultSet rs=ptst.executeQuery();
	if(rs.next()) {
	if(rs.getBigDecimal("BALANCE").subtract(amount).compareTo(BigDecimal.valueOf(0.0))>=0)
	{
		BigDecimal existingBal= rs.getBigDecimal("BALANCE");
		BigDecimal newBal = existingBal.subtract(amount);
		String updateQuery = "update customer_wallet set balance='"+newBal+"' where mobile_no = '"+customer.getMobileNumber() +"'";
	    ptst=con.prepareStatement(updateQuery);
	    int i=ptst.executeUpdate();
	    customer.getWallet().setBalance(newBal);
    String query = "Insert Into Transactions VALUES (?,?,?,?,?)";
    PreparedStatement pstmt= con.prepareStatement(query);
    pstmt.setInt(1, getTransactionId());
    pstmt.setString(2,customer.getMobileNumber());
    java.util.Date today = new java.util.Date();
    pstmt.setTimestamp(3,new java.sql.Timestamp(today.getTime()) );
    pstmt.setString(4, "Debited");
    pstmt.setBigDecimal(5, amount);
    int j=pstmt.executeUpdate();
    if(j==1)
    	result=true;
	}
	else
		throw new WalletException(IWalletException.insufficientFunds);
	}
	return result;
}
public boolean customerExists(String receiverMobile) throws SQLException {
	boolean result= false;
	Customer customer = null;
	String sql="select * from customer_wallet where mobile_no = '"+receiverMobile +"'";
	PreparedStatement ptst=con.prepareStatement(sql);
	ResultSet rs=ptst.executeQuery();
	if(rs.next())
	{
		customer = new Customer();
		customer.setMobileNumber(rs.getString("MOBILE_NO"));
		customer.setName(rs.getString("CUSTOMER_NAME"));
		customer.setEmailId(rs.getString("EMAIL_ID"));
		customer.setPassword(rs.getString("CUSTOMER_PASSWORD"));
		customer.getWallet().setBalance(rs.getBigDecimal("BALANCE"));
	}
	if(customer!=null)
		result=true;
	return result;
}
public Customer transfer(String senderMobile, String receiverMobile, BigDecimal amount) throws ClassNotFoundException, SQLException, WalletException {
	boolean result=false;
	String sqls="select * from customer_wallet where mobile_no = '"+senderMobile +"'";
	String sqlr="select * from customer_wallet where mobile_no = '"+receiverMobile +"'";
	PreparedStatement ptst=con.prepareStatement(sqls);
	ResultSet rs=ptst.executeQuery();
	Customer senderCustomer = null;
	if(rs.next()) {
		senderCustomer = new Customer();
		senderCustomer.setMobileNumber(rs.getString("MOBILE_NO"));
		senderCustomer.setName(rs.getString("CUSTOMER_NAME"));
		senderCustomer.setEmailId(rs.getString("EMAIL_ID"));
		senderCustomer.setPassword(rs.getString("CUSTOMER_PASSWORD"));
		senderCustomer.getWallet().setBalance(rs.getBigDecimal("BALANCE"));
	}
	ptst = con.prepareStatement(sqlr);
	rs=ptst.executeQuery();
	Customer receiverCustomer = null;
	if(rs.next()) {
		receiverCustomer = new Customer();
		receiverCustomer.setMobileNumber(rs.getString("MOBILE_NO"));
		receiverCustomer.setName(rs.getString("CUSTOMER_NAME"));
		receiverCustomer.setEmailId(rs.getString("EMAIL_ID"));
		receiverCustomer.setPassword(rs.getString("CUSTOMER_PASSWORD"));
		receiverCustomer.getWallet().setBalance(rs.getBigDecimal("BALANCE"));
	}
	
	if(withdraw(senderCustomer, amount))
	{

		deposit(receiverCustomer, amount);
		result=true;
	}
	return senderCustomer;
}
public String printTransactions(Customer customer) throws ClassNotFoundException, SQLException {
    String query = "Select * from transactions where Mobile_no = '"+customer.getMobileNumber()+"' order by id";
    PreparedStatement pstmt= con.prepareStatement(query);
    ResultSet resultSet= pstmt.executeQuery();
    StringBuilder builder=new StringBuilder();
    while(resultSet.next())
    {
    	builder.append(resultSet.getTimestamp("TIMESTAMPOFTRANS") + " " + resultSet.getString("TYPE")+ " " + resultSet.getBigDecimal("AMOUNT"));
    	builder.append(",");
    }
	return builder.toString();
}


}
