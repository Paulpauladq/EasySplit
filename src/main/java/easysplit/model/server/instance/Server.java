package easysplit.model.server.instance;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

import easysplit.model.server.api.EasySplit;
import easysplit.model.server.api.EasySplitImpl;
import easysplit.view.EasySplitGUI;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Server {

  private static Registry easySplitRegistry;
  private static int instancePort;
  private static Map<String, String> instancePortMap;

  public static void main(String[] args) {

    log.info("Get serverConfig file from resource...");
    try {
      String configJson;
      try (InputStream stream =
                   EasySplitGUI.class.getClassLoader().getResourceAsStream("serverConfig.txt")) {
        configJson = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
      }
      instancePortMap = ImmutableMap.copyOf(new Gson().fromJson(configJson, Map.class));
      log.info("instancePortMap = {}" + instancePortMap);
    } catch (Exception e) {
      log.warn("Fail to get instancePortMap from serverConfig...");
      log.warn("Error: {}", e);
      return;
    }

    log.info("Creating registry for EasySplit...");
    for (Map.Entry<String, String> entry : instancePortMap.entrySet()) {
      try{
        final int port = Integer.valueOf(entry.getValue());
        log.info("Try to create EasySplit registry at port {}", entry.getValue());
        easySplitRegistry = LocateRegistry.createRegistry(port);
        instancePort = port;
        log.info("EasySplit registry created at port {}", entry.getValue());
        break;
      } catch (Exception e) {
        log.warn("Fail to create registry using port {}, retry...", entry.getValue());
        log.debug("Error: {}", e);
      }
    }

    if (null == easySplitRegistry) {
      log.error("No port available for new instance...");
      return;
    }

    log.info("Creating EasySplit stub and binding to registry...");
    try {
      final EasySplit easySplit = new EasySplitImpl(instancePort, instancePortMap);
      final String easySplitURL =
              String.format("rmi://%s:%d/%s", "localhost", instancePort, EasySplit.REGISTRY_NAME);
      Naming.rebind(easySplitURL, easySplit);
      log.info("Stub created and bind to: " + easySplitURL);
      easySplit.sendNewInstanceAnnounce(instancePort, easySplit.getPaxos());
      log.info("Instance is ready for client to connect... :)");
    } catch (Exception e) {
      log.warn("Fail to bind stub to registry...");
      log.warn("Error: {}", e);
      return;
    }
  }
}
