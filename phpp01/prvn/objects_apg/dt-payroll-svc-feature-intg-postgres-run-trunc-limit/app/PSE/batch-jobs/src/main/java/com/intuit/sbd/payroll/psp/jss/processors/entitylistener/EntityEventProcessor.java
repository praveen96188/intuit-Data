package com.intuit.sbd.payroll.psp.jss.processors.entitylistener;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.intuit.platform.messaging.pulsar.jms.client.PulsarConnectionFactory;
import com.intuit.platform.messaging.pulsar.jms.client.PulsarQueue;
import com.intuit.pmo.client.model.PayrollCheckResponse;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.amo.DatabaseFailureException;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.EntityUpdate;
import com.intuit.sbd.payroll.psp.domain.OfferingServiceCharge;
import com.intuit.sbd.payroll.psp.domain.Status;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.gateways.amo.AMODTO;
import com.intuit.sbd.payroll.psp.gateways.amo.AMOExceptionListener;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.psp.dd.util.GsonFieldNameStrategy;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import javax.jms.*;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.naming.Context;
import javax.naming.InitialContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQMessageProducer;
import org.apache.activemq.command.ActiveMQQueue;
import org.hibernate.FlushMode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.jms.core.MessageCreator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.time.DateUtils;
import java.sql.Timestamp;
import com.intuit.sbd.payroll.psp.jss.util.DateTimeTypeAdapter;
import com.intuit.sbd.payroll.psp.jss.util.DateTypeAdapter;

/**
 * This job is used to publish entity message to the
 * internal queue 
 * It also does exponential retry of message
 * If failure count is greater than a number in time 
 * window then it will pause and retry after sometime
 * 
 * @author dchoudhary1
 *
 */
@ScheduledJob(name = "EntityEvent", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class EntityEventProcessor extends JSSBatchJob{

    private SpcfCalendar mRunDate;
    private Boolean mPriorDate = false;

    
    public EntityEventProcessor(String[] pArguments) {
        super(pArguments);
    }

    public EntityEventProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    public SpcfCalendar getRunDate() {
        return mRunDate;
    }

    public void setRunDate(SpcfCalendar pRunDate) {
        mRunDate = pRunDate;
    }

    public Boolean isPriorDate() {
        return mPriorDate;
    }

    public void setIsPriorDate(Boolean pPriorDate) {
        mPriorDate = pPriorDate;
    }

    @Override
    protected void validateRuntimeParameters() {
    	
    }
    
    @Override
    protected void validateStepRuntimeParameters(String stepName) {
        validateRuntimeParameters();
    }

    @Override
    protected void execute() {
        getLogger().info("Starting Entity Event processor");

        StopWatch timer = StopWatch.startTimer();

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.EntityEventBatchJob));

        executeStep(EntityEventPublish.class);

        getLogger().info("Completed Entity Event processor. Elapsed time: " + timer.stop().getElapsedTimeString());	
    }
    
    public static class EntityEventPublish extends JSSBatchJobStep<EntityEventProcessor> {
    	
    	private static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZ";
    	DateTimeFormatter longFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");
        public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

		private int interval;
        private int minPoolSize;
        private int maxPoolSize ;
        private int maxWait;
        private int batchSize;
      
        public void execute() {
        	try{
	        		//gets the list of messages in batch size and publish to queue when retry count < configured time
	                batchSize = SystemParameter.findIntValue(SystemParameter.Code.ENTITY_BATCH_SIZE, 50);
	                interval = SystemParameter.findIntValue(SystemParameter.Code.ENTITY_THREAD_POOL_INTERVAL, 60);
	                maxWait = SystemParameter.findIntValue(SystemParameter.Code.ENTITY_THREAD_POOL_MAX_WAIT, 5 * 60);
	                minPoolSize = SystemParameter.findIntValue(SystemParameter.Code.ENTITY_MIN_THREAD_POOL_SIZE, 10);
	                maxPoolSize = SystemParameter.findIntValue(SystemParameter.Code.ENTITY_MAX_THREAD_POOL_SIZE, 40);
	
	                getLogger().info("system properties: "+batchSize+" "+interval+" "+maxWait+" "+minPoolSize+" "+maxPoolSize);	
	
	                
					PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
	
					DateTime nowOld = DateTime.now();
		    		getLogger().info("nowOld {}",nowOld);

					DateTime now = DateTime.now().minusSeconds( 15 );
				
					//find
					List<RangeMessage> rangeMessages =findUnprocessedEntityMessage(now,batchSize);
	                
					getLogger().info("rangeMessages {}", rangeMessages);
	                
	                JmsTemplate jmstemplate=null;
	                if(rangeMessages.size()>0){
	                	jmstemplate=initializepublisher();	                
		            	getLogger().info("The thread start : " + Thread.currentThread().getId());
		                StopWatch timer1 = StopWatch.startTimer();
						multithreadMessageProcessing(rangeMessages,jmstemplate);
				        getLogger().info("Completed processing {} message in :{} ", rangeMessages.size(), timer1.stop().getElapsedTimeString());	
	                }
			        updateLastProcessedTime(now);
			        PayrollServices.commitUnitOfWork();
                }catch (Throwable t) {
                PayrollServices.rollbackUnitOfWork();
                getLogger().error("EntityEventPublish: exception message {}"+t);
                throw new RuntimeException("Exception in EntityEventPublish job "+ getClass().getSimpleName(), t.getCause());
            }finally {
				PayrollServices.rollbackUnitOfWork();
			}
        }
        
        private void updateLastProcessedTime(DateTime lastProcessedTime) {
			getLogger().info("updateLastProcessedTime start");

        	//lastProcessedTime = lastProcessedTime.toLocal();
    		getLogger().info("Update the SystemParameter Code LAST_PROCESSED_TIME to {} ",
    				lastProcessedTime);
    		DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateTime().withZoneUTC();
    		SystemParameter.update(SystemParameter.Code.LAST_PROCESSED_TIME,
    				DATE_FORMAT.print(lastProcessedTime));

			getLogger().info("updateLastProcessedTime end"+DATE_FORMAT.print(lastProcessedTime));
			getLogger().info("updateLastProcessedTime end strin"+lastProcessedTime.toString());
			
		}

        /**
         * findUnprocessedEntityMessage get unprocessed message
         * @param now
         * @return
         */
		private List<RangeMessage> findUnprocessedEntityMessage(DateTime now, int batchSize) {
    		DateTime lastProcessedTime = findLastProcessedTime(
    				SystemParameter.Code.LAST_PROCESSED_TIME); 

    		getLogger().info("SystemParameter Code LAST_PROCESSED_TIME is {} and  isLocal {}",
    				lastProcessedTime, lastProcessedTime);
    		
    		getLogger().info("now {}",now);
    		

    		Timestamp sqltimestamp = jodaToSQLTimestamp(lastProcessedTime);
    		
    		getLogger().info("Last processed time in timestamp {}",sqltimestamp);
    		
    		Timestamp sqltimestampnow = jodaToSQLTimestamp(now);

    		getLogger().info("Now processed time in timestamp  {}",sqltimestampnow);

    		List<Object[]> rangesMessageObjects = Application.executeNamedQuery("findRangePartitions",
    				new String[] { "lastProcessedTimeStamp", "now", "BatchSize" },
    				new Object[] { sqltimestamp, sqltimestampnow, batchSize });

    		if(rangesMessageObjects!=null){
    			getLogger().info("size of range message"+rangesMessageObjects.size());
    		}else{
    			getLogger().info("range message is null");
    		}
    		
    		List<RangeMessage> rangeMessages = new ArrayList<>();
    		rangesMessageObjects.forEach(rangesMessageObject -> {
    			
    			SpcfCalendar startTime = (SpcfCalendar)rangesMessageObject[0];
    			SpcfCalendar endTime =  (SpcfCalendar)rangesMessageObject[1];

    			getLogger().info( "spcf starttime{} endtime{}:", startTime,endTime);
    			        		
    			DateTime dtstartTime1=new DateTime(startTime.toLocal().getTimeInMilliseconds()).toDateTime(DateTimeZone.UTC);
    			DateTime dtendTime1=new DateTime(endTime.toLocal().getTimeInMilliseconds()).toDateTime(DateTimeZone.UTC);
    			
    			getLogger().info( "dtstartTime1{} dtendTime1{}:", dtstartTime1,dtendTime1);

    			RangeMessage message = new RangeMessage();

    			if (Objects.isNull(startTime)) {
    				throw new RuntimeException("Start time of the Range Message cannot be null");
    			}

    			if (Objects.isNull(endTime)) {
    				throw new RuntimeException("End time of the Range Message cannot be null");
    			}

    			message.setStartTime(dtstartTime1);
    			message.setEndTime(dtendTime1);

    			getLogger().info("SystemParameter Code LAST_PROCESSED_TIME is {} and  isLocal {}",
    					message.getStartTime(), message.getEndTime());
    			rangeMessages.add(message);
    		});
			getLogger().info("findUnprocessedEntityMessage end");

    		return rangeMessages;
		}
        
        
		/**
		 * findLastProcessedTime to get last processed time
		 * @param pCode
		 * @return
		 */
    	private DateTime findLastProcessedTime(SystemParameter.Code pCode) {
			getLogger().info("findLastProcessedTime start");

    		String lastProcessedTimeString = SystemParameter
    				.findStringValue(SystemParameter.Code.LAST_PROCESSED_TIME);

    		DateTime lastProcessedTime = null;
    		try {
        		DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateTime().withZoneUTC();
        		lastProcessedTime=DATE_FORMAT.parseDateTime(lastProcessedTimeString);

    		} catch (Exception e) {
    			lastProcessedTime = null;
    			getLogger().info("findLastProcessedTime didnt find"+lastProcessedTime+" "+e.getMessage());
    		}
			getLogger().info("findLastProcessedTime end"+lastProcessedTime);

    		return lastProcessedTime;
    	}

		private String getConnectionUrl() {
				getLogger().info("OMS3: Pulsar in getConnectionURL");
				return ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_entity_jms_pulsar_broker_url");
		}

		public ConnectionFactory getConnectionFactory(String username, String password, String connectionUrl) throws URISyntaxException {

				getLogger().info("OMS3: Pulsar in getConnectionFactory");
    			return new PulsarConnectionFactory(username, password, connectionUrl);

		}

		/**
         * initialize the queue connection
         * @return
         */
        private JmsTemplate initializepublisher() {
            int receiveTimeout = SystemParameter.findIntValue(SystemParameter.Code.ENTITY_MESSAGE_RECEIVE_TIMEOUT, 5000);
            int maxAttempts = SystemParameter.findIntValue(SystemParameter.Code.ENTITY_CONNECTION_RETRY_ATTEMPTS, 5);
            long waitInterval = SystemParameter.findIntValue(SystemParameter.Code.ENTITY_CONNECTION_RETRY_WAIT_PERIOD, 5000);

            getLogger().info("system properties receiveTimeout: "+receiveTimeout+" "+maxAttempts+" "+waitInterval);	

            Exception exception = null;
            boolean failure = true;



			String connectionurl = getConnectionUrl();
            String username = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_entity_jms_user");
            String password = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_entity_jms_password");
            String destination = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_entity_jms_destination");

            JmsTemplate template =null;

			getLogger().info("EntityEventPublisher: Broker url: {}", connectionurl);

			//connect to the queue
            for (int x = 0; x < maxAttempts; x++) {
                try {
                        getLogger().info("Setting connection factory");

                        ConnectionFactory producerConnectionFactory = getConnectionFactory(username,password,connectionurl);

                        QueueConnectionFactory factory = (QueueConnectionFactory) (producerConnectionFactory);

                        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(factory);
                        cachingConnectionFactory.setReconnectOnException(true);
                        cachingConnectionFactory.setExceptionListener(new AMOExceptionListener(cachingConnectionFactory));
                        cachingConnectionFactory.setCacheConsumers(true);

                        template = new JmsTemplate(cachingConnectionFactory);
						getLogger().info("OMS3: Pulsar in default destination");
						template.setDefaultDestination(new PulsarQueue(destination));

                        template.setReceiveTimeout(receiveTimeout);
                        template.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);                        
                        getLogger().info("added the jms template");

                        failure = false;
                        break;
                    } catch (Exception e) {
                        exception = e;
                        getLogger().warn("error in trying to connect to the queue"+e.getMessage(), e);
                        try {
                            Thread.sleep(waitInterval);
                            
                        } catch (Exception s) {
                            getLogger().warn("Failed to connect to queue"+s.getMessage());
                            
                        }
                    }
                }

                if (failure) {
                	getLogger().error("Error initializing the queue connectivity.", exception);
                    throw new ExceptionInInitializerError(exception);
                }

                return template;
        }


        /**
         * publish message in one thread
         * 
         * @param entityMsgs
         * @param template
         * @throws DatabaseFailureException
         */
        private void multithreadMessageProcessing( List<RangeMessage> rangeMessages, JmsTemplate template) throws DatabaseFailureException {
        	

            ExecutorService threadPool = null;
            int numberOfmessagesProcessed = 0;
            try {
                // Create threadPool with given parameters
                threadPool = new ThreadPoolExecutor(minPoolSize, maxPoolSize, interval, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
                CompletionService<String> completionService = new ExecutorCompletionService<String>(threadPool);

                // Process each dto in a separate thread
                for (RangeMessage rangeMsg : rangeMessages) {
                	numberOfmessagesProcessed++;
                    Runnable runnableTask = () -> {
                    	try{
                        	getLogger().info("The thread1 : " + Thread.currentThread().getId());
                    		publish(rangeMsg,template);
                        	getLogger().info("The thread 2: " + Thread.currentThread().getId());

                    	}
                    	catch(Exception ex){
                            getLogger().error("error in publishing "+ex.getMessage());
                    	}
                    	};
                Future<String> result = completionService.submit(runnableTask, "DONE");

                //check for completion of all the process then return
                while(result.isDone() == false) 
                {
                    try
                    {
                    	getLogger().info("The method return value : " + result.get() + Thread.currentThread().getId());
                        break;
                    } 
                    catch (InterruptedException | ExecutionException e) 
                    {
                    	getLogger().error("The publishing could not  complete with exception{} ", e.getMessage());
                    }
                     
                    //Sleep for 1 second
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                    	getLogger().error("The publishing could not  complete with exception{} ", e.getMessage());

                    }
                }
                 
                }
                } finally {
                if (threadPool != null) {
                	getLogger().info("The number of messages processed : " +numberOfmessagesProcessed);
                    ThreadingUtils.shutdownAndAwaitTermination(threadPool, interval, maxWait);
                }
            }
        }

        
        protected void publish(RangeMessage rangeMessage,JmsTemplate template){
            try {
                getLogger().info("publishing the entity message for id {}", rangeMessage.toString());
                if (template != null) {


                	Gson gsonval = null;

                	gsonval = new GsonBuilder().setPrettyPrinting().setFieldNamingStrategy(new GsonFieldNameStrategy())
               				.registerTypeAdapter(LocalDate.class, new DateTypeAdapter())
               				.registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter()).
               				create();
                	
                	try{
	            		String jsonInString = gsonval.toJson(rangeMessage);
	                    getLogger().info("jsonInString"+jsonInString);
                   
	                    template.send(new MessageCreator() {
	                    public Message createMessage(Session session) throws JMSException {
	                        Message notification = session.createTextMessage(jsonInString);
	                        return notification;
	                    }
                    
                    
	                });
	                    getLogger().info("deserialization testing");

	                    //deserialization
	                    RangeMessage rangeresponse = (RangeMessage) gsonval.fromJson(jsonInString,
	    						RangeMessage.class);
	                    getLogger().info("rangeresponse:StartTime"+rangeresponse.getStartTime());
	                    getLogger().info("rangeresponse:EndTime"+rangeresponse.getEndTime());
	
	                	}catch(Exception ex){
						getLogger().info("exception while deserialization"+ex.getMessage());
	                	}
  
                    getLogger().info("published the rangeMessage {} successfully", rangeMessage.toString());
                }
            }catch(Exception ex){
            	 getLogger().error("not able to publish the rangeMessage with error {} for the range{}",ex.getMessage(), rangeMessage.toString());

            }finally{
            	//Application.rollbackUnitOfWork();
            }
        }
        



    	public  Timestamp jodaToSQLTimestamp(DateTime localDateTime) {
    		getLogger().info("jodaToSQLTimestamp");
   		 Timestamp nowTimestamp = new Timestamp(CalendarUtils.convertLocalTimestamp(localDateTime.getMillis()).getTime());
   		 return nowTimestamp;
    	}
    }
    




}
