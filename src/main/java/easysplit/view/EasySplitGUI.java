package easysplit.view;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;

import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.*;

import easysplit.client.pojo.ClientServerLink;
import easysplit.client.util.ClientServerLinkUtil;
import easysplit.model.enums.PaymentStatus;
import easysplit.model.pojo.ExpenseItem;
import easysplit.model.pojo.Payment;
import easysplit.model.server.api.EasySplit;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class EasySplitGUI extends JFrame {

  private static final String spaceMask = "a1483626-f971-46e9-b5aa-b7952729df3f";

  private static int portConnected;
  private static EasySplit easySplit;
  private static Map<String, String> instancePortMap;
  private static ClientServerLinkUtil clientServerLinkUtil;

  private CardLayout cardBaseLayout;
  private CardLayout cardTeamLayout;
  private CardLayout cardActionLayout;
  private String userName;
  private Set<String> memberSet;

  private JPanel base;
  private JPanel p_home;
  private JPanel p_signup;
  private JPanel p_login;
  private JLabel title;
  private JButton b_signup;
  private JButton b_login;
  private JPasswordField t_password_su;
  private JPasswordField t_password_li;
  private JTextField t_username_su;
  private JTextField t_username_li;
  private JLabel signuptitle;
  private JLabel logintitle;
  private JLabel usernamesignup;
  private JLabel passwordsignup;
  private JLabel usernamelogin;
  private JLabel passwordlogin;
  private JPanel cardbase;
  private JPanel usermain;
  private JPanel cardteam;
  private JPanel p_team;
  private JLabel teamtitle;
  private JButton b_createteam;
  private JScrollPane s_teamscroll;
  private JList l_teamlist;
  private JPanel p_createteam;
  private JPanel p_teaminfo;
  private JLabel teamname;
  private JTextField t_teamname;
  private JButton b_checkteamname;
  private JLabel teammembers;
  private JTextField t_membername;
  private JButton b_addmember;
  private JList l_memberlist;
  private JLabel teamlist;
  private JLabel memberlist;
  private JButton b_confirmteam;
  private JLabel l_welcomebanner;
  private JButton b_logout;
  private JButton b_deletemembers;
  private JLabel l_teamname;
  private JLabel memberlist1;
  private JPanel cardaction;
  private JPanel p_addexpense;
  private JPanel p_showhistory;
  private JPanel p_showsnapshot;
  private JPanel p_makepayment;
  private JPanel p_showpaymenthistory;
  private JLabel description;
  private JTextField t_expensedescription;
  private JLabel expenseamount;
  private JTextField t_expenseamount;
  private JLabel options;
  private JList l_teammemberlist;
  private JButton b_submitexpense;
  private JButton b_addexpense;
  private JButton b_makepayment;
  private JButton b_showfullhistory;
  private JButton b_showsnapshot;
  private JButton b_showpaymenthistory;
  private JList l_fullhistory;
  private JList l_snapshot;
  private JLabel payto;
  private JTextField t_payto;
  private JLabel payamount;
  private JTextField t_payamount;
  private JButton b_submitpayment;
  private JLabel completedpayment;
  private JList l_completedpayment;
  private JList l_pendingpayment;
  private JButton b_confirmpayment;
  private JLabel latestsnapshot;
  private JLabel expensehistory;
  private JButton b_refresh;

  public EasySplitGUI() {

    super("EasySplitGUI");

    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setContentPane(cardbase);

    // card layout init
    log.info("Card layout init...");
    cardBaseLayout = (CardLayout) cardbase.getLayout();
    cardbase.add(p_home, "home");
    cardbase.add(usermain, "userMain");
    cardBaseLayout.show(cardbase, "home");

    cardTeamLayout = (CardLayout) cardteam.getLayout();
    cardteam.add(p_createteam, "createTeam");
    cardteam.add(p_teaminfo, "teamInfo");
    cardTeamLayout.show(cardteam, "createTeam");

    cardActionLayout = (CardLayout) cardaction.getLayout();
    cardaction.add(p_addexpense, "addExpense");
    cardaction.add(p_showhistory, "showHistory");
    cardaction.add(p_showpaymenthistory, "showPaymentHistory");
    cardaction.add(p_showsnapshot, "showSnapshot");
    cardaction.add(p_makepayment, "makePayment");
    cardActionLayout.show(cardaction, "addExpense");

    this.pack();

    // signup
    b_signup.addActionListener(e -> {
      instanceHealthCheck();
      try {
        if (t_username_su.getText().contains(" ")) {
          JOptionPane.showMessageDialog(new Frame(), "User name cannot have space in between!", "Info", JOptionPane.INFORMATION_MESSAGE);
          return;
        }

        if (t_password_su.getPassword().length < 8) {
          JOptionPane.showMessageDialog(new Frame(), "Password needs to be at least 8 characters long!", "Info", JOptionPane.INFORMATION_MESSAGE);
          return;
        }

        String result = easySplit.signUp(t_username_su.getText(), new String(t_password_su.getPassword()));
        if ("SUCCESS".equals(result)) {
          userName = t_username_su.getText();
          cardBaseLayout.show(cardbase, "userMain");
          cardTeamLayout.show(cardteam, "createTeam");
          l_welcomebanner.setText("Welcome to EasySplit - " + userName);
          t_teamname.setText("");
          t_membername.setText("");
          l_memberlist.setModel(new DefaultListModel());

          DefaultListModel<String> model = new DefaultListModel<>();
          for (String team : easySplit.getTeamSet(userName)) {
            model.addElement(team);
          }
          l_teamlist.setModel(model);
        } else {
          JOptionPane.showMessageDialog(new Frame(), result, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
      } catch (Exception e1) {
        JOptionPane.showMessageDialog(new Frame(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    });

    // login
    b_login.addActionListener(e -> {
      instanceHealthCheck();
      try {
        String result = easySplit.login(t_username_li.getText(), new String(t_password_li.getPassword()));
        if ("SUCCESS".equals(result)) {
          userName = t_username_li.getText();
          cardBaseLayout.show(cardbase, "userMain");
          cardTeamLayout.show(cardteam, "createTeam");
          l_welcomebanner.setText("Welcome to EasySplit - " + userName);
          t_teamname.setText("");
          t_membername.setText("");
          l_memberlist.setModel(new DefaultListModel());
          DefaultListModel<String> model = new DefaultListModel<>();
          for (String team : easySplit.getTeamSet(userName)) {
            model.addElement(team);
          }
          l_teamlist.setModel(model);
        } else {
          JOptionPane.showMessageDialog(new Frame(), result, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
      } catch (Exception e1) {
        JOptionPane.showMessageDialog(new Frame(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    });

    b_logout.addActionListener(e -> {
      cardBaseLayout.show(cardbase, "home");
      l_teamlist.clearSelection();
    });

    b_checkteamname.addActionListener(e -> {
      instanceHealthCheck();
      try {
        if (Strings.isNullOrEmpty(t_teamname.getText())) {
          JOptionPane.showMessageDialog(new Frame(), "Team Name Cannot Be Empty", "Info", JOptionPane.INFORMATION_MESSAGE);
        } else if (!easySplit.isTeamExist(t_teamname.getText())) {
          JOptionPane.showMessageDialog(new Frame(), "Good Choice!", "Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
          JOptionPane.showMessageDialog(new Frame(), "Team Name Already Exists", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
      } catch (Exception e1) {
        JOptionPane.showMessageDialog(new Frame(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    });

    b_createteam.addActionListener(e -> {
      cardTeamLayout.show(cardteam, "createTeam");
      t_teamname.setText("");
      t_membername.setText("");
      l_memberlist.setModel(new DefaultListModel());
    });

    l_teamlist.addListSelectionListener(e -> {
      instanceHealthCheck();
      try {
        cardTeamLayout.show(cardteam, "teamInfo");
        String teamName = (String) l_teamlist.getSelectedValue();
        if (null == teamName) {
          return;
        }
        l_teamname.setText(teamName);
        DefaultListModel<String> model = new DefaultListModel<>();
        memberSet = easySplit.getMemberSet(teamName);
        for (String member : memberSet) {
          model.addElement(member);
        }
        l_teammemberlist.setModel(model);
        cardActionLayout.show(cardaction, "addExpense");
        t_expensedescription.setText("");
        t_expenseamount.setText("");
      } catch (Exception e1) {
        JOptionPane.showMessageDialog(new Frame(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    });

    b_confirmteam.addActionListener(e -> {
      instanceHealthCheck();
      try {
        if (!Strings.isNullOrEmpty(t_teamname.getText()) && !easySplit.isTeamExist(t_teamname.getText())) {
          Set<String> members = Sets.newHashSet();
          for (int i = 0; i < l_memberlist.getModel().getSize(); i++) {
            members.add((String) l_memberlist.getModel().getElementAt(i));
          }

          if (members.isEmpty()) {
            JOptionPane.showMessageDialog(new Frame(), "Cannot form one man team", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
          }

          String result = easySplit.createTeam(userName, t_teamname.getText(), members);
          JOptionPane.showMessageDialog(new Frame(), "Team Created", "Info", JOptionPane.INFORMATION_MESSAGE);
          DefaultListModel<String> model = new DefaultListModel<>();
          for (String team : easySplit.getTeamSet(userName)) {
            model.addElement(team);
          }
          l_teamlist.setModel(model);
          // clean-up
          t_teamname.setText("");
          t_membername.setText("");
          l_memberlist.setModel(new DefaultListModel());
        } else {
          JOptionPane.showMessageDialog(new Frame(), "Invalid Team Name", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
      } catch (Exception e1) {
        JOptionPane.showMessageDialog(new Frame(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    });

    b_deletemembers.addActionListener(e -> {
      DefaultListModel model = (DefaultListModel) l_memberlist.getModel();
      model.remove(l_memberlist.getSelectedIndex());
    });

    b_addmember.addActionListener(e -> {
      instanceHealthCheck();
      DefaultListModel model = (DefaultListModel) (l_memberlist.getModel());
      try {
        if (userName.equals(t_membername.getText())) {
          JOptionPane.showMessageDialog(new Frame(), "You Are Already In The Team", "Info", JOptionPane.INFORMATION_MESSAGE);
          t_membername.setText("");
          return;
        }
        if (easySplit.isUserExist(t_membername.getText())) {
          model.addElement(t_membername.getText());
        } else {
          JOptionPane.showMessageDialog(new Frame(), "User Does Not Exist", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
      } catch (Exception e1) {
        JOptionPane.showMessageDialog(new Frame(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
      t_membername.setText("");
    });

    b_addexpense.addActionListener(e -> {
      cardActionLayout.show(cardaction, "addExpense");
      t_expensedescription.setText("");
      t_expenseamount.setText("");
    });

    b_submitexpense.addActionListener(e -> {
      instanceHealthCheck();
      try {
        final double amount = Double.valueOf(t_expenseamount.getText());
        if (Double.compare(amount, 0) < 0) {
          JOptionPane.showMessageDialog(new Frame(), "Amount must be positive", "Info", JOptionPane.INFORMATION_MESSAGE);
          t_expensedescription.setText("");
          t_expenseamount.setText("");
          return;
        }
        final Set<String> lenee = Sets.newHashSet(memberSet);
        lenee.remove(userName);
        final String description = t_expensedescription.getText().replaceAll(" ", spaceMask);
        ExpenseItem expenseItem = new ExpenseItem(userName, lenee, description, amount);
        String result = easySplit.addExpense(userName, l_teamname.getText(), expenseItem);
        JOptionPane.showMessageDialog(new Frame(), result, "Info", JOptionPane.INFORMATION_MESSAGE);
      } catch (IllegalStateException e1) {
        JOptionPane.showMessageDialog(new Frame(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        log.error("Error: " + e1);
      } catch (Exception e1) {
        log.error("Error: " + e1);
      }
      t_expensedescription.setText("");
      t_expenseamount.setText("");
    });

    b_showfullhistory.addActionListener(e -> {
      instanceHealthCheck();
      cardActionLayout.show(cardaction, "showHistory");
      try {
        DefaultListModel<ExpenseItem> model = new DefaultListModel<>();
        List result = easySplit.showHistory(userName, l_teamname.getText());
        Iterator iterator = result.iterator();
        while (iterator.hasNext()) {
          ExpenseItem expenseItem = new Gson().fromJson(iterator.next().toString(), ExpenseItem.class);
          final String description = expenseItem.getDescription().replaceAll(spaceMask, " ");
          expenseItem.setDescription(description);
          model.addElement(expenseItem);
        }
        l_fullhistory.setModel(model);
      } catch (IllegalStateException e1) {
        JOptionPane.showMessageDialog(new Frame(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        log.error("Error: " + e1);
      } catch (Exception e1) {
        log.error("Error: " + e1);
      }
    });

    b_showsnapshot.addActionListener(e -> {
      instanceHealthCheck();
      cardActionLayout.show(cardaction, "showSnapshot");
      try {
        DefaultListModel<Payment> model = new DefaultListModel<>();
        List result = easySplit.showSnapshot(userName, l_teamname.getText());
        Iterator iterator = result.iterator();
        while (iterator.hasNext()) {
          Payment payment = new Gson().fromJson(iterator.next().toString(), Payment.class);
          model.addElement(payment);
        }
        l_snapshot.setModel(model);
      } catch (Exception e1) {
        JOptionPane.showMessageDialog(new Frame(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    });

    b_makepayment.addActionListener(e -> {
      cardActionLayout.show(cardaction, "makePayment");
      t_payto.setText("");
      t_payamount.setText("");
    });

    b_submitpayment.addActionListener(e -> {
      instanceHealthCheck();
      try {
        final double amount = Double.valueOf(t_payamount.getText());
        if (Double.compare(amount, 0) < 0) {
          JOptionPane.showMessageDialog(new Frame(), "Amount must be positive", "Info", JOptionPane.INFORMATION_MESSAGE);
          return;
        }
        final Set<String> lender = Sets.newHashSet(memberSet);
        lender.remove(userName);
        final String destination = t_payto.getText();
        if (!lender.contains(destination)) {
          JOptionPane.showMessageDialog(new Frame(), destination + " is not in the team", "Info", JOptionPane.INFORMATION_MESSAGE);
          return;
        }
        Payment payment = new Payment(PaymentStatus.PENDING, UUID.randomUUID(), userName, destination, amount);
        String result = easySplit.makePayment(userName, l_teamname.getText(), payment);
        JOptionPane.showMessageDialog(new Frame(), result, "Info", JOptionPane.INFORMATION_MESSAGE);
      } catch (Exception e1) {
        JOptionPane.showMessageDialog(new Frame(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
      t_payto.setText("");
      t_payamount.setText("");
    });

    b_showpaymenthistory.addActionListener(e -> {
      instanceHealthCheck();
      cardActionLayout.show(cardaction, "showPaymentHistory");
      try {
        DefaultListModel<Payment> modelPending = new DefaultListModel<>();
        DefaultListModel<Payment> modelCompleted = new DefaultListModel<>();
        List result = easySplit.showPaymentHistory(userName, l_teamname.getText());
        Iterator iterator = result.iterator();
        while (iterator.hasNext()) {
          Payment payment = new Gson().fromJson(iterator.next().toString(), Payment.class);
          if (PaymentStatus.PENDING == payment.getPaymentStatus()) {
            modelPending.addElement(payment);
          } else {
            modelCompleted.addElement(payment);
          }
        }
        l_pendingpayment.setModel(modelPending);
        l_completedpayment.setModel(modelCompleted);
      } catch (Exception e1) {
        JOptionPane.showMessageDialog(new Frame(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    });

    b_confirmpayment.addActionListener(e -> {
      instanceHealthCheck();
      try {
        Payment pendingPayment = (Payment) l_pendingpayment.getSelectedValue();
        if (null == pendingPayment) {
          JOptionPane.showMessageDialog(new Frame(), "Please select a payment to confirm!!", "Info", JOptionPane.INFORMATION_MESSAGE);
          return;
        }
        if (pendingPayment.getSource().equals(userName)) {
          JOptionPane.showMessageDialog(new Frame(), "Not you!! Let the recipient confirm your payment!!", "Info", JOptionPane.INFORMATION_MESSAGE);
          return;
        }
        pendingPayment.setPaymentStatus(PaymentStatus.COMPLETED);
        easySplit.confirmPayment(userName, l_teamname.getText(), pendingPayment);

        // Update list
        DefaultListModel<Payment> modelPending = new DefaultListModel<>();
        DefaultListModel<Payment> modelCompleted = new DefaultListModel<>();
        List result = easySplit.showPaymentHistory(userName, l_teamname.getText());
        Iterator iterator = result.iterator();
        while (iterator.hasNext()) {
          Payment payment = new Gson().fromJson(iterator.next().toString(), Payment.class);
          if (PaymentStatus.PENDING == payment.getPaymentStatus()) {
            modelPending.addElement(payment);
          } else {
            modelCompleted.addElement(payment);
          }
        }
        l_pendingpayment.setModel(modelPending);
        l_completedpayment.setModel(modelCompleted);
        JOptionPane.showMessageDialog(new Frame(), "Payment Confirmed", "Info", JOptionPane.INFORMATION_MESSAGE);
      } catch (Exception e1) {
        JOptionPane.showMessageDialog(new Frame(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    });

    b_refresh.addActionListener(e -> {
      instanceHealthCheck();
      try {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String team : easySplit.getTeamSet(userName)) {
          model.addElement(team);
        }
        l_teamlist.setModel(model);
      } catch (Exception e1) {
        JOptionPane.showMessageDialog(new Frame(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    });
  }

  private void instanceHealthCheck() {

    log.info("Instance health check...");
    try {
      easySplit.isAlive();
    } catch (Exception e) {
      log.error("Current Instance Failed...");
      if (!connectToServer()) {
        log.error("Fail to connect to any instance...");
        throw new IllegalStateException("Fail to connect to any instance...");
      }
    }
  }

  private static boolean connectToServer() {
    try {
      final ClientServerLink clientServerLink = clientServerLinkUtil.getClientServerLink();
      portConnected = clientServerLink.getPortConnected();
      easySplit = clientServerLink.getEasySplit();
      return true;
    } catch (Exception e) {
      log.error("Error: {}", e);
      return false;
    }
  }

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

    clientServerLinkUtil = new ClientServerLinkUtil(instancePortMap);
    JFrame frame = new EasySplitGUI();
    frame.setVisible(true);
    log.info("Client setup completed...");
  }
}
