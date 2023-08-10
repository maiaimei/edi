package cn.maiaimei.edi.x12;

import lombok.Data;

@Data
public class Segment {

  /**
   * ID
   */
  private String id;
  /**
   * Elements
   */
  private String[] elements;
  /**
   * Functional Group No.
   */
  private Integer fgNo;
  /**
   * Transaction Set No.
   */
  private Integer tsNo;
  /**
   * Loop
   */
  private String loop;
}
