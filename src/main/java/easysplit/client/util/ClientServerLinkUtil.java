package easysplit.client.util;

import java.rmi.Naming;
import java.util.Map;

import easysplit.client.pojo.ClientServerLink;
import easysplit.model.server.api.EasySplit;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ClientServerLinkUtil {

  private final Map<String, String> instancePortMap;

  public ClientServerLinkUtil(final Map<String, String> instancePortMap) {

    this.instancePortMap = instancePortMap;
  }

  public ClientServerLink getClientServerLink() {

    log.info("Locating registry for EasySplit...");
    for (Map.Entry<String, String> entry : instancePortMap.entrySet()) {
      try{
        final int portConnected = Integer.valueOf(entry.getValue());
        log.info("Try to locate EasySplit registry at port {}", portConnected);
        final String easySplitURL = String.format("rmi://%s:%d/%s", "localhost", portConnected, EasySplit.REGISTRY_NAME);
        EasySplit easySplit = (EasySplit) Naming.lookup(easySplitURL);
        log.info("EasySplit stub located at port {}", entry.getValue());
        return new ClientServerLink(portConnected, easySplit);
      } catch (Exception e) {
        log.warn("Fail to locate EasySplit stub at port {}, retry...", entry.getValue());
        log.debug("Error: {}", e);
      }
    }

    throw new IllegalStateException("No instance available...");
  }
}
