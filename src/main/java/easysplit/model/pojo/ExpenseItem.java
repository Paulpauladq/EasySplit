package easysplit.model.pojo;

import java.io.Serializable;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExpenseItem implements Serializable {

  private String lender;
  private Set<String> lenee;
  private String description;
  private double amount;

  @Override
  public String toString() {
    return description + " ($" + amount + " paid by " + lender + ") ";
  }
}
