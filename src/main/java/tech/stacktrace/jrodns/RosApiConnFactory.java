package tech.stacktrace.jrodns;

import me.legrange.mikrotik.ApiConnection;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.net.DatagramSocket;

public class RosApiConnFactory implements PooledObjectFactory<ApiConnection> {

    @Override
    public PooledObject<ApiConnection> makeObject() throws Exception {
        ApiConnection rosConn = ApiConnection.connect(Application.rosIp);
        rosConn.login(Application.rosUser, Application.rosPwd);
        return new DefaultPooledObject<>(rosConn);
    }

    @Override
    public void destroyObject(PooledObject<ApiConnection> p) throws Exception {
        p.getObject().close();
    }

    @Override
    public boolean validateObject(PooledObject<ApiConnection> p) {
        return p.getObject().isConnected();
    }

    @Override
    public void activateObject(PooledObject<ApiConnection> p) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<ApiConnection> p) throws Exception {

    }
}
