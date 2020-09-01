package easysplit.model.server.api;

import com.google.common.collect.Maps;
import com.google.gson.Gson;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.Set;

import easysplit.model.enums.OperationType;
import easysplit.model.server.paxos.Paxos;
import easysplit.model.server.paxos.PaxosImpl;
import easysplit.model.server.paxos.pojo.Operation;
import easysplit.model.pojo.ExpenseItem;
import easysplit.model.pojo.Payment;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class EasySplitImpl extends UnicastRemoteObject implements EasySplit {

  private final Map<String, String> instancePortMap;
  private final Map<Integer, Paxos> portPaxosMap;

  // self
  private final Paxos paxos;

  public EasySplitImpl(int port, Map<String, String> instancePortMap) throws RemoteException {

    super(port);
    this.instancePortMap = instancePortMap;
    this.portPaxosMap = Maps.newConcurrentMap();
    this.paxos = new PaxosImpl(portPaxosMap, port);
  }

  // paxos util
  @Override
  public Paxos getPaxos() throws RemoteException {

    return this.paxos;
  }

  @Override
  public Paxos saveNewInstanceStub(int sourcePort, Paxos paxos) throws RemoteException {

    log.info("Save new instance paxos at port '{}' to map", sourcePort);
    portPaxosMap.put(sourcePort, paxos);
    log.info("Send self paxos back to port '{}'", sourcePort);
    return this.paxos;
  }

  @Override
  public void sendNewInstanceAnnounce(int port, Paxos paxos) {

    log.info("New instance launched at port '{}', send announcement to all...", port);
    for (Map.Entry<String, String> entry : instancePortMap.entrySet()) {
      try{
        final int destinationPort = Integer.valueOf(entry.getValue());
        log.info("Try to locate instance stub at port {}", destinationPort);
        final String easySplitURL = String.format("rmi://%s:%d/%s", "localhost", destinationPort, EasySplit.REGISTRY_NAME);
        EasySplit easySplit = (EasySplit) Naming.lookup(easySplitURL);
        log.info("EasySplit stub located at port {}", destinationPort);
        log.info("Try to register self to port {}", destinationPort);
        Paxos destinationPaxos = easySplit.saveNewInstanceStub(port, paxos);
        log.info("Save destination paxos to map...");
        portPaxosMap.put(destinationPort, destinationPaxos);
      } catch (Exception e) {
        log.warn("Fail to locate EasySplit stub at port {}, retry...", entry.getValue());
        log.debug("Error: {}", e);
      }
    }
  }

  // ---------------------------------------
  @Override
  public boolean isAlive() throws RemoteException {

    return true;
  }

  @Override
  public String signUp(String userName, String password) throws RemoteException, InterruptedException {

    return paxos.paxosOperation(Operation.builder()
            .operationType(OperationType.SIGN_UP)
            .userName(userName)
            .password(password)
            .build());
  }

  @Override
  public String login(String userName, String password) throws RemoteException, InterruptedException {

    return paxos.paxosOperation(Operation.builder()
            .operationType(OperationType.LOG_IN)
            .userName(userName)
            .password(password)
            .build());
  }

  @Override
  public Set<String> getTeamSet(String userName) throws RemoteException, InterruptedException {

    final String result = paxos.paxosOperation(Operation.builder()
            .operationType(OperationType.GET_TEAM_SET)
            .userName(userName)
            .build());
    return new Gson().fromJson(result, Set.class);
  }

  @Override
  public boolean isUserExist(String memberName) throws RemoteException, InterruptedException {

    final String result = paxos.paxosOperation(Operation.builder()
            .operationType(OperationType.IS_USER_EXIST)
            .userName(memberName)
            .build());
    return "EXIST".equals(result);
  }


  @Override
  public boolean isTeamExist(String teamName) throws RemoteException, InterruptedException {

    final String result = paxos.paxosOperation(Operation.builder()
            .operationType(OperationType.IS_TEAM_EXIST)
            .teamName(teamName)
            .build());
    return "EXIST".equals(result);
  }

  @Override
  public String createTeam(String userName, String teamName, Set<String> members) throws RemoteException, InterruptedException {

    return paxos.paxosOperation(Operation.builder()
            .operationType(OperationType.CREATE_TEAM)
            .userName(userName)
            .teamName(teamName)
            .members(members)
            .build());
  }

  @Override
  public Set<String> getMemberSet(String teamName) throws RemoteException, InterruptedException {

    final String result = paxos.paxosOperation(Operation.builder()
            .operationType(OperationType.GET_MEMBER_SET)
            .teamName(teamName)
            .build());
    return new Gson().fromJson(result, Set.class);
  }

  @Override
  public String addExpense(String userName, String teamName, ExpenseItem expenseItem) throws RemoteException, InterruptedException {

    return paxos.paxosOperation(Operation.builder()
            .operationType(OperationType.ADD_EXPENSE)
            .userName(userName)
            .teamName(teamName)
            .expenseItem(expenseItem)
            .build());
  }

  @Override
  public String makePayment(String userName, String teamName, Payment payment) throws RemoteException, InterruptedException {

    return paxos.paxosOperation(Operation.builder()
            .operationType(OperationType.MAKE_PAYMENT)
            .userName(userName)
            .teamName(teamName)
            .payment(payment)
            .build());
  }

  @Override
  public String confirmPayment(String userName, String teamName, Payment payment) throws RemoteException, InterruptedException {

    return paxos.paxosOperation(Operation.builder()
            .operationType(OperationType.CONFIRM_PAYMENT)
            .userName(userName)
            .teamName(teamName)
            .payment(payment)
            .build());
  }

  @Override
  public List<ExpenseItem> showHistory(String userName, String teamName) throws RemoteException, InterruptedException {

    final String result = paxos.paxosOperation(Operation.builder()
            .operationType(OperationType.SHOW_HISTORY)
            .userName(userName)
            .teamName(teamName)
            .build());
    return new Gson().fromJson(result, List.class);
  }

  @Override
  public List<Payment> showSnapshot(String userName, String teamName) throws RemoteException, InterruptedException {

    final String result = paxos.paxosOperation(Operation.builder()
            .operationType(OperationType.SHOW_SNAPSHOT)
            .userName(userName)
            .teamName(teamName)
            .build());
    return new Gson().fromJson(result, List.class);
  }

  @Override
  public List<Payment> showPaymentHistory(String userName, String teamName) throws RemoteException, InterruptedException {

    final String result = paxos.paxosOperation(Operation.builder()
            .operationType(OperationType.SHOW_PAYMENT_HISTORY)
            .userName(userName)
            .teamName(teamName)
            .build());
    return new Gson().fromJson(result, List.class);
  }
}
