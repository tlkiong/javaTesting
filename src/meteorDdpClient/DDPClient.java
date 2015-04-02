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
import java.util.Map;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.java_websocket.WebSocket.READYSTATE;
import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import org.jmock.expectation.Null;

import com.google.common.collect.BiMap;
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
    };
    
    public enum MSGTYPE {
    	PING,
    	SUBSCRIPTION,
    	PONG,
    	CALL,
    	CONNECT,
    	UNSUBSCRIBE
    };
    private CONNSTATE mConnState;
    /** current command ID */
    private int mCurrentId;
    /** Listeners for method functions */
    private Map<String, DDPListener> mMsgListeners;
    /** Listeners for subscribe functions */
    private BiMap<String, DDPListener> mSubscribeListeners;
    /** web socket client */
    private WebSocketClient mWsClient;
    /** web socket address for reconnections */
    private String mMeteorServerAddress;
    /** we can't connect more than once on a new socket */
    private boolean mConnectionStarted;
    /** Google GSON object */
    private final Gson mGson = new Gson();
    /** Queue to keep pending messages to send out once reconnect */
    Queue<String> pendingMsgQueue;
    /** Queue to keep subscription messages to send out once reconnect */
    Queue<String> subscriptionMsg;
    //private boolean isReconnect;
    private boolean hasReconnected;
    private DDPListener errorListener;
    private boolean isFirstTimeConnect;
    private int reconnectCount;
    private int reconnectCounter;
    private  int delayReconnectTime = 10000;//10 seconds default
    
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
    	setFirstTimeConnect(true);
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
     * @param meteorServerIp
     *            - IP of Meteor server
     * @param meteorServerPort
     *            - Port of Meteor server, if left null it will default to 3000
     * @throws Exception 
     */
    public DDPClient(String meteorServerIp, Integer meteorServerPort, DDPListener errorListener, int reconnectCount)
            throws Exception {
    	if(errorListener == null){
    		throw new Exception("ErrorListener cannot be null"); 
    	}
    	setFirstTimeConnect(true);
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
        mConnState = CONNSTATE.Disconnected;
        if (meteorServerPort == null)
            meteorServerPort = 3000;
        mMeteorServerAddress = (useSSL ? "wss://" : "ws://")
                + meteorServerIp + ":"
                + meteorServerPort.toString() + "/websocket";
        this.mCurrentId = 0;
        this.mMsgListeners = new ConcurrentHashMap<String, DDPListener>();
        createWsClient(mMeteorServerAddress);
        
        if (useSSL) {
            try {
                // set up trustkeystore w/ Java's default trusted 
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                KeyStore trustKeystore = null;
                trustManagerFactory.init(trustKeystore);
                /* 
                for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
                    if (trustManager instanceof X509TrustManager) {
                        X509TrustManager x509TrustManager = (X509TrustManager)trustManager;
                    }
                }
                */
                SSLContext sslContext = null;
                sslContext = SSLContext.getInstance( "TLS" );
                sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
                // now we can set the web service client to use this SSL context
                getmWsClient().setWebSocketFactory( new DefaultSSLWebSocketClientFactory( sslContext ) );
            } catch (KeyStoreException e) {
                log.warn("Error accessing Java default cacerts keystore {}", e);
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
        this.setmWsClient(new WebSocketClient(new URI(meteorServerAddress)) {

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
        });
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
        //TODO
        received(closeMsg);
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
    public void connect() {
        if ((this.getmWsClient().getReadyState() == READYSTATE.CLOSED)||(this.getmWsClient().getReadyState() == READYSTATE.CLOSING)) {
            // we need to create a new wsClient because a closed websocket cannot be reused
            try {
            	System.out.println("connect ready state: "+this.getmWsClient().getReadyState());
                createWsClient(mMeteorServerAddress);
            } catch (URISyntaxException e) {
                // we shouldn't get URI exceptions because the address was validated in initWebsocket
            }
        } else {
        	System.out.println("Readystate: "+this.getmWsClient().getReadyState());
        }
        if (!mConnectionStarted) {
            // only do the connect if no connection attempt has been done for this websocket client
            this.getmWsClient().connect();
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~ CONNECT()! = mConnectionStarted: "+mConnectionStarted + " this.getmWsClient().getReadyState(): "+this.getmWsClient().getReadyState());
            mConnectionStarted = true;
        } else {
        	System.out.println("mConnectionStarted: "+mConnectionStarted);
        }
    }
    
    /**
     * Closes an open websocket connection.
     * This is async, so you'll get a close notification callback when it eventually closes.
     */
    public void disconnect() {
        if (this.getmWsClient().getReadyState() != READYSTATE.CLOSED) {
        	mConnState = CONNSTATE.Disconnected;
            mConnectionStarted=false;
            System.out.println("mConnState: "+mConnState+" mConnectionStarted: "+mConnectionStarted);
            this.getmWsClient().close();
            System.out.println("dc: "+this.getmWsClient().getReadyState());
            
            //setFirstTimeConnect(true);
        }
    }
    
    public void reconnect() {
    	try{
    		if (this.getmWsClient().getReadyState() != READYSTATE.CLOSED) {
            	mConnState = CONNSTATE.Disconnected;
                mConnectionStarted=false;
                System.out.println("mConnState: "+mConnState+" mConnectionStarted: "+mConnectionStarted);
                this.getmWsClient().close();
                System.out.println("dc: "+this.getmWsClient().getReadyState());
                
                //setFirstTimeConnect(true);
        		connect();
            }
    	} catch (Exception ex){
    		ex.printStackTrace();
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
        
        String id = addSubscribeCommmand("users",resultListener);
        
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
        try {
        	System.out.println("\tsend: "+json);
        	if(type == MSGTYPE.SUBSCRIPTION||type==MSGTYPE.CALL){
        		subscriptionMsg.add(json);
        	}
        	this.getmWsClient().send(json);
        } catch (WebsocketNotConnectedException ex) {
        	System.out.println(" pendingMsgQueue: "+json);
        	this.pendingMsgQueue.add(json);
            handleError(ex);
            mConnState = CONNSTATE.Closed;
            connect();
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
        System.out.println("message type: " + msgtype);//TODO
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
            if (listener != null) {
                listener.onNoSub(msgId, (Map<String, Object>) jsonFields
                        .get(DdpMessageField.ERROR));
                mMsgListeners.remove(msgId);
            }
        } else if (msgtype.equals(DdpMessageType.RESULT)) {
            String msgId = (String) jsonFields.get(DdpMessageField.ID
                    .toString());
            if (msgId != null) {
                DDPListener listener = (DDPListener) mMsgListeners.get(msgId);
                if (listener != null) {
                    listener.onResult(jsonFields);
                    mMsgListeners.remove(msgId);
                }
            }
        } else if (msgtype.equals(DdpMessageType.CONNECTED)) {
            mConnState = CONNSTATE.Connected;
            
            sendPendingMgs();
            if((getReconnectCount()>0)&&(!isHasReconnected())){
            	resetReconnectCounter();
            	setHasReconnected(true);
            	resubscribe();
            }
        } else if (msgtype.equals(DdpMessageType.CLOSED)) {
        	// When meteor server is down
        	
        	if(getIsFirstTimeConnect()){
				System.out.println("DdpMessageType.ERROR Set false??");
				setFirstTimeConnect(false);
			} else {
				if(getReconnectCount()==0){
					clearPendingMsgs();
				} else if ((getReconnectCount()>0)&&(hasReconnected)){
					setHasReconnected(false);
				}
			}
        	
        	System.out.println("\t\tmeteor server is down || connstate: "+mConnState);
        	mConnState = CONNSTATE.Closed;
			errorListener.onResult(jsonFields);
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
            if (listener != null) {
                listener.onPong(pingId);
                mMsgListeners.remove(pingId);
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
			errorListener.onResult(jsonFields);
		}
    }
    
    private void resetReconnectCounter() {
    	setReconnectCounter(getReconnectCount());
	}

	public void sendPendingMgs(){
    	String pendingMsg;
		// Send out all pending messages in the queue
		while ((pendingMsg = this.pendingMsgQueue.poll()) != null) {
			this.getmWsClient().send(pendingMsg);
			System.out.println("Send Pending Messages: " + pendingMsg);
		}
    }
    
    /**
     * Will empty out pendingMsgQueue
     */
    public void clearPendingMsgs(){
    	this.pendingMsgQueue.clear();
    	System.out.println("pendingMsgQueue cleared: "+pendingMsgQueue);
    }
    
    public void resubscribe(){
    	//TODO: send out all the data in the subscribe list!
    	for(String msg : subscriptionMsg){
    		System.out.println("Resubscribing: "+msg);
    		this.getmWsClient().send(msg);
    	}
    }
    
    /**
     * @return current DDP connection state (disconnected/connected/closed)
     */
    public CONNSTATE getState() {
        return mConnState;
    }

	
//    
//    public boolean isReconnect() {
//		return isReconnect;
//	}
//    
//
//	private void setReconnect(boolean isReconnect) {
//		this.isReconnect = isReconnect;
//	}
//	

	public DDPListener getErrorListener() {
		return errorListener;
	}
	

	private void setErrorListener(DDPListener errorListener) {
		this.errorListener = errorListener;
	}

	public boolean getIsFirstTimeConnect() {
		return isFirstTimeConnect;
	}

	public void setFirstTimeConnect(boolean isFirstTimeConnect) {
		this.isFirstTimeConnect = isFirstTimeConnect;
	}

	public WebSocketClient getmWsClient() {
		return mWsClient;
	}

	private void setmWsClient(WebSocketClient mWsClient) {
		this.mWsClient = mWsClient;
	}

	public boolean ismConnectionStarted() {
		return mConnectionStarted;
	}

	public void setmConnectionStarted(boolean mConnectionStarted) {
		this.mConnectionStarted = mConnectionStarted;
	}

	public boolean isHasReconnected() {
		return hasReconnected;
	}

	public void setHasReconnected(boolean hasReconnected) {
		this.hasReconnected = hasReconnected;
	}

	
	public int getReconnectCount() {
		return reconnectCount;
	}

	
	private void setReconnectCount(int reconnectCount) {
		this.reconnectCount = reconnectCount;
		setReconnectCounter(reconnectCount);
	}

	
	public int getReconnectCounter() {
		return reconnectCounter;
	}

	
	public void setReconnectCounter(int reconnectCounter) {
		this.reconnectCounter = reconnectCounter;
	}

	public int getDelayReconnectTime() {
		return delayReconnectTime;
	}

	public void setDelayReconnectTime(int delayReconnectTime) {
		this.delayReconnectTime = delayReconnectTime;
	}
}
