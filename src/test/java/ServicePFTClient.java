/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import consumidor.ClientCallback;
import edu.utpl.pft.ws.ServicePftInterface;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JStaxOutInterceptor;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.junit.Test;

/**
 *
 * @author cesar
 */
public class ServicePFTClient {

    //SOLO AUTENTICACION
    @Test
    public void buscarPorCedula() throws Exception {
        URL wsdlUrl = new URL("http://localhost:8080/ptf-ws/ServicePft?wsdl");

        QName qname = new QName("http://ws.pft.utpl.edu/", "ServicePft");

        Service service = Service.create(wsdlUrl, qname);

        ServicePftInterface client = service.getPort(ServicePftInterface.class);

        buildRequestHeader(client);

        System.out.println("BUSCAR POR CEDULA: " + client.getPersonaPorCedula("1104885114"));
        System.out.println("PASO ");

    }

    //Para la autenticacion en header
    private void buildRequestHeader(ServicePftInterface client) {
        Map<String, Object> requestContext = ((BindingProvider) client).getRequestContext();

        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:8080/ptf-ws/ServicePft?wsdl");

        Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();

        requestHeaders.put("username", Collections.singletonList("usuario"));
        requestHeaders.put("password", Collections.singletonList("1234"));

        requestContext.put(MessageContext.HTTP_REQUEST_HEADERS, requestHeaders);
    }

    //METODO con sifrado de datos 
    @Test
    public void encryptMessageSoap() {

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();

        // Use the URL defined in the soap address portion of the WSDL
        factory.setAddress("http://localhost:8080/ptf-ws/ServicePft");

        // Utilize the class which was auto-generated by Apache CXF wsdl2java
        factory.setServiceClass(ServicePftInterface.class);

        ServicePftInterface client = (ServicePftInterface) factory.create();

        
         // Adding Logging Interceptors
         LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
         loggingOutInterceptor.setPrettyLogging(true);
         ClientProxy.getClient(client).getOutInterceptors().add(loggingOutInterceptor);

         LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
         loggingInInterceptor.setPrettyLogging(true);
         ClientProxy.getClient(client).getInInterceptors().add(loggingInInterceptor);

         //Para la autenticacion en header
         buildRequestHeader(client);
         
         
         //((BindingProvider)client).getRequestContext().put("password", "clientstorepass");
        
         // Set up WS-Security Encryption
         // Reference: https://ws.apache.org/wss4j/using.html
         Map<String, Object> props = new HashMap<>();
         props.put(WSHandlerConstants.USER, "testkey");
         props.put(WSHandlerConstants.ACTION, WSHandlerConstants.ENCRYPT);
         props.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PASSWORD_TEXT);
         props.put(WSHandlerConstants.ENC_PROP_FILE, "clientkeystore.properties");
         props.put(WSHandlerConstants.ENCRYPTION_PARTS, "{Content}{http://schemas.xmlsoap.org/soap/envelope/}Body");
         props.put(WSHandlerConstants.PW_CALLBACK_CLASS, ClientCallback.class.getName());

         WSS4JOutInterceptor wss4jOut = new WSS4JOutInterceptor(props);
         ClientProxy.getClient(client).getOutInterceptors().add(wss4jOut);
                
         /*
         Map<String, String> prefixes = new HashMap<String, String>();
         prefixes.put("ser", "http://www.sdj.pl");
         prefixes.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");
         
         List<XPathExpression> xpaths = Arrays.asList(
         new XPathExpression("//ser:Header", CoverageType.SIGNED,
         CoverageScope.ELEMENT),
         new XPathExpression("//soap:Body", CoverageType.ENCRYPTED,
         CoverageScope.CONTENT));

         CryptoCoverageChecker checker = new CryptoCoverageChecker(prefixes, xpaths);
         */
        //System.out.println("Crypto: "+checker.toString());
        try {
            String cedula = "1104885114";
            // Call the Web Service to perform an operation
            String personaJson = client.getPersonaPorCedula(cedula);
            //String personaJson = client.getPersonas();

            System.out.println(personaJson);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    //@Test
    public void test2() {
        consumidor.ServicePFTClient client = new consumidor.ServicePFTClient();
        client.encryptMessageSoap2();
    }

    //@Test
    public void test3() {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();

        // Use the URL defined in the soap address portion of the WSDL
        factory.setAddress("http://localhost:8080/ptf-ws/ServicePft");

        // Utilize the class which was auto-generated by Apache CXF wsdl2java
        factory.setServiceClass(ServicePftInterface.class);

        ServicePftInterface clientWS = (ServicePftInterface) factory.create();

        Client client = ClientProxy.getClient(clientWS);
        Map<String, Object> props = new HashMap<>();
        props.put("user", "testkey");
        props.put("action", "UsernameToken");
        props.put(WSHandlerConstants.PASSWORD_TYPE, "PasswordText");
        props.put(WSHandlerConstants.ENC_PROP_FILE, "clientkeystore.properties");
        props.put(WSHandlerConstants.ENCRYPTION_PARTS, "{Content}{http://schemas.xmlsoap.org/soap/envelope}Body");
        props.put(WSHandlerConstants.PW_CALLBACK_CLASS, ClientCallback.class.getName());

        WSS4JStaxOutInterceptor wss4jOut = new WSS4JStaxOutInterceptor(props);
        client.getOutInterceptors().add(wss4jOut);
        ((BindingProvider) clientWS).getRequestContext().put("password", "clientstorepass");

        String cedula = "1104885114";        
        String personaJson = clientWS.getPersonaPorCedula(cedula);
        
        System.out.println(personaJson);

    }
}
