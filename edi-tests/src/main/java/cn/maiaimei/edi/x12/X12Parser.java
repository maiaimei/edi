package cn.maiaimei.edi.x12;

import cn.maiaimei.validation.ValidationMessages;
import cn.maiaimei.validation.ValidationType;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("unchecked")
public class X12Parser {

  private static final String INTERCHANGE = "Interchange";

  private final String segmentSeparator;
  private final String elementSeparator;
  private final String subElementSeparator;
  private final ValidationMessages validationMessages;

  public X12Parser(String segmentSeparator, String elementSeparator, String subElementSeparator) {
    if (StringUtils.isBlank(segmentSeparator)) {
      this.segmentSeparator = System.lineSeparator();
    } else {
      this.segmentSeparator = segmentSeparator;
    }
    this.elementSeparator = elementSeparator;
    this.subElementSeparator = subElementSeparator;
    this.validationMessages = ValidationMessages.newInstance();
  }

  public ValidationMessages getValidationMessages() {
    return validationMessages;
  }

  public Map<String, Object> parse(String content) {
    Map<String, Object> result = Maps.newLinkedHashMap();
    final String[] segments = StringUtils.split(content, segmentSeparator);
    if (Objects.isNull(segments)) {
      return result;
    }
    for (int i = 0; i < segments.length; i++) {
      String segment = segments[i];
      final String[] elements = StringUtils.split(segment, elementSeparator);
      if (Objects.isNull(elements)) {
        continue;
      }
      String seg = elements[0];
      if (i == 0) {
        parseISA(seg, elements, result);
      }
      if (i == segments.length - 1) {
        parseIEA(seg, elements, result);
      }
    }
    return result;
  }

  private void parseISA(String seg, String[] elements, Map<String, Object> result) {
    if (!X12Segment.ISA.name().equals(seg)) {
      validationMessages.addMessage(X12Segment.ISA.name(), ValidationType.FileContent.name(),
          "Missing segment ISA");
      return;
    }
    final HashMap<String, Object> interchange = Maps.newLinkedHashMap();
    interchange.put(X12Segment.ISA.name(), elements);
    result.put(INTERCHANGE, interchange);
  }

  private void parseIEA(String seg, String[] elements, Map<String, Object> result) {
    if (!X12Segment.IEA.name().equals(seg)) {
      validationMessages.addMessage(X12Segment.IEA.name(), ValidationType.FileContent.name(),
          "Missing segment IEA");
      return;
    }
    final HashMap<String, Object> interchange = (HashMap<String, Object>) result.get(
        INTERCHANGE);
    interchange.put(X12Segment.IEA.name(), elements);
  }

}
