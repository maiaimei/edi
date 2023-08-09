package cn.maiaimei.validation;

import com.google.common.collect.Lists;
import java.util.List;

public class ValidationMessages {

  private List<ValidationMessage> validationMessageList;

  public ValidationMessages() {
    this.validationMessageList = Lists.newArrayList();
  }

  public static ValidationMessages newInstance() {
    ValidationMessages validationMessages = new ValidationMessages();
    validationMessages.validationMessageList = Lists.newArrayList();
    return validationMessages;
  }

  public void addMessage(String name, String description) {
    this.addMessage(name, null, description, null, null);
  }

  public void addMessage(String name, String type, String description) {
    this.addMessage(name, type, description, null, null);
  }

  public void addMessage(String name, String type, String description,
      String originalValue, String invalidValue) {
    ValidationMessage validationMessage = new ValidationMessage();
    validationMessage.setName(name);
    validationMessage.setType(type);
    validationMessage.setDescription(description);
    validationMessage.setOriginalValue(originalValue);
    validationMessage.setInvalidValue(invalidValue);
    this.validationMessageList.add(validationMessage);
  }

  public boolean hasErrors() {
    return !validationMessageList.isEmpty();
  }

  public List<ValidationMessage> getValidationMessageList() {
    return validationMessageList;
  }
}
