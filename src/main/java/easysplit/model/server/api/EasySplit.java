package easysplit.model.server.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import easysplit.model.server.paxos.Paxos;
import easysplit.model.pojo.ExpenseItem;
import easysplit.model.pojo.Payment;

public interface EasySplit extends Remote {

  String REGISTRY_NAME = "EasySplit";

  // paxos utils
  Paxos getPaxos() throws RemoteException;

  Paxos saveNewInstanceStub(int sourcePort, Paxos paxos) throws RemoteException;

  void sendNewInstanceAnnounce(int port, Paxos paxos) throws RemoteException;

  // check if stub alive
  boolean isAlive() throws RemoteException;

  // main page //
  //--------------------------------------------------------------------------//
  // show pop-up on return...next page if succeed
  String signUp(String userName, String password) throws RemoteException, InterruptedException;

  // show pop-up on return...next page if succeed
  String login(String userName, String password) throws RemoteException, InterruptedException;
  //--------------------------------------------------------------------------//

  // personal page //
  //--------------------------------------------------------------------------//
  // show team list in personal view
  Set<String> getTeamSet(String userName) throws RemoteException, InterruptedException;

  // for adding members
  boolean isUserExist(String memberName) throws RemoteException, InterruptedException;

  // for adding teams
  boolean isTeamExist(String teamName) throws RemoteException, InterruptedException;

  // show pop-up on duplicate team name...next page if succeed
  // member list including self
  String createTeam(String userName, String teamName, Set<String> members) throws RemoteException, InterruptedException;
  //--------------------------------------------------------------------------//

  // team page //
  //--------------------------------------------------------------------------//
  Set<String> getMemberSet(String teamName) throws RemoteException, InterruptedException;

  // expenseItem can not be negative -> update history table and snapshot table
  String addExpense(String userName, String teamName, ExpenseItem expenseItem) throws RemoteException, InterruptedException;

  // payment can not be negative -> update pending action table
  String makePayment(String userName, String teamName, Payment payment) throws RemoteException, InterruptedException;

  // update pending table, history table (change to ExpenseItem) and snapshot table
  String confirmPayment(String userName, String teamName, Payment payment) throws RemoteException,
          InterruptedException;

  List<ExpenseItem> showHistory(String userName, String teamName) throws RemoteException, InterruptedException;

  List<Payment> showSnapshot(String userName, String teamName) throws RemoteException, InterruptedException;

  List<Payment> showPaymentHistory(String userName, String teamName) throws RemoteException, InterruptedException;
  //--------------------------------------------------------------------------//

}
