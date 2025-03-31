# **Apache Kafka**

## **What is Kafka?**
Apache Kafka is a distributed event streaming platform designed for:
* High-throughput (millions of messages per second)
* Real-time data streaming (events like clicks, transactions, logs)
* Durability (stores messages for long periods)
* Scalability (handles massive data loads across clusters)

### **Use Cases:**
✔ Real-time analytics (Uber, Netflix)  
✔ Log aggregation (tracking app errors)  
✔ Microservices communication (decoupling services)

Throughput in computer systems refers to the rate at which a system processes information or data over a given period.
High throughput indicates that a system processes large amounts of data or performs many operations efficiently within a short time frame.
Low throughput means the system processes data or operations at a slower rate, often due to limitations such as congestion, hardware inefficiencies, or software bottlenecks.

## **Overall Architecture**

### **MySQL vs. Kafka:**
#### **When is MySQL Enough?**
MySQL (or any traditional database) works well when:
* You need to store and retrieve structured data (like user profiles, orders, etc.)
* You mostly do simple reads/writes (e.g., "Get user details" or "Save a new order")
* Your system is not dealing with massive real-time data streams

#### **When Do You Need Kafka?**
**Real-time Data Streaming**
* Example: Uber needs to track millions of ride updates (location, ETAs) in real-time

**Decoupling Systems**
* Example: An e-commerce site sends order data to inventory, payment, and shipping systems

**Handling High Volume**
* Example: Facebook processes billions of likes, comments, and posts daily
* MySQL would choke under such load, but Kafka buffers and distributes the data smoothly

#### **Simple Analogy**
* MySQL = A notebook where you write down final answers
* Kafka = A live news ticker that broadcasts updates to many people at once

Many systems use both:
* Kafka for real-time event streaming (e.g., "User clicked button")
* MySQL for storing final processed data (e.g., "User's total clicks")

It is advisable to not use Kafka if your system is having very simple operations that can be handled by RDBMS database.

## **Key Kafka Concepts**

### **1. Producer**
**What?** A sender (app/service) that **publishes messages** (events) to Kafka.  
**Example:**  
- A **weather app** (producer) sends temperature updates to Kafka.  
- A **food delivery app** sends "order placed" events.  

### **2. Consumer**
**What?** A receiver (app/service) that **reads messages** from Kafka.  
**Example:**  
- A **notification service** (consumer) reads "order placed" events to send SMS.  
- A **fraud detection system** reads transactions to flag fraud.  

### **3. Event (or Message)**
**What?** A piece of data (e.g., "User X clicked button Y at 5 PM").  
**Example:**  
- `{ "user": "Alice", "action": "login", "time": "2023-10-01" }`  

### **4. Cluster**
**What?** A group of computers (servers) working together to run Kafka.  
**Example:**  
- Like a **team of postmen** (cluster) delivering letters (messages) efficiently.  

### **5. Broker**
**What?** A single Kafka server (part of a cluster) that stores messages.  
**Example:**  
- One **post office branch** (broker) in a postal network (cluster).  

### **6. Topic**
**What?** A named channel (like a folder) where messages are stored.  
**Example:**  
- A **newspaper** (topic) has different sections: *Sports*, *Politics*, *Weather*.  
- Kafka topics: `orders`, `user_logins`, `payment_events`.  

### **7. Partition**
**What?** A topic is split into partitions for **parallel processing**.  
**Example:**  
- A **highway with multiple lanes** (partitions) to avoid traffic jams.  
- Topic `orders` has 3 partitions → 3x faster processing.  

### **8. Offset**
**What?** A unique ID for each message in a partition (like a page number in a book).  
**Example:**
- Partition 0: `Offset 0 (Order 1)`, `Offset 1 (Order 2)`, ...  
- Consumers track offsets to know "where they left off."  

### **9. Topic Partition**
A specific partition within a topic.  
**Example:**
- `orders` topic → Partition 0, Partition 1, Partition 2.  

### **10. Consuming Topics**
Consumers read messages from topics (like subscribing to a YouTube channel).  
**Example:** 
- A billing service consumes the `payments` topic to generate invoices.  
- A dashboard consumes `website_clicks` to show real-time stats.  

## **Real-World Kafka Flow**
1. Producer (e.g., Uber app) → Sends "ride booked" event → Topic `rides` (Partition 0).  
2. Consumer 1 (Billing service) reads event → charges the user.  
3. Consumer 2 (Driver app) reads event → assigns a driver.  

If we want to identify one message we need three things: topic id, partition id, and offset.

## **Apache Kafka Architecture Flow**

### **How Topic Partitioning Works in Kafka**
Imagine a Kafka topic is like a book, and partitions are like chapters in that book. Each chapter (partition) holds a part of the story (data).

#### **Why Partitioning?**
* **Parallel Processing**: Multiple consumers can read different partitions at the same time (like multiple people reading different chapters)
* **Scalability**: More partitions = More throughput (handles more data)
* **Fault Tolerance**: If one partition fails, others keep working

#### **How Partitioning Happens?**
When a producer sends a message, Kafka decides which partition it goes to based on:

1. **Key-Based Routing** (Default if a key is provided)
   * Same key → Same partition (ensures order)
   * Example:
     * If user_id=101 always goes to Partition 0, all messages for that user stay in order

2. **Round-Robin** (If no key is provided)
   * Messages are distributed evenly across partitions
   * Example:
     * Message 1 → Partition 0
     * Message 2 → Partition 1
     * Message 3 → Partition 2
     * Message 4 → Partition 0 (cycle repeats)

**Key Points:**
1. Partitioning = Splitting data for parallelism & speed
2. Key-based routing keeps related messages together
3. Round-robin balances load if no key is given
4. Consumers read partitions in parallel for efficiency

#### **Real-World Use Case:**
Uber uses partitioning to ensure all updates for one ride go to the same partition (so drivers & riders see events in order).

## **How Kafka Ensures Message Ordering**

Imagine Kafka is like a **conveyor belt in a pizza kitchen**, where orders (messages) need to be processed **in the exact sequence they were received**.

### **Problem Without Ordering**
- If orders come in as **"1. Dough → 2. Sauce → 3. Cheese → 4. Bake"**, but get mixed up:  
  - **Wrong sequence:** Cheese → Bake → Dough → Sauce → **Disaster!**  

### **How Kafka Solves This?**
#### **1. Ordering *Within* a Partition**
- Each **partition** is like a **single-file queue** (FIFO: First-In, First-Out).  
- Example:  
  - **Topic:** `pizza_orders` (with 3 partitions).  
  - If all messages for **Order #101** go to **Partition 0**, they'll stay in order:  
    ```
    Partition 0: [Dough → Sauce → Cheese → Bake]  
    Partition 1: [Other orders...]  
    Partition 2: [Other orders...]  
    ```  
  -  **Guarantee:** All steps for **Order #101** will execute sequentially.  

#### **2. Using Message Keys**
- If you assign a **key** (e.g., `order_id`), Kafka sends all messages with the **same key to the same partition**.  
  - Example:  
    - `{key: "order_101", step: "dough"}` → Partition 0  
    - `{key: "order_101", step: "sauce"}` → Partition 0 *(Same key = Same partition!)*  
    - `{key: "order_102", step: "dough"}` → Partition 1 *(Different key = Different partition)*  

#### **3. Trade-off: Parallelism vs. Ordering**
- **More partitions = More parallelism** (faster processing) but **order is only kept per partition**.  
  - If you need **global order** (all messages in sequence), use **1 partition** (but this slows Kafka down).  
  - Practical approach: **Group related messages by keys** (e.g., all events for a user/order).  

### **Real-World Example: Uber Ride Updates**
1. **Topic:** `ride_updates`  
2. **Partitioning Strategy:**  
   - All updates for **Ride #123** use key=`ride_123` → always go to **Partition 2**.  
   - Sequence in Partition 2:  
     ```
     "Driver assigned" → "Car arriving" → "Trip started" → "Trip completed"  
     ```  
3. **Result:**  
   - Rider & driver see updates **in order** because they're reading from **the same partition**.  
   - Other rides (e.g., `ride_456`) go to other partitions and won't interfere.  

## **Can a Kafka Consumer Read the Same Message Again and Again?**
**Short Answer:** **Yes, but only if you want it to!**  

Kafka stores messages until they expire (default: 7 days). Consumers control whether they re-read messages by managing their **"offset"** (like a bookmark).  

### **How It Works?**
1. **Each consumer tracks its position** (offset) in the partition.  
   - Example:  
     - Partition 0: `[A (offset 0), B (1), C (2), D (3)]`  
     - Consumer reads A → B → C → **saves offset = 3**.  

### **Key Rule:**
- Kafka **does not delete messages** after reading (unlike traditional queues).  
- Consumers decide when to move forward (like rewinding a DVD).  

**Analogy:**  
- Reading a Kafka topic is like reading a **book with a bookmark**. You can re-read pages by moving the bookmark back!  

## **What Happens When a New Consumer Joins a Kafka System?**

When a **new consumer** is added to a **consumer group** in Kafka, Kafka automatically reorganizes how partitions are assigned to consumers to balance the workload. Here's how it works:

### **Key Points to Remember:**
1. **Rebalancing is Automatic:** Kafka handles partition reassignment seamlessly.  
2. **Fair Distribution:** Each consumer gets ~equal partitions (if possible).  
3. **Temporary Pause:** During rebalance, consumers briefly stop processing messages.  
4. **Scalability:** Adding consumers = More parallel processing power!  

### **Real-World Example: Uber's Ride Updates**
- **Topic:** `ride_updates` (12 partitions).  
- **Initial:** 3 consumers (each handles 4 partitions).  
- **After Adding a 4th Consumer:**  
  - Each now handles **3 partitions**, improving efficiency.  

### **What If a Consumer Crashes?**
- Kafka detects the failure and **rebalances partitions** to the remaining consumers.  

### **Summary**
- New consumer → Kafka **rebalances partitions** → Workload is shared fairly.  
- Pros: **Scalable & fault-tolerant**.  
- Cons: **Brief pause** during rebalance.  

## **What Happens When a Kafka Broker Goes Down?**
*(Explained Simply with a Real-Life Example)*  

Imagine Kafka brokers (servers) as **post offices** in a city, and messages (events) as **letters** being delivered.  

### **Scenario: 1 Post Office (Broker) Suddenly Closes**
1. **Before Failure:**  
   - 3 post offices (Brokers 1, 2, 3) handle letters (messages).  
   - Each stores **copies** of some letters (replication).  

2. **When Broker 2 Fails:**  
   - Letters stored in **Broker 2** are **temporarily unavailable**.  
   - Kafka automatically **redirects traffic** to Brokers 1 and 3.  
   - If Broker 2 had **replicas** (backup copies), Kafka uses those to keep delivering letters.  

3. **Result:**  
   - **No data loss** (if replication was set up).  
   - **Temporary slowdown** (like longer lines at the remaining post offices).  
   - When Broker 2 comes back, it **syncs up** and rejoins the system.  

### **Real-Life Example: Netflix Streaming**
- **Brokers = Data centers** streaming video events (clicks, pauses, etc.).  
- If one data center fails:  
  - Users **keep watching** (other brokers take over).  
  - No interruption, just slightly slower analytics.  

## **What is a Consumer Group in Kafka?**
A Consumer Group is like a team of workers sharing the task of reading messages from Kafka. Here's the key idea:
* One message is read by only one consumer in the group (to avoid duplicate processing)
* All consumers in the group work together to read all partitions of a topic

## **Parallelism in Kafka**

**Parallelism** means **processing multiple messages at the same time** to speed up data consumption.  

### **How Kafka Achieves Parallelism?**
1. **Partitions = Parallel Units**  
   - Each **partition** in a topic is processed independently.  
   - Example: A topic with **3 partitions** can be read by **3 consumers simultaneously**.  

2. **One Consumer per Partition**  
   - In a **consumer group**, each consumer reads from **one partition**.  
   - More partitions = More consumers = More parallelism.  

3. **Scalability**  
   - If data volume increases, just **add more partitions & consumers**.  

### **Example: Order Processing System**
- **Topic:** `orders` (4 partitions).  
- **Consumer Group:** `payment_team` (4 consumers).  
  - Each consumer processes **one partition** in parallel.  
  - Throughput: **4x faster** than a single consumer.  

### **Key Points**
- **Partitions enable parallelism** (like multiple lanes on a highway).  
- **1 consumer = 1 partition** (no two consumers read the same partition).  
- **More partitions = More speed** (but too many can cause overhead).  
- **Consumer groups isolate workloads** (e.g., `payment_team` vs `analytics_team`).  

## **Partition Replication in Kafka**
Kafka replicates (copies) partitions across multiple brokers (servers) for fault tolerance.

### **How It Works?**
1. **Leader-Follower Model**
   * Each partition has 1 leader (handles read/write) and N replicas (followers)
   * Example: Partition P0 has Leader (Broker1) + Replica (Broker2) + Replica (Broker3)
2. **Automatic Sync**
   * Followers copy data from the leader in real-time
3. **If Leader Fails**
   * Kafka promotes a follower to leader (no data loss)

## **Function of KRaft in Kafka**

KRaft (**K**afka **Raft**) is Kafka's built-in **metadata management system** that replaces Zookeeper. It handles critical internal coordination tasks, ensuring the cluster runs smoothly.  

### **Key Functions of KRaft**

#### **1. Manages Cluster Metadata**
   - Tracks **which brokers are alive**, **topic configurations**, and **partition leadership**.  
   - *Example:* Knows that `Topic-orders` has 3 partitions, with `Broker-2` as leader for `Partition-1`.  

#### **2. Elects Leaders (No Zookeeper Needed!)**
   - Uses the **Raft consensus algorithm** (like a voting system) to:  
     - Select a **controller** (primary broker for metadata decisions).  
     - Promote followers to leaders if a broker fails.  
   - *Example:* If `Broker-3` dies, KRaft automatically promotes `Broker-1` to lead its partitions.  

#### **3. Ensures Consistency**
   - Syncs metadata across brokers so all agree on cluster state.  
   - Prevents conflicts (e.g., two brokers thinking they're the leader).  

#### **4. Handles Scaling & Recovery**
   - Supports dynamic scaling (adding/removing brokers).  
   - Recovers quickly from failures (self-healing).  

## **Kafka Acks & Retries**

### **1. acks (Acknowledgment)**
Controls how many brokers must confirm they received a message before the producer considers it "successful."

![Kafka Acknowledgments](https://github.com/Roshankumar0808/Apache_Kafka_SpringBoot/blob/master/kafkaimg1.png)

### **2. retries**
Number of times the producer resends a failed message.

![Kafka Retries](https://github.com/Roshankumar0808/Apache_Kafka_SpringBoot/blob/master/kafkaimg1.png)

## **How Two Microservices Communicate via Kafka (Without Eureka)**

### **1. Kafka as the Communication Bridge**
- **Producer Service** sends events (messages) to a **Kafka topic**.  
- **Consumer Service** subscribes to that topic and processes the events.  
- **No Direct Connection Needed**: Services don't need to know each other's IPs/ports (unlike REST APIs).  

**Example:**  
- **Order Service (Producer)** → Publishes `order_created` event to Kafka.  
- **Notification Service (Consumer)** → Reads `order_created` and sends an email.  

### **Why Eureka is Still Used?**
- **Service Discovery**: Eureka helps services find each other **for synchronous calls** (e.g., REST APIs).  
- **Kafka is Asynchronous**: No need for service discovery because communication is via topics (not direct HTTP calls).  
- **Hybrid Use Case**:  
  - Use **Eureka** for REST calls (e.g., fetching user details).  
  - Use **Kafka** for event-driven workflows (e.g., order processing).  

## **Producer-Consumer Message Flow**
![Producer-Consumer Flow](https://github.com/Roshankumar0808/Apache_Kafka_SpringBoot/blob/master/kafkaimg1.png)

## **Kafka Schema Registry: Why It's Needed**

Imagine you're running a **food delivery app** where:  
- The **Order Service** (Producer) sends orders to Kafka.  
- The **Delivery Service** (Consumer) reads orders to assign drivers.  

### **Problem Without Schema Registry**
- If the `Order` schema changes (e.g., adding a `tip` field), consumers **break** because they expect the old format.  
- Example:  
  ```json
  // Old Schema (v1)
  {"order_id": "123", "customer": "Alice"}

  // New Schema (v2)
  {"order_id": "123", "customer": "Alice", "tip": 5.0}
  ```
  - The Delivery Service crashes if it doesn't know about `tip`.  

### **Solution: Schema Registry**
A **centralized repository** that:  
1. Stores all schemas (like a library of contracts).  
2. Ensures **backward/forward compatibility**.  
3. Assigns a **unique version ID** to each schema.  

#### **How It Works?**
1. **Producer** registers the schema (e.g., `Order` v1) before sending data.  
2. **Consumer** checks the schema version before reading.  
3. If the schema evolves (v2), Schema Registry validates compatibility:  
   - ✅ **Backward Compatible**: Consumers using v1 can read v2 (ignoring new fields).  
   - ✅ **Forward Compatible**: Consumers using v2 can read v1 (default values for missing fields).
