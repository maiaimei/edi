package cn.maiaimei.edi.tests;

import cn.maiaimei.edi.x12.X12Element;
import cn.maiaimei.edi.x12.X12Parser;
import cn.maiaimei.framework.utils.FileUtil;
import cn.maiaimei.framework.utils.JsonUtil;
import cn.maiaimei.validation.ValidationMessages;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

/**
 * parse validate transform
 */
@Slf4j
public class X12Test {


  @Test
  public void test_split_X12() {
    String segmentSeparator = System.lineSeparator();
    String elementSeparator = "*";
    String subElementSeparator = ">";
    final String content = FileUtil.readFileToString("/X12/X12_850_000000007.txt");
    final String[] segments = StringUtils.split(content, segmentSeparator);
    if (Objects.nonNull(segments)) {
      for (int i = 0; i < segments.length; i++) {
        String segment = segments[i];
        final String[] elements = StringUtils.split(segment, elementSeparator);
        System.out.printf("\"%s\":%s%s\n",
            elements[0],
            JsonUtil.stringify(elements),
            i == segments.length - 1 ? "" : ",");
      }
    }
  }

  @Test
  public void test_parse_X12() {
    String segmentSeparator = System.lineSeparator();
    String elementSeparator = "*";
    String subElementSeparator = ">";
    String content = FileUtil.readFileToString("/X12/X12_850_000000007.txt");
    X12Parser x12Parser = new X12Parser(segmentSeparator, elementSeparator, subElementSeparator);
    Map<String, Object> result = x12Parser.parse(content);
    final ValidationMessages validationMessages = x12Parser.getValidationMessages();
    if (validationMessages.hasErrors()) {
      System.out.println(JsonUtil.stringify(validationMessages.getValidationMessageList()));
    } else {
      System.out.println(JsonUtil.stringify(result));
    }
  }

  @Test
  public void test_parse_1() {
    String content = FileUtil.readFileToString("/X12/X12_850_000000007.txt");
    List<X12Element> x12ElementList = Lists.newArrayList();
    final String[] lines = content.split("\n");
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i].replace("\r", "");
      final String[] elements = line.split("\\*");
      Integer segmentNo = i + 1;
      String segmentName = elements[0];
      for (int j = 1; j < elements.length; j++) {
        Integer elementNo = j;
        String elementName = segmentName.concat(StringUtils.leftPad(elementNo.toString(), 2, "0"));
        String elementValue = elements[j];
        final X12Element model = new X12Element();
        model.setSegmentPositionNo(segmentNo);
        model.setSegmentId(segmentName);
        model.setElementPositionNo(elementNo);
        model.setElementId(elementName);
        model.setElementValue(elementValue);
        x12ElementList.add(model);
      }
    }
    System.out.println();
  }

  @Test
  public void test_parse_2() {
    String fileContent = FileUtil.readFileToString("/X12/X12_850_000000007.txt");

    Map<String, String> segLoopConfig = Maps.newLinkedHashMap();
    segLoopConfig.put("SAC", "SACLoop1");
    segLoopConfig.put("N9", "N9Loop1");
    segLoopConfig.put("N1", "N1Loop1");
    segLoopConfig.put("PO1", "PO1Loop1");
    segLoopConfig.put("CTT", "CTTLoop1");
    segLoopConfig.put("SACLoop1", "SAC;CUR");
    segLoopConfig.put("N9Loop1", "N9;MSG");
    segLoopConfig.put("N1Loop1", "N1;N2;N3;N4");
    segLoopConfig.put("PO1Loop1", "PO1;PIDLoop1;REF;SCHLoop1");
    segLoopConfig.put("PIDLoop1", "PID");
    segLoopConfig.put("SCHLoop1", "SCH");
    segLoopConfig.put("CTTLoop1", "CTT");

    List<X12Element> elements = transform(fileContent, segLoopConfig);
    populateLoopInformation(elements, segLoopConfig);

    System.out.println();
  }

  private List<X12Element> transform(String fileContent, Map<String, String> segLoopConfig) {
    List<X12Element> elements = Lists.newArrayList();
    final String[] lines = fileContent.split("\n");
    String line, segmentId, elementId, elementValue, loopId, loopIdentifier;
    int segmentPositionNo, elementPositionNo;
    for (int i = 0; i < lines.length; i++) {
      line = lines[i].replace("\r", "");
      final String[] arr = line.split("\\*");
      segmentPositionNo = i + 1;
      segmentId = arr[0];
      loopId = null;
      loopIdentifier = null;
      if (segLoopConfig.containsKey(segmentId)) {
        loopId = segLoopConfig.get(segmentId);
        loopIdentifier = loopId;
      }
      for (int j = 1; j < arr.length; j++) {
        elementPositionNo = j;
        elementId = String.format("%s%s", segmentId,
            StringUtils.leftPad(String.valueOf(elementPositionNo), 2, "0"));
        elementValue = arr[j];
        final X12Element model = new X12Element();
        model.setSegmentPositionNo(segmentPositionNo);
        model.setSegmentId(segmentId);
        model.setElementPositionNo(elementPositionNo);
        model.setElementId(elementId);
        model.setElementValue(elementValue);
        model.setLoopId(loopId);
        model.setLoopIdentifier(loopIdentifier);
        elements.add(model);
      }
    }
    return elements;
  }

  private void populateLoopInformation(List<X12Element> elements,
      Map<String, String> segLoopConfig) {
    for (int i = 0; i < elements.size(); i++) {
      final X12Element element = elements.get(i);
      final String loopId = element.getLoopId();
      if (StringUtils.isNotBlank(loopId)) {
        final List<String> list = getSegments(loopId, segLoopConfig);
        for (int j = i + 1; j < elements.size(); j++) {
          final X12Element x12Element1 = elements.get(j);
          if (StringUtils.isNotBlank(x12Element1.getLoopId())) {
            break;
          }
          if (list.contains(x12Element1.getSegmentId())) {
            x12Element1.setLoopId(loopId);
            x12Element1.setLoopIdentifier(loopId);
          } else {
            final List<String> loop1 = list.stream().filter(item -> item.contains("Loop"))
                .collect(Collectors.toList());
            for (String key : loop1) {
              final List<String> list2 = getSegments(key, segLoopConfig);
              if (list2.contains(x12Element1.getSegmentId())) {
                x12Element1.setLoopId(key);
                x12Element1.setLoopIdentifier(loopId.concat("_").concat(key));
              }
            }
          }
        }
      }
    }
  }

  private List<String> getSegments(String key, Map<String, String> segLoopConfig) {
    return Arrays.asList(segLoopConfig.get(key).split(";"));
  }
}

