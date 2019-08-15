package io.fabric8.quickstarts.camel.pojo;

import java.util.HashMap;
import java.util.Map;

public class Fields {

private String name;
private String type;
private String groupby_rank;
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

public String getName() {
return name;
}

public void setName(String name) {
this.name = name;
}

public String getType() {
return type;
}

public void setType(String type) {
this.type = type;
}

public String getGroupby_rank() {
return groupby_rank;
}

public void setGroupby_rank(String groupby_rank) {
this.groupby_rank = groupby_rank;
}

public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}