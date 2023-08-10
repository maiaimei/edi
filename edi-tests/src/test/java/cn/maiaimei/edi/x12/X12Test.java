package cn.maiaimei.edi.x12;

import cn.maiaimei.framework.utils.FileUtil;
import cn.maiaimei.framework.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

/**
 * parse validate transform
 */
@Slf4j
public class X12Test {

  private static final String ASTERISK = "*";

  @Test
  public void parseToMap_00401_850_000000007() {
    String content = FileUtil.readFileToString("/X12/00401_850_000000007.txt");
    final X12Parser x12Parser = newX12Parser("/X12/00401_850_loop_config.json");
    final Map<String, Object> map = x12Parser.parseToMap(content);
    System.out.println(JsonUtil.stringify(map));
  }

  @Test
  public void parseToMap_00403_850_000405272() {
    String content = FileUtil.readFileToString("/X12/00403_850_000405272.txt");
    final X12Parser x12Parser = newX12Parser("/X12/00403_850_loop_config.json");
    final Map<String, Object> map = x12Parser.parseToMap(content);
    System.out.println(JsonUtil.stringify(map));
  }

  @Test
  public void simpleParse_00401_850_000000007() {
    String content = FileUtil.readFileToString("/X12/00401_850_000000007.txt");
    final X12Parser x12Parser = newX12Parser("/X12/00401_850_loop_config.json");
    final Map<String, String[]> map = x12Parser.simpleParse(content);
    System.out.println(JsonUtil.stringify(map));
  }

  private X12Parser newX12Parser(String loopConfigPath) {
    return newX12Parser(loopConfigPath, StringUtils.LF, ASTERISK);
  }

  private X12Parser newX12Parser(String loopConfigPath,
      String segmentSeparator, String elementSeparator) {
    String config = FileUtil.readFileToString(loopConfigPath);
    Map<String, String> loopConfig;
    if (StringUtils.isNotBlank(config)) {
      loopConfig = JsonUtil.parse(config,
          new TypeReference<Map<String, String>>() {
          });
    } else {
      loopConfig = Maps.newLinkedHashMap();
    }
    return new X12Parser(segmentSeparator, elementSeparator, loopConfig);
  }
}

