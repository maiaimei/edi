package cn.maiaimei.edi.x12;

import com.google.common.collect.Maps;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("unchecked")
public class X12Parser {

  private static final String INTERCHANGE_CONTROL_HEADER_TAG = "ISA";
  private static final String INTERCHANGE_CONTROL_TRAILER_TAG = "IEA";
  private static final String FUNCTIONAL_GROUP_HEADER_TAG = "GS";
  private static final String FUNCTIONAL_GROUP_TRAILER_TAG = "GE";
  private static final String TRANSACTION_SET_HEADER_TAG = "ST";
  private static final String TRANSACTION_SET_TRAILER_TAG = "SE";
  private static final Map<String, String> KEY_MAP = Maps.newHashMap();

  static {
    KEY_MAP.put(INTERCHANGE_CONTROL_HEADER_TAG, "InterchangeList");
    KEY_MAP.put(FUNCTIONAL_GROUP_HEADER_TAG, "FunctionalGroupList");
    KEY_MAP.put(TRANSACTION_SET_HEADER_TAG, "TransactionSetList");
  }

  private final String segmentSeparator;
  private final String elementSeparator;
  private final Map<String, String> loopConfig;

  public X12Parser(String segmentSeparator, String elementSeparator,
      Map<String, String> loopConfig) {
    this.segmentSeparator = segmentSeparator;
    this.elementSeparator = elementSeparator;
    this.loopConfig = Optional.ofNullable(loopConfig).orElseGet(Maps::newLinkedHashMap);
  }

  public Map<String, String[]> simpleParse(String content) {
    Map<String, String[]> result = Maps.newLinkedHashMap();
    final String[] lines = StringUtils.split(content, segmentSeparator);
    for (String line : lines) {
      final String[] elements = StringUtils.split(replaceCrLf(line), elementSeparator);
      String segId = elements[0];
      result.put(segId, elements);
    }
    return result;
  }

  public Map<String, Object> parseToMap(String content) {
    Map<String, Object> result = Maps.newLinkedHashMap();
    ArrayDeque<Map<String, Object>> lineInCreationDeque = new ArrayDeque<>();
    ArrayDeque<String> loopInCreationDeque = new ArrayDeque<>();
    lineInCreationDeque.add(result);
    final String[] lines = StringUtils.split(content, segmentSeparator);
    for (String line : lines) {
      handleLine(line, lineInCreationDeque, loopInCreationDeque);
    }
    return result;
  }

  private void handleLine(String line,
      ArrayDeque<Map<String, Object>> lineInCreationDeque,
      ArrayDeque<String> loopInCreationDeque) {
    final String[] elements = StringUtils.split(replaceCrLf(line), elementSeparator);
    String segId = elements[0];
    if (INTERCHANGE_CONTROL_HEADER_TAG.equals(segId)
        || FUNCTIONAL_GROUP_HEADER_TAG.equals(segId)
        || TRANSACTION_SET_HEADER_TAG.equals(segId)) {
      pushLine(KEY_MAP.get(segId), segId, elements, lineInCreationDeque);
    } else if (INTERCHANGE_CONTROL_TRAILER_TAG.equals(segId)
        || FUNCTIONAL_GROUP_TRAILER_TAG.equals(segId)
        || TRANSACTION_SET_TRAILER_TAG.equals(segId)) {
      breakLoopIfNeed(segId, lineInCreationDeque, loopInCreationDeque);
      putLine(segId, elements, lineInCreationDeque);
      lineInCreationDeque.pop();
    } else if (loopConfig.containsKey(segId)) {
      breakLoopIfNeed(segId, lineInCreationDeque, loopInCreationDeque);
      handleLoop(segId, elements, lineInCreationDeque, loopInCreationDeque);
    } else {
      breakLoopIfNeed(segId, lineInCreationDeque, loopInCreationDeque);
      putLine(segId, elements, lineInCreationDeque);
    }
  }

  private void handleLoop(String segId, String[] elements,
      ArrayDeque<Map<String, Object>> lineInCreationDeque,
      ArrayDeque<String> loopInCreationDeque) {
    final String loop = loopConfig.get(segId);
    if (loopInCreationDeque.contains(loop)) {
      lineInCreationDeque.pop();
    } else {
      loopInCreationDeque.push(loop);
    }
    pushLine(loop, segId, elements, lineInCreationDeque);
  }

  private void breakLoopIfNeed(String segId,
      ArrayDeque<Map<String, Object>> lineInCreationDeque,
      ArrayDeque<String> loopInCreationDeque) {
    for (String loop : loopInCreationDeque) {
      if (!loopConfig.get(loop).contains(segId)) {
        lineInCreationDeque.pop();
        loopInCreationDeque.pop();
      }
    }
  }

  private void pushLine(String key, String segId, String[] elements,
      ArrayDeque<Map<String, Object>> lineInCreationDeque) {
    final List<Map<String, Object>> list = (List<Map<String, Object>>)
        Objects.requireNonNull(lineInCreationDeque.peek())
            .computeIfAbsent(key, id -> new ArrayList<>());
    Map<String, Object> item = Maps.newLinkedHashMap();
    item.put(segId, elements);
    list.add(item);
    lineInCreationDeque.push(item);
  }

  private void putLine(String segId, String[] elements,
      ArrayDeque<Map<String, Object>> lineInCreationDeque) {
    Objects.requireNonNull(lineInCreationDeque.peek()).put(segId, elements);
  }

  private String replaceCrLf(String line) {
    return StringUtils.replaceEach(line,
        new String[]{StringUtils.CR, StringUtils.LF},
        new String[]{StringUtils.EMPTY, StringUtils.EMPTY});
  }
}
