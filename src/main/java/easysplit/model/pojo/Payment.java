package easysplit.model.pojo;

import java.io.Serializable;
import java.util.UUID;

import easysplit.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Payment implements Serializable {

  private PaymentStatus paymentStatus;
  private final UUID uuid;
  private final String source;
  private final String destination;
  private final double amount;

  @Override
  public String toString() {
    return source + " -> " + destination + " ($" + amount + ") ";
  }
}
