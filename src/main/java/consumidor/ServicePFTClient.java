package consumidor;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import edu.utpl.pft.ws.ServicePftInterface;
import java.net.URL;
import java.util.Arrays;
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
import org.apache.cxf.ws.security.wss4j.CryptoCoverageChecker;
import org.apache.cxf.ws.security.wss4j.CryptoCoverageUtil;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.handler.WSHandlerConstants;

/**
 *
 * @author cesar
 */
public class ServicePFTClient {

    public static void main(String[] args) throws Exception {
        ServicePFTClient s = new ServicePFTClient();

        s.test4();
    }

    public void buscarPorCedula() throws Exception {
        URL wsdlUrl = new URL("http://localhost:8080/ptf-ws/ServicePft?wsdl");

        QName qname = new QName("http://ws.pft.utpl.edu/", "ServicePft");

        Service service = Service.create(wsdlUrl, qname);

        ServicePftInterface client = service.getPort(ServicePftInterface.class);

        buildRequestHeader(client);

        System.out.println("BUSCAR POR CEDULA: " + client.getPersonaPorCedula("1104885114"));
        System.out.println("PASO ");

    }

    private void buildRequestHeader(ServicePftInterface client) {
        Map<String, Object> requestContext = ((BindingProvider) client).getRequestContext();

        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:8080/ptf-ws/ServicePft?wsdl");

        Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();

        requestHeaders.put("username", Collections.singletonList("usuario"));
        requestHeaders.put("password", Collections.singletonList("1234"));

        requestContext.put(MessageContext.HTTP_REQUEST_HEADERS, requestHeaders);

    }

    public void encryptMessageSoap() {

        System.out.println("PASO 2");

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        WSS4JInInterceptor in;

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

        //buildRequestHeader(client);
        // Set up WS-Security Encryption
        // Reference: https://ws.apache.org/wss4j/using.html
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(WSHandlerConstants.USER, "s1as");
        props.put(WSHandlerConstants.ACTION, WSHandlerConstants.ENCRYPT);
        props.put(WSHandlerConstants.PASSWORD_TYPE, "PasswordText");
        props.put(WSHandlerConstants.ENC_PROP_FILE, "clientkeystore.properties");
        props.put(WSHandlerConstants.ENCRYPTION_PARTS, "{Content}{http://schemas.xmlsoap.org/soap/envelope/}Body");
        props.put(WSHandlerConstants.PW_CALLBACK_CLASS, ClientCallback.class.getName());

        WSS4JOutInterceptor wss4jOut = new WSS4JOutInterceptor(props);

        ClientProxy.getClient(client).getOutInterceptors().add(wss4jOut);

        try {
            String cedula = "1104885114";
            // Call the Web Service to perform an operation
            String personaJson = client.getPersonaPorCedula(cedula);

            System.out.println(personaJson);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    public void encryptMessageSoap2() {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();

        // Use the URL defined in the soap address portion of the WSDL
        factory.setAddress("http://localhost:8080/ptf-ws/ServicePft");

        // Utilize the class which was auto-generated by Apache CXF wsdl2java
        factory.setServiceClass(ServicePftInterface.class);

        ServicePftInterface clientService = (ServicePftInterface) factory.create();

        //encription 
        org.apache.cxf.endpoint.Client client = ClientProxy.getClient(clientService);
        org.apache.cxf.endpoint.Endpoint cxfEndpoint = client.getEndpoint();

        Map<String, Object> inProps = new HashMap<String, Object>();
        // how to configure the properties is outlined below;

        WSS4JInInterceptor wssIn = new WSS4JInInterceptor(inProps);
        cxfEndpoint.getInInterceptors().add(wssIn);

        Map<String, Object> outProps = new HashMap<String, Object>();
        // how to configure the properties is outlined below;

        WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);

        //outProps.put(WSHandlerConstants.ACTION, "Signature");
        outProps.put(WSHandlerConstants.ACTION,
                WSHandlerConstants.TIMESTAMP + " "
                + WSHandlerConstants.SIGNATURE + " "
                + WSHandlerConstants.ENCRYPT);

        outProps.put(WSHandlerConstants.USER, "s1as");
        outProps.put(WSHandlerConstants.PW_CALLBACK_CLASS, ClientCallback.class.getName());
        outProps.put(WSHandlerConstants.SIG_PROP_FILE, "clientkeystore.properties");

        cxfEndpoint.getOutInterceptors().add(wssOut);

        Map<String, String> prefixes = new HashMap<String, String>();
        prefixes.put("ser", "http://www.sdj.pl");
        prefixes.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");

        List<CryptoCoverageChecker.XPathExpression> xpaths = Arrays.asList(
                new CryptoCoverageChecker.XPathExpression("//ser:Header", CryptoCoverageUtil.CoverageType.SIGNED,
                        CryptoCoverageUtil.CoverageScope.ELEMENT),
                new CryptoCoverageChecker.XPathExpression("//soap:Body", CryptoCoverageUtil.CoverageType.ENCRYPTED,
                        CryptoCoverageUtil.CoverageScope.CONTENT));

        CryptoCoverageChecker checker = new CryptoCoverageChecker(prefixes, xpaths);

        //invoque metodh
        try {
            String cedula = "1104885114";
            // Call the Web Service to perform an operation
            String personaJson = clientService.getPersonaPorCedula(cedula);

            System.out.println("paso 3");

            System.out.println(personaJson);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void test3() {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();

        // Use the URL defined in the soap address portion of the WSDL
        factory.setAddress("http://localhost:8080/ptf-ws/ServicePft");

        // Utilize the class which was auto-generated by Apache CXF wsdl2java
        factory.setServiceClass(ServicePftInterface.class);

        ServicePftInterface clientWS = (ServicePftInterface) factory.create();

        System.out.println("paso 1");
        String cedula = "1104885114";
        String personaJson = clientWS.getPersonaPorCedula(cedula);
        System.out.println("paso 2");
        System.out.println(personaJson);

    }

    public void test4() {
        
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();

        // Use the URL defined in the soap address portion of the WSDL
        factory.setAddress("http://localhost:8080/ptf-ws/ServicePft");

        // Utilize the class which was auto-generated by Apache CXF wsdl2java
        factory.setServiceClass(ServicePftInterface.class);

        ServicePftInterface clientWS = (ServicePftInterface) factory.create();
        
        Client client = ClientProxy.getClient(clientWS);
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("action", "UsernameToken");
        props.put("user", "s1as");
        props.put("passwordType", "PasswordText");
        WSS4JOutInterceptor wss4jOut = new WSS4JOutInterceptor(props);
        
        client.getOutInterceptors().add(wss4jOut);

        ((BindingProvider)clientWS).getRequestContext().put("password", "changeit");
        
        System.out.println("paso 4");
        client.getRequestContext();
        
        clientWS.getPersonas();
    }
}
