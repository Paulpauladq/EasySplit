package easysplit.model.server.paxos.acceptor;

import com.google.gson.Gson;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import easysplit.model.server.dao.EasySplitDao;
import easysplit.model.server.dao.EasySplitDaoImpl;
import easysplit.model.server.paxos.enums.PaxosStatus;
import easysplit.model.server.paxos.pojo.AcceptResponse;
import easysplit.model.server.paxos.pojo.Operation;
import easysplit.model.server.paxos.pojo.PrepareResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class AcceptorImpl extends UnicastRemoteObject implements Acceptor {

  // backend database
  private final EasySplitDao easySplitDao;

  private String maxProposalId;
  private boolean proposalAccepted;
  private String acceptedId;
  private Operation acceptedOperation;

  public AcceptorImpl(final int port) throws RemoteException {

    // Create remote object at any available port
    super(port);
    log.info("Stub initializing...");
    // Use concurrent map to ensure thread safe
    this.easySplitDao = new EasySplitDaoImpl();
    this.maxProposalId = "0.0"; // initial 0
    this.proposalAccepted = false; // initial false
    this.acceptedId = null;
    this.acceptedOperation = null;
    log.info("Stub initialization complete");
  }

  private synchronized void setMaxProposalId(final String maxProposalId) {

    this.maxProposalId = maxProposalId;
    log.info("Update maxProposalId to " + this.maxProposalId);
  }

  private synchronized void setProposalAccepted(final boolean proposalAccepted) {

    this.proposalAccepted = proposalAccepted;
    log.info("Update proposalAccepted to: " + this.proposalAccepted);
  }

  private synchronized void setAcceptedId(final String acceptedId) {

    this.acceptedId = acceptedId;
    log.info("Update acceptedId to: " + this.acceptedId);
  }

  private synchronized void setAcceptedOperation(final Operation acceptedOperation) {

    this.acceptedOperation = acceptedOperation;
    log.info("Update acceptedOperation to: " + this.acceptedOperation);
  }

  // adopt operation
  private String doOperation(Operation operation) {

    log.info("Do operation: " + operation);
    switch (operation.getOperationType()) {
      case IS_USER_EXIST:
        if (easySplitDao.isUserExist(operation.getUserName())) {
          return "EXIST";
        } else {
          return "NOT_EXIST";
        }
      case SIGN_UP:
        return easySplitDao.signUp(operation.getUserName(), operation.getPassword());
      case LOG_IN:
        return easySplitDao.login(operation.getUserName(), operation.getPassword());
      case GET_TEAM_SET:
        return new Gson().toJson(easySplitDao.getTeamSet(operation.getUserName())); // back to set
      case GET_MEMBER_SET:
        return new Gson().toJson(easySplitDao.getMemberSet(operation.getTeamName())); // back to set
      case IS_TEAM_EXIST:
        if (easySplitDao.isTeamExist(operation.getTeamName())) {
          return "EXIST";
        } else {
          return "NOT_EXIST";
        }
      case CREATE_TEAM:
        return easySplitDao.createTeam(operation.getUserName(), operation.getTeamName(), operation.getMembers());
      case ADD_EXPENSE:
        return easySplitDao.addExpense(operation.getUserName(), operation.getTeamName(), operation.getExpenseItem());
      case MAKE_PAYMENT:
        return easySplitDao.makePayment(operation.getUserName(), operation.getTeamName(), operation.getPayment());
      case SHOW_HISTORY:
        return new Gson().toJson(easySplitDao.showHistory(operation.getTeamName())); // back to list
      case SHOW_SNAPSHOT:
        return new Gson().toJson(easySplitDao.showSnapshot(operation.getUserName(), operation.getTeamName())); // back to list
      case CONFIRM_PAYMENT:
        return easySplitDao.confirmPayment(operation.getUserName(), operation.getTeamName(), operation.getPayment());
      case SHOW_PAYMENT_HISTORY:
        return new Gson().toJson(easySplitDao.getPaymentHistory(operation.getUserName(), operation.getTeamName())); // back to list
      default:
        return "UNKNOWN OPERATION";
    }
  }

  @Override // Phase 1b: Acceptor (PROMISE)
  public PrepareResponse prepare(final String fullProposalId) throws RemoteException {

    log.info("Receive prepare message from leader with fullProposalId: " + fullProposalId);
    if (Double.valueOf(fullProposalId).compareTo(Double.valueOf(maxProposalId)) <= 0) {
      log.info("ProposalId is behind, maxProposalId: " + maxProposalId);
      return PrepareResponse.builder()
              .paxosStatus(PaxosStatus.FAILED)
              .build();
    } else {
      log.info("ProposalId is ahead, update maxProposalId");
      setMaxProposalId(fullProposalId); // save highest ID we've seen so far
      if (proposalAccepted) {
        log.info("Instance has accepted previous proposal with id: " + acceptedId + "; " + acceptedOperation);
        return PrepareResponse.builder()
                .paxosStatus(PaxosStatus.PROMISED)
                .fullProposalId(fullProposalId)
                .acceptedId(acceptedId)
                .acceptedOperation(acceptedOperation)
                .build();
      } else {
        log.info("Promise to the latest proposal");
        return PrepareResponse.builder()
                .paxosStatus(PaxosStatus.PROMISED)
                .fullProposalId(fullProposalId)
                .build();
      }
    }
  }

  @Override // Phase 2b: Acceptor (ACCEPT)
  public AcceptResponse accept(String fullProposalId, Operation operation) throws RemoteException {

    log.info("Receive accept: " + operation + "; from leader with fullProposalId: " + fullProposalId);
    if (fullProposalId.equals(maxProposalId)) {
      log.info("Commit operation: " + operation);
      setProposalAccepted(true);
      setAcceptedId(fullProposalId);
      setAcceptedOperation(operation);

      // do operation
      final String result = doOperation(operation);
      return AcceptResponse.builder()
              .paxosStatus(PaxosStatus.ACCEPTED)
              .acceptedOperation(operation)
              .operationResult(result)
              .build();
    } else {
      log.warn("Different proposalId has been promised...");
      return AcceptResponse.builder()
              .paxosStatus(PaxosStatus.FAILED)
              .build();
    }
  }

  @Override // Phase 3: Acceptor (RESET)
  public void resetForNextPaxosRun() throws RemoteException {

    log.info("Reset for next paxos run...");
    setProposalAccepted(false);
    setAcceptedId(null);
    setAcceptedOperation(null);
  }

  @Override
  public String getMaxProposalId() throws RemoteException {

    return this.maxProposalId;
  }
}
