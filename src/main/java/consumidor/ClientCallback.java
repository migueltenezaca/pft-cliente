package consumidor;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.apache.wss4j.common.ext.WSPasswordCallback;

/**
 *
 * @author cesar
 */
public class ClientCallback implements CallbackHandler {

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
        // set the password for our message.
        //pc.setPassword("clientstorepass");
        if (pc.getIdentifier().equals("testKey") ) {
            if (!pc.getPassword().equals("clientstorepass")) {
                throw new IOException("contraseña no es correcta");
            }
        }

    }

}
