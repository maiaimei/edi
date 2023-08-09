package cn.maiaimei.validation;

import lombok.Data;

@Data
public class ValidationMessage {

  private String name;
  private String type;
  private String description;
  private String originalValue;
  private String invalidValue;

}
