package commons.scriptsimport java.util.Map;import org.example.humor.app.domain.EchoIntentRequest;import org.example.humor.app.domain.EchoLaunchRequest;import org.example.humor.app.domain.EchoRequest;import org.example.humor.app.domain.EchoResponse;import org.example.humor.app.domain.EchoSessionEndedRequest;import org.example.humor.app.domain.EchoSlot;import org.example.humor.app.domain.Joke;import ai.vital.prime.groovy.v2.VitalPrimeGroovyScriptV2;
import ai.vital.prime.groovy.v2.VitalPrimeScriptInterfaceV2;import ai.vital.query.querybuilder.VitalBuilder;import ai.vital.vitalservice.VitalStatus;import ai.vital.vitalservice.model.Appimport ai.vital.vitalservice.query.ResultElementimport ai.vital.vitalservice.query.ResultList;import ai.vital.vitalservice.query.VitalSelectQueryimport ai.vital.vitalsigns.uri.URIGenerator;
class HumorAppHandlerDatascript implements VitalPrimeGroovyScriptV2 {	static List<Joke> jokes = null		static Random r = new Random()		void initJokesList(VitalPrimeScriptInterfaceV2 scriptInterface) {				if(jokes != null) return		synchronized (HumorAppHandlerDatascript.class) {						if(jokes != null) return						VitalSelectQuery vsq = new VitalBuilder().query {								SELECT {										value segments: ['humor-app']									value offset: 0					value limit: 10000										node_constraint { Joke.class }										}							}.toQuery()						ResultList rl = scriptInterface.selectQuery(vsq)						if(rl.status.status != VitalStatus.Status.ok) {				throw new RuntimeException("Jokes list generation error: ${rl.status.message}")			}						List<Joke> l = []						for(Joke j : rl) {				l.add(j)										}						if(l.size() == 0) throw new RuntimeException("Jokes list is empty - app non-functional")						jokes = Collections.unmodifiableList(l)					}			}		@Override	public ResultList executeScript(			VitalPrimeScriptInterfaceV2 scriptInterface,			Map<String, Object> parameters) {		ResultList rl = new ResultList()				try {						initJokesList(scriptInterface)						EchoRequest request = null						List<EchoSlot> slots = []						for(Object object : parameters.values()) {				if(object instanceof EchoRequest) {					if(request != null) throw new RuntimeException("More than 1 input EchoRequest object")					request = object				} else if(object instanceof EchoSlot) {					slots.add((EchoSlot)object)				}			}						if(request == null) throw new RuntimeException("No input EchoRequest object")						EchoResponse response = new EchoResponse().generateURI((App)scriptInterface.getApp())			response.cardTitle = 'The FunnyBot'						if(request instanceof EchoLaunchRequest) {				handleLaunchRequest(request, response)			} else if(request instanceof EchoIntentRequest) {				handleIntentRequest(request, response, slots)			} else if(request instanceof EchoSessionEndedRequest) {				handleSessionEndedRequest(request, response)			} else {				throw new RuntimeException('Unhandled request type: ' + request.class.canonicalName)			}						rl.getResults().add(new ResultElement(response, 1D))					} catch(Exception e) {			rl.setStatus(VitalStatus.withError(e.localizedMessage))		}					return rl;	}				void handleLaunchRequest(EchoLaunchRequest request, EchoResponse response) {				String t = "Hi! I'm the FunnyBot. Would you like to hear a joke?"				response.outputSpeechText = t 				response.cardContent = t				response.repromptOutputSpeechText = "I didn't understand that. Would you like to hear a joke?"			}		void handleIntentRequest(EchoIntentRequest request, EchoResponse response, List<EchoSlot> slots) {				String name = request.name				if(!name) throw new RuntimeException("No request intent name string")				String outputSpeechText = null				if(name == 'TellAJoke') {						outputSpeechText = jokes.get(r.nextInt(jokes.size())).body + '. Do you want to hear another joke?'						response.shouldEndSession = false					} else if( name == 'WantAJoke' ) {					EchoSlot Answer = null						for(EchoSlot slot : slots) {				if('Answer'.equals(slot.name?.toString())) {					Answer = slot				}			}						if(!Answer) throw new RuntimeException("No 'Answer' slot")						String answerVal = Answer.value?.toString()						if(!answerVal) throw new RuntimeException("No 'Answer' slot value")									if(answerVal.equalsIgnoreCase('no')) {								outputSpeechText = "Have a nice day!"								response.shouldEndSession = true;							} else {							outputSpeechText = jokes.get(r.nextInt(jokes.size())).body + '. Do you want to hear another joke?'							response.shouldEndSession = false			}					} else {			throw new RuntimeException('Unknown intent: ${name}');		}				response.cardContent = outputSpeechText				response.repromptOutputSpeechText = "I didn't understand that. Would you like to hear another joke?"			}		void handleSessionEndedRequest(EchoSessionEndedRequest request, EchoResponse response) {				String t = "Bye!"				response.outputSpeechText = t		response.repromptOutputSpeechText = t		response.cardContent = t				response.shouldEndSession = true		
	}
}
