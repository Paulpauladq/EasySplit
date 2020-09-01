package easysplit.model.server.dao;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import easysplit.model.enums.PaymentStatus;
import easysplit.model.server.dao.item.TeamItem;
import easysplit.model.server.dao.item.UserItem;
import easysplit.model.pojo.ExpenseItem;
import easysplit.model.pojo.Payment;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class EasySplitDaoImpl implements EasySplitDao, Serializable {

  private Map<String, UserItem> userTable;
  private Map<String, TeamItem> teamTable;

  public EasySplitDaoImpl() {

    this.userTable = Maps.newConcurrentMap();
    this.teamTable = Maps.newConcurrentMap();
  }

  @Override
  public boolean isUserExist(String userName) {

    log.info("Check if userName '{}' exist...", userName);
    return null != userTable.get(userName);
  }

  @Override
  public String signUp(String userName, String password) {

    log.info("Add user to userTable...");
    if (isUserExist(userName)) {
      log.warn("userName '{}' already exists, please choose a new one...", userName);
      return "userName already exists, please choose a new one...";
    }

    log.info("Add new user '{}' to userTable...", userName);
    final Set<String> teamSet = Sets.newConcurrentHashSet();
    userTable.put(userName, new UserItem(userName, password, teamSet));

    return "SUCCESS";
  }

  @Override
  public String login(String userName, String password) {

    log.info("Verify login information...");
    if (!isUserExist(userName)) {
      log.warn("userName '{}' does not exist, please verify...", userName);
      return "userName does not exist, please verify...";
    }

    final String savedPassword = userTable.get(userName).getPassword();
    if (!password.equals(savedPassword)) {
      log.warn("Password does not match for userName '{}', please verify...", userName);
      return "Password does not match, please verify...";
    }

    return "SUCCESS";
  }


  @Override
  public boolean isTeamExist(String teamName) {

    log.info("Check if teamName '{}' exist...", teamName);
    return null != teamTable.get(teamName);
  }

  @Override
  public String createTeam(String userName, String teamName, Set<String> members) {

    log.info("Add team to teamTable...");
    if (isTeamExist(teamName)) {
      log.warn("teamName '{}' already exists, please choose a new one...", userName);
      return "teamName already exists, please choose a new one...";
    }

    log.info("Add new team '{}' to teamTable...", teamName);
    final List<ExpenseItem> history = Lists.newCopyOnWriteArrayList();
    members.add(userName);
    final Map<String, Map<String, Payment>> snapshot = Maps.newConcurrentMap();
    final Map<String, Map<String, Map<UUID, Payment>>> paymentHistory = Maps.newConcurrentMap();
    for (String member : members) {
      snapshot.put(member, Maps.newConcurrentMap());
      paymentHistory.put(member, Maps.newConcurrentMap());
      for (String member1 : members) {
        if (!member1.equals(member)) {
          paymentHistory.get(member).put(member1, Maps.newConcurrentMap());
          snapshot.get(member).put(member1, new Payment(PaymentStatus.SNAPSHOT, UUID.randomUUID(), member, member1, 0.0));
        }
      }
      userTable.get(member).getTeamSet().add(teamName);
    }
    teamTable.put(teamName, new TeamItem(teamName, ImmutableSet.copyOf(members), history, snapshot, paymentHistory));
    log.info("Update team '{}' in userTable...", teamName);
    userTable.get(userName).getTeamSet().add(teamName);

    return "SUCCESS";
  }

  @Override
  public Set<String> getTeamSet(String userName) {

    log.info("Get team set for user '{}'...", userName);

    return userTable.get(userName).getTeamSet();
  }

  @Override
  public Set<String> getMemberSet(String teamName) {

    log.info("Get member set for team '{}'...", teamName);

    return teamTable.get(teamName).getMemberSet();
  }

  @Override
  public String addExpense(String userName, String teamName, ExpenseItem expenseItem) {

    log.info("Add expense: {}, for user {}, at team {}...", expenseItem, userName, teamName);
    teamTable.get(teamName).getHistory().add(expenseItem);
    log.info("Expense history updated...");
    final Map<String, Map<String, Payment>> snapshot = teamTable.get(teamName).getSnapshot();
    final double amount = expenseItem.getAmount() / (expenseItem.getLenee().size() + 1);
    log.info("Total amount is {}, split amount is {}", expenseItem.getAmount(), amount);
    for (String lenee : expenseItem.getLenee()) {
      Payment lenderPayment = snapshot.get(userName).get(lenee);
      Payment newLenderPayment =
              new Payment(PaymentStatus.SNAPSHOT, UUID.randomUUID(), userName, lenee, lenderPayment.getAmount() - amount);
      snapshot.get(userName).put(lenee, newLenderPayment);
      log.info("Lender payment updated, original payment: {}, new payment: {}", lenderPayment, newLenderPayment);

      Payment leneePayment = snapshot.get(lenee).get(userName);
      Payment newLeneePayment =
              new Payment(PaymentStatus.SNAPSHOT, UUID.randomUUID(), lenee, userName, leneePayment.getAmount() + amount);
      snapshot.get(lenee).put(userName, newLeneePayment);
      log.info("Lenee payment updated, original payment: {}, new payment: {}", leneePayment, newLeneePayment);
    }

    return "SUCCESS";
  }

  @Override
  public List<ExpenseItem> showHistory(String teamName) {

    log.info("Get history for team '{}'...", teamName);
    return teamTable.get(teamName).getHistory();
  }

  @Override
  public List<Payment> showSnapshot(String userName, String teamName) {

    log.info("Get snapshot for user '{}', at team '{}'...", userName, teamName);
    return Lists.newArrayList(teamTable.get(teamName).getSnapshot().get(userName).values());
  }

  @Override
  public String makePayment(String userName, String teamName, Payment payment) {

    log.info("Make payment: {}, for user {}, at team {}...", payment, userName, teamName);
    log.info("Update payment history table with payment...");
    payment.setPaymentStatus(PaymentStatus.PENDING);
    final UUID uuid = payment.getUuid();
    final String source = payment.getSource();
    final String destination = payment.getDestination();
    final Map<String, Map<String, Map<UUID, Payment>>> paymentHistory = teamTable.get(teamName).getPaymentHistory();
    paymentHistory.get(source).get(destination).put(uuid, payment);
    paymentHistory.get(destination).get(source).put(uuid, payment);

    return "SUCCESS";
  }

  @Override
  public String confirmPayment(String userName, String teamName, Payment payment) {

    log.info("Confirm payment: {}, for user {}, at team {}...", payment, userName, teamName);
    log.info("Update payment history table with payment...");
    payment.setPaymentStatus(PaymentStatus.COMPLETED);
    final Map<String, Map<String, Map<UUID, Payment>>> paymentHistory = teamTable.get(teamName).getPaymentHistory();
    final UUID uuid = payment.getUuid();
    final String source = payment.getSource();
    final String destination = payment.getDestination();
    final double amount = payment.getAmount();
    paymentHistory.get(source).get(destination).put(uuid, payment);
    paymentHistory.get(destination).get(source).put(uuid, payment);

    log.info("Update snapshot with payment...");
    final Map<String, Map<String, Payment>> snapshot = teamTable.get(teamName).getSnapshot();
    Payment sourcePayment = snapshot.get(source).get(destination);
    Payment newSourcePayment =
            new Payment(PaymentStatus.SNAPSHOT, UUID.randomUUID(), source, destination, sourcePayment.getAmount() - amount);
    snapshot.get(source).put(destination, newSourcePayment);
    log.info("Source payment updated, original payment: {}, new payment: {}", sourcePayment, newSourcePayment);

    Payment destinationPayment = snapshot.get(destination).get(source);
    Payment newDestinationPayment =
            new Payment(PaymentStatus.SNAPSHOT, UUID.randomUUID(), destination, source, destinationPayment.getAmount() + amount);
    snapshot.get(destination).put(source, newDestinationPayment);
    log.info("Destination payment updated, original payment: {}, new payment: {}", destinationPayment, newDestinationPayment);

    return "SUCCESS";
  }

  @Override
  public List<Payment> getPaymentHistory(String userName, String teamName) {

    log.info("Get payment history for user '{}', at team '{}'...", userName, teamName);
    List<Payment> paymentHistory = Lists.newArrayList();
    for (Map<UUID, Payment> paymentMap : teamTable.get(teamName).getPaymentHistory().get(userName).values()) {

      paymentHistory.addAll(paymentMap.values());
    }

    return paymentHistory;
  }
}
