package wot.jtd.model;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.rdf.model.Model;
import com.apicatalog.jsonld.JsonLdError;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import wot.jtd.JTD;
import wot.jtd.RDFHandler;
import wot.jtd.annotations.RdfDatatypeProperty;
import wot.jtd.vocabulary.Vocabulary;


public abstract class AbstractJTDObject {

	protected static final Logger LOGGER = Logger.getLogger(AbstractJTDObject.class.getName());
	static {
		LOGGER.setLevel(Level.WARNING);
	}
	
	// Shared common attributes
	@JsonProperty(Vocabulary.JSONLD_TYPE)
	@RdfDatatypeProperty(value="https://www.w3.org/2019/wot/td#type")
	protected Collection<String> type;
	protected URI id;
	

	public Collection<String> getType() {
		return type;
	}
	public void setType(Collection<String> type) {
		this.type = type;
	}
	
	public URI getId() {
		return id;
	}
	public void setId(URI id) {
		this.id = id;
	}
	
	
	
	// Any other property outside the standard
	protected Map<String,Object> unknownProperties = new HashMap<>();
	
	@JsonIgnore
	protected Boolean hasExternalProperties = false;
	
	@JsonAnySetter
	public void setExternal(String name, Object value) {
        unknownProperties.put(name, value);
        hasExternalProperties = true;
        if(JTD.getShowExternalValuesWarnings())
        		LOGGER.log( Level.WARNING, getWariningMessage());
	}
	
	private String getWariningMessage() {
		StringBuilder message = new StringBuilder();
		try {
			String clazzName = this.getClass().getSimpleName();
			ObjectMapper mapper = new ObjectMapper();
			String jsonUnknownProperties = mapper.writeValueAsString(unknownProperties);
			message.append("Json elements outside the standard found in '").append(clazzName).append("' : ").append(jsonUnknownProperties);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return message.toString();
	}
	
	@JsonAnyGetter
	public Map<String,Object> getExternal() {
	    return unknownProperties;
	}
	
	public  Model toRDF(JsonObject td) throws JsonLdError {
		RDFHandler handler = new RDFHandler();	
		return handler.toRDF(td);
	}
	

	
	// -- Hash code and equals
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((unknownProperties == null) ? 0 : unknownProperties.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractJTDObject other = (AbstractJTDObject) obj;
		if (unknownProperties == null) {
			if (other.unknownProperties != null)
				return false;
		} else if (!unknownProperties.equals(other.unknownProperties))
			return false;
		return true;
	}
	
	
}
