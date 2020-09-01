package easysplit.client;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import java.io.File;
import java.util.Map;
import java.util.Scanner;

import easysplit.client.pojo.ClientServerLink;
import easysplit.client.util.ClientServerLinkUtil;
import easysplit.model.enums.OperationType;
import easysplit.model.server.api.EasySplit;
import easysplit.model.server.instance.Server;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Client {
//
//  private static int portConnected;
//  private static EasySplit easySplit;
//  private static Map<String, String> instancePortMap;
//  private static ClientServerLinkUtil clientServerLinkUtil;
//
//  private static boolean connectToServer() {
//    try {
//      final ClientServerLink clientServerLink = clientServerLinkUtil.getClientServerLink();
//      portConnected = clientServerLink.getPortConnected();
//      easySplit = clientServerLink.getEasySplit();
//      return true;
//    } catch (Exception e) {
//      log.error("Error: {}", e);
//      return false;
//    }
//  }
//
//  public static void main(String[] args) {
//
//    log.info("Get serverConfig file from resource...");
//    try {
//      final File serverConfigFile =
//              new File(Server.class.getClassLoader().getResource("serverConfig.txt").getFile());
//      final Scanner scanner = new Scanner(serverConfigFile).useDelimiter("\\Z");
//
//      log.info("Get instancePortMap from serverConfig");
//      final String configJson = scanner.next();
//      instancePortMap = ImmutableMap.copyOf(new Gson().fromJson(configJson, Map.class));
//      log.info("instancePortMap = {}" + instancePortMap);
//    } catch (Exception e) {
//      log.warn("Fail to get instancePortMap from serverConfig...");
//      log.warn("Error: {}", e);
//      return;
//    }
//
//    clientServerLinkUtil = new ClientServerLinkUtil(instancePortMap);
//
//    final Scanner scanner = new Scanner(System.in);
//    showUsage();
//    String command = scanner.nextLine();
//    while (!"quit".equalsIgnoreCase(command)) {
//      // check heartbeat
//      try {
//        easySplit.isAlive();
//      } catch (Exception e) {
//        log.error("Current Instance Failed...");
//        if (!connectToServer()) {
//          log.error("Fail to connect to any instance...");
//          return;
//        }
//      }
//
//      // looping for commands
//      process(command);
//      showUsage();
//      command = scanner.nextLine();
//    }
//
//    log.info("Exiting...");
//    System.out.println("Bye~");
//  }
//
//  private static void showUsage() {
//
//    log.info("Prompt user for new command...");
//    System.out.println("\nPlease use the following commands: ");
//    System.out.println("1. sign-up:");
//    System.out.println("\tADD_USER <userName> <password>");
//    System.out.println("2. log-in:");
//    System.out.println("\tLOGIN <userName> <password>");
//    System.out.println("3. check-user-exist:");
//    System.out.println("\tIS_USER_EXIST <userName>");
//    System.out.println("4. Exit application.");
//    System.out.println("\tQUIT");
//  }
//
//  private static void process(final String command) {
//
//    log.info(String.format("Processing command '%s'...", command));
//    final String[] parsedCommand = command.split("\\s+");
//
//    log.info("Getting operationType...");
//    OperationType operationType;
//    try {
//      operationType = OperationType.valueOf(parsedCommand[0].toUpperCase());
//    } catch (IllegalArgumentException e) {
//      operationType = OperationType.UNKNOWN;
//    }
//    log.info(String.format("Type is '%s'", operationType));
//
//    log.info("Executing operation on remote...");
//    try {
//      switch (operationType) {
//        case IS_USER_EXIST:
//          final boolean result1 = easySplit.isUserExist(parsedCommand[1]);
//          System.out.println(String.format("Result is '%s'", result1));
//          log.info(String.format("Result is '%s'", result1));
//          break;
//        case SIGN_UP:
//          final String result2 = easySplit.signUp(parsedCommand[1], parsedCommand[2]);
//          System.out.println(String.format("Result is '%s'", result2));
//          log.info(String.format("Result is '%s'", result2));
//          break;
//        case LOG_IN:
//          final String result3 = easySplit.login(parsedCommand[1], parsedCommand[2]);
//          System.out.println(String.format("Result is '%s'", result3));
//          log.info(String.format("Result is '%s'", result3));
//          break;
//        case UNKNOWN:
//        default:
//          log.warn(String.format("Unknown operation type for command '%s'", command));
//          System.out.println(String.format("Unknown operation type for command '%s', please retry", command));
//      }
//    } catch (Exception e) {
//      log.error(String.format("Invalid command '%s' due to %s", command, e));
//      System.out.println("Invalid command, please retry: " + e.getMessage());
//    }
//    log.info("Operation complete");
//  }
}
