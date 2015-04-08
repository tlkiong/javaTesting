package meteorDdpClient;

/*
* (c)Copyright 2013-2014 Ken Yee, KEY Enterprise Solutions 
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.java_websocket.WebSocket.READYSTATE;
import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.Gson;

/**
 * Java Meteor DDP websocket client
 * @author kenyee
 */
public class DDPClient extends Observable {
    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

    /** Field names supported in the DDP protocol */
    public class DdpMessageField {
        public static final String MSG = "msg";
        public static final String ID = "id";
        public static final String METHOD = "method";
        public static final String METHODS = "methods";
        public static final String SUBS = "subs";
        public static final String PARAMS = "params";
        public static final String RESULT = "result";
        public static final String NAME = "name";
        public static final String SERVER_ID = "server_id";
        public static final String ERROR = "error";
        public static final String SESSION = "session";
        public static final String VERSION = "version";
        public static final String SUPPORT = "support";
        public static final String SOURCE = "source";
        public static final String ERRORMSG = "errormsg";
        public static final String CODE = "code";
        public static final String REASON = "reason";
        public static final String REMOTE = "remote";
        public static final String COLLECTION = "collection";
        public static final String FIELDS = "fields";
        public static final String CLEARED = "cleared";
    }

    /** Message types supported in the DDP protocol */
    public class DdpMessageType {
        // client -> server
        public static final String CONNECT = "connect";
        public static final String METHOD = "method";
        // server -> client
        public static final String CONNECTED = "connected";
        public static final String UPDATED = "updated";
        public static final String READY = "ready";
        public static final String NOSUB = "nosub";
        public static final String RESULT = "result";
        public static final String SUB = "sub";
        public static final String UNSUB = "unsub";
        public static final String ERROR = "error";
        public static final String CLOSED = "closed";
        public static final String ADDED = "added";
        public static final String REMOVED = "removed";
        public static final String CHANGED = "changed";
        public static final String PING = "ping";
        public static final String PONG = "pong";
    }

    /** DDP protocol version */
    private final static String DDP_PROTOCOL_VERSION = "1";
    /** DDP connection state */
    public enum CONNSTATE {
        Disconnected,
        Connected,
        Closed,
        Reconnecting
    };
    
    public enum MSGTYPE {
    	PING,
    	SUBSCRIPTION,
    	PONG,
    	CALL,
    	CONNECT,
    	UNSUBSCRIBE,
    	RESUME
    };
    private CONNSTATE mConnState;
    /** current command ID */
    private int mCurrentId;
    /** Listeners for method functions */
    private Map<String, DDPListener> mMsgListeners;
    /** web socket client */
    private WebSocketClient mWsClient;
    /** web socket address for reconnections */
    private String mMeteorServerAddress;
    /** we can't connect more than once on a new socket */
    private boolean mConnectionStarted;
    /** Google GSON object */
    private final Gson mGson = new Gson();
	private ConcurrentLinkedQueue<String> pendingMsgQueue;
	private ConcurrentLinkedQueue<String> subscriptionMsg;
	private DDPListener errorListener;
	private int reconnectCount;
	private int reconnectCounter;
    private  int delayReconnectTime = 10000;//10 seconds default
    private boolean hasReconnected;

    /**
     * Instantiates a Meteor DDP client for the Meteor server located at the
     * supplied IP and port (note: running Meteor locally will typically have a
     * port of 3000 but port 80 is the typical default for publicly deployed
     * servers)
     * 
     * @param meteorServerIp IP of Meteor server
     * @param meteorServerPort Port of Meteor server, if left null it will default to 3000
     * @param useSSL Whether to use SSL for websocket encryption
     * @throws URISyntaxException URI error
     */
    public DDPClient(String meteorServerIp, Integer meteorServerPort, DDPListener errorListener, int reconnectCount, boolean useSSL)
            throws Exception {
    	if(errorListener == null){
    		throw new Exception("ErrorListener cannot be null"); 
    	}
    	this.pendingMsgQueue = new ConcurrentLinkedQueue<String>();
    	this.subscriptionMsg = new ConcurrentLinkedQueue<String>();
    	setReconnectCount(reconnectCount);
    	setErrorListener(errorListener);
        initWebsocket(meteorServerIp, meteorServerPort, useSSL);
    }
    
    /**
     * Instantiates a Meteor DDP client for the Meteor server located at the
     * supplied IP and port (note: running Meteor locally will typically have a
     * port of 3000 but port 80 is the typical default for publicly deployed
     * servers)
     * 
     * @param meteorServerIp IP of Meteor server
     * @param meteorServerPort Port of Meteor server, if left null it will default to 3000
     * @param trustManagers Explicitly defined trust managers, if null no SSL encryption would be used.
     * @throws URISyntaxException URI error
     */
    public DDPClient(String meteorServerIp, Integer meteorServerPort, TrustManager[] trustManagers)
            throws URISyntaxException {
        initWebsocket(meteorServerIp, meteorServerPort, trustManagers);
    }
    
    /**
     * Instantiates a Meteor DDP client for the Meteor server located at the
     * supplied IP and port (note: running Meteor locally will typically have a
     * port of 3000 but port 80 is the typical default for publicly deployed
     * servers)
     * 
     * @param meteorServerIp
     *            - IP of Meteor server
     * @param meteorServerPort
     *            - Port of Meteor server, if left null it will default to 3000
     * @throws URISyntaxException URI error
     */
    public DDPClient(String meteorServerIp, Integer meteorServerPort, DDPListener errorListener, int reconnectCount)
            throws Exception {
    	if(errorListener == null){
    		throw new Exception("ErrorListener cannot be null"); 
    	}
    	this.pendingMsgQueue = new ConcurrentLinkedQueue<String>();
    	this.subscriptionMsg = new ConcurrentLinkedQueue<String>();
    	setReconnectCount(reconnectCount);
    	setErrorListener(errorListener);
        initWebsocket(meteorServerIp, meteorServerPort, false);
    }
    
    /**
     * Initializes a websocket connection
     * @param meteorServerIp IP address of Meteor server
     * @param meteorServerPort port of Meteor server, if left null it will default to 3000
     * @param useSSL whether to use SSL
     * @throws URISyntaxException
     */
    private void initWebsocket(String meteorServerIp, Integer meteorServerPort, boolean useSSL)
            throws URISyntaxException {
    	TrustManager[] trustManagers = null;
        if (useSSL) {
            try {
                // set up trustkeystore w/ Java's default trusted 
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                KeyStore trustKeystore = null;
                trustManagerFactory.init(trustKeystore);
                trustManagers = trustManagerFactory.getTrustManagers();
            } catch (KeyStoreException e) {
                log.warn("Error accessing Java default cacerts keystore {}", e);
            } catch (NoSuchAlgorithmException e) {
                log.warn("Error accessing Java default trustmanager algorithms {}", e);
            }
        }
        
        initWebsocket(meteorServerIp, meteorServerPort, trustManagers);
    }
    
    /**
     * Initializes a websocket connection
     * @param meteorServerIp IP address of Meteor server
     * @param meteorServerPort port of Meteor server, if left null it will default to 3000
     * @param trustManagers array explicitly defined trust managers, can be null
     * @throws URISyntaxException
     */
    private void initWebsocket(String meteorServerIp, Integer meteorServerPort, TrustManager[] trustManagers)
            throws URISyntaxException {
        mConnState = CONNSTATE.Disconnected;
        if (meteorServerPort == null)
            meteorServerPort = 3000;
        mMeteorServerAddress = (trustManagers != null ? "wss://" : "ws://")
                + meteorServerIp + ":"
                + meteorServerPort.toString() + "/websocket";
        this.mCurrentId = 0;
        this.mMsgListeners = new ConcurrentHashMap<String, DDPListener>();
        createWsClient(mMeteorServerAddress);
        
        if (trustManagers != null) {
            try {
                SSLContext sslContext = null;
                sslContext = SSLContext.getInstance( "TLS" );
                sslContext.init(null, trustManagers, null);
                // now we can set the web service client to use this SSL context
                mWsClient.setWebSocketFactory( new DefaultSSLWebSocketClientFactory( sslContext ) );
            } catch (NoSuchAlgorithmException e) {
                log.warn("Error accessing Java default trustmanager algorithms {}", e);
            } catch (KeyManagementException e) {
                log.warn("Error accessing Java default cacert keys {}", e);
            }
        }
    }
    
    /**
     * Creates a web socket client
     * @param meteorServerAddress Websocket address of Meteor server
     * @throws URISyntaxException URI error
     */
    public void createWsClient(String meteorServerAddress)
            throws URISyntaxException {
        this.mWsClient = new WebSocketClient(new URI(meteorServerAddress)) {

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                connectionOpened();
            }

            @Override
            public void onMessage(String message) {
                received(message);
            }

            @Override
            public void onError(Exception ex) {
                handleError(ex);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                connectionClosed(code, reason, remote);
            }
        };
        mConnectionStarted = false;
    }

    /**
     * Called after initial web-socket connection. Sends back a connection
     * confirmation message to the Meteor server.
     */
    private void connectionOpened() {
        log.trace("WebSocket connection opened");
        // reply to Meteor server with connection confirmation message ({"msg":
        // "connect"})
        Map<String, Object> connectMsg = new HashMap<String, Object>();
        connectMsg.put(DdpMessageField.MSG, DdpMessageType.CONNECT);
        connectMsg.put(DdpMessageField.VERSION, DDP_PROTOCOL_VERSION);
        connectMsg.put(DdpMessageField.SUPPORT,
                new String[] { DDP_PROTOCOL_VERSION });
        send(connectMsg, MSGTYPE.CONNECT);
        // we'll get a msg:connected from the Meteor server w/ a session ID when we connect
        // note that this may return an error that the DDP protocol isn't correct
    }

    /**
     * Called when connection is closed
     * 
     * @param code WebSocket Error code
     * @param reason Reason msg for error
     * @param remote Whether error is from remote side
     */
    private void connectionClosed(int code, String reason, boolean remote) {
        // changed formatting to always return a JSON object
        String closeMsg = "{\"msg\":\"closed\",\"code\":\"" + code
                + "\",\"reason\":\"" + reason + "\",\"remote\":" + remote + "}";
        log.debug("{}", closeMsg);
        received(closeMsg);
        //TODO: handle the reconnection here
        if(getReconnectCounter()>0){
        	//Do not delay the first time trying to reconnect
        	if(getReconnectCounter()<getReconnectCount()){
        		try {
    				Thread.sleep(getDelayReconnectTime());
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
        	}
        	System.out.println("reconnectCounter: "+getReconnectCounter());//TODO
        	reconnect();
        	minusReconnectCounter();
        }
    }

    /**
     * Error handling for any errors over the web-socket connection
     * 
     * @param ex exception to convert to event
     */
    private void handleError(Exception ex) {
        // changed formatting to always return a JSON object
        String errmsg = ex.getMessage();
        if (errmsg == null) {
            errmsg = "Unknown websocket error (exception in callback?)";
        }
        String errorMsg = "{\"msg\":\"error\",\"source\":\"WebSocketClient\",\"errormsg\":\""
                + errmsg + "\"}";
        log.debug("{}", errorMsg);
        // ex.printStackTrace();
        received(errorMsg);
    }

    /**
     * Increments and returns the client's current ID
     * 
     * @note increment/decrement/set on int (but not long) are atomic on the JVM
     * @return integer DDP call ID
     */
    private int nextId() {
        return ++mCurrentId;
    }

    /**
     * Registers a client DDP command results callback listener
     * 
     * @param DDP command results callback
     * @return ID for next command
     */
    private int addCommmand(DDPListener resultListener) {
        int id = nextId();
        if (resultListener != null) {
            // store listener for callbacks
            mMsgListeners.put(Integer.toString(id), resultListener);
        }
        return id;
    }
    
    /**
    * Registers a client DDP command results callback listener
    * 
    * @param DDP command results callback
    * @return ID for next command
    */
   private String addSubscribeCommmand(String collectionName, DDPListener resultListener) {
       String id = collectionName;
       if (resultListener != null) {
           // store listener for callbacks
           mMsgListeners.put(id, resultListener);
       }
       return id;
   }

    /**
     * Initiate connection to meteor server
     */
    public void connect() {//TODO
        if ((this.mWsClient.getReadyState() == READYSTATE.CLOSED)||(this.mWsClient.getReadyState() == READYSTATE.CLOSING)) {
            // we need to create a new wsClient because a closed websocket cannot be reused
            try {
            	System.out.println("\t ~~ Create new websocket");
                createWsClient(mMeteorServerAddress);
            } catch (URISyntaxException e) {
                // we shouldn't get URI exceptions because the address was validated in initWebsocket
            	e.printStackTrace();
            }
        } else {
        	System.out.println("mWsClient.getReadyState(): "+this.mWsClient.getReadyState());
        }
        if (!mConnectionStarted) {
            // only do the connect if no connection attempt has been done for this websocket client
            this.mWsClient.connect();
            mConnectionStarted = true;
        } else {
        	System.out.println("ConnectionStarted: "+mConnectionStarted);
        }
    }
    
    /**
     * Closes an open websocket connection.
     * This is async, so you'll get a close notification callback when it eventually closes.
     */
    public void disconnect() {
        if (this.mWsClient.getReadyState() != READYSTATE.CLOSED) {
        	System.out.println("\t ~~ Close websocket");
            this.mWsClient.close();
        }
    }

    /**
     * Call a meteor method with the supplied parameters
     * 
     * @param method name of corresponding Meteor method
     * @param params arguments to be passed to the Meteor method
     * @param resultListener DDP command listener for this method call
     * @return ID for next command
     */
    public int call(String method, Object[] params, DDPListener resultListener) {
        Map<String, Object> callMsg = new HashMap<String, Object>();
        callMsg.put(DdpMessageField.MSG, DdpMessageType.METHOD);
        callMsg.put(DdpMessageField.METHOD, method);
        callMsg.put(DdpMessageField.PARAMS, params);

        int id = addCommmand(resultListener/*
                                            * "method,"+method+","+Arrays.toString
                                            * (params)
                                            */);
        callMsg.put(DdpMessageField.ID, Integer.toString(id));
        send(callMsg, MSGTYPE.CALL);
        return id;
    }

    /**
     * Call a meteor method with the supplied parameters
     * 
     * @param method name of corresponding Meteor method
     * @param params arguments to be passed to the Meteor method
     * @return ID for next command
     */
    public int call(String method, Object[] params) {
        return call(method, params, null);
    }

    /**
     * Subscribe to a Meteor record set with the supplied parameters
     * 
     * @param name name of the corresponding Meteor subscription
     * @param params arguments corresponding to the Meteor subscription
     * @param resultListener DDP command listener for this call
     * @return ID for next command
     */
    public String subscribe(String name, Object[] params,
            DDPListener resultListener) {
        Map<String, Object> subMsg = new HashMap<String, Object>();
        subMsg.put(DdpMessageField.MSG, DdpMessageType.SUB);
        subMsg.put(DdpMessageField.NAME, name);
        subMsg.put(DdpMessageField.PARAMS, params);
        
        String id = addSubscribeCommmand(name,resultListener);
        
        subMsg.put(DdpMessageField.ID, id);
        send(subMsg,MSGTYPE.SUBSCRIPTION);
        return id;
    }

    /**
     * Subscribe to a Meteor record set with the supplied parameters
     * 
     * @param name name of the corresponding Meteor subscription
     * @param params arguments corresponding to the Meteor subscription
     * @return ID for next command
     */
    public String subscribe(String name, Object[] params) {
        return subscribe(name, params, null);
    }

    /**
     * Unsubscribe from a Meteor record set
     * 
     * @param name name of the corresponding Meteor subscription
     * @param resultListener DDP command listener for this call
     * @return ID for next command
     */
    public int unsubscribe(String name, DDPListener resultListener) {
        Map<String, Object> unsubMsg = new HashMap<String, Object>();
        unsubMsg.put(DdpMessageField.MSG, DdpMessageType.UNSUB);
        unsubMsg.put(DdpMessageField.NAME, name);

        int id = addCommmand(resultListener/* "unsub,"+name */);
        unsubMsg.put(DdpMessageField.ID, Integer.toString(id));
        send(unsubMsg, MSGTYPE.UNSUBSCRIBE);
        return id;
    }

    /**
     * Unsubscribe from a Meteor record set
     * 
     * @param name name of the corresponding Meteor subscription
     * @return ID for next command
     */
    public int unsubscribe(String name) {
        return unsubscribe(name, null);
    }
    
    /**
     * Inserts document into collection from the client
     * 
     * @param collectionName Name of collection
     * @param insertParams Document fields
     * @param resultListener DDP command listener for this call
     * @return Returns command ID
     */
    public int collectionInsert(String collectionName,
            Map<String, Object> insertParams, DDPListener resultListener) {
        Object[] collArgs = new Object[1];
        collArgs[0] = insertParams;
        return call("/" + collectionName + "/insert", collArgs);
    }

    /**
     * Inserts document into collection from client-side
     * 
     * @param collectionName Name of collection
     * @param insertParams Document fields
     * @return Returns command ID
     */
    public int collectionInsert(String collectionName,
            Map<String, Object> insertParams) {
        return collectionInsert(collectionName, insertParams, null);
    }

    /**
     * Deletes collection document from the client
     * 
     * @param collectionName Name of collection
     * @param docId _id of document
     * @param resultListener Callback handler for command results
     * @return Returns command ID
     */
    public int collectionDelete(String collectionName, String docId,
            DDPListener resultListener) {
        Object[] collArgs = new Object[1];
        Map<String, Object> selector = new HashMap<String, Object>();
        selector.put("_id", docId);
        collArgs[0] = selector;
        return call("/" + collectionName + "/remove", collArgs);
    }

    public int collectionDelete(String collectionName, String docId) {
        return collectionDelete(collectionName, docId, null);
    }

    /**
     * Updates a collection document from the client NOTE: for security reasons,
     * you can only do this one document at a time.
     * 
     * @param collectionName
     *            Name of collection
     * @param docId _id of document
     * @param updateParams Map w/ mongoDB parameters to pass in for update
     * @param resultListener Callback handler for command results
     * @return Returns command ID
     */
    public int collectionUpdate(String collectionName, String docId,
            Map<String, Object> updateParams, DDPListener resultListener) {
        Map<String, Object> selector = new HashMap<String, Object>();
        Object[] collArgs = new Object[2];
        selector.put("_id", docId);
        collArgs[0] = selector;
        collArgs[1] = updateParams;
        return call("/" + collectionName + "/update", collArgs);
    }

    /**
     * Updates a collection document from the client NOTE: for security reasons,
     * you can only do this one document at a time.
     * 
     * @param collectionName Name of collection
     * @param docId _id of document
     * @param updateParams Map w/ mongoDB parameters to pass in for update
     * @return Returns command ID
     */
    public int collectionUpdate(String collectionName, String docId,
            Map<String, Object> updateParams) {
        return collectionUpdate(collectionName, docId, updateParams, null);
    }
    
    /**
     * Pings the server...you'll get a Pong message back in the DDPListener
     * @param pingId of ping message so you can tell if you have data loss
     * @param resultListener DDP command listener for this call
     */
    public void ping(String pingId, DDPListener resultListener) {
        Map<String, Object> pingMsg = new HashMap<String, Object>();
        pingMsg.put(DdpMessageField.MSG, DdpMessageType.PING);
        if (pingId != null) {
            pingMsg.put(DdpMessageField.ID, pingId);
        }
        send(pingMsg,MSGTYPE.PING);
        if (resultListener != null) {
            // store listener for callbacks
            mMsgListeners.put(pingId, resultListener);
        }
    }

    /**
     * Converts DDP-formatted message to JSON and sends over web-socket
     * 
     * @param msgParams parameters for DDP msg
     */
    public void send(Map<String, Object> msgParams,MSGTYPE type) {
        String json = mGson.toJson(msgParams);
        /*System.out.println*/log.debug("Sending {}", json);
        System.out.println("\t +++++++++ Sending {}"+ json);
        if(((type == MSGTYPE.SUBSCRIPTION)||(type == MSGTYPE.RESUME))&&(!subscriptionMsg.contains(json))){
        	System.out.println("\t +++++++++ Inside Sending {}"+ json + " type: "+type);
    		subscriptionMsg.add(json);
    	}
        
        try {
        	System.out.println("\tsend: "+json);
        	System.out.println("\t ~~ mWsClient.getReadyState(): "+mWsClient.getReadyState());
        	if((mWsClient.getReadyState()!= READYSTATE.OPEN)&&(!this.pendingMsgQueue.contains(json))){
        		this.pendingMsgQueue.add(json);
        	}
        	
        	this.mWsClient.send(json);
        } catch (WebsocketNotConnectedException ex) {
        	if(!this.pendingMsgQueue.contains(json)){
        		this.pendingMsgQueue.add(json);
        	}
            handleError(ex);
            if(getState()!=CONNSTATE.Closed){
            	mConnState = CONNSTATE.Closed;
            }
        }
    }

    /**
     * Notifies observers of this DDP client of messages received from the
     * Meteor server
     * 
     * @param msg received msg from websocket
     */
    @SuppressWarnings("unchecked")
    public void received(String msg) {
         /*System.out.println*/log.debug("Received response: {}", msg);
         System.out.println("Received response: {}" + msg);//TODO
         
        this.setChanged();
        // generic object deserialization is from
        // http://programmerbruce.blogspot.com/2011/06/gson-v-jackson.html
        Map<String, Object> jsonFields = mGson.fromJson((String) msg,
                HashMap.class);
        this.notifyObservers(jsonFields);

        // notify any command listeners if we get updated or result msgs
        String msgtype = (String) jsonFields
                .get(DdpMessageField.MSG.toString());
        if (msgtype == null) {
            // ignore {"server_id":"GqrKrbcSeDfTYDkzQ"} web socket msgs
            return;
        }
        if (msgtype.equals(DdpMessageType.UPDATED)) {
            ArrayList<String> methodIds = (ArrayList<String>) jsonFields
                    .get(DdpMessageField.METHODS);
            for (String methodId : methodIds) {
                DDPListener listener = (DDPListener) mMsgListeners.get(methodId);
                if (listener != null) {
                    listener.onUpdated(methodId);
                }
            }
        } else if (msgtype.equals(DdpMessageType.READY)) {
            ArrayList<String> methodIds = (ArrayList<String>) jsonFields
                    .get(DdpMessageField.SUBS);
            for (String methodId : methodIds) {
                DDPListener listener = (DDPListener) mMsgListeners.get(methodId);
                if (listener != null) {
                    listener.onReady(methodId);
                }
            }
        } else if (msgtype.equals(DdpMessageType.NOSUB)) {
            String msgId = (String) jsonFields.get(DdpMessageField.ID
                    .toString());
            DDPListener listener = (DDPListener) mMsgListeners.get(msgId);
            mMsgListeners.remove(msgId);
            if (listener != null) {
                listener.onNoSub(msgId, (Map<String, Object>) jsonFields
                        .get(DdpMessageField.ERROR));
               // mMsgListeners.remove(msgId);
            }
        } else if (msgtype.equals(DdpMessageType.RESULT)) {
            String msgId = (String) jsonFields.get(DdpMessageField.ID
                    .toString());
            if (msgId != null) {
                DDPListener listener = (DDPListener) mMsgListeners.get(msgId);
                mMsgListeners.remove(msgId);
                if (listener != null) {
                    listener.onResult(jsonFields);
                    //mMsgListeners.remove(msgId);
                }
            }
        } else if (msgtype.equals(DdpMessageType.CONNECTED)) {
            mConnState = CONNSTATE.Connected;
            sendPendingMsg();//TODO
            resetReconnectCounter();
            if(getReconnectCount()>0){
            	setHasReconnected(true);
            	if(getResumeMsg()!=null)
            	{
            		this.resume();
            	}
            	resubscribe();
            }
        } else if (msgtype.equals(DdpMessageType.CLOSED)) {
        	System.out.println("\t\tmeteor server is down || connstate: "+mConnState);
        	mConnState = CONNSTATE.Closed;
        	if(getErrorListener()!=null){
        		errorListener.onResult(jsonFields);
        	}
        } else if (msgtype.equals(DdpMessageType.PING)) {
        	//PING sent by server
            String pingId = (String) jsonFields.get(DdpMessageField.ID
                    .toString());
            // automatically send PONG command back to server
            Map<String, Object> pongMsg = new HashMap<String, Object>();
            pongMsg.put(DdpMessageField.MSG, DdpMessageType.PONG);
            if (pingId != null) {
                pongMsg.put(DdpMessageField.ID, pingId);
            }
            send(pongMsg,MSGTYPE.PONG);
        } else if (msgtype.equals(DdpMessageType.PONG)) {
        	//PONG send by server
            String pingId = (String) jsonFields.get(DdpMessageField.ID
                    .toString());
            // let listeners know a Pong happened
            DDPListener listener = (DDPListener) mMsgListeners.get(pingId);
            mMsgListeners.remove(pingId);
            if (listener != null) {
                listener.onPong(pingId);
                //mMsgListeners.remove(pingId);
            }
        } else if (msgtype.equals(DdpMessageType.CHANGED)) { //TODO
			System.out.println("\t changed: " + msgtype);
			String subscribeListenerId = (String) jsonFields.get(DdpMessageField.COLLECTION.toString());
			if (subscribeListenerId != null) {
				System.out.println("\t msgId: " + subscribeListenerId);
				DDPListener subscribeListener = (DDPListener) mMsgListeners.get(subscribeListenerId);
				System.out.println("\t listener out: " + subscribeListener);
				if (subscribeListener != null) {
					System.out.println("\t listener in: " + subscribeListener);
					System.out.println("\t jsonFields: " + jsonFields);
					subscribeListener.onChanged(jsonFields);
				}
			}
		} else if (msgtype.equals(DdpMessageType.ERROR)) { // Receive this
															// message when
															// there is no
															// connection to the
															// meteor server
			System.out.println("\t\tno connection with meteor server || connstate: "+mConnState);
			if(getErrorListener()!=null){
        		errorListener.onResult(jsonFields);
        	}
		}
    }
    
    private void reconnect() {
    	disconnect();
    	connect();
    }
    
    public void sendPendingMsg(){
    	String pendingMsg;
		// Send out all pending messages in the queue
		while ((pendingMsg = this.pendingMsgQueue.poll()) != null) {
			this.mWsClient.send(pendingMsg);
			System.out.println("~~ Send Pending Messages: " + pendingMsg);
		}
    }
    
    public void resubscribe(){
    	for(String msg : subscriptionMsg){
    		System.out.println("  Resubscribing: "+msg);
    		this.mWsClient.send(msg);
    	}
    }
    
    private Map<String, Object> resumeMsg;
    
    public void setResumeMsg(String username, String authToken, String macAddress) {
    	Object[] params = new Object[2];
		Map<String, Object> userInformation = new LinkedHashMap<String, Object>();
		userInformation.put("resume", authToken);
		Map<String, Object> userUsername = new HashMap<String, Object>();
		userUsername.put("username", username);
		userInformation.put("user", userUsername);
		// machine id
		Map<String, Object> userMacAddress = new HashMap<String, Object>();
		userMacAddress.put("machineId", macAddress);
		params[0] = userInformation;
		params[1] = userMacAddress;

		resumeMsg = this.resumeMsg("login", params, null);
    }
    
    public Map<String, Object> getResumeMsg() {
		return resumeMsg;
	}

	/**
     * Call a meteor method with the supplied parameters
     * 
     * @param method name of corresponding Meteor method
     * @param params arguments to be passed to the Meteor method
     * @param resultListener DDP command listener for this method call
     * @return ID for next command
     */
    public Map<String, Object> resumeMsg(String method, Object[] params, DDPListener resultListener) {
        Map<String, Object> resumeMsg = new HashMap<String, Object>();
        resumeMsg.put(DdpMessageField.MSG, DdpMessageType.METHOD);
        //TODO: Try out with social media login
        resumeMsg.put(DdpMessageField.METHOD, "login");
        resumeMsg.put(DdpMessageField.PARAMS, params);
        
        int id = addCommmand(resultListener/*
                                            * "method,"+method+","+Arrays.toString
                                            * (params)
                                            */);
        resumeMsg.put(DdpMessageField.ID, Integer.toString(id));
        return resumeMsg;
    }
    
    public void resume(){
    	send(this.getResumeMsg(), MSGTYPE.RESUME);
    }
    
	/**
     * @return current DDP connection state (disconnected/connected/closed)
     */
    public CONNSTATE getState() {
        return mConnState;
    }

	public DDPListener getErrorListener() {
		return errorListener;
	}

	public void setErrorListener(DDPListener errorListener) {
		this.errorListener = errorListener;
	}

	public int getReconnectCount() {
		return reconnectCount;
	}

	public void setReconnectCount(int reconnectCount) {
		this.reconnectCount = reconnectCount;
		//If count is 0 or less, it means it will not reconnnect
		if(reconnectCount >0){
			setReconnectCounter(reconnectCount);
			setHasReconnected(false);
		}
	}

	
	public int getReconnectCounter() {
		return reconnectCounter;
	}
	

	public void setReconnectCounter(int reconnectCounter) {
		this.reconnectCounter = reconnectCounter;
	}
	
	public void minusReconnectCounter(){
		setReconnectCounter(getReconnectCounter()-1);
	}
	
	public void resetReconnectCounter(){
		setReconnectCounter(getReconnectCount());
	}
	

	public int getDelayReconnectTime() {
		return delayReconnectTime;
	}

	public void setDelayReconnectTime(int delayReconnectTime) {
		this.delayReconnectTime = delayReconnectTime;
	}

	public boolean isHasReconnected() {
		return hasReconnected;
	}

	public void setHasReconnected(boolean hasReconnected) {
		this.hasReconnected = hasReconnected;
	}
}
