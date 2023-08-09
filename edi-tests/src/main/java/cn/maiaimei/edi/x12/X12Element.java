package cn.maiaimei.edi.x12;

import lombok.Data;

@Data
public class X12Element {

  private Integer segmentPositionNo;
  private String segmentId;
  private Integer elementPositionNo;
  private String elementId;
  private String elementValue;
  private Integer subElementPositionNo;
  private String subElementId;
  private String subElementValue;
  private String loopId;
  private String loopIdentifier;
}
