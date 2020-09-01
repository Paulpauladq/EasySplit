package easysplit.model.server.paxos.leader;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

import easysplit.model.server.paxos.Paxos;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LeaderImpl extends UnicastRemoteObject implements Leader {

  private final Map<Integer, Paxos> portPaxosMap;

  private final int port;
  private int leaderPort;

  public LeaderImpl(final Map<Integer, Paxos> portPaxosMap,
                    final int port) throws RemoteException {

    // Create remote object at any available port
    super(port);
    log.info("Stub initializing...");
    // Use concurrent map to ensure thread safe
    this.portPaxosMap = portPaxosMap;
    this.port = port;
    this.leaderPort = -1; // initial -1
    log.info("Stub initialization complete");
  }

  private synchronized void setLeaderPort(final int leaderPort) {

    this.leaderPort = leaderPort;
    log.info("Update leader to instance at port: " + this.leaderPort);
  }

  private void announceNewLeader() {
    log.info("Announce self as leader at port: " + port);
    for (final Map.Entry<Integer, Paxos> entry : portPaxosMap.entrySet()) {
      final int voterPort = entry.getKey();
      try {
        final Leader voter = entry.getValue().getLeaderRMI();
        log.info("Send announcement message to port: " + voterPort);
        voter.acceptNewLeader(port);
      } catch (Exception e) {
        log.warn("No response from voter at port: " + port);
        log.warn("Error: " + e);
      }
    }
  }

  @Override
  public boolean isAlive() throws RemoteException {

    return true;
  }

  @Override
  public void startNewElection() {

    log.info("Start new leader at port: " + port);
    for (final Map.Entry<Integer, Paxos> entry : portPaxosMap.entrySet()) {
      final int voterPort = entry.getKey();
      try {
        final Leader voter = entry.getValue().getLeaderRMI();
        log.info("Send leader message to port: " + voterPort);
        int responsePort = voter.sendElectionMessage(port);
        if (responsePort > port) {
          log.info("Receive response from higher port, quit leader election...");
          log.info("wait for the leader announcement...");
          return;
        }
      } catch (Exception e) {
        log.warn("No response from voter at port: " + port);
        log.warn("Error: " + e);
      }
    }
    log.info("Leader is self from bully leader election at port: " + port);
    announceNewLeader();
  }

  @Override
  public int sendElectionMessage(int instancePort) throws RemoteException {

    log.info("New leader leader was raised from port: " + instancePort);
    if (port > instancePort) { // instance with higher port, start own leader
      startNewElection();
    }
    return port;
  }

  @Override
  public void acceptNewLeader(int leaderPort) throws RemoteException {

    log.info("New leader has been elected at port: " + leaderPort);
    setLeaderPort(leaderPort);
    log.info("Current leader is at port: " + leaderPort);
  }

  @Override
  public int getLeaderPort() throws RemoteException {

    return leaderPort;
  }
}
