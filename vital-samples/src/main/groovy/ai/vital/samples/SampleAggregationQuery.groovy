package ai.vital.samples

import ai.vital.vitalsigns.model.property.URIProperty
import ai.vital.query.querybuilder.VitalBuilder
import ai.vital.vitalservice.VitalService
import ai.vital.vitalservice.query.ResultList
import ai.vital.vitalservice.query.VitalSelectQuery
import ai.vital.vitalsigns.VitalSigns
import ai.vital.vitalservice.factory.VitalServiceFactory

import ai.vital.vitalsigns.model.VitalSegment
import ai.vital.domain.*
import ai.vital.domain.properties.*
import ai.vital.query.Order;
import ai.vital.query.graph.*
import ai.vital.vitalsigns.model.*
import ai.vital.vitalsigns.model.properties.*
import ai.vital.vitalsigns.meta.GraphContext

import com.vitalai.domain.wordnet.*


class SampleAggregationQuery {

	static main(args) {
	
		VitalSigns vs = VitalSigns.get()
		
		
		VitalApp app = new VitalApp()
	
		app.URI = "http://vital.ai/ontology/app/123"
		app.appID = "app"
		
		VitalServiceKey serviceKey = new VitalServiceKey().generateURI(app)
		serviceKey.key = "aaaa-aaaa-aaaa"
		
		VitalService service = VitalServiceFactory.openService(serviceKey, 'lucenedisk')
		println "Ping: " + service.ping()
		
		
		def wordnet = service.getSegment('wordnet')
		
		
		def builder = new VitalBuilder()
		
		VitalSelectQuery q = builder.query {
			
			SELECT {
				
				value segments: [wordnet]
								
				value  limit: 20
							
				node_constraint { SynsetNode.expandSubclasses(true) }
					
				//DISTINCT( SynsetNode.props().name ) 
				
				 DISTINCT( SynsetNode.props().name, order: Order.DESCENDING ) 
				
//				COUNT_DISTINCT ( AdjectiveSynsetNode.props().name )
//				COUNT  ( AdjectiveSynsetNode.props().name )
			}
			
		}.toQuery()
		
		
		ResultList list = service.query( q )
		
		println "Results:\n" + list.results.size()
		
		list.each {println it }
		/*
		list.each { 
			URIProperty obj_uri = it."source1"
			println "URI: " + obj_uri
			def o = service.get(GraphContext.ServiceWide, obj_uri)
			println o.first().name
			 
			}
	*/
		
		
		service.close()
		
	}

}
