package tech.stacktrace.jrodns;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.net.DatagramSocket;

public class UdpSocketFactory implements PooledObjectFactory<DatagramSocket> {
    @Override
    public PooledObject<DatagramSocket> makeObject() throws Exception {
        return new DefaultPooledObject<>(new DatagramSocket());
    }

    @Override
    public void destroyObject(PooledObject<DatagramSocket> pooledObject) throws Exception {
        pooledObject.getObject().close();
        pooledObject.getObject().disconnect();
    }

    @Override
    public boolean validateObject(PooledObject<DatagramSocket> pooledObject) {
        return pooledObject.getObject().isBound();
    }

    @Override
    public void activateObject(PooledObject<DatagramSocket> pooledObject) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<DatagramSocket> pooledObject) throws Exception {

    }
}
