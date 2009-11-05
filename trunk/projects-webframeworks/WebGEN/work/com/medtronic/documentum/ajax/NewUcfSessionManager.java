package com.medtronic.documentum.ajax;

// This is a re-impl of the decomiplation of the WDK UcfSessionManager, removing
// WDK dependencies for more pure invocation of UCF Content Transfer operations via
// Ajax or other non-WDK webapps. Ideally, our sole dependencies are DFC and UCF

// so far, all we did is remove tracing and change a WrapperRuntimeException to a RuntimeException
// -- thankfully, since there seems to be a lot going on in this class. 

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

import com.documentum.ucf.common.transport.TransportException;
import com.documentum.ucf.server.transport.ICommManager;
import com.documentum.ucf.server.transport.IServerSession;
import com.documentum.web.util.Base64;

public class NewUcfSessionManager
 implements Serializable
{
 private class ClientException extends RuntimeException
 {

     public String getMessage()
     {
         if(m_msg != null && m_msg.length() > 0)
             return m_msg;
         else
             return super.getMessage();
     }

     public void printStackTrace(PrintStream s)
     {
         if(m_msg != null && m_msg.length() > 0)
             s.println(m_msg);
         if(m_trace != null && m_trace.length() > 0)
             s.println(m_trace);
         else
             super.printStackTrace(s);
     }

     public void printStackTrace(PrintWriter s)
     {
         if(m_msg != null && m_msg.length() > 0)
             s.println(m_msg);
         if(m_trace != null && m_trace.length() > 0)
             s.println(m_trace);
         else
             super.printStackTrace(s);
     }

     private String m_msg;
     private String m_trace;

     ClientException(String msg, String trace)
     {
         m_msg = msg;
         m_trace = trace;
     }
 }

 private class RefCount
 {

     public void inc()
     {
         m_count++;
     }

     public void dec()
     {
         if(m_count == 0)
         {
             throw new IllegalStateException("Session already released");
         } else
         {
             m_count--;
             return;
         }
     }

     public int getCount()
     {
         return m_count;
     }

     private int m_count;

     private RefCount()
     {
         m_count = 0;
     }

 }

 private class SessionKey
 {

     public String getRequestKey()
     {
         return m_requestKey;
     }

     public String getSessionId()
     {
         return m_sessionId;
     }

     public boolean isSessionIdValid()
     {
         return m_sessionId != null && !m_sessionId.equals("null") && !m_sessionId.equals("-1");
     }

     public String getClientMsg()
     {
         return m_errorMsg;
     }

     public String getClientTrace()
     {
         return m_errorTrace;
     }

     public boolean equals(Object obj)
     {
         if(this == obj)
             return true;
         if(obj instanceof SessionKey)
         {
             SessionKey sk = (SessionKey)obj;
             return m_requestKey.equals(sk.m_requestKey);
         } else
         {
             return false;
         }
     }

     public int hashCode()
     {
         return m_requestKey.hashCode();
     }

     public String toString()
     {
         StringBuffer sb = new StringBuffer();
         String cname = getClass().getName();
         sb.append(cname.substring(cname.lastIndexOf('.') + 1));
         sb.append("[");
         if(m_sessionId != null && m_sessionId.length() > 0)
             sb.append(m_sessionId).append(';');
         sb.append(m_requestKey);
         sb.append("]");
         return sb.toString();
     }

     private String m_requestKey;
     private String m_sessionId;
     private String m_errorMsg;
     private String m_errorTrace;

     public SessionKey(String sessionStr, String msg)
     {
         if(sessionStr.length() == 0)
             throw new IllegalArgumentException("session string cannot be empty");
         StringTokenizer stok = new StringTokenizer(sessionStr, String.valueOf(';'));
         int countTokens = stok.countTokens();
         if(countTokens == 1)
             m_requestKey = stok.nextToken();
         else
         if(countTokens > 1)
         {
             m_sessionId = stok.nextToken();
             m_requestKey = stok.nextToken();
             if(m_requestKey.length() == 0)
                 throw new IllegalArgumentException("request key cannot be empty");
         } else
         {
             throw new IllegalArgumentException("invalid session id: " + sessionStr);
         }
         if(msg != null)
         {
             String delim = String.valueOf(';');
             stok = new StringTokenizer(msg, delim, true);
             String token;
             if(stok.hasMoreTokens() && !(token = stok.nextToken()).equals(delim))
             {
                 if(token != null && token.length() > 0)
                     m_errorMsg = Base64.decode(token);
                 if(stok.hasMoreTokens())
                     stok.nextToken();
             }
             if(stok.hasMoreTokens() && !(token = stok.nextToken()).equals(delim) && token != null && token.length() > 0)
                 m_errorTrace = Base64.decode(token);
         }
     }
 }

 NewUcfSessionManager(File workingDir)
 {
     m_workingDir = workingDir;
 }

 public synchronized boolean isSessionAvailable()
 {
     if(!m_unusedSessions.isEmpty())
     {
         for(int i = 0; i < m_unusedSessions.size(); i++)
         {
             SessionKey sessionKey = (SessionKey)m_unusedSessions.get(i);
             if(!isSessionReserved(sessionKey))
                 return true;
         }

     }
     if(!m_connectedSessions.isEmpty())
     {
         for(int i = 0; i < m_connectedSessions.size(); i++)
         {
             IServerSession session = (IServerSession)m_connectedSessions.get(i);
             if(!isSessionReserved(session))
                 return true;
         }

     }
     return isExpectingSessionId();
 }

 public synchronized IServerSession getSession()
 {
     IServerSession session = null;
     if(m_connectedSessions.isEmpty())
     {
         session = newSession();
         if(session != null)
         {
             RefCount rc = getRefCount(session);
             rc.inc();
             reserveConnectedSession(session);
         }
     } else
     {
         for(int i = 0; i < m_connectedSessions.size(); i++)
         {
             IServerSession s = (IServerSession)m_connectedSessions.get(i);
             if(isSessionReserved(s))
                 continue;
             session = s;
             RefCount rc = getRefCount(session);
             rc.inc();
             if(rc.getCount() == 1)
                 reserveConnectedSession(session);
         }

     }
     return session;
 }

 public synchronized void release(IServerSession session)
 {
     if(session == null)
         throw new NullPointerException("session cannot be null");
     RefCount rc = getRefCount(session);
     rc.dec();
     if(rc.getCount() == 0)
     {
         unreserveConnectedSession(session);
         if(!isInRequest())
             disconnectSession(session);
     }
 }

 public synchronized void cleanup()
 {
     if(isExpectingSessionId())
         newSession();
     for(int i = m_unusedSessions.size() - 1; i >= 0; i--)
     {
         SessionKey sessionKey = (SessionKey)m_unusedSessions.get(i);
         if(!isSessionReserved(sessionKey))
             disconnectUnusedSession(sessionKey);
     }

     for(int i = m_connectedSessions.size() - 1; i >= 0; i--)
     {
         IServerSession session = (IServerSession)m_connectedSessions.get(i);
         if(getRefCount(session).getCount() == 0)
             disconnectSession(session);
     }

 }

 void setInRequest(boolean inRequest)
 {
     if(inRequest)
         s_inRequest.set(Boolean.TRUE);
     else
         s_inRequest.set(null);
 }

 private boolean isInRequest()
 {
     return s_inRequest.get() != null;
 }

 synchronized void setCommMgr(ICommManager mgr)
 {
     if(m_commManager == null)
         m_commManager = mgr;
 }

 synchronized ICommManager getCommMgr()
 {
     return m_commManager;
 }

 synchronized void setLauncherCookie(String cookie)
 {
     m_ucfCookie = cookie;
 }

 synchronized String getLauncherCookie()
 {
     return m_ucfCookie;
 }

 synchronized void setLocale(Locale locale)
 {
     m_locale = locale;
 }

 synchronized void addNewId(String sessionStr, String msg)
 {
     SessionKey sessionKey = new SessionKey(sessionStr, msg);
     if(m_unusedSessions.contains(sessionKey))
         throw new IllegalArgumentException("Session already exists: " + sessionStr);
     m_unusedSessions.add(sessionKey);
     notifyAll();
 }

 synchronized void reserveSession(String requestKey)
 {
     SessionKey sessionKey = new SessionKey(requestKey, null);
     if(m_reservedSessions.containsKey(sessionKey) && !Thread.currentThread().equals(m_reservedSessions.get(sessionKey)))
         throw new IllegalStateException("request key " + sessionKey + " already reserved by " + m_reservedSessions.get(sessionKey));
     m_reservedSessions.put(sessionKey, Thread.currentThread());
 }

 synchronized void unreserveSession()
 {
     Thread cthread = Thread.currentThread();
     Object requestKey = m_reservedSessions.removeValue(cthread);
 }

 synchronized void shutdown()
 {
     for(int i = m_unusedSessions.size() - 1; i >= 0; i--)
     {
         SessionKey sessionKey = (SessionKey)m_unusedSessions.get(i);
         releaseUnusedSession(sessionKey);
     }

     for(int i = m_connectedSessions.size() - 1; i >= 0; i--)
     {
         IServerSession session = (IServerSession)m_connectedSessions.get(i);
         releaseSession(session);
     }

 }

 private IServerSession newSession()
 {
     SessionKey sessionKey = (SessionKey)m_reservedSessions.getKey(Thread.currentThread());
     if(sessionKey == null)
     {
         int i = 0;
         do
         {
             if(i >= m_unusedSessions.size())
                 break;
             sessionKey = (SessionKey)m_unusedSessions.get(i);
             if(!isSessionReserved(sessionKey))
                 break;
             i++;
         } while(true);
     } else
     {
         try
         {
             long start = System.currentTimeMillis();
             int index;
             for(long waitTime = 60000L; (index = m_unusedSessions.indexOf(sessionKey)) == -1; waitTime = 60000L - (System.currentTimeMillis() - start))
             {
                 if(waitTime <= 0L)
                     throw new IllegalStateException("ucf session wait timeout");
                 wait(waitTime);
             }

             sessionKey = (SessionKey)m_unusedSessions.get(index);
             if(sessionKey != null)
                 m_reservedSessions.put(sessionKey, Thread.currentThread());
         }
         catch(InterruptedException ignore) { }
     }
     if(sessionKey != null)
     {
         m_unusedSessions.remove(sessionKey);
         if(sessionKey.isSessionIdValid())
         {
             String sessionId = sessionKey.getSessionId();
             IServerSession session = newSession(sessionId);
             return session;
         } else
         {
             ClientException e = new ClientException(sessionKey.getClientMsg(), sessionKey.getClientTrace());
             throw new RuntimeException(e);
         }
     }
     return null;
 }

 private IServerSession newSession(String sessionId)
 {
     IServerSession session = m_commManager == null ? null : m_commManager.newSession(sessionId, m_workingDir);
     if(session != null)
     {
         m_connectedSessions.add(session);
         if(m_locale != null)
             session.setClientLocale(m_locale);
     }
     return session;
 }

 private RefCount getRefCount(IServerSession session)
 {
     RefCount c = (RefCount)m_refCount.get(session);
     if(c == null)
     {
         c = new RefCount();
         m_refCount.put(session, c);
     }
     return c;
 }

 private void disconnectUnusedSession(SessionKey sessionKey)
 {
     String sessionId = sessionKey.getSessionId();
     IServerSession session = newSession(sessionId);
     disconnectSession(session);
     m_unusedSessions.remove(sessionKey);
     m_reservedSessions.remove(sessionKey);
 }

 private void disconnectSession(IServerSession session)
 {
     if(session == null)
         return;
     String id = session.getUID();
     try
     {
         com.documentum.ucf.server.transport.requests.spi.IExitRequest exitRequest = session.getRequestFactory().newExitRequest();
         session.addRequest(exitRequest);
         session.execute();
     }
     catch(TransportException e)
     {
         e.printStackTrace();
     }
     finally
     {
         if(m_commManager != null)
             m_commManager.release(id);
         m_connectedSessions.remove(session);
         m_reservedConnectedSessions.remove(session);
     }
 }

 private void releaseUnusedSession(SessionKey sessionKey)
 {
     String sessionId = sessionKey.getSessionId();
     IServerSession session = newSession(sessionId);
     releaseSession(session);
     m_unusedSessions.remove(sessionKey);
     m_reservedSessions.remove(sessionKey);
 }

 private void releaseSession(IServerSession session)
 {
     if(session == null)
         return;
     String id = session.getUID();
     if(m_commManager != null)
         m_commManager.release(id);
     m_connectedSessions.remove(session);
     m_reservedConnectedSessions.remove(session);
 }

 private boolean isSessionReserved(SessionKey sessionKey)
 {
     Thread t = (Thread)m_reservedSessions.get(sessionKey);
     return t != null && !Thread.currentThread().equals(t);
 }

 private boolean isSessionReserved(IServerSession session)
 {
     Thread t = (Thread)m_reservedConnectedSessions.get(session);
     return t != null && !Thread.currentThread().equals(t);
 }

 private void reserveConnectedSession(IServerSession session)
 {
     Thread cthread = Thread.currentThread();
     if(m_reservedConnectedSessions.containsKey(session) && !cthread.equals(m_reservedConnectedSessions.get(session)))
         throw new IllegalStateException("session " + session + " already reserved by " + m_reservedConnectedSessions.get(session));
     m_reservedConnectedSessions.put(session, cthread);
 }

 private void unreserveConnectedSession(IServerSession session)
 {
     Object reservingThread = m_reservedConnectedSessions.remove(session);
 }

 private boolean isExpectingSessionId()
 {
     SessionKey reservedKey;
     return (reservedKey = (SessionKey)m_reservedSessions.getKey(Thread.currentThread())) != null && reservedKey.getSessionId() == null;
 }

 private final List m_unusedSessions = new ArrayList();
 private final List m_connectedSessions = new ArrayList();
 private final Map m_refCount = new WeakHashMap();
 private final BidiMap m_reservedSessions = new DualHashBidiMap();
 private final BidiMap m_reservedConnectedSessions = new DualHashBidiMap();
 private ICommManager m_commManager;
 private final File m_workingDir;
 private String m_ucfCookie;
 private Locale m_locale;
 private static final long MAX_SESSION_WAIT_TIME = 60000L;
 static final char ID_SEPARATOR = 59;
 private static final ThreadLocal s_inRequest = new ThreadLocal();

}
