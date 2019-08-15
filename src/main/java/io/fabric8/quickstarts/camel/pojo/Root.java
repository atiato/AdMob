package io.fabric8.quickstarts.camel.pojo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Root {

private Boolean preview;
private int init_offset;
private List<Object> messages = null;
private List<Fields> fields = null;
private List<Result> results = null;
private Highlighted highlighted;
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

public Boolean getPreview() {
return preview;
}

public void setPreview(Boolean preview) {
this.preview = preview;
}

public int getInit_offset() {
return init_offset;
}

public void setInit_offset(int init_offset) {
this.init_offset = init_offset;
}

public List<Object> getMessages() {
return messages;
}

public void setMessages(List<Object> messages) {
this.messages = messages;
}

public List<Fields> getFields() {
return fields;
}

public void setFields(List<Fields> fields) {
this.fields = fields;
}

public List<Result> getResults() {
return results;
}

public void setResults(List<Result> results) {
this.results = results;
}

public Highlighted getHighlighted() {
return highlighted;
}

public void setHighlighted(Highlighted highlighted) {
this.highlighted = highlighted;
}

public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}