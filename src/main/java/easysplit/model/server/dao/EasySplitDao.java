package easysplit.model.server.dao;

import java.util.List;
import java.util.Set;

import easysplit.model.pojo.ExpenseItem;
import easysplit.model.pojo.Payment;

public interface EasySplitDao {

  //User table//
  //--Check sign up user--
  boolean isUserExist(String userName);

  //--add user to table--
  String signUp(String userName, String password);

  //--Check login username and password--
  String login(String userName, String password);

  //Team table//
  boolean isTeamExist(String teamName);

  String createTeam(String userName, String teamName, Set<String> members);

  Set<String> getTeamSet(String userName);

  Set<String> getMemberSet(String teamName);

  String addExpense(String userName, String teamName, ExpenseItem expenseItem);

  List<ExpenseItem> showHistory(String teamName);

  List<Payment> showSnapshot(String userName, String teamName);

  String makePayment(String userName, String teamName, Payment payment);

  String confirmPayment(String userName, String teamName, Payment payment);

  List<Payment> getPaymentHistory(String userName, String teamName);
}
