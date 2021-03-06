package wot.jtd;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphExtract;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.TripleBoundary;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DC_11;
import org.apache.jena.vocabulary.RDF;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.RdfLiteral;
import com.apicatalog.rdf.RdfNQuad;
import com.apicatalog.rdf.RdfResource;
import com.apicatalog.rdf.RdfValue;
import com.google.gson.JsonObject;

import wot.jtd.annotations.RdfDatatypeProperty;
import wot.jtd.exception.RDFValidationException;
import wot.jtd.exception.SchemaValidationException;
import wot.jtd.model.Thing;
import wot.jtd.model.VersionInfo;

public class RDFHandler {

	

	private static final Resource THING = ResourceFactory.createResource("https://www.w3.org/2019/wot/td#Thing");
	private static final Property SECURITY_DEFINITIONS = ResourceFactory.createProperty("https://www.w3.org/2019/wot/td#securityDefinitions");
	
	
	public static void serialize() {
		VersionInfo version = new VersionInfo();

		for (Field field: version.getClass().getSuperclass().getDeclaredFields()) {
		    System.out.println(field.getName());
		    if (field.isAnnotationPresent(RdfDatatypeProperty.class)) {
		        System.out.print(" -- > "+getSerializedKey(field));
		    }
		}
		for (Field field: version.getClass().getDeclaredFields()) {
		    System.out.println(field.getName());
		    if (field.isAnnotationPresent(RdfDatatypeProperty.class)) {
		        System.out.print(" -- > "+getSerializedKey(field));
		    }
		}
		
    }

    private static String getSerializedKey(Field field) {
        String annotationValue = field.getAnnotation(RdfDatatypeProperty.class).value();

        if (annotationValue.isEmpty()) {
            return field.getName();
        }
        else {
            return annotationValue;
        }
    }
	
	
	public List<Thing> fromRDF(Model model) throws JsonLdError, IOException, SchemaValidationException {
		List<Thing> things = new ArrayList<>();
		// 1. Write model as JSON-LD 1.0
		Writer writer = new StringWriter();
		model.write(writer, "Turtle");
		String jsonLDModel = writer.toString();
		System.out.println(jsonLDModel);
		writer.close();
		// 1. Find any resource that is a Thing
		List<Resource> thingResources = findThingResource(model);
		if(thingResources.isEmpty())
			throw new SchemaValidationException("RDF provided has no td:Thing instances, or they lack of mandatory properties such as dc:title ir td:securityDefinitions ");
		for(int index=0; index < thingResources.size(); index++) {
			Resource thing = thingResources.get(index);
			// 1.1 Extract subgraph of this thing
			GraphExtract graphExtractor = new GraphExtract( TripleBoundary.stopNowhere );
			Graph thingGraph = graphExtractor.extract(thing.asNode(), model.getGraph());
			try {
				Thing newThing = buildThing(thing, thingGraph);
				things.add(newThing);
			}catch(Exception e) { // define more accurate exceptions
				e.printStackTrace();
			}
		}
		return things;
	}
	
	private List<Resource> findThingResource(Model model) {
		List<Resource> resources = model.listSubjectsWithProperty(RDF.type, THING).toList();
		if(resources.isEmpty()) {
			resources = model.listSubjectsWithProperty(DC_11.title).toList();
			resources.retainAll(model.listSubjectsWithProperty(SECURITY_DEFINITIONS).toList());
		}
		return resources;
	}
	
	private Thing buildThing(Resource resource, Graph thingGraph) throws URISyntaxException {
		Model thingModel = ModelFactory.createModelForGraph(thingGraph);
		// minimum thing to buid
		Thing thing = new Thing();
		thing.setId(new URI(resource.toString()));
		
		//setTitle(Thing thing, Resource resource, Model thingModel, Property property)
		
		return thing;
	}
	/*
	private void setLiteralElements(Thing thing, Resource resource, Model thingModel, Property property) {
		Map<String,String> langElements = getLangElements(resource, thingModel, property);
		if(!langElements.isEmpty()) {
			String title = null;
			if(titles.containsKey("en")) {
				title = titles.get("en");
			}else {
				title = (String) titles.values().toArray()[0];
			}
			thing.setTitle(title);
		}else {
			
		}
	}*/
	
	// 
	
	public static void fromRDF() {
		VersionInfo thing = new VersionInfo();
		
		Method[] annotations = VersionInfo.class.getMethods();
		for(int index=0; index < annotations.length; index++) {
			Method annotation = annotations[index];
			System.out.println(annotation.getName());
			List<Annotation> annon = Arrays.asList(annotation.getAnnotations());
			annon.forEach(elem -> System.out.println(elem));
		}
		/*PropertyDescriptor[] properties = PropertyUtils.getPropertyDescriptors(thing);
		for(int index=0;index < properties.length; index++) {
			PropertyDescriptor property = properties[index];
			System.out.println(property.getDisplayName());
			System.out.println(property.getName());
			Method method = property.getPropertyType()
			if (method.isAnnotationPresent(Init.class)) {
	            method.setAccessible(true);
	            method.invoke(object);
	        }
			for(Annotation anon: .getAnnotations()) {
				System.out.println(anon);
			}
		}*/
	}
	
	protected Literal extractUnitaryObjectLiteral(Model model, Resource subject, Property property) throws RDFValidationException {
		Literal literal = null;
		RDFNode object = extractUnitaryObject(model, subject, property);
		if (object == null)
			throw new RDFValidationException(concatStrings("The porperty ", property.toString(),
					" must point to an existing literal, currently is not been used"));
		if (object.isLiteral()) {
			literal = object.asLiteral();
		} else {
			throw new RDFValidationException(concatStrings("The porperty ", property.toString(),
					" must point to a literal, currently is pointing to a non-literal object"));
		}

		return literal;
	}
	
	protected RDFNode extractUnitaryObject(Model model, Resource subject, Property property) throws RDFValidationException {
		RDFNode node = null;
		List<RDFNode> objects = model.listObjectsOfProperty(property).toList();
		if(objects.size()>1) {
			throw new RDFValidationException(concatStrings("The porperty ", property.toString()," is unitary but it was used more than once with the subject ", subject.toString()));
		}else {
			node = objects.get(0);
		}
		return node;
	}
	
	protected String concatStrings(String ...message) {
		StringBuilder builder = new StringBuilder();
		for(int index =0; index < message.length; index++)
			builder.append(message[index]);
		return builder.toString();
	}

	
	
	
	
	
	// -- to RDF methods
	
	public  Model toRDF(JsonObject td) throws JsonLdError {
		Model model = ModelFactory.createDefaultModel();
		InputStream tdStream = new ByteArrayInputStream(td.toString().getBytes());
		Document documentTD = JsonDocument.of(tdStream);
		RdfDataset dataset = JsonLd.toRdf(documentTD).get();
		Map<String,Resource> existingBlankNodes = new HashMap<>();
		dataset.toList().forEach(triple -> populateModel(triple, model, existingBlankNodes));
		return model;
	}
	
	private  void populateModel(RdfNQuad triple, Model model, Map<String,Resource> existingBlankNodes) {
		Resource subject = instantiateResource(triple.getSubject(), model, existingBlankNodes);
		Property predicate = ResourceFactory.createProperty(triple.getPredicate().getValue());

		RdfValue object = triple.getObject();
		if(object.isIRI()) {
			Resource objectJena = ResourceFactory.createResource(object.getValue());
			model.add(subject, predicate, objectJena);
		}else if(object.isBlankNode()){
			Resource objectJena = instantiateBlankNode(object.getValue(), model, existingBlankNodes);
			model.add(subject, predicate, objectJena);
		}else if(object.isLiteral()) {
			RdfLiteral literal = object.asLiteral();
			Optional<String> lang = literal.getLanguage();
			String datatype = literal.getDatatype();
			Literal jenaLiteral = ResourceFactory.createPlainLiteral(literal.getValue());
			if(lang.isPresent()) {
				jenaLiteral = ResourceFactory.createLangLiteral(literal.getValue(), lang.get());
			}else if(datatype!=null) {
				RDFDatatype dt = NodeFactory.getType(datatype);
				jenaLiteral = ResourceFactory.createTypedLiteral(literal.getValue(), dt);
			}
			model.add(subject, predicate, jenaLiteral);
		}
	}
	
	private  Resource instantiateResource(RdfResource resource, Model model, Map<String,Resource> existingBlankNodes) {
		Resource instantiatedResource = null;
		if(resource.isIRI()) {
			instantiatedResource = ResourceFactory.createResource(resource.getValue());
		}else {
			instantiatedResource = instantiateBlankNode(resource.getValue(), model, existingBlankNodes);
		}
		return instantiatedResource;
	}
	
	private  Resource instantiateBlankNode(String blankNode, Model model, Map<String,Resource> existingBlankNodes) {
		Resource instantiatedResource = null;
		if(!existingBlankNodes.containsKey(blankNode)) {
			instantiatedResource =  model.createResource();
			existingBlankNodes.put(blankNode, instantiatedResource);
		}else {
			instantiatedResource = existingBlankNodes.get(blankNode);
		}
		return instantiatedResource;
	}
	
}
