package org.ak.trafficController.messaging.mem;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This queue manager creates multiple in memory queue.
 * This helps user to directly create queue, add listeners, add data without taking any reference.
 * @author Amit Khosla
 */
public class InMemoryQueueManager {

	/**
	 * Queues mapping to this manager.
	 */
	protected Map<String, InMemoryQueue> queues = new HashMap<String, InMemoryQueue>();

	/**
	 * Dynamic setting registered to this manager.
	 */
	protected Map<String, DynamicSettings> dynamicSettings = new HashMap<>();
	
	/**
	 * Initialize a queue with passed name, consumer and number of consumers.
	 * The consumer passed is treated as direct consumer.
	 * @param queueName Name of queue
	 * @param consumer Consumer logic
	 * @param numberOfConsumers Number of consumers
	 * @param <T> type of consumer
	 */
	public <T> void initialize(String queueName, Consumer<T> consumer, int numberOfConsumers) {
		InMemoryQueue<T> queue = registerQueueIfAbsent(queueName);
		setConsumers(consumer, numberOfConsumers, queue);
	}
	
	/**
	 * This method sets the batch size which will be used by batch consumer for given queue having passed queue name.
	 * @param queueName Queue name
	 * @param batchSize batch size
	 */
	public void setBatchSize(String queueName, int batchSize) {
		InMemoryQueue queue = queues.get(queueName);
		if (queue != null) {
			queue.setBatchSize(batchSize);
		}
	}
	
	/**
	 * Initialize a queue with passed name, consumer and number of consumers.
	 * The consumer passed is treated as batch consumer.
	 * @param queueName Name of queue
	 * @param consumer Consumer logic
	 * @param numberOfConsumers Number of consumers
	 * @param <T> type of consumer
	 */
	public <T> void initializeForBatch(String queueName, Consumer<List<T>> consumer, int numberOfConsumers) {
		InMemoryQueue<T> queue = registerQueueIfAbsent(queueName);
		setBatchConsumers(consumer, numberOfConsumers, queue);
	}

	/**
	 * This makes sure only one queue is created for a given name.
	 * @param queueName Name of the queue
	 * @param <T> type of queue required
	 * @return Queue for the given queue
	 */
	synchronized protected <T> InMemoryQueue<T> registerQueueIfAbsent(String queueName) {
		InMemoryQueue<T> queue = queues.get(queueName);
		if (queue == null) {
			queue =new InMemoryQueue<T>(queueName);
			queues.put(queueName, queue);
		}
		return queue;
	}

	/**
	 * Set consumers in a given queue.
	 * If already attached consumers is less than required, this method register more.
	 * Else, this method unregister consumers.
	 * @param consumer Consumer of the queue which is looking for working on data present in queue.
	 * @param numberOfConsumers number of consumers required for queue to 
	 * @param queue Queue on which we are looking for setting consumers.
	 * @param <T> type of queue required
	 */
	protected <T> void setConsumers(Consumer<T> consumer,
			int numberOfConsumers, InMemoryQueue<T> queue) {
		queue.setDirectConsumer(consumer);
		queue.setDirectConsumerCount(numberOfConsumers);
	}
	
	/**
	 * Set batch consumers in a given queue. Batch consumers work on batch of data instead of individual records.
	 * If already attached consumers is less than required, this method register more.
	 * Else, this method unregister consumers.
	 * @param consumer Consumer of the queue which is looking for working on data present in queue.
	 * @param numberOfConsumers number of consumers required for queue to 
	 * @param queue Queue on which we are looking for setting consumers.
	 * @param <T> type of queue required
	 */
	protected <T> void setBatchConsumers(Consumer<List<T>> consumer,
			int numberOfConsumers, InMemoryQueue<T> queue) {
		queue.setBatchConsumer(consumer);
		queue.setBatchConsumerCount(numberOfConsumers);
	}
	
	
	/**
	 * Adds an item in queue. If queue is not created till now, this method also creates one.
	 * @param queueName Name of queue
	 * @param item Item to be added
	 * @return true if successfully adds item in queue
	 * @param <T> type of queue required
	 */
	public <T> boolean addAndRegisterIfRequired(String queueName, T item) {
		DynamicSettings<T> dynamicSetting = dynamicSettings.get(queueName);
		if (dynamicSetting != null) {
			return dynamicSetting.addItemInQueue(item);
		}
		InMemoryQueue<T> imq = registerQueueIfAbsent(queueName);
		imq.add(item);
		return true;
	}
	
	/**
	 * Adds all items in collection to queue. If queue is not created till now, this method also creates one.
	 * @param queueName Name of queue
	 * @param item Collection
	 * @param <T> type of queue required
	 * @return true if successfully adds in queue
	 */
	public <T> boolean addAndRegisterIfRequiredForCollection(String queueName, Collection<T> item) {
		DynamicSettings<T> dynamicSetting = dynamicSettings.get(queueName);
		if (dynamicSetting != null) {
			return dynamicSetting.addItemsInQueue(item);
		}
		InMemoryQueue<T> imq = registerQueueIfAbsent(queueName);
		imq.addAllFromCollection(item);
		return true;
	}
	
	/**
	 * Add message to the queue.
	 * @param queueName name of queue
	 * @param item Item to be added
	 * @param <T> type of queue
	 * @return true if successfully adds item to queue
	 */
	public <T> boolean add(String queueName, T item) {
		DynamicSettings<T> dynamicSetting = dynamicSettings.get(queueName);
		if (dynamicSetting != null) {
			return dynamicSetting.addItemInQueue(item);
		}
		InMemoryQueue<T> imq = queues.get(queueName);
		if (imq == null) {
			return false;
		}
		imq.add(item);
		return true;
	}
	
	/**
	 * Add messages to the queue.
	 * @param queueName Name of queue
	 * @param items Items to be added
	 * @param <T> type of queue
	 * @return true if successfully added
	 */
	public <T> boolean addItems(String queueName, Collection<T> items) {
		DynamicSettings<T> dynamicSetting = dynamicSettings.get(queueName);
		if (dynamicSetting != null) {
			return dynamicSetting.addItemsInQueue(items);
		}
		InMemoryQueue<T> imq = queues.get(queueName);
		if (imq == null) {
			return false;
		}
		imq.addAllFromCollection(items);
		return true;
	}
	
	/**
	 * Add listener to queue having passed name.
	 * @param queueName Name of queue
	 * @param consumer Consumer logic
	 * @param numberOfConsumers Number of consumers
	 * @param <T> type of queue
	 */
	public <T> void addListener(String queueName, Consumer<T> consumer, int numberOfConsumers) {
		InMemoryQueue<T> queue = queues.get(queueName);
		if (queue!=null) {
			setConsumers(consumer, numberOfConsumers, queue);
		}
	}
	
	/**
	 * Add batch listener to queue having passed name
	 * @param queueName Name of queue
	 * @param consumer Consumer logic
	 * @param numberOfConsumers Number of consumers
	 * @param <T> type of queue
	 */
	public <T> void addBatchListener(String queueName, Consumer<List<T>> consumer, int numberOfConsumers) {
		InMemoryQueue<T> queue = queues.get(queueName);
		if (queue!=null) {
			setBatchConsumers(consumer, numberOfConsumers, queue);
		}
	}
	
	
	/**
	 * Add message. If required create queue with no data.
	 * WARN - This should be used with thorough analysis as in case you passed wrong name for adding, it will create extra queue.
	 * @param queueName Name of queue
	 * @param item Data to be added
	 * @param <T> type of queue
	 */
	synchronized public <T> void addAndCreate(String queueName, T item) {
		InMemoryQueue<T> imq = registerQueueIfAbsent(queueName);
		imq.add(item);
	}

	/**
	 * Get number of consumers currently attached to queue.
	 * @param queueName Name of queue for which consumers count is required
	 * @return Number of consumers
	 */
	public Integer getDirectConsumerCount(String queueName) {
		InMemoryQueue inMemoryQueue = queues.get(queueName);
		if (inMemoryQueue == null) {
			return 0;
		}
		return inMemoryQueue.isDirectConsumerSet() ? inMemoryQueue.getDirectConsumerCount() : 0;
	}
	
	
	/**
	 * Get number of batch consumers currently attached to queue.
	 * @param queueName Name of queue for which consumers count is required
	 * @return Number of consumers
	 */
	public Integer getBatchConsumerCount(String queueName) {
		InMemoryQueue inMemoryQueue = queues.get(queueName);
		if (inMemoryQueue == null) {
			return 0;
		}
		return inMemoryQueue.isBatchConsumerSet() ? inMemoryQueue.getBatchConsumerCount() : 0;
	}

	/**
	 * Set Number of direct consumers of queue having passed name.
	 * @param queueName Name of queue
	 * @param consumerCount Consumer count
	 */
	public void setConsumerCount(String queueName, int consumerCount) {
		queues.get(queueName).setDirectConsumerCount(consumerCount);
	}
	
	/**
	 * Set number of batch consumers of queue having passed name.
	 * @param queueName Name of queue
	 * @param batchConsumerCount Batch consumer count to be set
	 */
	public void setBatchConsumerCount(String queueName, int batchConsumerCount) {
		queues.get(queueName).setBatchConsumerCount(batchConsumerCount);
	}
	
	/**
	 * This method makes queue dynamic. By dynamic it means that number of consumer will increase and reduce on basis of load.
	 * This method returns DynamicSettings by which one can configure particular queue behavior.
	 * @param queueName Queue Name
	 * @param <T> type of queue.
	 * @return Dynamic settings
	 */
	public <T> DynamicSettings<T> setDynamic(String queueName) {
		InMemoryQueue<T> queue = queues.get(queueName);
		DynamicSettings<T> settings = new DynamicSettings<T>().setQueue(queue);
		this.dynamicSettings.put(queueName, settings);
		return settings;
	}
	
	
	/**
	 * Shutdown all queues and dynamic settings.
	 */
	public void shutdown() {
		this.queues.values().forEach(InMemoryQueue::shutdown);
		this.dynamicSettings.values().forEach(DynamicSettings::shutdown);
	}
	
}
